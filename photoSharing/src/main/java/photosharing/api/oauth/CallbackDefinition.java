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

import photosharing.api.Configuration;
import photosharing.api.base.APIDefinition;

/**
 * OAuth 2.0 Flow Callback Definition
 * 
 * The callback is the URL /{webAppRoot}/api/callback The called URL is of the
 * format /{webAppRoot}/api/callback?code=<CODE>
 * 
 * Example http://localhost:9080/photoSharing/api/callback?code=11111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111
 * 
 * @author Paul Bastide <pbastide@us.ibm.com>
 * 
 */
public class CallbackDefinition implements APIDefinition {

	// Logger
	private final static String className = CallbackDefinition.class.getName();
	private Logger logger = Logger.getLogger(className);

	/**
	 * processes the callback and converts the code into permanent OAuth 2.0
	 * credentials
	 * 
	 * @param request
	 *            the http request object
	 * @param request
	 *            the http response object
	 */
	public void run(HttpServletRequest request, HttpServletResponse response) {
		// Checks the Referrer Header and enables processing based on the valid
		// header - it's not used for validation, rather invalidation, and aligns
		// with the best practices described in http://stackoverflow.com/questions/8319862/can-i-rely-on-referer-http-header
		
		String server = Configuration
				.getConfigurationValue(Configuration.SERVER);
		String referrer = request.getHeader("Referer");
		if (server != null && referrer != null
				&& referrer.compareTo(server) == 0) {
			HttpSession session = request.getSession();
			if (session != null) {
				String code = request.getParameter("code");

				// Code should not be null and the length should be greater than
				// or equal 256 characters per the flow
				if (code != null && code.length() >= 256) {

					// Accesses the OAuth 20 Data
					OAuth20Handler handler = OAuth20Handler.getInstance();
					OAuth20Data oauthData = handler.getAccessToken();
					
					//Checks the OAuth 2.0 data
					if(oauthData != null){
						// When there is credential data persist in the session and return SC_OK with no body
						session.setAttribute("credentials", oauthData);
						response.setStatus(HttpStatus.SC_OK);
					}else{
						//
						logger.log(Level.WARNING,"Error handling the oauth data");
						response.setStatus(HttpStatus.SC_BAD_REQUEST);
					}
				} else {
					// When there is no code, set SC_BAD_REQUEST
					logger.log(Level.WARNING, "No Code passed into the URL "
							+ request.getPathInfo());
					response.setStatus(HttpStatus.SC_BAD_REQUEST);
				}

			} else {
				// When there is no session, set SC_BAD_REQUEST
				logger.log(Level.WARNING, "Invalid Session");
				response.setStatus(HttpStatus.SC_BAD_REQUEST);
			}

		} else {
			// When the referrer is bad, set SC_BAD_REQUEST
			logger.log(Level.WARNING, "Invalid Referer should match expected");
			response.setStatus(HttpStatus.SC_BAD_REQUEST);
		}

	}

}
