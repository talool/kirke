package com.talool.kirke.xml.merchant;

import org.apache.log4j.Logger;

import com.talool.kirke.JobStatus;
import com.talool.kirke.KirkeException;


public class MerchantJobManager {

	private static final Logger log = Logger.getLogger(MerchantJobManager.class);
	
	public static void main(String[] args) {

		if (args.length >= 3)
		{
			String namespace = null;
			if (args.length ==  4) namespace = args[3];

			JobStatus.get().print(JobStatus.get().getSetupSummary(args[0], args[1], args[2], namespace));
			
			try 
			{
				MerchantJob job = new MerchantJob(args[0], args[1], args[2], namespace);
				job.execute();
			} 
			catch (KirkeException e) 
			{
				log.error("Merchant Job Failed.", e);
				JobStatus.get().setFailed();
			}
			
			JobStatus.get().print(JobStatus.get().getJobSummary());
			
		}
		else
		{
			JobStatus.get().print(JobStatus.get().getUsageMessage());
		}
		
		System.exit(1);
	}

}



/**
 * 
 * LocalSaver url: 
 * http://api.datasphere.com/SyndicationCoupons/SyndicationCoupons.svc/All/All/?uid=477139B4-C00F-4C3E-AEC4-C9786EDC0571&fdType=DsFormatV4CJ&pg=1
 * LocalSaver UID: 477139B4-C00F-4C3E-AEC4-C9786EDC0571
 * LocalSaver CJ_PID: 4155713
 * 
 * java -jar kirke.jar http://api.datasphere.com/SyndicationCoupons/SyndicationCoupons.svc/All/All/?uid=477139B4-C00F-4C3E-AEC4-C9786EDC0571\&fdType=DsFormatV4CJ\&pg=1 /xsl/LocalSaver.xsl 37
 * */
