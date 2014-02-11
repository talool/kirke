package com.talool.kirke.xml.merchant;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.talool.core.MerchantAccount;
import com.talool.core.service.ServiceException;
import com.talool.kirke.ServiceUtils;

public class MerchantJob {

	private static final Logger log = Logger.getLogger(MerchantJob.class);
	
	private Transformer transformer;
	private Validator validator;
	private MerchantAccount merchantAccount;
	private String xmlPath;
	private boolean validateXml;
	

	public MerchantJob(String xmlPath, String xslFilePath, String merchantAccountIdString, String namespace) {
		super();
		
		this.xmlPath = xmlPath;
		this.validateXml = true;
		
		try 
		{
			
			Long merchantAccountId = Long.parseLong(merchantAccountIdString);
			merchantAccount = ServiceUtils.get().getService().getMerchantAccountById(merchantAccountId);
			
			// Load the XSL file
			InputStream xslIn = this.getClass().getResourceAsStream(xslFilePath);
			transformer = TransformerFactory.newInstance().newTransformer(new StreamSource(xslIn));
			if (namespace != null)
			{
				transformer.setParameter("namespance", namespace);
			}
			
			// Load the XSD
			SchemaFactory schemaFactory = SchemaFactory .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			InputStream xsdIn = this.getClass().getResourceAsStream("/XML/Talool.xsd");
			Schema schema = schemaFactory.newSchema(new StreamSource(xsdIn));
			validator = schema.newValidator();
		}
		catch (ServiceException se) {
			log.error("failed to get the Merchant Account by id",se);
		} catch (SAXException e) {
			log.error("Failed to create transformer",e);
		} catch (TransformerConfigurationException e) {
			log.error("Failed to setup transformation",e);
		} catch (TransformerFactoryConfigurationError e) {
			log.error("Failed to setup transformation",e);
		}
		
	}

	public void execute()
	{
		try 
		{
			// Load the XML source
			InputStream taloolXml = getTransformedXml();
			if (taloolXml != null)
			{
				boolean processing = true;
				while (processing)
				{
					DOMParser parser = new DOMParser();
				    parser.parse(new InputSource(taloolXml));
				    Document doc = parser.getDocument();
				    
				    MerchantDocHandler handler = new MerchantDocHandler();
				    handler.process(doc, merchantAccount);
				    if (handler.hasNextPage())
				    {
				    	xmlPath = handler.getNextPage();
				    	System.out.println("Next page: "+xmlPath);
				    	taloolXml = getTransformedXml();
				    	if (taloolXml == null) processing = false;
				    }
				    else
				    {
				    	processing = false;
				    }
				}
			}
		}
		catch(TransformerException te)
		{
			log.error("XML transformation failed",te);
		}
		catch(SAXException se)
		{
			log.error("DOM Parser failed",se);
		}
		catch (IOException ioe) {
			log.error("Failed to convert talool xml to input stream or input source.",ioe);
		} 
		
	}
	
	private InputStream getXML(String src)
	{
		InputStream xmlIn = null;
		if (src.toLowerCase().startsWith("http"))
		{
			// it's a URL
			URL url;
			try {
				url = new URL( src );
				xmlIn = url.openStream();
			} catch (MalformedURLException e) {
				log.error(e);
			} catch (IOException ioe) {
				log.error(ioe);
			} 
	        
		}
		else
		{
			// it's a resource file
			xmlIn = this.getClass().getResourceAsStream(src);
		}
		
		return xmlIn;
	}
	
	private InputStream getTransformedXml() throws TransformerException, IOException
	{
		StringWriter taloolXmlWriter = new StringWriter();
		InputStream xmlIn = getXML(xmlPath);
		StreamResult result = new StreamResult(taloolXmlWriter);
		transformer.transform( new StreamSource(xmlIn), result);
		
		InputStream xmlOut = null;
		if (validateXml && validate(IOUtils.toInputStream(taloolXmlWriter.getBuffer().toString(), "UTF-8")))
		{
			xmlOut = IOUtils.toInputStream(taloolXmlWriter.getBuffer().toString(), "UTF-8");
		}
		return xmlOut;
	}
	
	public boolean validate(InputStream xml)
	{
		try 
		{
			validator.validate(new StreamSource(xml));
			return true;
		} 
		catch (Exception e) 
		{
			log.error("XSD Validation failed", e);
			return false;
		}
	}
}
