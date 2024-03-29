package com.talool.kirke.xml.convertor;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.talool.core.Deal;
import com.talool.core.MediaType;
import com.talool.core.Merchant;
import com.talool.core.MerchantAccount;
import com.talool.core.MerchantMedia;
import com.talool.core.SearchOptions;
import com.talool.core.Tag;
import com.talool.core.service.ServiceException;
import com.talool.kirke.JobStatus;
import com.talool.kirke.KirkeErrorCode;
import com.talool.kirke.KirkeException;
import com.talool.kirke.ServiceUtils;
import com.talool.utils.KeyValue;

public class DealConvertor extends NodeConvertor {

	private static final DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
	private static final Logger log = Logger.getLogger(DealConvertor.class);
	
	private static final String SummaryTag = "Summary";
	private static final String DetailsTag = "Details";
	private static final String ImageTag = "Image";
	private static final String TagsTag = "Tags";
	
	private static final int maxDetailsLength = 510;
	
	static private Deal convert(Node dealNode, Merchant merchant, MerchantAccount merchantAccount, List<Deal> existingDeals, List<MerchantMedia> existingMedia) throws KirkeException 
	{
		NodeList dealData = dealNode.getChildNodes();
		
		// look up the deal by merchantAccount and summary & exp date before we create a new one.
		// using summary and exp date because it is unlikely that we would want to change these in our admin.
		String dateString = getNodeAttr("expires", dealNode);
		String dealSummary = getNodeValue(SummaryTag, dealData);
		dealSummary = StringUtils.normalizeSpace(dealSummary);
		dealSummary = WordUtils.capitalizeFully(dealSummary);
		Deal deal = getDeal(existingDeals, dealSummary, dateString);
		
		// new deal
		if (deal==null)
		{
			try
			{
				deal = ServiceUtils.get().getFactory().newDeal(merchant.getId(), merchantAccount, false);
			}
			catch (Exception e)
			{
				log.error("Ran into trouble creating a new deal for merchant id: "+ merchant.getId(), e);
				throw new KirkeException(KirkeErrorCode.DEAL_ERROR, e);
			}
			
			// only set this for new deals
			// - title is safe to edit in the admin
			// - changing summary or expDate will cause a new deal to be created on each import of this deal
			String title = getNodeAttr("title", dealNode);
			title = StringUtils.normalizeSpace(title);
			title = WordUtils.capitalizeFully(title);
			deal.setTitle(title); 
			deal.setSummary(dealSummary);
			try
			{
				Date expDate = dateFormatter.parse(dateString);
				deal.setExpires(expDate);
			}
			catch (ParseException pe)
			{
				log.error("Failed to parse expiration date for deal: "+ deal.getTitle(), pe);
				throw new KirkeException(KirkeErrorCode.DEAL_ERROR, pe);
			}
		}
		
		// this stuff always gets updated
		String details = getNodeValue(DetailsTag, dealData);
		details = StringUtils.normalizeSpace(details);
		details = StringUtils.capitalize(details);
		if (details.length() > maxDetailsLength)
		{
			details = details.substring(0,maxDetailsLength);
		}
		deal.setDetails(details);
		
		// convert the deal tags
		Node tagsNode = getNode(TagsTag,dealData);
		List<Tag> tagList = TagConvertor.get().convert(tagsNode.getChildNodes(), merchant.getCategory());
		Set<Tag> tags = new HashSet<Tag>(tagList);
		deal.setTags(tags);
				
				
		Node image = getNode(ImageTag,dealData);
		if (image != null)
		{
			MerchantMedia media = MerchantMediaConvertor.convert(image, merchant.getId(),merchantAccount, existingMedia);
			deal.setImage(media);
		}
		else
		{
			// get a stock image
			MerchantMedia defaultMedia = MerchantMediaConvertor.getStockMedia(tags);
			if (defaultMedia != null){
				deal.setImage(defaultMedia);
			}
		}
		
		// put the rating and value in properties on the deal
		String rating = getNodeAttr("rating", dealNode);
		if (!StringUtils.isEmpty(rating))
		{
			deal.getProperties().createOrReplace(KeyValue.dealRating, rating);
		}
		String value = getNodeAttr("value", dealNode);
		if (!StringUtils.isEmpty(value))
		{
			deal.getProperties().createOrReplace(KeyValue.dealValue, value);
		}
		
		return deal;
	}
	
	static private Deal getDeal(List<Deal> existingDeals, String dealSummary, String dateString)
	{
		Deal deal = null;
		
		// get the dateString in the right format
		try {
			Date d = dateFormatter.parse(dateString);
			dateString = dateFormatter.format(d);
		} catch (ParseException e) {
			// invalid date
			log.error("deal has invalid date");
			return null;
		}
		
		for (Deal existingDeal:existingDeals)
		{
			String existingDateString = dateFormatter.format(existingDeal.getExpires());
			if (existingDeal.getSummary().equals(dealSummary) && existingDateString.equals(dateString))
			{
				deal = existingDeal;
				break;
			}
		}
		
		return deal;
	}
	
	static public List<Deal> convert(NodeList nodes, Merchant merchant, MerchantAccount merchantAccount) {
		
		// get all the deals for this merchant that were create by this merchant account
		List<Deal> existingDeals = new ArrayList<Deal>();
		try
		{
			existingDeals = ServiceUtils.get().getService().getDealsByMerchantId(merchant.getId());
		}
		catch (ServiceException se)
		{
			log.error("Failed to load existing deals for merchant: "+merchant.getId(), se);
		}
		
		// load the existing media for the merchant
		List<MerchantMedia> existingMedia  = new ArrayList<MerchantMedia>();
		try
		{
			SearchOptions searchOptions = new SearchOptions.Builder().maxResults(100).page(0).sortProperty("merchantMedia.mediaUrl")
					.ascending(true).build();
			MediaType[] mediaTypes = new MediaType[]{MediaType.DEAL_IMAGE};
			existingMedia = ServiceUtils.get().getService().getMerchantMedias(merchant.getId(), mediaTypes, searchOptions);
		}
		catch (ServiceException se)
		{
			log.error("Failed to get existing media for merchant: "+merchant.getId(), se);
		}
		
		// convert the deals
		List<Deal> list = new ArrayList<Deal>();
		for (int i=0; i<nodes.getLength(); i++)
	    {
			Node dealNode = nodes.item(i);
			if (!dealNode.getNodeName().equals("Deal"))
			{
				//JobStatus.get().println("skipped empty node.");
				continue;
			}
			try 
			{
				Deal deal = convert(dealNode, merchant, merchantAccount, existingDeals, existingMedia);
				list.add(deal);
			} 
			catch (KirkeException e) 
			{
				// record that we skipped this deal
				NodeList dealData = dealNode.getChildNodes();
				String dealSummary = getNodeValue(SummaryTag, dealData);
				StringBuilder sb = new StringBuilder();
				sb.append(dealSummary)
				  .append("for merchantId ")
				  .append(merchant.getId())
				  .append(" because ")
				  .append(e.getMessage());
				JobStatus.get().skippedDeal(sb.toString());
			}
			
	    }
		return list;
	}
}
