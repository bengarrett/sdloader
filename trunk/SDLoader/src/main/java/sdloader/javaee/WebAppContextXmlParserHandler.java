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
package sdloader.javaee;

import java.io.File;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import sdloader.log.SDLoaderLog;
import sdloader.log.SDLoaderLogFactory;
import sdloader.util.PathUtils;
import sdloader.util.TextFormatUtil;
/**
 * @author c9Katayama
 *
 */
public class WebAppContextXmlParserHandler extends DefaultHandler {

	private static SDLoaderLog log = SDLoaderLogFactory
			.getLog(WebAppContextXmlParserHandler.class);

	private String fileName;
	private String webAppDirPath;

	private WebAppContext webAppContext;

	public WebAppContextXmlParserHandler(String fileName, String webAppDirPath) {
		this.fileName = fileName;
		this.webAppDirPath = webAppDirPath;
	}

	public WebAppContext getWebAppContext() {
		return webAppContext;
	}

	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if (qName.equals("Context")) {
			processContextTag(attributes);
		}
	}

	protected void processContextTag(Attributes attributes) {
		String contextPath = attributes.getValue("path");
		String docBase = attributes.getValue("docBase");
		if (docBase == null) {
			log.error("docBase attribute not found. file=" + fileName);
			return;
		}
		if (contextPath == null)
			contextPath = contextPathFromFileName();

		docBase = TextFormatUtil.formatTextBySystemProperties(docBase);
		contextPath = TextFormatUtil.formatTextBySystemProperties(contextPath);
		docBase = docBase.replace('\\', '/');
		if (docBase.startsWith(".")) {// 相対パスの場合、webappsまでのパスを追加
			docBase = webAppDirPath + "/" + docBase;
		}
		if (!new File(docBase).exists()) {
			log.error("docBase not exist. file=" + fileName + " contextPath="
					+ contextPath + " docBase=" + docBase);
			return;
		}
		log.info("detect webapp context. contextPath=" + contextPath
				+ " docBase=" + docBase);
		webAppContext = new WebAppContext(contextPath, PathUtils.file2URL(docBase));
	}

	protected String contextPathFromFileName() {
		return "/" + fileName.substring(0, fileName.length() - ".xml".length());
	}
}
