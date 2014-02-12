package com.talool.kirke.xml;

import org.w3c.dom.Document;

import com.talool.core.MerchantAccount;
import com.talool.kirke.KirkeException;

public interface XMLDocHandler {

	public void process(Document doc, MerchantAccount merchantAccount) throws KirkeException;
	public boolean hasNextPage();
	public String getNextPage();
	
}
