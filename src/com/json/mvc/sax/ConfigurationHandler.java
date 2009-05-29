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

package com.json.mvc.sax;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.json.mvc.models.RegisteredAction;

public class ConfigurationHandler extends DefaultHandler {
	private List<RegisteredAction> actions = new ArrayList<RegisteredAction>();
	
	public ConfigurationHandler() {
		if(actions.size() > 0)
			actions.clear();
	}
	
	public void startElement(String namespaceUri,
            String localName,
            String qualifiedName,
            Attributes attributes) throws SAXException {
		
		if(qualifiedName.equals("action")) {
			String path = attributes.getValue("path");
			
			if(isEmpty(path))
				throw new SAXException("Action entry is missing required value: 'path'");
			
			String method = attributes.getValue("method");
			
			if(isEmpty(method))
				throw new SAXException("Action entry is missing required value: 'method'");
			
			String className = attributes.getValue("className");
			
			if(isEmpty(className))
				throw new SAXException("Action entry is missing required value: 'className'");
			
			RegisteredAction action = new RegisteredAction(path, method, className);
			
			actions.add(action);
		}
	}

	/**
	 * @return the actions
	 */
	public List<RegisteredAction> getActions() {
		return actions;
	}
	
	public boolean isEmpty(String str) {
		return str == null || str.length() <= 0;
	}
}
