package com.talool.kirke.xml.convertor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.talool.core.MediaType;
import com.talool.core.MerchantAccount;
import com.talool.core.MerchantLocation;
import com.talool.core.MerchantMedia;
import com.talool.core.service.ServiceException;
import com.talool.kirke.ServiceUtils;
import com.talool.service.ErrorCode;
import com.talool.utils.HttpUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;

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
		

		// Get the geo.  Try not to hammer Google.
		Point geo = createGeometry(getNodeAttr("latitude",location), getNodeAttr("longitude", location));
		if (geo == null) geo = getGeometry(mloc); 	// if we can't create it, look it up with Google
		if (geo == null) return null; 				// if Google can't find it, bail
		mloc.setGeometry(geo);
		
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
		
		return mloc;
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
	
	static private Point createGeometry(String lat, String lng)
	{
		if (StringUtils.isEmpty(lat) || StringUtils.isEmpty(lng)) return null;
		
		Point point = null;
		try
		{
			Double latitude = Double.parseDouble(lat);
			Double longitude = Double.parseDouble(lng);
			
			final GeometryFactory factory = new GeometryFactory(
					new PrecisionModel(PrecisionModel.FLOATING), 4326);
			
			point = factory.createPoint(new Coordinate(longitude, latitude));
		}
		catch (Exception e)
		{
			log.debug("failed to create geometry", e);
		}
		
		return point;
	}
	
	static private Point getGeometry(MerchantLocation mloc)
	{
		Point point = null;
		try
		{
			// hit Google
			point = HttpUtils.getGeometry(mloc);
			
		}
		catch(ServiceException se)
		{
			log.error("failed to get geo for new merchant location: "+HttpUtils.buildAddress(mloc), se);
			if (se.getErrorCode().equals(ErrorCode.GEOCODER_OVER_QUERY_LIMIT))
			{
				log.error("ABORT: "+se.getErrorCode().getMessage());
				System.exit(0);
			}
		}
		catch(Exception e)
		{
			log.error("failed to get geo for new merchant location: "+HttpUtils.buildAddress(mloc), e);
		}
		return point;
	}


}
