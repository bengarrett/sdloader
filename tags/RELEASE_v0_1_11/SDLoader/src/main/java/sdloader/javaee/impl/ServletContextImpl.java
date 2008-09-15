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
package sdloader.javaee.impl;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import sdloader.javaee.ServletMapping;
import sdloader.javaee.WebApplication;
import sdloader.log.SDLoaderLog;
import sdloader.log.SDLoaderLogFactory;
import sdloader.util.CollectionsUtil;
import sdloader.util.IteratorEnumeration;
import sdloader.util.ResourceUtil;

/**
 * ServletContext実装クラス
 * 
 * @author c9katayama
 */
public class ServletContextImpl implements ServletContext {
	private static final SDLoaderLog log = SDLoaderLogFactory
			.getLog(ServletConfigImpl.class);

	private WebApplication webApp;

	private String servletContextName;// コンテキスト名 /で始まるコンテキストディレクトリ名

	private URL docBase;// ドキュメントルート

	private Map<String, Servlet> servletMap;

	private Map<String, Object> attributeMap = CollectionsUtil.newHashMap();

	private Map<String, String> initParamMap = CollectionsUtil.newHashMap();

	public ServletContextImpl(WebApplication webapp) {
		this.webApp = webapp;
	}

	public ServletContext getContext(String contextPath) {
		WebApplication webapp = this.webApp.getWebApplicationManager()
				.findWebApp(contextPath);
		if (webapp != null)
			return webapp.getServletContext();

		return null;
	}

	public Set<String> getResourcePaths(String path) {
		if (path == null)
			return null;
		if (path.endsWith("/"))
			path = path.substring(0, path.length() - 1);

		String absPath = docBase + path;
		File targetResource = new File(absPath);
		if (targetResource.exists()) {
			Set<String> pathSet = CollectionsUtil.newHashSet();
			if (targetResource.isDirectory()) {
				File[] resources = targetResource.listFiles();
				if (resources != null) {
					for (int i = 0; i < resources.length; i++) {
						File resource = resources[i];
						String name = path + "/" + resource.getName();
						if (resource.isDirectory())
							name += "/";
						pathSet.add(name);
					}
				}
			} else {
				String name = path + "/" + targetResource.getName();
				pathSet.add(name);
			}
			if (!pathSet.isEmpty())
				return pathSet;
		}
		return null;
	}

	public URL getResource(String resource) throws MalformedURLException {
		URL url = null;
		if(ResourceUtil.isAbsoluteURL(resource)){
			url = new URL(resource);
		}else{
			url = ResourceUtil.createURL(docBase,resource);
		}
		return ResourceUtil.isResourceExist(url) ? url : null;
	}

	public InputStream getResourceAsStream(String resource) {
		try{
			URL url = getResource(resource);
			if(url != null){
				return url.openStream();
			}else{
				return null;
			}
		}catch(Exception e){
			return null;
		}
	}

	public Servlet getServlet(String name) throws ServletException {
		if (servletMap == null) {
			return null;
		}
		return (Servlet) servletMap.get(name);
	}

	public Enumeration<Servlet> getServlets() {
		if (servletMap == null)
			return new IteratorEnumeration<Servlet>();
		return new IteratorEnumeration<Servlet>(servletMap.values().iterator());
	}

	public Enumeration<String> getServletNames() {
		if (servletMap == null) {
			return new IteratorEnumeration<String>();
		}
		return new IteratorEnumeration<String>(servletMap.keySet().iterator());
	}

	public String getRealPath(String resource) {
		URL url = ResourceUtil.createURL(docBase,resource);
		if(url.getProtocol().startsWith("file")){
			return url.getFile();
		}else{
			return url.toExternalForm();
		}
	}

	public String getInitParameter(String key) {
		return (String) initParamMap.get(key);
	}

	public Enumeration<String> getInitParameterNames() {
		return new IteratorEnumeration<String>(initParamMap.keySet().iterator());
	}

	public Object getAttribute(String key) {
		return this.attributeMap.get(key);
	}

	public Enumeration<String> getAttributeNames() {
		return new IteratorEnumeration<String>(attributeMap.keySet().iterator());
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

		WebApplication webapp = webApp.getWebApplicationManager().findWebApp(
				this.servletContextName);
		ServletMapping mapping = webapp.findServletMapping(requestPath);
		if (mapping == null)
			return null;
		Servlet servlet = webapp.findServlet(mapping.getServletName());
		if (servlet == null)
			return null;
		String contextPath = webapp.getContextPath();
		
		if(!contextPath.endsWith("/") && !requestPath.startsWith("/")){
			requestPath = "/"+requestPath;
		}
		
		String requestURI = webapp.getContextPath() + requestPath;
		return new RequestDispatcherImpl(mapping, servlet, webapp
				.getServletContext(), requestURI);
	}

	public RequestDispatcher getNamedDispatcher(String arg0) {
		return null;
	}

	public String getServerInfo() {
		return null;
	}

	// /non interface method
	public void setServletMap(Map<String, Servlet> servletMap) {
		this.servletMap = servletMap;
	}

	public void addInitParameter(String paramName, String paramValue) {
		this.initParamMap.put(paramName, paramValue);
	}

	public void setServletContextName(String servletContextName) {
		this.servletContextName = servletContextName;
	}

	public void setDocBase(URL absoluteContextPath) {
		this.docBase = absoluteContextPath;
	}

	public WebApplication getWebApplication() {
		return webApp;
	}
}