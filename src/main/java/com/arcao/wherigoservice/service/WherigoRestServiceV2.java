package com.arcao.wherigoservice.service;

import com.arcao.wherigoservice.datamapper.ResponseDataMapper;
import com.arcao.wherigoservice.rest.RestServlet;
import com.sonalb.net.http.HTTPRedirectHandler;
import com.sonalb.net.http.cookie.CookieJar;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WherigoRestServiceV2 extends RestServlet {
	@SuppressWarnings("unused")
	public void guidToReferenceCode(HttpServletRequest req, ResponseDataMapper resp) throws IOException {
		String cacheGuid = getParameter(req, String.class, "guid", null);
		
		HTTPRedirectHandler hrh = createGetUrl("http://www.geocaching.com/seek/cache_details.aspx?guid=" + cacheGuid, null);
		if (handleConnectionError(hrh, resp))
			return;
		
		String content = getContent(hrh);
		
		Pattern p = Pattern.compile(">(GC[A-Z0-9]+)<");
		Matcher m = p.matcher(content);
		if (m.find() && m.groupCount() == 1) {
			resp.writeGuidToReferenceCode(m.group(1));
		} else {
			p = Pattern.compile("wp=(GC[A-Z0-9]+)&amp;title");
			m = p.matcher(content);
			if (m.find() && m.groupCount() == 1) {
				resp.writeGuidToReferenceCode(m.group(1));
			} else {
				resp.writeErrorV2Response(404, "Not Found", "Geocache not found");
			}
		}
	}

		
	private static boolean handleConnectionError(HTTPRedirectHandler hrh, ResponseDataMapper resp) throws IOException {
		if (hrh.getConnection().getResponseCode() != 200) {
			resp.writeErrorV2Response(hrh.getConnection().getResponseCode(), hrh.getConnection().getResponseMessage(), "");
			return true;
		}
				
		return false;
	}

	
	// ----------------------- Url Connection helper methods -------------------------------
	
	private static HTTPRedirectHandler createGetUrl(String url, CookieJar cookieJar) throws IOException {
		try {
			HttpURLConnection huc = (HttpURLConnection) new URL(url).openConnection();

			HTTPRedirectHandler hrh = new HTTPRedirectHandler(huc);
			
			hrh.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 5.1; rv:2.0.1) Gecko/20100101 Firefox/4.0.1");
			hrh.addHeader("Accept", "text/xml,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
			hrh.addHeader("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
			hrh.addHeader("Accept-Language", "en-us,en;q=0.5");
			
			if (cookieJar != null)
				hrh.addCookies(cookieJar);
			
			hrh.setHeaders();
			
			hrh.connect();
			
			return hrh;
		} catch (MalformedURLException e) {
			throw new IOException(e);
		}
	}

	
	private static String getContent(HTTPRedirectHandler hrh) throws IOException {
		StringBuilder sb = new StringBuilder();
		
		InputStream is = hrh.getConnection().getInputStream();
		if (is != null) {
			InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
			
			char[] buffer = new char[8192];
			int len;
			
			while((len = isr.read(buffer)) > 0) {
				sb.append(buffer, 0, len);
			}
			isr.close();
		}
		
		return sb.toString();
	}
}
