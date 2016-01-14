/**
 * © Copyright IBM Corp. 2016
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package photosharing.api.bss;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;

import photosharing.api.Configuration;
import photosharing.api.ExecutorUtil;
import photosharing.api.base.APIDefinition;

/**
 * Invalidates the Application Session and Invalidates the IBM Connections Cloud Session
 * 
 * @author Paul Bastide <pbastide@us.ibm.com>
 *
 */
public class LogoutDefinition implements APIDefinition {
	
	// Logger 
	private static String className = LogoutDefinition.class.getName();
	private Logger logger = Logger.getLogger(className);
	
	/**
	 * The URL accepts a request with cookies, and invalidates those cookies (tokens)
	 */
	public final static String apiUrl  = "/manage/account/logoutSSO";
		
	/** 
	 * redirects the user to the logout SSO to destroy the login tokens and login sessions
	 * 
	 * @see photosharing.api.conx.APIDefinition#run(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public void run(HttpServletRequest request, HttpServletResponse response) {
		Configuration config = Configuration.getInstance(request);
		String api = config.getValue(Configuration.BASEURL) + apiUrl;
		try{
			//Invalidating photosharing session on the AppServer and IBM Connections Cloud
			HttpSession session = request.getSession(false);
			
			if(session != null){
				logger.info(session.getId() + " is being logged out");
				
				Request get = Request.Get(api);
				
				try {
					Executor exec = ExecutorUtil.getExecutor();
					Response apiResponse = exec.execute(get);
					HttpResponse hr = apiResponse.returnResponse();

					/**
					 * Check the status codes and if 200, convert to String
					 */
					int code = hr.getStatusLine().getStatusCode();
					if(code == HttpStatus.SC_OK){
						
					}else{
						logger.log(Level.SEVERE,"Exception Encountered with IBM Connections Cloud Session");
					}
								
				} catch (IOException e) {
					//Catches Exception Related to a Request
					logger.log(Level.SEVERE,"Exception Encountered");
					response.setHeader("X-Application-Error", className);
					response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
				} 
				
				//Indvalidates the User's current session and logs them out
				session.invalidate();
				request.logout();
								
				//Sets the Status to SC_OK (Http Status Code 200) to indicate a successful logout
				response.setStatus(HttpStatus.SC_NO_CONTENT);
			}
			else{ 
				//Something bad has happened
				logger.log(Level.SEVERE,"Invalid Request");
				response.setStatus(HttpStatus.SC_BAD_REQUEST);
			}
			
		}catch(Exception e){
			logger.log(Level.SEVERE,"Exception Encountered - " + e.toString());
			
			//Sets the Status to SC_INTERNAL_SERVER_ERROR (Http Status Code 500)
			//Indicates an issue with the Server
			response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			
		}
	}

}
