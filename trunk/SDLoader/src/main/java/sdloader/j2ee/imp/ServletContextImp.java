/*
 * Copyright 2005-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sdloader.j2ee.imp;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import sdloader.j2ee.ServletMapping;
import sdloader.j2ee.WebApplication;
import sdloader.log.SDLoaderLog;
import sdloader.log.SDLoaderLogFactory;
import sdloader.util.IteratorEnumeration;

/**
 * ServletContext実装クラス
 * 
 * @author c9katayama
 */
public class ServletContextImp implements ServletContext {
	private static final SDLoaderLog log = SDLoaderLogFactory
			.getLog(ServletConfigImp.class);
	
	private WebApplication webApp;
	
	private String servletContextName;// コンテキスト名 /で始まるコンテキストディレクトリ名

	private String docBase;// ドキュメントルート

	private Map servletMap;

	private Map attributeMap = new HashMap();

	private Map initParamMap = new HashMap();

	public ServletContextImp(WebApplication webapp) {
		this.webApp = webapp;
	}

	public ServletContext getContext(String contextPath) {
		WebApplication webapp = this.webApp.getManager().findWebApp(contextPath);
		if (webapp != null)
			return webapp.getServletContext();

		return null;
	}

	public Set getResourcePaths(String path) {
		if (path == null)
			return null;
		if (path.endsWith("/"))
			path = path.substring(0, path.length() - 1);

		String absPath = docBase + path;
		File targetResource = new File(absPath);
		if (targetResource.exists()) {
			Set set = new HashSet();
			if (targetResource.isDirectory()) {
				File[] resources = targetResource.listFiles();
				if (resources != null) {
					for (int i = 0; i < resources.length; i++) {
						File resource = resources[i];
						String name = path + "/" + resource.getName();
						if (resource.isDirectory())
							name += "/";
						set.add(name);
					}
				}
			} else {
				String name = path + "/" + targetResource.getName();
				set.add(name);
			}
			if (!set.isEmpty())
				return set;
		}
		return null;
	}

	public URL getResource(String resource) throws MalformedURLException {
		log.debug(resource);
		String resourcePath = createResourcePath(resource);
		File file = new File(resourcePath);
		if(file.exists())
			return new URL("file", null, resourcePath);
		else
			return null;
	}

	public InputStream getResourceAsStream(String resource) {
		try {
			return new FileInputStream(createResourcePath(resource));
		} catch (Exception e) {
			return null;
		}
	}

	public Servlet getServlet(String name) throws ServletException {
		if (servletMap == null)
			return null;
		return (Servlet) servletMap.get(name);
	}

	public Enumeration getServlets() {
		if (servletMap == null)
			return new IteratorEnumeration();
		return new IteratorEnumeration(servletMap.values().iterator());
	}

	public Enumeration getServletNames() {
		if (servletMap == null)
			return new IteratorEnumeration();
		return new IteratorEnumeration(servletMap.keySet().iterator());
	}

	public String getRealPath(String resource) {
		return createResourcePath(resource);
	}

	public String getInitParameter(String key) {
		return (String) initParamMap.get(key);
	}

	public Enumeration getInitParameterNames() {
		return new IteratorEnumeration(initParamMap.keySet().iterator());
	}

	public Object getAttribute(String key) {
		return this.attributeMap.get(key);
	}

	public Enumeration getAttributeNames() {
		return new IteratorEnumeration(attributeMap.keySet().iterator());
	}

	public void setAttribute(String key, Object value) {
		this.attributeMap.put(key, value);
	}

	public void removeAttribute(String key) {
		this.attributeMap.remove(key);
	}

	public String getServletContextName() {
		return servletContextName;
	}

	public void log(String logValue) {
		log.info(logValue);
	}

	public void log(Exception ex, String logValue) {
		log.info(logValue, ex);
	}

	public void log(String logValue, Throwable t) {
		log.info(logValue, t);
	}

	public int getMajorVersion() {
		return 0;
	}

	public int getMinorVersion() {
		return 0;
	}

	public String getMimeType(String arg0) {
		return null;
	}

	public RequestDispatcher getRequestDispatcher(String requestPath) {

		WebApplication webapp = webApp.getManager().findWebApp(this.servletContextName);
		ServletMapping mapping = webapp.findServletMapping(requestPath);
		if(mapping==null)
			return null;
		Servlet servlet = webapp.findServlet(mapping.getServletName());
		if(servlet==null)
			return null;
		String requestURI = webapp.getContextPath()+requestPath; 
		return new RequestDispatcherImp(mapping,servlet,webapp.getServletContext(),requestURI);
	}

	public RequestDispatcher getNamedDispatcher(String arg0) {
		return null;
	}

	public String getServerInfo() {
		return null;
	}

	// /non interface method
	public void setServletMap(Map servletMap) {
		this.servletMap = servletMap;
	}

	public void addInitParameter(String paramName, String paramValue) {
		this.initParamMap.put(paramName, paramValue);
	}

	public void setServletContextName(String servletContextName) {
		this.servletContextName = servletContextName;
	}

	public void setDocBase(String absoluteContextPath) {
		this.docBase = absoluteContextPath;
	}

	private String createResourcePath(String resource) {
		if (resource.startsWith("/") || resource.startsWith("\\")) {
			String path = docBase + resource;
			return path;
		} else {
			String path = docBase + "/" + resource;
			return path;
		}
	}
}
