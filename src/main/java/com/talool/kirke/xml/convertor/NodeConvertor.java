package com.talool.kirke.xml.convertor;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.talool.kirke.xml.XMLUtils;


abstract public class NodeConvertor {
	
	static protected Node getNode(String tagName, NodeList nodes) {
	    return XMLUtils.getNode(tagName, nodes);
	}
	 
	static protected String getNodeValue( Node node ) {
		return XMLUtils.getNodeValue(node);
	}
	 
	static protected String getNodeValue(String tagName, NodeList nodes ) {
		return XMLUtils.getNodeValue(tagName, nodes);
	}
	 
	static protected String getNodeAttr(String attrName, Node node ) {
		return XMLUtils.getNodeAttr(attrName, node);
	}
	 
	static protected String getNodeAttr(String tagName, String attrName, NodeList nodes ) {
		return XMLUtils.getNodeAttr(tagName, attrName, nodes);
	}

}
