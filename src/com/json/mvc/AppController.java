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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.json.mvc.exceptions.AppActionNotFoundException;
import com.json.mvc.exceptions.AppException;
import com.json.mvc.models.ActionResponse;
import com.json.mvc.models.AppError;
import com.json.mvc.models.RegisteredAction;
import com.json.mvc.util.ActionConfiguration;

import flexjson.JSONSerializer;

/**
 * Controls the main flow and full life-cycle of a request.
 * 
 * @author toddmcneal
 */
public class AppController {
	private HttpServletRequest httpRequest;
	private HttpServletResponse httpResponse;
	private String httpMethod;
	private JSONSerializer serializer;
	
	public AppController() { };
	
	public AppController(HttpServletRequest httpRequest, HttpServletResponse httpResponse, String httpMethod) {
		this.httpRequest = httpRequest;
		this.httpResponse = httpResponse;
		this.httpMethod = httpMethod;
		
		// don't let the JSON response include the 'class' property of the returned object
		serializer = new JSONSerializer().exclude("*.class");
	}
	
	/**
	 * Executes an action from within a new Hibernate transaction.  If the action is successful, a JSON response
	 * will be returned, otherwise a JSON object containing an error message will be generated.
	 * 
	 * @return the String representation of a JSON response
	 */
	public String execute() {
		AppAction action = null;
		
		try {
			
			String actionPath = retrieveActionFromRequest(httpRequest.getRequestURI(), httpRequest.getContextPath());
			
			Class<AppAction> actionClass = lookupAction(actionPath);
			
			List<String> arguments = extractArguments(httpRequest.getRequestURI(), httpRequest.getContextPath());
			
			Map<String, String[]> parameters = retrieveParametersFromRequest();
			
			action = retrieveAction(actionClass);
			
			action.preExecute();
			
			ActionResponse actionResponse = executeAction(action, arguments, parameters);
			
			String jsonResponse = generateJSONResponse(actionResponse);
			
			action.postExecute();
	      
	        return jsonResponse;
		} catch(AppException ex) {		
			ex.printStackTrace();
			
			httpResponse.setStatus(500);
			
			if(action != null)
				action.onError();
			
			String errorResponse = generateJSONErrorResponse(ex);		
			return errorResponse;
		} catch(Exception ex) {
			ex.printStackTrace();
			
			httpResponse.setStatus(500);
			
			if(action != null)
				action.onError();
			
			String errorResponse = generateJSONErrorResponse(ex);	
			return errorResponse;
		}
	}
	
	/**
	 * Returns a list containing the action in this URL request, as well as any arguments in the URL.
	 * Note that arguments are different from parameters.
	 * 
	 * <pre>
	 * Example URL: http://<server-name>/<servlet-path>/view/planet/1?maxSize=500&locale=en
	 * 
	 * The example URL above will be broken down into the following elements:
	 * 
	 * Action: view
	 * Argument list: planet, 1
	 * Parameter list: maxSize=500, locale=en
	 * </pre>
	 */
	protected String retrieveActionFromRequest(String uri, String contextPath) throws AppException {
		if(uri.startsWith(contextPath)) {
			// strip off the servlet-path location from the URL
			String shortenedPath = uri.substring(contextPath.length());
			
			// URL requests which match the context-path will use the 'default' action
			if(shortenedPath.length() <= 1) {
				return "/";
			}
			
			return shortenedPath;
		}
		
		throw new AppException("URL is invalid. URL: '" + uri + "'");
	}
	
	/**
	 * Returns the 'arguments' of the URL.
	 * <p>
	 * i.e. for '/test/view/1' a list containing three elements (test, view, 1) would be returned.
	 * </p>
	 * 
	 * @param actionsAndArguments the list containing the relative URL of the request.
	 * @return the list of arguments
	 */
	protected List<String> extractArguments(String uri, String contextPath) throws AppException {
		if(uri.startsWith(contextPath)) {
			// strip off the servlet-path location from the URL
			String shortenedPath = uri.substring(contextPath.length());
			
			// URL requests which match the context-path will use the 'default' action
			if(shortenedPath.length() <= 1) {
				List<String> result = new ArrayList<String>();
				result.add("default");
				return result;
			}
			
			//remove initial forward-slash
			shortenedPath = shortenedPath.substring(1);
			String[] result = shortenedPath.split("/");
			return Arrays.asList(result);
		}
		
		throw new AppException("URL is invalid. URL: '" + uri + "'");
	}
	
	protected Map<String, String[]> retrieveParametersFromRequest() {	
		return httpRequest.getParameterMap();
	}
	
