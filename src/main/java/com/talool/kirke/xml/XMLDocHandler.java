package com.talool.kirke.xml;

import org.w3c.dom.Document;

import com.talool.core.MerchantAccount;

public interface XMLDocHandler {

	public void process(Document doc, MerchantAccount merchantAccount);
	public boolean hasNextPage();
	public String getNextPage();
	
}
