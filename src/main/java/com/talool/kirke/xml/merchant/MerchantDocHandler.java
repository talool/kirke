package com.talool.kirke.xml.merchant;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.talool.core.MerchantAccount;
import com.talool.kirke.JobStatus;
import com.talool.kirke.KirkeException;
import com.talool.kirke.xml.XMLDocHandler;
import com.talool.kirke.xml.XMLUtils;
import com.talool.kirke.xml.convertor.MerchantConvertor;

public class MerchantDocHandler implements XMLDocHandler {
	
	private static final String TaloolTag = "Talool";
	
	private String nextPage;

	@Override
	public void process(Document doc, MerchantAccount merchantAccount) throws KirkeException 
	{
		NodeList root = doc.getChildNodes();
	    Node talool = XMLUtils.getNode(TaloolTag, root);
	    NodeList merchants = talool.getChildNodes();
	    
	    nextPage = XMLUtils.getNodeAttr("nextPage", talool);
	    
	    MerchantConvertor convertor = MerchantConvertor.get();
		convertor.setMerchantAccount(merchantAccount);
	    convertor.convert(merchants);
	    
	    StringBuilder sb = new StringBuilder();
	    sb.append("Processing complete.  ");
	    if (hasNextPage())
	    {
	    	sb.append("Ready for the next page.  ");
	    }
	    
	    JobStatus.get().println(sb.toString());
	    JobStatus.get().addPage();
	}

	@Override
	public boolean hasNextPage() {
		return !nextPage.isEmpty();
	}

	@Override
	public String getNextPage() {
		return nextPage;
	}
	
}