	/**
	 * Checks the current action against a list of "registered" actions that are stored in the configuration 
	 * file.  If the current action is registered, then the Class object for that action is returned.  
	 * 
	 * <pre>
	 * Example action: /view/planet/%1 (no trailing slashes allowed)
	 * </pre>
	 * 
	 * @param action
	 * @return
	 */
	protected Class<AppAction> lookupAction(String path) throws AppException {
		String actionClassName = null;
		
		// retrieve list of actions from the json-mvc.xml configuration file
		List<RegisteredAction> actions = ActionConfiguration.getActions(httpRequest);

		// match the HTTP METHOD and URL of current action with the HTTP METHOD and URL pattern of each registered action
		for(int i=0; i < actions.size(); i++) {
			RegisteredAction action = actions.get(i);
			
			if(httpMethod.equals(action.getHttpMethod()) && action.matchesPath(path)) {
				actionClassName = action.getClassName();
				continue;
			}
		}
		
		if(actionClassName == null)
			throw new AppActionNotFoundException("No action found for the path '" + path + "' and method '" + httpMethod + "'");
		
		try {
			Class<AppAction> actionClass = (Class<AppAction>)Class.forName(actionClassName);	
			return actionClass;
		} catch (ClassNotFoundException ex) {
			//TODO: Log error
			throw new AppActionNotFoundException("No class found for classname: '" + actionClassName + "'");
		}
	}
	
	/**
	 * Attempts to convert the passed Class object into an instance of that object.
	 * 
	 * @param actionClass the AppAction to execute
	 * @return an instance of this class
	 * @throws AppException when an instance of this class could not be created
	 */
	protected AppAction retrieveAction(Class<AppAction> actionClass) throws AppException {
		//TODO: Check if user has authenticated
		AppAction action = null;
		
		try {
			action = (AppAction)actionClass.newInstance();
			action.setHttpServletRequest(httpRequest);
			action.setHttpServletResponse(httpResponse);
		} catch (IllegalAccessException ex) {
			throw new AppException(ex.getMessage());
		} catch (InstantiationException ex) {
			throw new AppException(ex.getMessage());
		} catch (ClassCastException ex) {
			throw new AppException("Action class is not a subclass of com.test.action.AppAction. Class: '" + actionClass.getCanonicalName() + "'");
		}
		
		return action;
	}
	
	/**
	 * Executes the life-cycle of an AppAction.
	 * 
	 * @param action the action to be executed
	 * @param parameters the parameters retrieved from the HTTP request
	 * @return a JSONSerializble Object constructed by the Action
	 * @throws AppException when the AppActon class can not be instantiated, or an error occurs while executing the action.
	 */
	protected ActionResponse executeAction(AppAction action, List<String> arguments, Map<String, String[]> parameters) throws AppException {
		
		try {
			action.validate(arguments, parameters);
			
			Object responseObject = action.execute(arguments, parameters);
			
			ActionResponse actionResponse = new ActionResponse(responseObject, action.exclusions());
			
			return actionResponse;
		} catch (AppException ex) {
			ex.printStackTrace();
			throw ex;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new AppException(ex.getMessage());
		}
	}
	
	/**
	 * Serializes a Java object into a JSON object.
	 * 
	 * @param responseObject
	 * @return the String representation of a JSON object
	 */
	protected String generateJSONResponse(ActionResponse actionResponse) {
		if(actionResponse.getExclusions() != null && actionResponse.getExclusions().length > 0)
			serializer.exclude(actionResponse.getExclusions());
		
		return serializer.deepSerialize(actionResponse.getResponseObject());
	}
	
	/**
	 * Constructs a JSON error response which contains an error code and user-friendly message.
	 * 
	 * @param ex
	 * @return
	 */
	protected String generateJSONErrorResponse(Exception ex) {
		AppError errorBean = new AppError();
		errorBean.setErrorMessage(ex.getMessage());
		return generateJSONResponse(new ActionResponse(errorBean));
	}

	/**
	 * @return the httpRequest
	 */
	public HttpServletRequest getHttpRequest() {
		return httpRequest;
	}

	/**
	 * @param httpRequest the httpRequest to set
	 */
	public void setHttpRequest(HttpServletRequest httpRequest) {
		this.httpRequest = httpRequest;
	}

	/**
	 * @return the httpResponse
	 */
	public HttpServletResponse getHttpResponse() {
		return httpResponse;
	}

	/**
	 * @param httpResponse the httpResponse to set
	 */
	public void setHttpResponse(HttpServletResponse httpResponse) {
		this.httpResponse = httpResponse;
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
}
