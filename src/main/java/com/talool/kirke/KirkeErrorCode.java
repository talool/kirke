package com.talool.kirke;

import java.util.HashMap;
import java.util.Map;

public enum KirkeErrorCode
{
	UNKNOWN(0, "Unknown"),
	JOB_FAILED(1, "The job exited prematurely."),
	MERCHANT_ERROR(100, "Failed to convert merchant."),
	LOCATION_ERROR(101, "Failed to convert merchant location."),
	TAG_ERROR(102, "Failed to convert tag."),
	CATEGORY_ERROR(103, "Failed to get category."),
	DEAL_ERROR(104, "Failed to convert deal."),
	GEO_ERROR(105, "Failed to find geometry for location."),
	MEDIA_ERROR(106, "Failed to convert media."),
	MEDIA_TOO_BIG_ERROR(107, "Media was too large."),
	MEDIA_EXISTS_ERROR(108, "Media already exists for this merchant."),
	MEDIA_NOT_FOUND_ERROR(109, "Media not found at url provided in 3rd party xml."),
	MEDIA_TYPE_ERROR(110, "Media was not of type PNG or JPG.");
	

	private final int code;
	private final String message;
	private static Map<Integer, KirkeErrorCode> errorCodeMap = new HashMap<Integer, KirkeErrorCode>();

	static
	{
		for (KirkeErrorCode ec : KirkeErrorCode.values())
		{
			errorCodeMap.put(ec.getCode(), ec);
		}

	}

	public static KirkeErrorCode findByCode(final int code)
	{
		return errorCodeMap.get(code);
	}

	public int getCode()
	{
		return code;
	}

	public String getMessage()
	{
		return message;
	}

	KirkeErrorCode(int code, String message)
	{
		this.code = code;
		this.message = message;
	}

}