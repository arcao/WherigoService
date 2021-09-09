package com.sonalb.net.http;

/**
 * Represents a single name-value pair of an HTTP Header.
 * @author Sonal Bansal
 */
public class HeaderEntry implements Cloneable
{
	private String key;
	private String value;

	private HeaderEntry()
	{
	}

	/** 
	 * Creates a HeaderEntry with specified key and value.
	 * @param key the name; must be non-null
	 * @param value the value
	 */
	public HeaderEntry(String key, String value)
	{
		if(key == null)
		{
			throw new IllegalArgumentException("The Key can't be null");
		}

		this.key = key;
		this.value = value;
	}

	/**
	 * Gets the Key/Name.
	 */
	public String getKey()
	{
		return(key);
	}

	/**
	 * Gets the Value.
	 */
	public String getValue()
	{
		return(value);
	}

	public boolean equals(Object o)
	{
		int i=0;

		if(o instanceof HeaderEntry)
		{
			HeaderEntry x = (HeaderEntry) o;

			if(key.equalsIgnoreCase(x.getKey()))
			{
				i++;
			}

			if(value != null)
			{
				if(value.equals(x.getValue()))
				{
					i++;
				}
			}
			else if(x.getValue() == null)
			{
				i++;
			}
		}

		if(i != 2)
		{
			return(false);
		}

		return(true);
	}

	public String toString()
	{
		return(key + ":" + value);
	}

	public Object clone() throws CloneNotSupportedException
	{
		return(super.clone());
	}
}