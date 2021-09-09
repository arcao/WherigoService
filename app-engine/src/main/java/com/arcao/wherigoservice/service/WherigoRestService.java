package com.arcao.wherigoservice.service;

import com.arcao.wherigoservice.datamapper.ResponseCode;
import com.arcao.wherigoservice.datamapper.ResponseDataMapper;
import com.arcao.wherigoservice.rest.RestServlet;
import com.sonalb.net.http.HTTPRedirectHandler;
import com.sonalb.net.http.cookie.Cookie;
import com.sonalb.net.http.cookie.CookieJar;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WherigoRestService extends RestServlet {
	public void login(HttpServletRequest req, ResponseDataMapper resp) throws IOException {
		String username = getParameter(req, String.class, "username", null);
		String password = getParameter(req, String.class, "password", null);
		
		if (username == null || password == null) {
			resp.writeErrorResponse(ResponseCode.ApiError, "Not valid parameters");
			return;
		}
		
		Map<String, String> p = new HashMap<>();
		
		HTTPRedirectHandler hrh = createGetUrl("http://www.wherigo.com/login/default.aspx?ReturnUrl=%2fhome.aspx", null);
		
		if (handleConnectionError(hrh, resp))
			return;
		
		Collection<String> inputs = getFormInputs(getContent(hrh));
		
		p.put("__VIEWSTATE", getFormInputValue(inputs, "__VIEWSTATE", ""));
		p.put("ctl00$ContentPlaceHolder1$Login1$Login1$UserName", username);
		p.put("ctl00$ContentPlaceHolder1$Login1$Login1$Password", password);
		p.put("ctl00$ContentPlaceHolder1$Login1$Login1$LoginButton", "Sign In");
		
		hrh.getConnection().disconnect();
		
		hrh = createPostUrl("http://www.wherigo.com/login/default.aspx?ReturnUrl=%2fhome.aspx", p, hrh.getCookieJar());
		
		if (handleConnectionError(hrh, resp))
			return;
		
		if (!"/home.aspx".equals(hrh.getConnection().getURL().getPath())) {
			resp.writeErrorResponse(ResponseCode.InvalidCredentials);
			return;
		}
			
		
		CookieJar cookies = hrh.getCookieJar().getCookies("ASP.NET_SessionId");
		if (cookies.isEmpty()) {
			resp.writeErrorResponse(ResponseCode.ApiError, "Session cookie not found");
			return;
		}
		
		Cookie c = (Cookie) cookies.iterator().next();
				
		resp.writeLoginResponse(c.getValue());
	}
	
	public void getCartridgeGuid(HttpServletRequest req, ResponseDataMapper resp) throws IOException {
		String cacheCode = getParameter(req, String.class, "cacheCode", null);
		
		if (cacheCode == null) {
			resp.writeErrorResponse(ResponseCode.ApiError, "Not valid cacheCode");
			return;
		}
		
		HTTPRedirectHandler hrh = createGetUrl("http://www.geocaching.com/seek/cache_details.aspx?wp=" + cacheCode, null);
		if (handleConnectionError(hrh, resp))
			return;
		
		String content = getContent(hrh);
		
		Pattern p = Pattern.compile(Pattern.quote("http://www.wherigo.com/cartridge/details.aspx?CGUID=") + "([A-Za-z0-9-]+)");
		Matcher m = p.matcher(content);
		if (m.find() && m.groupCount() == 1) {
			resp.writeGetCartridgeResponse(m.group(1));
		} else {
			resp.writeErrorResponse(ResponseCode.CartridgeNotFound);
		}
	}
	
	public void getCartridgeDownloadData(HttpServletRequest req, ResponseDataMapper resp) throws IOException {
		String cartridgeGuid = getParameter(req, String.class, "CartridgeGUID", null);
		String session = getParameter(req, String.class, "session", null);
 
		if (cartridgeGuid == null || session == null) {
			resp.writeErrorResponse(ResponseCode.ApiError, "Not valid parameters");
			return;
		}
		
		CookieJar cookies = new CookieJar();
		cookies.add(new Cookie("ASP.NET_SessionId", session, "www.wherigo.com", "/"));
		
		HTTPRedirectHandler hrh = createGetUrl("http://www.wherigo.com/cartridge/download.aspx?CGUID=" + cartridgeGuid, cookies);
		if (handleConnectionError(hrh, resp))
			return;
		
		String content = getContent(hrh);

		if (content.contains("Not Signed In")) {
			resp.writeErrorResponse(ResponseCode.InvalidSession);
			return;
		}
		
		if (!content.contains("ctl00$ContentPlaceHolder1$uxDeviceList")) {
			resp.writeErrorResponse(ResponseCode.ApiError, "Cartridge download isn't possible");
			return;
		}
		
		Collection<String> inputs = getFormInputs(content);
		
		Map<String, String> p = new HashMap<>();
		p.put("__VIEWSTATE", getFormInputValue(inputs, "__VIEWSTATE", ""));
		p.put("ctl00$ContentPlaceHolder1$uxDeviceList", "4");
		p.put("ctl00$ContentPlaceHolder1$btnDownload", "Download Now");
		p.put("ctl00$SimpleSearchControl$txtZip", "Zip Code");
		p.put("ctl00$SimpleSearchControl$ddlState1", "-1");
		p.put("ctl00$SimpleSearchControl$ddlCountry1", "-1");
		
		resp.writeGetCartridgeDownloadDataResponse(p, session);
	}
	
	public void getCacheCodeFromGuid(HttpServletRequest req, ResponseDataMapper resp) throws IOException {
		String cacheGuid = getParameter(req, String.class, "CacheGUID", null);
		
		HTTPRedirectHandler hrh = createGetUrl("http://www.geocaching.com/seek/cache_details.aspx?guid=" + cacheGuid, null);
		if (handleConnectionError(hrh, resp))
			return;
		
		String content = getContent(hrh);
		
		Pattern p = Pattern.compile(">(GC[A-Z0-9]+)<");
		Matcher m = p.matcher(content);
		if (m.find() && m.groupCount() == 1) {
			resp.writeGetCacheCodeFromGuidResponse(m.group(1));
		} else {
			p = Pattern.compile("wp=(GC[A-Z0-9]+)&amp;title");
			m = p.matcher(content);
			if (m.find() && m.groupCount() == 1) {
				resp.writeGetCacheCodeFromGuidResponse(m.group(1));
			} else {
				resp.writeErrorResponse(ResponseCode.CacheNotFound);
			}
		}
	}

	public void getTime(HttpServletRequest req, ResponseDataMapper resp) throws IOException {
		resp.writeTimeResponse(System.currentTimeMillis());
	}
		
	private static boolean handleConnectionError(HTTPRedirectHandler hrh, ResponseDataMapper resp) throws IOException {
		if (hrh.getConnection().getResponseCode() != 200) {
			resp.writeErrorResponse(ResponseCode.ConnectionError, hrh.getConnection().getResponseCode() + " " + hrh.getConnection().getResponseMessage());
			return true;
		}
				
		return false;
	}
	
	private static String getFormInputValue(Collection<String> inputCollection, String name, String defaultValue) {
		Pattern p = Pattern.compile("name\\s*=\\s*([\"'])" + Pattern.quote(name) + "\\1");
		
		for (String input : inputCollection) {
			if (p.matcher(input).find()) {
				Matcher m = Pattern.compile("value\\s*=\\s*([\"'])((?:(?!\\1).)*)\\1").matcher(input);
				if (m.find() && m.groupCount() > 1) {
					return m.group(2);
				}
			}
		}
		
		return defaultValue;
	}
	
	private static Collection<String> getFormInputs(String content) {
		Pattern p = Pattern.compile("<input[^>]*>");
		
		ArrayList<String> list = new ArrayList<>();
		
		Matcher m = p.matcher(content);
		
		while (m.find()) {
			list.add(m.group());
		}
		
		return list;
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
	
	private static HTTPRedirectHandler createPostUrl(String url, Map<String, String> body, CookieJar cookieJar) throws IOException {
		// create body part
		StringBuilder sb = new StringBuilder();
		for (Entry<String, String> entry : body.entrySet()) {
			if (sb.length() > 0)
				sb.append("&");
			
			sb.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
			sb.append("=");
			sb.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
		}
		
		return createPostUrl(url, sb.toString().getBytes(), cookieJar);
	}
	
	private static HTTPRedirectHandler createPostUrl(String url, byte[] body, CookieJar cookieJar) throws IOException {
		try {
			HttpURLConnection huc = (HttpURLConnection) new URL(url).openConnection();
			
			huc.setDoOutput(true);
			huc.setRequestMethod("POST");
										
			HTTPRedirectHandler hrh = new HTTPRedirectHandler(huc);
			
			hrh.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 5.1; rv:2.0.1) Gecko/20100101 Firefox/4.0.1");
			hrh.addHeader("Accept", "text/xml,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
			hrh.addHeader("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
			hrh.addHeader("Accept-Language", "en-us,en;q=0.5");
			
			if (cookieJar != null)
				hrh.addCookies(cookieJar);
			
			hrh.setHeaders();
			
			OutputStream os = huc.getOutputStream();
			os.write(body);
			os.close();
			
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
			InputStreamReader isr = new InputStreamReader(is, "UTF-8");
			
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
