package com.arcao.wherigoservice.rest;

import com.arcao.wherigoservice.datamapper.JsonResponseDataMapper;
import com.arcao.wherigoservice.datamapper.ResponseDataMapper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Logger;

public class RestServlet extends HttpServlet {
	private static final Logger log = Logger.getLogger(RestServlet.class.getName());
	private static final long serialVersionUID = 7679840895295353407L;

	
	@SuppressWarnings("unchecked")
	protected static <T> T getParameter(HttpServletRequest req, Class<T> clazz, String name, T defaultValue) {
		String value = req.getParameter(name);
		if (value == null)
			return defaultValue;
		
		// some most used types 
		if (clazz.equals(String.class)) {
			return (T) value;
		} else if (clazz.equals(Integer.class)) {
			return (T) new Integer(value);
		} else if (clazz.equals(Double.class)) {
			return (T) new Double(value);
		} else if (clazz.equals(Boolean.class)) {
			return (T) Boolean.valueOf(value);
		}
		
		try {
			Constructor<T> c = clazz.getConstructor(String.class);
			return c.newInstance(value);
		} catch (Exception e) {
			log.severe("Wrong data type for parameter " + name + "! Expected: " + clazz.getName() + " Value: " + value);
			return defaultValue;
		}
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		try {
			req.setCharacterEncoding("UTF-8");
			String path = req.getPathInfo();
			if (path == null || path.length() < 2) {
				resp.sendError(403);
				return;
			}
			String[] pathParts = path.split("/");

			String methodName = pathParts[1];

			Method m = getClass().getMethod(methodName, HttpServletRequest.class, ResponseDataMapper.class);
			
			ResponseDataMapper mapper = new JsonResponseDataMapper(req, resp);
			
			// detect DEBUG mode
			if (req.getParameter("debug") != null) {
				resp.setContentType("text/plain;charset=UTF-8");
			} else {
				resp.setContentType("application/json;charset=UTF-8");
			}

			resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
			resp.setHeader("Pragma", "no-cache"); // HTTP 1.0.
			resp.setHeader("Expires", "0"); // Proxies.

			resp.setCharacterEncoding("UTF-8");
			
			m.invoke(this, req, mapper);
			
			mapper.flush();
		} catch (SecurityException | IllegalAccessException e) {
			resp.sendError(403, e.getMessage());
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			resp.sendError(404, e.getMessage());
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			resp.sendError(400, e.getMessage());
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			resp.sendError(503, e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			resp.sendError(500, e.getMessage());
			e.printStackTrace();
		}
	}
}
