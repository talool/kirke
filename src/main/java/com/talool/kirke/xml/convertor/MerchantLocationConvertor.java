package com.talool.kirke.xml.convertor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.talool.core.MediaType;
import com.talool.core.MerchantAccount;
import com.talool.core.MerchantLocation;
import com.talool.core.MerchantMedia;
import com.talool.core.service.ServiceException;
import com.talool.kirke.ServiceUtils;

public class MerchantLocationConvertor extends NodeConvertor {
	
	private static final Logger log = Logger.getLogger(MerchantLocationConvertor.class);
	
	static private MerchantLocation convert(Node location, UUID merchantId, MerchantAccount merchantAccount, List<MerchantLocation> existingLocations) 
	{
		boolean isNewLocation = false;
		
		String address1 = getNodeAttr("address1", location);
		String zip = getNodeAttr("zip", location);
		MerchantLocation mloc = null;
		
		for (MerchantLocation existingLocation:existingLocations)
		{
			if (existingLocation.getAddress1().equalsIgnoreCase(address1) && existingLocation.getZip().equals(zip))
			{
				mloc = existingLocation;
				break;
			}
		}
		if (mloc==null)
		{
			isNewLocation = true;
			mloc = ServiceUtils.get().getFactory().newMerchantLocation();
			mloc.setAddress1(address1);
			mloc.setZip(zip);
			
			// not using this
			mloc.setEmail("foo@talool.com");
			
			mloc.setCreatedByMerchantAccount(merchantAccount);
		}
		
		// add or update data
		mloc.setAddress2(getNodeAttr("address2", location));
		mloc.setCity(getNodeAttr("city", location));
		mloc.setStateProvinceCounty(getNodeAttr("state", location));
		mloc.setCountry(getNodeAttr("country", location));
		mloc.setLocationName(getNodeAttr("name", location));
		mloc.setPhone(getNodeAttr("phone", location));
		mloc.setWebsiteUrl(getNodeAttr("url", location));
		
		//List<MerchantMedia> images = loadImages(location, merchantId);
		List<MerchantMedia> images = MerchantMediaConvertor.convert(location.getChildNodes(), merchantId, merchantAccount);
		for (MerchantMedia image:images)
		{
			if (image.getMediaType() == MediaType.MERCHANT_IMAGE)
			{
				mloc.setMerchantImage(image);
			}
			else if (image.getMediaType() == MediaType.MERCHANT_LOGO)
			{
				mloc.setLogo(image);
			}
		}
		
		// we'll only return new locations
		if (isNewLocation)
		{
			return mloc;
		}
		else
		{
			try {
				ServiceUtils.get().getService().save(mloc);
			}
			catch (ServiceException se)
			{
				log.error("failed to save existing merchant location: "+mloc.getId(), se);
			}
			return null;
		}
	}

	static public List<MerchantLocation> convert(NodeList nodes, UUID merchantId, MerchantAccount merchantAccount) {
		
		List<MerchantLocation> existingLocations = new ArrayList<MerchantLocation>();
		try {
			existingLocations = ServiceUtils.get().getService().getLocationsForMerchant(merchantId);
		}
		catch (ServiceException se)
		{
			log.error("failed to load existing merchant locations", se);
		}
		
		List<MerchantLocation> locations = new ArrayList<MerchantLocation>();
		for (int i=0; i<nodes.getLength(); i++)
	    {
			Node location = nodes.item(i);
			MerchantLocation mloc = convert(location, merchantId, merchantAccount, existingLocations);
			if (mloc != null)
			{
				locations.add(mloc);
			}
	    }
		return locations;
	}


}
