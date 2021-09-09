package com.sonalb.net.http.cookie;

import java.net.URLConnection;
import java.net.URL;
import java.util.Iterator;

import com.sonalb.net.http.Header;
import com.sonalb.net.http.HeaderUtils;

/*
According to RFC2965, if Set-Cookie and set-cookie2 both describe the same cookie, then sc2 should be used.
	This distinction has not been incorporated.
If Request URL="http://www.example.com/acme" and Cookie header path="/acme". The cookie
	object path="/acme/". Thus, pathMatch fails. Is this bad ?

Strict parsing means that if one cookie is bad, then processing stops. and MCE is thrown. Lenient 
parsing ignores bad cookies and continues trying to parse valid ones.
*/

/**
 * This class is used to invoke the cookie-handling logic of the jCookie Library. 
 * It is the developer's view of the library. All cookie-handling methods are invoked on this 
 * object. The following snippet shows common-case usage. The highlighted portion is the only 
 * cookie-handling code, as far as the developer is concerned.
 * <br>
 * <pre>
 * import com.sonalb.net.http.cookie.*;
 * import java.net.*;
 * import java.io.*;
 * ...
 *
 * public class Example
 * {
 *      ...
 *      
 *      public void someMethod()
 *      {
 *         ...
 *         URL url = new URL("http://www.site.com/");
 *         HttpURLConnection huc = (HttpURLConnection) url.openConnection();
 *
 *         // Setup the HttpURLConnection here
 *         ...
 *
 *         huc.connect();
 *         InputStream is = huc.getInputStream();
 *         <strong>Client client = new Client();
 *         CookieJar cj = client.getCookies(huc);</strong>
 *
 *         // Do some processing
 *         ...
 *
 *         huc.disconnect();
 *
 *         // Make another request
 *         url = new URL("http://www.site.com/");
 *         huc = (HttpURLConnection) url.openConnection();
 *         <strong>client.setCookies(huc, cj);</strong>
 *
 *         huc.connect();
 *         ...
 *      }
 *   }
 * </pre>
 * @author	Sonal Bansal
 */

public class Client
{
	private static final CookieParser defaultCookieParser = new RFC2965CookieParser();

	private CookieParser currentCookieParser = defaultCookieParser;

	/**
	 * Returns the built-in <code>CookieParser</code> implementation. Current implementation 
	 * conforms to RFC-2965.
	 * 
	 * @return the default <code>CookieParser</code> implementation
	 * @see RFC2965CookieParser
	 */
	public static CookieParser getDefaultCookieParser()
	{
		return(defaultCookieParser);
	}

	/**
	 * Resets the <code>CookieParser</code> implementation to be used for this instance, to the default 
	 * (built-in) implementation.
	 * 
	 * @see #setCookieParser(CookieParser)
	 */
	public void resetToDefaultCookieParser()
	{
		synchronized(currentCookieParser)
		{
				currentCookieParser = defaultCookieParser;
		}
	}

	/**
	 * Sets the <code>CookieParser</code> implementation to be used in this instance.
	 * 
	 * @param cp	the CookieParser to be used
	 */
	public void setCookieParser(CookieParser cp)
	{
		if(cp == null)
		{
			return;
		}

		synchronized(currentCookieParser)
		{
			currentCookieParser = cp;
		}
	}

	/**
	 * Gets the <code>CookieParser</code> implementation being used in this instance.
	 * 
	 * @return the CookieParser in use
	 */
	public CookieParser getCookieParser()
	{
		return(currentCookieParser);
	}

	/**
	 * Constructs an instance using the default CookieParser
	 * 
	 * @see CookieParser
	 * @see #getDefaultCookieParser()
	 */
	public Client()
	{
	}

	/**
	 * Processes cookie headers from the given URLConnection. This method <em>must</em> be called 
	 * <strong>after</strong> the URLConnection is connected.
	 *
	 * @param urlConn	the URLConnection to be processed
	 * @returns the CookieJar containing all the Cookies extracted
	 * @throws MalformedCookieException	if there was some error during cookie processing
	 */
	public CookieJar getCookies(URLConnection urlConn) throws MalformedCookieException
	{
		return(getCookies(urlConn, urlConn.getURL()));
	}

	protected CookieJar getCookies(URLConnection urlConn, URL url) throws MalformedCookieException
	{
		if(urlConn == null)
		{
			return(getCookies((Header) null,url));
		}

		return(getCookies(HeaderUtils.extractHeaders(urlConn),url));
	}

	protected CookieJar getCookies(Header header, URL url) throws MalformedCookieException
	{
		return(currentCookieParser.parseCookies(header, url));
	}

	/**
	 * Sets cookie headers on the given URLConnection, using Cookies in the CookieJar. 
	 * This method <em>must</em> be called <strong>before</strong> the URLConnection is connected.
	 *
	 * @param urlConn	the URLConnection to be processed
	 * @param cj	the CookieJar containing the Cookies to be set
	 * @returns the CookieJar containing the Cookies that were actually set
	 */
	public CookieJar setCookies(URLConnection urlConn, CookieJar cj)
	{
		/*A particular cookie header should contain only one version of cookies.
		Are multiple cookie headers allowed ?*/

		if(urlConn == null || cj == null)
		{
			throw new IllegalArgumentException("Null URLConnection or CookieJar");
		}

		if(cj.isEmpty())
		{
			return(cj);
		}

		//System.out.println();
		//System.out.println("Client.setCookies(): I've been asked to set following cookies:-");
		//System.out.println(cj);

		CookieJar eligibleCookies = CookieUtils.getCookiesForURL(cj, currentCookieParser, urlConn.getURL(),true);

		//System.out.println();
		//System.out.println("Client.setCookies(): Eligible cookies are:-");
		//System.out.println(eligibleCookies);

		Header h = currentCookieParser.getCookieHeaders(eligibleCookies);
		//System.out.println("Client.setCookies(): Headers Set = " + h);
		HeaderUtils.setHeaders(urlConn, h);

		return(eligibleCookies);
	}
}