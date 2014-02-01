package com.talool.kirke.xml.convertor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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
import com.talool.kirke.ServiceUtils;

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
	
	public void setMerchantAccount(MerchantAccount ma)
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
			// get the injest offer or create a new one
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
				dealOffer.setSummary("This book stores deals that were imported into Talool.  You can't sell this book, but you can move its deals into other books you'd like to sell.");
				ServiceUtils.get().getService().save(dealOffer);
			}
		}
		catch(ServiceException se)
		{
			log.error("failed to load merchants for mearchant account id: "+merchantAccount.getId(), se);
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
	
	private Merchant convert(Node merchantNode)
	{
		// look up the merchant by name and merchant account before we create a new one
		boolean isNewMerchant = false;
		String merchantName = getNodeAttr("name", merchantNode);
		Merchant merchant = getMerchant(merchantName);
				
		if (merchant==null)
		{
			merchant = ServiceUtils.get().getFactory().newMerchant(true);
			merchant.setName(merchantName);
			isNewMerchant = true;
		}
		
		Category category = getCategory(getNodeAttr("category", merchantNode));
		merchant.setCategory(category);
		
		NodeList nodes = merchantNode.getChildNodes();
		
		// convert the merchant tags
		Node tagsNode = getNode(TagsTag,nodes);
		List<Tag> tagList = TagConvertor.get().convert(tagsNode.getChildNodes());
		Set<Tag> tags = new HashSet<Tag>(tagList);
		merchant.setTags(tags);
		
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
			}
		}
		
		UUID merchantId = merchant.getId();
		if (merchantId != null)
		{
			// convert the locations
			Node locationsNode = getNode(LocationsTag,nodes);
			List<MerchantLocation> locations = MerchantLocationConvertor.convert(locationsNode.getChildNodes(), merchantId, merchantAccount);
			for (MerchantLocation loc : locations)
			{
				merchant.addLocation(loc);
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
			}
			
			// convert the deals and put each one in our deal offer
			Node dealsNode = getNode(DealsTag,nodes);
			List<Deal> deals = DealConvertor.convert(dealsNode.getChildNodes(), merchantId, merchantAccount);
			if (deals.size() > dealsNode.getChildNodes().getLength())
			{
				log.error("too many deals!");
			}
			for (Deal deal:deals)
			{
				try 
				{
					deal.setDealOffer(dealOffer);
					deal.setMerchant(merchant);
					ServiceUtils.get().getService().save(deal);
				}
				catch(ServiceException dealSE)
				{
					log.error("Failed to save deal: "+deal.getSummary(), dealSE);
				}
			}
			
		}
		
		return merchant;
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
	
	private Category getCategory(String cat)
	{
		Category category = null;
		try
		{
			category = ServiceUtils.get().getService().getCategory(cat);
		} 
		catch (ServiceException se)
		{
			log.error("Failed to look up category", se);
		}
		return category;
	}
	
	public List<Merchant> convert(NodeList nodes) {
				
		// convert the merchants
		List<Merchant> list = new ArrayList<Merchant>();
		for (int i=0; i<nodes.getLength(); i++)
	    {
			Node merchantNode = nodes.item(i);
			Merchant merchant = convert(merchantNode);
			list.add(merchant);
	    }
		return list;
		
	}
}
