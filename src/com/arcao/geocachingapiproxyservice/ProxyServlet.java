package com.arcao.geocachingapiproxyservice;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ProxyServlet extends HttpServlet {
	private static final Logger log = Logger.getLogger(ProxyServlet.class.getName());
	private static final long serialVersionUID = -1601990536155916742L;
	
	String API_URL = "https://api.groundspeak.com/LiveV6/geocaching.svc";

	@Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) {
		doPost(request, response);
	}

	@Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) {
		try {
			int len;
			byte[] buffer = new byte[8192];

			String urlString = API_URL + request.getPathInfo();
			String queryString = request.getQueryString();

			urlString += queryString == null ? "" : "?" + queryString;
			URL url = new URL(urlString);

			log.info("Fetching >" + url.toString());

			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setConnectTimeout(30000);
			con.setReadTimeout(30000);
			
			String methodName = request.getMethod();
			
			con.setRequestMethod(methodName);
			con.setDoOutput(true);
			con.setDoInput(true);
			//HttpURLConnection.setFollowRedirects(false);
			con.setUseCaches(true);

			for (Enumeration e = request.getHeaderNames(); e.hasMoreElements();) {
				String headerName = e.nextElement().toString();
				con.setRequestProperty(headerName, request.getHeader(headerName));
			}

			con.connect();

			if (methodName.equals("POST")) {
				BufferedInputStream clientToProxyBuf = new BufferedInputStream(request.getInputStream());
				BufferedOutputStream proxyToWebBuf = new BufferedOutputStream(con.getOutputStream());

				while ((len = clientToProxyBuf.read(buffer)) != -1)
					proxyToWebBuf.write(buffer, 0, len);

				proxyToWebBuf.flush();
				proxyToWebBuf.close();
				clientToProxyBuf.close();
			}

			int statusCode = con.getResponseCode();
			response.setStatus(statusCode);

			for (Entry<String, List<String>> header : con.getHeaderFields().entrySet()) {
				for (String entryItem : header.getValue()) {
					if (entryItem != null) {
						response.setHeader(header.getKey(), entryItem);
					}
				}
			}

			BufferedInputStream webToProxyBuf = new BufferedInputStream(con.getInputStream());
			BufferedOutputStream proxyToClientBuf = new BufferedOutputStream(response.getOutputStream());

			while ((len = webToProxyBuf.read(buffer)) != -1)
				proxyToClientBuf.write(buffer, 0, len);

			proxyToClientBuf.flush();
			proxyToClientBuf.close();

			webToProxyBuf.close();
			con.disconnect();

		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			log.severe(sw.toString());
		}
	}
}