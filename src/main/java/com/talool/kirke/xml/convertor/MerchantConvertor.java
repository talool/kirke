package com.talool.kirke.xml.convertor;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.talool.core.Category;
import com.talool.core.Deal;
import com.talool.core.DealOffer;
import com.talool.core.DealType;
import com.talool.core.Merchant;
import com.talool.core.MerchantAccount;
import com.talool.core.MerchantLocation;
import com.talool.core.Tag;
import com.talool.core.service.ServiceException;
import com.talool.kirke.JobStatus;
import com.talool.kirke.KirkeErrorCode;
import com.talool.kirke.KirkeException;
import com.talool.kirke.ServiceUtils;
import com.talool.utils.KeyValue;

public class MerchantConvertor extends NodeConvertor {

	static private MerchantConvertor INSTANCE;
	private static final Logger log = Logger.getLogger(MerchantConvertor.class);
	
	private static final String TagsTag = "Tags";
	private static final String LocationsTag = "Locations";
	private static final String DealsTag = "Deals";
	private static final String DealOfferTitle = "Talool Import Book";
	
	private List<Merchant> existingMerchants;
	private MerchantAccount merchantAccount;
	private DealOffer dealOffer;
	
	private MerchantConvertor() {}
	
	public void setMerchantAccount(MerchantAccount ma) throws KirkeException
	{
		merchantAccount = ma;
		
		try
		{
			// get all the merchants that were created by this merchant account
			existingMerchants = ServiceUtils.get().getService().getMerchantsCreatedByMerchant(merchantAccount.getMerchant().getId());
		}
		catch(ServiceException se)
		{
			existingMerchants = new ArrayList<Merchant>();
			log.error("failed to load merchants for mearchant account id: "+merchantAccount.getId(), se);
		}
		
		try
		{
			// get the offer or create a new one
			dealOffer = null;
			List<DealOffer> offers = ServiceUtils.get().getService().getDealOffersByMerchantId(merchantAccount.getMerchant().getId());
			for (DealOffer offer:offers)
			{
				if (offer.getTitle().equals(DealOfferTitle) && offer.getType() == DealType.KIRKE_BOOK)
				{
					dealOffer = offer;
					break;
				}
			}
			if (dealOffer == null)
			{
				dealOffer = ServiceUtils.get().getFactory().newDealOffer(merchantAccount.getMerchant(), merchantAccount);
				dealOffer.setTitle(DealOfferTitle);
				dealOffer.setActive(false);
				dealOffer.setDealType(DealType.KIRKE_BOOK);
				dealOffer.setPrice(1000.0f);
				dealOffer.setScheduledEndDate(new Date());
				dealOffer.setScheduledStartDate(new Date());
				dealOffer.setSummary("This book stores deals that were imported into Talool.  You can't sell this book, but you can move its deals into other books you'd like to sell.");
				ServiceUtils.get().getService().save(dealOffer);
			}
		}
		catch(ServiceException se)
		{
			log.error("failed to get a KirkeBook for mearchant account id: "+merchantAccount.getId(), se);
			throw new KirkeException(KirkeErrorCode.JOB_FAILED,se);
		}
	}
	
	static public MerchantConvertor get()
	{
		if (INSTANCE==null)
		{
			INSTANCE = new MerchantConvertor();
		}
		return INSTANCE;
	}
	
