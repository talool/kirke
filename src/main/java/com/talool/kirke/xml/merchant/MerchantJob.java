package com.talool.kirke.xml.merchant;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

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

	public void execute(String endPointUrl, String xslFilePath, String merchantAccountIdString, String namespace)
	{
		 
		StringWriter taloolXmlWriter = new StringWriter();
		      
		try 
		{
			
			Long merchantAccountId = Long.parseLong(merchantAccountIdString);
			MerchantAccount merchantAccount = ServiceUtils.get().getService().getMerchantAccountById(merchantAccountId);
			
			// Load the XSL file
			InputStream xslIn = this.getClass().getResourceAsStream(xslFilePath);
			Transformer xmlTransformer = TransformerFactory.newInstance().newTransformer(
			         new StreamSource(xslIn)
			      );

			if (namespace != null)
			{
				xmlTransformer.setParameter("namespance", namespace);
			}
			
			// Load the XML source
			InputStream xmlIn;
			if (endPointUrl.toLowerCase().startsWith("http"))
			{
				// it's a URL
				URL url = new URL( endPointUrl ); 
		        xmlIn = url.openStream();
			}
			else
			{
				// it's a resource file
				xmlIn = this.getClass().getResourceAsStream(endPointUrl);
			}
			
			
			// Transform the 3rd party XML to Talool XML
			StreamResult result = new StreamResult(taloolXmlWriter);
			xmlTransformer.transform( new StreamSource(xmlIn), result);
			log.debug(taloolXmlWriter.getBuffer().toString());
			InputStream taloolXml = IOUtils.toInputStream(taloolXmlWriter.getBuffer().toString(), "UTF-8");
			
			DOMParser parser = new DOMParser();
		    parser.parse(new InputSource(taloolXml));
		    Document doc = parser.getDocument();
		    
		    MerchantDocHandler handler = new MerchantDocHandler();
		    handler.process(doc, merchantAccount);
		}
		catch(TransformerFactoryConfigurationError tfce)
		{
			log.error("Failed to setup transformation",tfce);
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
		catch (ServiceException se) 
		{
			log.error("failed to get the Merchant Account by id",se);
		}
	}
}
