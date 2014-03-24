package com.talool.kirke.xml.convertor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.talool.core.Category;
import com.talool.core.CategoryTag;
import com.talool.core.Tag;
import com.talool.core.service.ServiceException;
import com.talool.kirke.JobStatus;
import com.talool.kirke.KirkeErrorCode;
import com.talool.kirke.KirkeException;
import com.talool.kirke.ServiceUtils;


public class TagConvertor extends NodeConvertor {

	static private TagConvertor INSTANCE;
	private static final Logger log = Logger.getLogger(TagConvertor.class);
	
	private Map<Category, List<Tag>> tags;
	
	private TagConvertor() throws KirkeException {
		try
		{
			tags = ServiceUtils.get().getService().getCategoryTags();
		}
		catch(ServiceException se)
		{
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
	
	public Tag convert(Node node, Category category) throws KirkeException 
	{
		String tagString = getNodeValue(node);
		Tag tag = getTag(category, tagString);
		if (tag==null)
		{
			if (tagString.length() > 32) 
			{
				log.error("Tag too long: "+tag);
				throw new KirkeException(KirkeErrorCode.TAG_ERROR, "Tag too long: "+tagString);
			}

			try
			{
				CategoryTag ct = ServiceUtils.get().getService().createCategoryTag(category.getName(), tagString);
				tag = ct.getCategoryTag();
				this.tags.get(category).add(tag);
				JobStatus.get().addTag();
			}
			catch (ServiceException se)
			{
				log.error("Failed to save tag",se);
				throw new KirkeException(KirkeErrorCode.TAG_ERROR, "Failed to save tag: "+tag);
			}
			catch (Exception e)
			{
				log.error("Failed to save tag",e);
				throw new KirkeException(KirkeErrorCode.TAG_ERROR, "Failed to save tag: "+tag);
			}
		}
		return tag;
	}

	public List<Tag> convert(NodeList nodes, Category category) 
	{
		List<Tag> list = new ArrayList<Tag>();
		for (int i=0; i<nodes.getLength(); i++)
	    {
			Node tagNode = nodes.item(i);
			try
			{
				Tag tag = convert(tagNode, category);
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
	
	private Tag getTag(Category cat, String tagName)
	{
		List<Tag> tagList = tags.get(cat);
		Tag t = null;
		for (Tag tag:tagList)
		{
			if (tag.getName().equalsIgnoreCase(tagName))
			{
				t=tag;
				break;
			}
		}
		return t;
	}
}
