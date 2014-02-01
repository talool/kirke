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
import com.talool.image.upload.FileManager;
import com.talool.image.upload.FileNameUtils;
import com.talool.kirke.ServiceUtils;
import com.talool.service.ServiceConfig;

public class MerchantMediaConvertor extends NodeConvertor {
	
	private static final FileManager fileManager = new FileManager(ServiceConfig.get().getUploadDir());
	private static final Logger log = Logger.getLogger(MerchantMediaConvertor.class);
	
	static public MerchantMedia convert(Node image, UUID merchantId, MerchantAccount merchantAccount) {
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
			File imageFile = fileManager.process(url, mediaType, merchantId);
			taloolMediaUrl = FileNameUtils.getImageUrl(imageFile, merchantId);
		}
		catch (MalformedURLException mue)
		{
			log.error("Failed to create url for "+mediaUrl, mue);
		}
		catch (IOException ioe)
		{
			log.error("Failed to process image for "+mediaUrl, ioe);
		}
		
		// TODO how do we prevent the storage of duplicate images?
		// I could choose not to randomize the image name... compare the filename in the url to the filenames for the other media for this merchant...
		// I could delete any other media of this type for the merchant... 
		// TODO what if the media is really big?  should the service check the size and delete the originals for anything too big?
		// big files will really slow down the downloads.  should we abort the script if there is a huge file?  
		// or just output download times, so we can monitor the job's performance
		MerchantMedia media = ServiceUtils.get().getFactory().newMedia(merchantId, taloolMediaUrl, mediaType);
		return media;
	}

	static public List<MerchantMedia> convert(NodeList nodes, UUID merchantId, MerchantAccount merchantAccount) {
		List<MerchantMedia> list = new ArrayList<MerchantMedia>();
		for (int i=0; i<nodes.getLength(); i++)
	    {
			Node image = nodes.item(i);
			MerchantMedia media = convert(image,merchantId,merchantAccount);
			list.add(media);
	    }
		return list;
	}

}
