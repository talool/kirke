package com.talool.kirke;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class JobStatus {

	private static final Logger log = Logger.getLogger(JobStatus.class);
	
	private int merchantCount;
	private int dealCount;
	private int pageCount;
	private int tagCount;
	private List<String> categoriesMissed;
	private List<String> merchantsSkipped;
	private List<String> dealsSkipped;
	private List<String> locationsSkipped;
	private List<String> mediaSkipped;
	private List<String> tagsSkipped;
	
	private StringBuilder setup;
	
	private boolean hitException;
	private boolean hitMaxPage;
	
	private static JobStatus instance;
	
	private JobStatus()
	{
		merchantCount=0;
		dealCount=0;
		pageCount=0;
		tagCount = 0;
		categoriesMissed = new ArrayList<String>();
		merchantsSkipped = new ArrayList<String>();
		dealsSkipped = new ArrayList<String>();
		locationsSkipped = new ArrayList<String>();
		mediaSkipped = new ArrayList<String>();
		tagsSkipped = new ArrayList<String>();
		setup = new StringBuilder("\n\n");
		hitException = false;
		hitMaxPage = false;
	}
	
	public static JobStatus get()
	{
		if (instance==null)
		{
			instance = new JobStatus();
		}
		return instance;
	}
	
	public void addMerchant()
	{
		merchantCount++;
	}
	
	public void addDeal()
	{
		dealCount++;
	}
	
	public void addTag()
	{
		tagCount++;
	}
	
	public void addPage()
	{
		pageCount++;
	}
	
	public int getPageCount()
	{
		return pageCount;
	}
	
	public void missedCategory(String cat)
	{
		if (!categoriesMissed.contains(cat))
		{
			categoriesMissed.add(cat);
		}
	}
	
	public void skippedMerchant(String o)
	{
		if (!merchantsSkipped.contains(o))
		{
			merchantsSkipped.add(o);
		}
	}
	
	public void skippedDeal(String o)
	{
		if (!dealsSkipped.contains(o))
		{
			dealsSkipped.add(o);
		}
	}
	
	public void skippedLocation(String o)
	{
		if (!locationsSkipped.contains(o))
		{
			locationsSkipped.add(o);
		}
	}
	
	public void skippedMedia(String o)
	{
		if (!mediaSkipped.contains(o))
		{
			mediaSkipped.add(o);
		}
	}
	
	public void skippedTag(String o)
	{
		if (!tagsSkipped.contains(o))
		{
			tagsSkipped.add(o);
		}
	}
	
	public void setFailed()
	{
		hitException = true;
	}
	
	public void setHitMaxPage()
	{
		hitMaxPage = true;
	}
	
	public void println(String s)
	{
		log.info(s);
	}
	
	public void print(String s)
	{
		log.info(s);
	}
	
	public String getSetupSummary(String xmlPath, String xslFilePath, String merchantAccountIdString, String namespace)
	{
		setup.append("====  MERCHANT JOB STARTING  =============================================\n")
			 .append("====  3rd Party XML: "+xmlPath+"\n")
		 	 .append("====  3rd Party XSL: "+xslFilePath+"\n")
		 	 .append("====  Created By Merchant Account Id: "+merchantAccountIdString+"\n")
		 	 .append("====  Namespace: "+namespace+"\n")
		 	 .append("==========================================================================\n");
		return setup.toString();
	}
	
	public String getJobSummary()
	{
		StringBuilder sb = new StringBuilder("\n");
		if (hitException)
		{
			sb.append("====  MERCHANT JOB FAILED  ===============================================\n\n");
		}
		if (hitMaxPage)
		{
			sb.append("====  MERCHANT JOB ENDED EARLY - HIT MAX PAGE  ===========================\n\n");
		}
		else
		{
			sb.append("====  MERCHANT JOB COMPLETE  =============================================\n\n");
		}
		
		sb.append("====  JOB SUMMARY ========================================================\n")
		  .append("XML files processed: ").append(pageCount).append("\n")
		  .append("Merchants converted: ").append(merchantCount).append("\n")
		  .append("Merchants skipped:   ").append(merchantsSkipped.size()).append("\n")
		  .append("Locations skipped:   ").append(locationsSkipped.size()).append("\n")
		  .append("Deals converted:     ").append(dealCount).append("\n")
		  .append("Deals skipped:       ").append(dealsSkipped.size()).append("\n")
		  .append("Media skipped:       ").append(mediaSkipped.size()).append("\n")
		  .append("Tags converted:      ").append(tagCount).append("\n")
		  .append("Tags skipped:        ").append(tagsSkipped.size()).append("\n");
		
		if (!categoriesMissed.isEmpty())
		{
			sb.append("\n====  MISSED CATEGORIES  ================================================\n");
			for (String c:categoriesMissed)
			{
				sb.append(c).append("\n");
			}
		}
		
		if (!merchantsSkipped.isEmpty())
		{
			sb.append("\n====  SKIPPED MERCHANTS  ================================================\n");
			for (String s:merchantsSkipped)
			{
				sb.append(s).append("\n");
			}
		}
		
		if (!locationsSkipped.isEmpty())
		{
			sb.append("\n====  SKIPPED LOCATIONS  ================================================\n");
			for (String s:locationsSkipped)
			{
				sb.append(s).append("\n");
			}
		}
		
		if (!dealsSkipped.isEmpty())
		{
			sb.append("\n====  SKIPPED DEALS  ====================================================\n");
			for (String s:dealsSkipped)
			{
				sb.append(s).append("\n");
			}
		}
		
		if (!mediaSkipped.isEmpty())
		{
			sb.append("\n====  SKIPPED MEDIA  ====================================================\n");
			for (String s:mediaSkipped)
			{
				sb.append(s).append("\n");
			}
		}
		
		if (!tagsSkipped.isEmpty())
		{
			sb.append("\n====  SKIPPED TAGS  =====================================================\n");
			for (String s:tagsSkipped)
			{
				sb.append(s).append("\n");
			}
		}
		
		sb.append("\n====  SUMMARY COMPLETE  =================================================\n\n");
		
		return sb.toString();
	}
	
	public String getUsageMessage()
	{
		StringBuilder sb = new StringBuilder("\n\n");
		sb.append("====  MERCHANT JOB ABORTING  =============================================\n");
		sb.append("You must provide the following arguments:\n");
		sb.append(" * A resource path or URL for the 3rd Party XML\n");
		sb.append(" * A resource path to the 3rd Party XSL\n");
		sb.append(" * Created By Merchant Account Id\n");
		sb.append(" \n");
		sb.append("You may also provide the following argument:\n");
		sb.append(" * Namespace for the 3rd Party XML\n");
		sb.append("====  MERCHANT JOB COMPLETE  =============================================\n");
		
		return sb.toString();
	}
}
