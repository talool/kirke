package com.talool.kirke.xml.convertor;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.talool.core.MediaType;
import com.talool.core.MerchantAccount;
import com.talool.core.MerchantMedia;
import com.talool.core.SearchOptions;
import com.talool.core.service.ServiceException;
import com.talool.image.upload.FileManager;
import com.talool.image.upload.FileNameUtils;
import com.talool.kirke.ServiceUtils;
import com.talool.service.ServiceConfig;

public class MerchantMediaConvertor extends NodeConvertor {
	
	private static final FileManager fileManager = new FileManager(ServiceConfig.get().getUploadDir());
	private static final Logger log = Logger.getLogger(MerchantMediaConvertor.class);
	
	static public MerchantMedia convert(Node image, UUID merchantId, MerchantAccount merchantAccount, List<MerchantMedia> existingMedia) 
	{
		MerchantMedia media = null;
		String mediaUrl = getNodeAttr("url", image);
		String mediaTypeString = getNodeAttr("type", image);
		MediaType mediaType;
		if (mediaTypeString.equals("photo"))
		{
			mediaType = MediaType.MERCHANT_IMAGE;
		}
		else
		{
			mediaType = MediaType.MERCHANT_LOGO;
		}
		
		String taloolMediaUrl = null;
		try
		{
			URL url = new URL(mediaUrl);
			
			// TODO what if the media is really big?  Do a HEAD request for the size.
			// with the file size and the url, we should be able to find existing media without downloading the image

			// save it to the baseFolder
			fileManager.save(url);
			File savedImage = FileNameUtils.getFile(url);

			// check for existing media
			media = FileNameUtils.getExistingMedia(existingMedia, savedImage, url);
			if (media==null)
			{
				File imageFile = fileManager.process(savedImage, url, mediaType, merchantId);
				taloolMediaUrl = FileNameUtils.getImageUrl(imageFile, merchantId);
				System.out.println("Created media with url: "+taloolMediaUrl);
				media = ServiceUtils.get().getFactory().newMedia(merchantId, taloolMediaUrl, mediaType);
			}
			else
			{
				System.out.println("Found existing media for url: "+mediaUrl);
				// delete the save file, cuz we already have it
				fileManager.delete(savedImage.getName());
			}
		}
		catch (MalformedURLException mue)
		{
			log.error("Failed to create url for "+mediaUrl, mue);
		}
		catch (IOException ioe)
		{
			log.error("Failed to process image for "+mediaUrl, ioe);
		}
		
		return media;
	}

	static public List<MerchantMedia> convert(NodeList nodes, UUID merchantId, MerchantAccount merchantAccount) {
		List<MerchantMedia> list = new ArrayList<MerchantMedia>();
		
		// load the existing media for the merchant
		List<MerchantMedia> existingMedia  = new ArrayList<MerchantMedia>();
		try
		{
			SearchOptions searchOptions = new SearchOptions.Builder().maxResults(100).page(0).sortProperty("merchantMedia.mediaUrl")
					.ascending(true).build();
			existingMedia = ServiceUtils.get().getService().getMerchantMedias(merchantId, MediaType.values(), searchOptions);
		}
		catch (ServiceException se)
		{
			log.error("Failed to get existing media for merchant: "+merchantId, se);
		}
		
		for (int i=0; i<nodes.getLength(); i++)
	    {
			Node image = nodes.item(i);
			MerchantMedia media = convert(image,merchantId,merchantAccount, existingMedia);
			list.add(media);
	    }
		return list;
	}

}
