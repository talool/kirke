package com.talool.kirke.xml.merchant;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.talool.core.MerchantAccount;
import com.talool.kirke.xml.XMLDocHandler;
import com.talool.kirke.xml.XMLUtils;
import com.talool.kirke.xml.convertor.MerchantConvertor;

public class MerchantDocHandler implements XMLDocHandler {
	
	private static final String TaloolTag = "Talool";

	@Override
	public void process(Document doc, MerchantAccount merchantAccount) {
		NodeList root = doc.getChildNodes();
	    Node talool = XMLUtils.getNode(TaloolTag, root);
	    NodeList merchants = talool.getChildNodes();
	    
	    MerchantConvertor convertor = MerchantConvertor.get();
		convertor.setMerchantAccount(merchantAccount);
	    convertor.convert(merchants);
	}
	
}
