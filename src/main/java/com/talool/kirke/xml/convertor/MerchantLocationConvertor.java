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
import com.talool.kirke.JobStatus;
import com.talool.kirke.KirkeErrorCode;
import com.talool.kirke.KirkeException;
import com.talool.kirke.ServiceUtils;
import com.talool.service.ErrorCode;
import com.talool.utils.HttpUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;

public class MerchantLocationConvertor extends NodeConvertor {
	
	private static final Logger log = Logger.getLogger(MerchantLocationConvertor.class);
	
	static private MerchantLocation convert(Node location, UUID merchantId, MerchantAccount merchantAccount, List<MerchantLocation> existingLocations) throws KirkeException 
	{
		String address1 = getNodeAttr("address1", location);
		if (address1.length() > 64)
		{
			throw new KirkeException(KirkeErrorCode.LOCATION_ERROR, "bogus address: "+address1);
		}
		
		String zip = getNodeAttr("zip", location);
		if (zip.length() > 64)
		{
			throw new KirkeException(KirkeErrorCode.LOCATION_ERROR, "bogus zip: "+zip);
		}
		
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
			mloc = ServiceUtils.get().getFactory().newMerchantLocation();
			mloc.setAddress1(address1);
			mloc.setZip(zip);
			
			// not using this
			mloc.setEmail("foo@talool.com");
			
			mloc.setCreatedByMerchantAccount(merchantAccount);
		}
		
		// add or update data
		String add2 = getNodeAttr("address2", location);
		if (add2.length() > 64)
		{
			throw new KirkeException(KirkeErrorCode.LOCATION_ERROR, "bogus address 2: "+add2);
		}
		mloc.setAddress2(add2);
		
		String city = getNodeAttr("city", location);
		if (city.length() > 64)
		{
			throw new KirkeException(KirkeErrorCode.LOCATION_ERROR, "bogus city: "+city);
		}
		mloc.setCity(city);
		
		String state = getNodeAttr("state", location);
		if (state.length() > 64)
		{
			throw new KirkeException(KirkeErrorCode.LOCATION_ERROR, "bogus state: "+state);
		}
		mloc.setStateProvinceCounty(state);
		
		String country = getNodeAttr("country", location);
		if (country.length() > 4)
		{
			throw new KirkeException(KirkeErrorCode.LOCATION_ERROR, "bogus country: "+country);
		}
		mloc.setCountry(country);
		
		String name = getNodeAttr("name", location);
		if (name.length() > 64)
		{
			throw new KirkeException(KirkeErrorCode.LOCATION_ERROR, "bogus name: "+name);
		}
		mloc.setLocationName(name);
		
		String phone = getNodeAttr("phone", location);
		if (phone.length() > 48)
		{
			throw new KirkeException(KirkeErrorCode.LOCATION_ERROR, "bogus phone: "+phone);
		}
		mloc.setPhone(phone);
		
		String url = getNodeAttr("url", location);
		if (url.length() > 128)
		{
			// TODO we need to increase the length of this field
			url = url.substring(0,127);
		}
		mloc.setWebsiteUrl(url);
		

		// Get the geo.  Try not to hammer Google.
		Point geo = createGeometry(getNodeAttr("latitude",location), getNodeAttr("longitude", location));
		if (geo == null) geo = getGeometry(mloc); 	// if we can't create it, look it up with Google
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

	static public List<MerchantLocation> convert(NodeList nodes, UUID merchantId, MerchantAccount merchantAccount) throws KirkeException {
		
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
			if (!location.getNodeName().equals("Location"))
			{
				//JobStatus.get().println("skipped empty node.");
				continue;
			}
			try
			{
				MerchantLocation mloc = convert(location, merchantId, merchantAccount, existingLocations);
				locations.add(mloc);
			}
			catch (KirkeException e)
			{
				if (e.getErrorCode().equals(KirkeErrorCode.JOB_FAILED))
				{
					throw e;
				}
				else
				{
					String address1 = getNodeAttr("address1", location);
					StringBuilder sb = new StringBuilder();
					sb.append(address1)
					  .append(" skipped for merchantId ")
					  .append(merchantId)
					  .append(" because ")
					  .append(e.getMessage());
					JobStatus.get().skippedLocation(sb.toString());
				}
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
	
	static private Point getGeometry(MerchantLocation mloc) throws KirkeException
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
				throw new KirkeException(KirkeErrorCode.JOB_FAILED,se);
			}
			throw new KirkeException(KirkeErrorCode.GEO_ERROR,se);
		}
		catch(Exception e)
		{
			log.error("failed to get geo for new merchant location: "+HttpUtils.buildAddress(mloc), e);
			throw new KirkeException(KirkeErrorCode.GEO_ERROR,e);
		}
		return point;
	}


}
