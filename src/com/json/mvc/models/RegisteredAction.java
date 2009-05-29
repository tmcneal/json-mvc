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

package com.json.mvc.models;

import java.util.regex.Pattern;

import com.json.mvc.util.ActionConfiguration;

public class RegisteredAction {
	private String path;
	private String httpMethod;
	private String className;
	private Pattern pattern;
	
	private RegisteredAction() {}
	
	public RegisteredAction(String path, String httpMethod, String className) {
		this.path = path;
		this.httpMethod = httpMethod;
		this.className = className;
		this.pattern = ActionConfiguration.createPattern(path);
	}
	
	public boolean matchesPath(String requestedPath) {
		return pattern.matcher(requestedPath).matches();
	}
	
	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}
	/**
	 * @param actionName the actionName to set
	 */
	public void setPath(String path) {
		this.path = path;
	}
	/**
	 * @return the httpMethod
	 */
	public String getHttpMethod() {
		return httpMethod;
	}
	/**
	 * @param httpMethod the httpMethod to set
	 */
	public void setHttpMethod(String httpMethod) {
		this.httpMethod = httpMethod;
	}
	/**
	 * @return the className
	 */
	public String getClassName() {
		return className;
	}
	/**
	 * @param className the className to set
	 */
	public void setClassName(String className) {
		this.className = className;
	}
}
