package com.talool.kirke.xml.merchant;


public class MerchantJobManager {

	public static void main(String[] args) {

		MerchantJob job = new MerchantJob();
		if (args.length >= 3)
		{
			String namespace = null;
			if (args.length ==  4) namespace = args[3];
			
			System.out.println("====  MERCHANT JOB STARTING  =============================================");
			System.out.println("====  3rd Party XML: "+args[0]);
			System.out.println("====  3rd Party XSL: "+args[1]);
			System.out.println("====  Created By Merchant Account Id: "+args[2]);
			System.out.println("====  Namespace: "+namespace);
			System.out.println("==========================================================================");
			job.execute(args[0], args[1], args[2], namespace);
			System.out.println("====  MERCHANT JOB COMPLETE  =============================================");
		}
		else
		{
			System.out.println("====  MERCHANT JOB ABORTING  =============================================");
			System.out.println("You must provide the following arguments:");
			System.out.println(" * A resource path or URL for the 3rd Party XML");
			System.out.println(" * A resource path to the 3rd Party XSL");
			System.out.println(" * Created By Merchant Account Id");
			System.out.println(" ");
			System.out.println("You may also provide the following argument:");
			System.out.println(" * Namespace for the 3rd Party XML");
			System.out.println("====  MERCHANT JOB COMPLETE  =============================================");
		}
		
		System.exit(1);
	}

}
