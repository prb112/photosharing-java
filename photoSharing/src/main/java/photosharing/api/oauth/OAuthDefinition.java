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

import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import photosharing.api.base.APIDefinition;

/**
 * Manages redirect to the OAuth URL
 * 
 * @author Paul Bastide <pbastide@us.ibm.com>
 */
public class OAuthDefinition implements APIDefinition {

	// Logger 
	private static String className = OAuthDefinition.class.getName();
	private Logger logger = Logger.getLogger(className);
			
	/**
	 * initiates the OAuth Flow
	 */
	@Override
	public void run(HttpServletRequest request, HttpServletResponse response) {
		
		try{
			OAuth20Handler handler = OAuth20Handler.getInstance();
			String redirect = handler.generateRedirect(request);
			response.sendRedirect(redirect);
		}catch(Exception e){
			logger.severe("Issue with redirect to Auth URL" + e.toString());
		}
		
	}

}
