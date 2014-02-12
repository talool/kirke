package com.talool.kirke.xml.convertor;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.talool.core.Tag;
import com.talool.core.service.ServiceException;
import com.talool.kirke.JobStatus;
import com.talool.kirke.KirkeErrorCode;
import com.talool.kirke.KirkeException;
import com.talool.kirke.ServiceUtils;


public class TagConvertor extends NodeConvertor {

	static private TagConvertor INSTANCE;
	private static final Logger log = Logger.getLogger(TagConvertor.class);
	
	private List<Tag> tags;
	
	private TagConvertor() throws KirkeException {
		try
		{
			tags = ServiceUtils.get().getService().getTags();
		}
		catch(ServiceException se)
		{
			tags = new ArrayList<Tag>();
			log.error("failed to load tags", se);
			throw new KirkeException(KirkeErrorCode.JOB_FAILED, "Failed to load tags");
		}
	}
	
	static public TagConvertor get() throws KirkeException
	{
		if (INSTANCE==null)
		{
			INSTANCE = new TagConvertor();
		}
		return INSTANCE;
	}
	
	public Tag convert(Node node) throws KirkeException 
	{
		String tagString = getNodeValue(node);
		Tag tag = getTag(tagString);
		if (tag==null)
		{
			if (tagString.length() > 32) 
			{
				throw new KirkeException(KirkeErrorCode.TAG_ERROR, "Tag too long: "+tag);
			}
			
			tag = ServiceUtils.get().getFactory().newTag(tagString);
			try
			{
				ServiceUtils.get().getService().save(tag);
				this.tags.add(tag);
				JobStatus.get().addTag();
			}
			catch (ServiceException se)
			{
				log.error("Failed to save tag",se);
				throw new KirkeException(KirkeErrorCode.TAG_ERROR, "Failed to save tag: "+tag);
			}
		}
		return tag;
	}

	public List<Tag> convert(NodeList nodes) 
	{
		List<Tag> list = new ArrayList<Tag>();
		for (int i=0; i<nodes.getLength(); i++)
	    {
			Node tagNode = nodes.item(i);
			try
			{
				Tag tag = convert(tagNode);
				list.add(tag);
			}
			catch(KirkeException e)
			{
				StringBuilder sb = new StringBuilder();
				sb.append("Failed to add tag: ")
				  .append(getNodeValue(tagNode))
				  .append(" because ")
				  .append(e.getMessage());
				
				JobStatus.get().skippedTag(sb.toString());
			}
			
	    }
		return list;
	}
	
	private Tag getTag(String name)
	{
		Tag t = null;
		for (Tag tag:tags)
		{
			if (tag.getName().equalsIgnoreCase(name))
			{
				t=tag;
				break;
			}
		}
		return t;
	}
}
