package com.arcao.wherigoservice.rest;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.arcao.wherigoservice.datamapper.JsonResponseDataMapper;
import com.arcao.wherigoservice.datamapper.ResponseDataMapper;

public class RestServlet extends HttpServlet {
	private static final Logger log = Logger.getLogger(RestServlet.class.getName());
	private static final long serialVersionUID = 7679840895295353407L;

	@Override
	public void init(ServletConfig config) throws ServletException {

	}
	
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
			return (T) new Boolean(value);
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

			Method m = getClass().getMethod(methodName, new Class[] { HttpServletRequest.class, ResponseDataMapper.class });		
			
			ResponseDataMapper mapper = new JsonResponseDataMapper(req, resp);
			
			// detect DEBUG mode
			if (req.getParameter("debug") != null) {
				resp.setContentType("text/plain;charset=UTF-8");
			} else {
				resp.setContentType("text/javascript;charset=UTF-8");
			}
			
			resp.setHeader("Expires", "Fri, 30 Oct 1998 14:19:41 GMT");
			resp.setHeader("Cache-Control", "no-cache, must-revalidate");
			resp.setCharacterEncoding("UTF-8");
			
			m.invoke(this, req, mapper);
			
			mapper.flush();
		} catch (SecurityException e) {
			resp.sendError(403, e.getMessage());
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			resp.sendError(404, e.getMessage());
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			resp.sendError(400, e.getMessage());
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			resp.sendError(403, e.getMessage());
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
