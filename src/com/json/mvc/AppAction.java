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

package com.json.mvc;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.json.mvc.exceptions.AppException;

/**
 * <p>
 * Abstract class which should be sub-classed to perform specific functions.  An AppAction is
 * invoked from the AppController and is passed all the information it needs to handle a request.
 * </p>
 * <p>
 * Helper methods for an action are also defined in this class.
 * </p>
 * 
 * @author toddmcneal
 */
public abstract class AppAction {
	
	private HttpServletRequest httpServletRequest;
	private HttpServletResponse httpServletResponse;
	
	/**
	 * Main execution method of an action.  Sub-classes will define logic in this method which operates
	 * against the parameters and arguments of the URL request.
	 * 
	 * @param arguments the values in the URL after the action-name that are delimited by forward-slashes
	 * @param parameters the key-value pairs in the URL which appear after the action name and arguments
	 * @return an object which gets serialized into JSON and is displayed in the HTTP response
	 * @throws AppException
	 */
	public abstract Object execute(List<String> arguments, Map<String, String[]> parameters) throws AppException;
	
	public void preExecute() {}
	
	public void postExecute() {}
	
	public void onError() {}
	
	public void validate(List<String> arguments, Map<String, String[]> parameters) throws AppException {
		
	}
	
	/**
	 * Optional method which can be defined by an Action in the event where certain properties in the returned
	 * 'response object' need to be excluded from the JSON response that is returned in the HTTP response.
	 * <p>
	 * See FlexJSON documentation on the dot-notation used to define exclusions.
	 * </p>
	 * 
	 * @return the list of model properties that should be excluded from the JSON response.
	 */
	public String[] exclusions() {
		return null;
	}

	/**
	 * @return the httpServletRequest
	 */
	public HttpServletRequest getHttpServletRequest() {
		return httpServletRequest;
	}

	/**
	 * @param httpServletRequest the httpServletRequest to set
	 */
	public void setHttpServletRequest(HttpServletRequest httpServletRequest) {
		this.httpServletRequest = httpServletRequest;
	}

	/**
	 * @return the httpServletResponse
	 */
	public HttpServletResponse getHttpServletResponse() {
		return httpServletResponse;
	}

	/**
	 * @param httpServletResponse the httpServletResponse to set
	 */
	public void setHttpServletResponse(HttpServletResponse httpServletResponse) {
		this.httpServletResponse = httpServletResponse;
	}
}
