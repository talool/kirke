package com.talool.kirke.xml.convertor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.talool.core.MediaType;
import com.talool.core.MerchantAccount;
import com.talool.core.MerchantMedia;
import com.talool.kirke.ServiceUtils;

public class MerchantMediaConvertor extends NodeConvertor {
	
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
		// TODO download the image and upload it to our servers so it gets our special processing
		// 		need to move the image processing logic into the service for this
		// TODO how do we prevent the storage of duplicate images?
		MerchantMedia media = ServiceUtils.get().getFactory().newMedia(merchantId, mediaUrl, mediaType);
		return media;
	}

	static public List<MerchantMedia> convert(NodeList nodes, UUID merchantId, MerchantAccount merchantAccount) {
		List<MerchantMedia> list = new ArrayList<MerchantMedia>();
		for (int i=0; i<nodes.getLength(); i++)
	    {
			Node image = nodes.item(i);
			MerchantMedia media = convert(image,merchantId,merchantAccount);
			// TODO return media after we process it
			//list.add(media);
	    }
		return list;
	}

}
