package com.talool.kirke;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.talool.core.DomainFactory;
import com.talool.core.service.TaloolService;
import com.talool.kirke.xml.merchant.MerchantJobManager;
import com.talool.service.ServiceFactory;

public class ServiceUtils {
	static protected ServiceUtils INSTANCE;

	private final DomainFactory domainFactory;
	private final TaloolService service;
	
	
	private ServiceUtils() {
		ApplicationContext context = new ClassPathXmlApplicationContext("/kirkeContext.xml",MerchantJobManager.class);
		ServiceFactory serviceFactory = (ServiceFactory) context.getBean("serviceFactory");
		domainFactory = (DomainFactory) context.getBean("domainFactory");
		service = serviceFactory.getTaloolService();
	}
	
	static public ServiceUtils get()
	{
		if (INSTANCE==null)
		{
			INSTANCE = new ServiceUtils();
		}
		return INSTANCE;
	}
	
	public TaloolService getService()
	{
		return service;
	}
	
	public DomainFactory getFactory()
	{
		return domainFactory;
	}
}
