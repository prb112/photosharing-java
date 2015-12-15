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
package photosharing.api.bss;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import photosharing.api.base.APIDefinition;

/**
 * Manages the login to the Application OAuth Application
 * 
 * @author Paul Bastide <pbastide@us.ibm.com>
 */
public class LoginDefinition implements APIDefinition {

	// Logger 
	private static String className = LogoutDefinition.class.getName();
	private Logger logger = Logger.getLogger(className);
			
	/**
	 * runs the login
	 */
	@Override
	public void run(HttpServletRequest request, HttpServletResponse response) {
		//Creates a Session for the Given User
		//The session is going to stash the OAuth20Data
		//The OAuth20Data is best stashed in a database or map
		HttpSession session = request.getSession(true);
		
		
		
		
		
	}

}
