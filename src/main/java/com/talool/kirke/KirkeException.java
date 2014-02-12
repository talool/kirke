package com.talool.kirke;

public class KirkeException extends Exception {

	private static final long serialVersionUID = -5466862769726650284L;
	
	protected KirkeErrorCode errorCode = KirkeErrorCode.UNKNOWN;

	public KirkeException()
	{
		super();
	}

	public KirkeException(String arg0, Throwable arg1)
	{
		super(arg0, arg1);
	}

	public KirkeException(String arg0)
	{
		super(arg0);
	}

	public KirkeException(Throwable arg0)
	{
		super(arg0);
	}

	public KirkeException(KirkeErrorCode errorCode)
	{
		super();
		this.errorCode = errorCode;
	}

	public KirkeException(KirkeErrorCode errorCode, String arg0, Throwable arg1)
	{
		super(arg0, arg1);
		this.errorCode = errorCode;
	}

	public KirkeException(KirkeErrorCode errorCode, String arg0)
	{
		super(arg0);
		this.errorCode = errorCode;
	}

	public KirkeException(KirkeErrorCode errorCode, Throwable arg0)
	{
		super(arg0);
		this.errorCode = errorCode;
	}

	public KirkeErrorCode getErrorCode()
	{
		return errorCode;
	}

	@Override
	public String getMessage()
	{
		StringBuilder sb = new StringBuilder();

		if (errorCode != null)
		{
			sb.append(errorCode.getMessage());
			if (super.getMessage() != null)
			{
				sb.append(": ").append(super.getMessage());
			}
			return sb.toString();
		}

		return super.getMessage();
	}
}
