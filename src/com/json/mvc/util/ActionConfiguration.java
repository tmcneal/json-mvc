/*
 * Copyright 2009 Todd McNeal
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

package com.json.mvc.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import com.json.mvc.exceptions.AppException;
import com.json.mvc.exceptions.ConfigurationException;
import com.json.mvc.models.RegisteredAction;
import com.json.mvc.sax.ConfigurationHandler;

public class ActionConfiguration {
	private static final String filename = "/WEB-INF/json-mvc.xml";
	private static List<RegisteredAction> actions = null;

	public static List<RegisteredAction> getActions(HttpServletRequest request) throws AppException {
		if(actions == null) 
			actions = retrieveActions(request.getSession().getServletContext().getResourceAsStream(filename));
		
		return actions;
	}
	
	public static List<RegisteredAction> retrieveActions(InputStream configuration) throws ConfigurationException {
		
		ConfigurationHandler handler = new ConfigurationHandler();
	    SAXParserFactory factory = SAXParserFactory.newInstance();
	    
	    try {
	    	SAXParser parser = factory.newSAXParser();
	    	parser.parse(configuration, handler);
	    } catch (SAXException ex) {
	    	throw new ConfigurationException("Error occurred when parsing configuration file: " + ex.getMessage());
	    } catch (IOException ex) {
	    	throw new ConfigurationException(ex.getMessage());
	    } catch (ParserConfigurationException ex) {
	    	throw new ConfigurationException(ex.getMessage());
	    }
		
	    return handler.getActions();
	}
	
	public static Pattern createPattern(String path) {
		StringBuffer pattern = new StringBuffer();
		
		// make this pattern Java compatible, and also add support for an optional trailing slash
		pattern.append(path.replaceAll("%[\\d]+", "[\\\\d]+")).append("[/]*");
		
		return Pattern.compile(pattern.toString());
	}
}