	private Merchant convert(Node merchantNode) throws KirkeException
	{
		// look up the merchant by name and merchant account before we create a new one
		boolean isNewMerchant = false;
		String merchantName = getNodeAttr("name", merchantNode);
		if (merchantName.length()>64)
		{
			merchantName = merchantName.substring(0, 63);
		}
		Merchant merchant = getMerchant(merchantName);
		
		if (merchant==null)
		{
			merchant = ServiceUtils.get().getFactory().newMerchant(true);
			merchant.setName(merchantName);
			isNewMerchant = true;
		}
		
		Category category = getCategory(getNodeAttr("category", merchantNode));
		merchant.setCategory(category);
		
		String fundraiser = getNodeAttr("fundraiser", merchantNode);
		if (!StringUtils.isEmpty(fundraiser) && StringUtils.equalsIgnoreCase(fundraiser, "true"))
		{
			merchant.getProperties().createOrReplace(KeyValue.fundraiser, "true");
		}
		
		String percentage = getNodeAttr("fundraiser_percentage", merchantNode);
		if (!StringUtils.isEmpty(percentage))
		{
			merchant.getProperties().createOrReplace(KeyValue.percentage, percentage);
		}
		
		NodeList nodes = merchantNode.getChildNodes();
		
		// convert the merchant tags
		Node tagsNode = getNode(TagsTag,nodes);
		if (tagsNode != null)
		{
			try
			{
				List<Tag> tagList = TagConvertor.get().convert(tagsNode.getChildNodes(), category);
				Set<Tag> tags = new HashSet<Tag>(tagList);
				merchant.setTags(tags);
			}
			catch (KirkeException e)
			{
				// TODO decide if we want to continue or fail the merchant
			}
		}
		
		
		// persist the merchant so we get a UUID
		if (isNewMerchant)
		{
			try 
			{
				ServiceUtils.get().getService().save(merchant);
			}
			catch(ServiceException merchSE)
			{
				log.error("Failed to save merchant: "+merchant.getName(), merchSE);
				throw new KirkeException(KirkeErrorCode.MERCHANT_ERROR, merchSE);
			}
		}
		
		UUID merchantId = merchant.getId();
		if (merchantId != null)
		{
			// convert the locations
			Node locationsNode = getNode(LocationsTag,nodes);
			if (locationsNode != null)
			{
				try
				{
					List<MerchantLocation> locations = MerchantLocationConvertor.convert(locationsNode.getChildNodes(), merchantId, merchantAccount);
					for (MerchantLocation loc : locations)
					{
						// check for an existing loc with this id
						dropLocation(merchant, loc);
						merchant.addLocation(loc);
					}
				}
				catch (KirkeException e)
				{
					log.error("Failed to convert location for: "+merchant.getName() + " because " + e.getMessage());
				}
				
				if (merchant.getLocations().size() == 0)
				{
					log.error("Skipping merchant because there were no locations converted: "+merchant.getName());
					// consider deleting the merchant to clean it up
					throw new KirkeException(KirkeErrorCode.MERCHANT_ERROR, "No locations converted");
				}
				
				// persist the merchant, this time with locations
				try 
				{
					ServiceUtils.get().getService().save(merchant);
					if (isNewMerchant)
					{
						existingMerchants.add(merchant);
					}
				}
				catch(ServiceException merchSE)
				{
					log.error("Failed to save merchant with locations: "+merchant.getName(), merchSE);
					throw new KirkeException(KirkeErrorCode.MERCHANT_ERROR, merchSE);
				}
				
				// convert the deals and put each one in our deal offer
				Node dealsNode = getNode(DealsTag,nodes);
				if (dealsNode != null)
				{
					List<Deal> deals = DealConvertor.convert(dealsNode.getChildNodes(), merchant, merchantAccount);
					for (Deal deal:deals)
					{
						try 
						{
							deal.setDealOffer(dealOffer);
							deal.setMerchant(merchant);
							ServiceUtils.get().getService().save(deal);
							
							JobStatus.get().addDeal();
						}
						catch(ServiceException dealSE)
						{
							log.error("Failed to save deal: "+deal.getSummary(), dealSE);
							StringBuilder sb = new StringBuilder();
							sb.append(deal.getSummary())
							  .append(" for merchant ")
							  .append(merchantName)
							  .append(" failed to save.");
							JobStatus.get().skippedDeal(sb.toString());
						}
					}
				}
			}
			
		}
		
		return merchant;
	}
	
	private void dropLocation(Merchant merchant, MerchantLocation loc)
	{
		for (MerchantLocation location:merchant.getLocations())
		{
			if (location.getId().equals(loc.getId()))
			{
				merchant.getLocations().remove(location);
				break;
			}
		}
	}
	
	private Merchant getMerchant(String name)
	{
		Merchant merchant = null;
		
		for (Merchant existingMerchant:existingMerchants)
		{
			if (existingMerchant.getName().equals(name))
			{
				merchant = existingMerchant;
				break;
			}
		}
		
		return merchant;
	}
	
	private Category getCategory(String cat) throws KirkeException
	{
		Category category = null;
		try
		{
			category = ServiceUtils.get().getService().getCategory(cat);
			
			if (category==null)
			{
				JobStatus.get().missedCategory(cat);
				throw new KirkeException(KirkeErrorCode.CATEGORY_ERROR);
			}
		} 
		catch (ServiceException se)
		{
			log.error("Failed to look up category", se);
			throw new KirkeException(KirkeErrorCode.CATEGORY_ERROR,se);
		}
		return category;
	}
	
	public List<Merchant> convert(NodeList nodes) throws KirkeException 
	{
				
		// convert the merchants
		List<Merchant> list = new ArrayList<Merchant>();
		for (int i=0; i<nodes.getLength(); i++)
	    {
			Node merchantNode = nodes.item(i);
			try 
			{
				Merchant merchant = convert(merchantNode);
				list.add(merchant);
				
				JobStatus.get().addMerchant();
				JobStatus.get().println("Saved "+merchant.getName());
			} 
			catch (KirkeException e) 
			{
				if (e.getErrorCode().equals(KirkeErrorCode.JOB_FAILED))
				{
					throw e;
				}
				else
				{
					String merchantName = getNodeAttr("name", merchantNode);
					
					StringBuilder sb = new StringBuilder();
					sb.append(merchantName)
					  .append(" skipped because ")
					  .append(e.getMessage());
					JobStatus.get().skippedMerchant(sb.toString());
				}
			}
			
	    }
		return list;
		
	}
}
