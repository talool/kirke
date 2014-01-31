package com.talool.kirke.xml.convertor;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.talool.core.Deal;
import com.talool.core.MerchantAccount;
import com.talool.core.MerchantMedia;
import com.talool.core.Tag;
import com.talool.core.service.ServiceException;
import com.talool.kirke.ServiceUtils;

public class DealConvertor extends NodeConvertor {

	private static final DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
	private static final Logger log = Logger.getLogger(DealConvertor.class);
	
	private static final String SummaryTag = "Summary";
	private static final String DetailsTag = "Details";
	private static final String ImageTag = "Image";
	private static final String TagsTag = "Tags";
	
	static private Deal convert(Node dealNode, UUID merchantId, MerchantAccount merchantAccount, List<Deal> existingDeals) 
	{
		NodeList dealData = dealNode.getChildNodes();
		
		// look up the deal by merchantAccount and summary & exp date before we create a new one.
		// using summary and exp date because it is unlikely that we would want to change these in our admin.
		String dateString = getNodeAttr("expires", dealNode);
		String dealSummary = getNodeValue(SummaryTag, dealData);
		Deal deal = getDeal(existingDeals, dealSummary, dateString);
		
		// new deal
		if (deal==null)
		{
			try
			{
				deal = ServiceUtils.get().getFactory().newDeal(merchantId, merchantAccount, false);
			}
			catch (Exception e)
			{
				log.error("Ran into trouble creating a new deal for merchant id: "+ merchantId, e);
			}
			
			// only set this for new deals
			// - title is safe to edit in the admin
			// - changing summary or expDate will cause a new deal to be created on each import of this deal
			deal.setTitle(getNodeAttr("title", dealNode)); 
			deal.setSummary(dealSummary);
			try
			{
				Date expDate = dateFormatter.parse(dateString);
				deal.setExpires(expDate);
			}
			catch (ParseException pe)
			{
				log.error("Failed to parse expiration date for deal: "+ deal.getTitle(), pe);
			}
		}
		
		// this stuff always gets updated
		deal.setDetails(getNodeValue(DetailsTag, dealData));
		Node image = getNode(ImageTag,dealData);
		if (image != null)
		{
			MerchantMedia media = MerchantMediaConvertor.convert(image, merchantId,merchantAccount);
			deal.setImage(media);
		}
		
		// convert the deal tags
		Node tagsNode = getNode(TagsTag,dealData);
		List<Tag> tagList = TagConvertor.get().convert(tagsNode.getChildNodes());
		Set<Tag> tags = new HashSet<Tag>(tagList);
		deal.setTags(tags);
		
		return deal;
	}
	
	static private Deal getDeal(List<Deal> existingDeals, String dealSummary, String dateString)
	{
		Deal deal = null;
		
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
	
	static public List<Deal> convert(NodeList nodes, UUID merchantId, MerchantAccount merchantAccount) {
		
		// get all the deals for this merchant that were create by this merchant account
		List<Deal> existingDeals = new ArrayList<Deal>();
		try
		{
			existingDeals = ServiceUtils.get().getService().getDealsByMerchantId(merchantId);
		}
		catch (ServiceException se)
		{
			log.error("Failed to load existing deals for merchant: "+merchantId, se);
		}
		
		// convert the deals
		List<Deal> list = new ArrayList<Deal>();
		for (int i=0; i<nodes.getLength(); i++)
	    {
			Node dealNode = nodes.item(i);
			Deal deal = convert(dealNode, merchantId, merchantAccount, existingDeals);
			list.add(deal);
	    }
		return list;
	}
}