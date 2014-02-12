package com.talool.kirke.xml.convertor;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
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
import com.talool.kirke.JobStatus;
import com.talool.kirke.KirkeErrorCode;
import com.talool.kirke.KirkeException;
import com.talool.kirke.ServiceUtils;
import com.talool.service.ServiceConfig;

public class MerchantMediaConvertor extends NodeConvertor {
	
	private static final FileManager fileManager = new FileManager(ServiceConfig.get().getUploadDir());
	private static final Logger log = Logger.getLogger(MerchantMediaConvertor.class);
	private static final int MAX_DOWNLOAD_FILE_SIZE_BYTES = 500000;
	
	static public MerchantMedia convert(Node image, UUID merchantId, MerchantAccount merchantAccount, List<MerchantMedia> existingMedia) throws KirkeException 
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
			
			// check the file size before we download it.  check for dups if we get a file size for the url.
			int fileSize = getFileSize(url);
			if (fileSize > MAX_DOWNLOAD_FILE_SIZE_BYTES)
			{
				// abort!!  the file was too big
				log.debug("Skipping conversion of media with file size of "+fileSize+" at url: "+mediaUrl);
				throw new KirkeException(KirkeErrorCode.MEDIA_TOO_BIG_ERROR, "file size: "+fileSize);
			}
			else if (fileSize > 0)
			{
				// Given the fileSize and the Url we can check if we already have this image
				media = FileNameUtils.getExistingMedia(existingMedia, fileSize, url);
				if (media != null)
				{
					// abort!!  we don't return existing media
					log.debug("Found existing media before download for url: "+mediaUrl);
					throw new KirkeException(KirkeErrorCode.MEDIA_EXISTS_ERROR, mediaUrl);
				}
			}
			else if (fileSize == 0)
			{
				// LocalSaver redirects from http to https which kills the upload
				StringBuilder sb = new StringBuilder();
				sb.append("https://").append(url.getHost()).append(url.getFile());
				url = new URL(sb.toString());
				fileSize = getFileSize(url);
				//System.out.println("Image file size was 0 for "+mediaUrl+", so tried HTTPS and got: "+fileSize);
				if (fileSize < 1)
				{
					throw new KirkeException(KirkeErrorCode.MEDIA_NOT_FOUND_ERROR, mediaUrl);
				}
				else
				{
					media = FileNameUtils.getExistingMedia(existingMedia, fileSize, url);
					if (media != null)
					{
						// abort!!  we don't return existing media
						log.debug("Found existing media before download for url: "+mediaUrl);
						throw new KirkeException(KirkeErrorCode.MEDIA_EXISTS_ERROR, mediaUrl);
					}
				}
			}
			else if (fileSize < 0)
			{
				// abort! it wasn't a png or jpg
				log.error("Skipping unsupported media in url: "+mediaUrl);
				throw new KirkeException(KirkeErrorCode.MEDIA_TYPE_ERROR, mediaUrl);
			}

			// save it to the baseFolder
			fileManager.save(url);
			File savedImage = FileNameUtils.getFile(url);

			// check for existing media again.  maybe we didn't know the fileSize before
			media = FileNameUtils.getExistingMedia(existingMedia, fileSize, url);
			if (media==null)
			{
				File imageFile = fileManager.process(savedImage, url, mediaType, merchantId);
				taloolMediaUrl = FileNameUtils.getImageUrl(imageFile, merchantId);
				log.debug("Created media with url: "+taloolMediaUrl);
				media = ServiceUtils.get().getFactory().newMedia(merchantId, taloolMediaUrl, mediaType);
			}
			else
			{
				log.debug("Found existing media after download for url: "+mediaUrl);
				// delete the save file, cuz we already have it
				fileManager.delete(savedImage.getName());
				throw new KirkeException(KirkeErrorCode.MEDIA_EXISTS_ERROR, mediaUrl);
			}
		}
		catch (MalformedURLException mue)
		{
			log.error("Failed to create url for "+mediaUrl, mue);
			throw new KirkeException(KirkeErrorCode.MEDIA_ERROR, mediaUrl, mue);
		}
		catch (IOException ioe)
		{
			log.error("Failed to process image for "+mediaUrl, ioe);
			throw new KirkeException(KirkeErrorCode.MEDIA_ERROR, mediaUrl, ioe);
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
			try
			{
				MerchantMedia media = convert(image,merchantId,merchantAccount, existingMedia);
				list.add(media);
			}
			catch (KirkeException e)
			{
				if (e.getErrorCode().equals(KirkeErrorCode.MEDIA_EXISTS_ERROR))
				{
					// TODO should this be an error?
				}
				StringBuilder sb = new StringBuilder();
				sb.append("Merchant id: ")
				  .append(merchantId)
				  .append(" media skipped because: ")
				  .append(e.getMessage());
				JobStatus.get().skippedMedia(sb.toString());
			}
	    }
		return list;
	}
	
	/*
	 * Does a HEAD request to get the file size of the media at a given url
	 */
	static private int getFileSize(URL imageUrl)
	{
		int fileSize = 0;
		
		try 
		{
			HttpURLConnection.setFollowRedirects(true);
		    HttpURLConnection con = (HttpURLConnection) imageUrl.openConnection();
		    con.setRequestMethod("HEAD");
		    if (con.getResponseCode() == HttpURLConnection.HTTP_OK)
		    {
		    	
		    	String fileType = con.getContentType();
		    	if (fileType.equalsIgnoreCase("image/png") || 
		    		fileType.equalsIgnoreCase("image/jpg") || 
		    		fileType.equalsIgnoreCase("image/jpeg"))
		    	{
		    		fileSize = con.getContentLength();
		    	}
		    	else
		    	{
		    		fileSize = -1;
		    	}
		    }
		}
		catch (Exception e) 
		{
		    log.error("Failed to get head response for media: "+imageUrl.getFile(), e);
		}
		
		return fileSize;
	}

}
