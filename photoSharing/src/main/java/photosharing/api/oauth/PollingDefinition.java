/**
 * © Copyright IBM Corp. 2015
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
package photosharing.api.oauth;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.http.HttpStatus;

import photosharing.api.base.APIDefinition;

/**
 * Polling checks the session to see if there is a valid OAuth Credential 
 * 
 * @author Paul Bastide <pbastide@us.ibm.com>
 * 
 */
public class PollingDefinition implements APIDefinition {

	// Logger
	private final static String className = PollingDefinition.class.getName();
	private Logger logger = Logger.getLogger(className);

	/**
	 * processes the polling 
	 * 
	 * @param request
	 *            the http request object
	 * @param request
	 *            the http response object
	 */
	public void run(HttpServletRequest request, HttpServletResponse response) {
		
		HttpSession session = request.getSession(false);
		if (session != null) {
			
			Object oData = session.getAttribute(OAuth20Handler.CREDENTIALS);
			
			if(oData != null){
				
				String oName = oData.getClass().getSimpleName();
				logger.warning("" + oName);
				if(oName.contains("OAuth20Data")){
					logger.log(Level.INFO, "Credentials found");
					response.setStatus(HttpStatus.SC_OK);
				}else{
					logger.log(Level.WARNING, "Error on getting credentials - bad value for odata");
					response.setStatus(HttpStatus.SC_BAD_REQUEST);
				}
				
			}else{
				logger.log(Level.WARNING, "No Credentials");
				response.setStatus(HttpStatus.SC_NO_CONTENT);
			} 

		} else {
			// When there is no session, set SC_BAD_REQUEST
			logger.log(Level.WARNING, "Invalid Session - Cookie found/exists - " + request.getHeader("Cookie"));
			response.setStatus(HttpStatus.SC_BAD_REQUEST);
		}

	}

}
