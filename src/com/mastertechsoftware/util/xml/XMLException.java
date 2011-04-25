/*
 * @author Kevin Moore
 *
 */
package com.mastertechsoftware.util.xml;

/**
 * 
 * @author Kevin Moore
 */
public class XMLException extends Exception
{
	/**
	 * Constructor for XMLException.
	 */
	public XMLException()
	{
		super();
	}

	/**
	 * Constructor for XMLException.
	 * 
	 * @param message
	 */
	public XMLException(String message)
	{
		super(message);
	}

	/**
	 * Constructor for XMLException.
	 * 
	 * @param message
	 * @param cause
	 */
	public XMLException(String message, Throwable cause)
	{
		super(message, cause);
	}

	/**
	 * Constructor for XMLException.
	 * 
	 * @param cause
	 */
	public XMLException(Throwable cause)
	{
		super(cause);
	}

}
