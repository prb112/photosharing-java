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
package photosharing.api.conx;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.http.HttpStatus;

import photosharing.api.Configuration;
import photosharing.api.base.APIDefinition;
import photosharing.api.oauth.OAuth20Data;
import photosharing.api.oauth.OAuth20Handler;

/**
 * The class calls the API for managing Files in IBM Connections<a
 * href="http://ibm.co/1RlASQK">Working with Files API</a>
 * 
 * @author Paul Bastide <pbastide@us.ibm.com>
 * 
 */
public class FileDefinition implements APIDefinition {

	// Logger
	private final static String className = FileDefinition.class.getName();
	private Logger logger = Logger.getLogger(className);

	/**
	 * generate the base api url for files
	 * 
	 * you can use basic or oauth in the path of the api url
	 * 
	 * @param userid
	 * @return url to the api
	 */
	private String getApiUrl() {
		Configuration config = Configuration.getInstance(null);
		StringBuilder builder = new StringBuilder();
		builder.append(config.getValue(Configuration.BASEURL));
		builder.append("/files/basic/api");
		return builder.toString();
	}

	/**
	 * manages files api definition
	 * 
	 * @see photosharing.api.base.APIDefinition#run(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void run(HttpServletRequest request, HttpServletResponse response) {
		// HTTP Method that the request was made with:
		String method = request.getMethod();

		/**
		 * get the users bearer token
		 */
		HttpSession session = request.getSession(false);
		OAuth20Data data = (OAuth20Data) session.getAttribute(OAuth20Handler.CREDENTIALS);
		String bearer = data.getAccessToken();

		// Create a Comment
		if (method.compareTo("GET") == 0) {
			
			
		} else {
			response.setStatus(HttpStatus.SC_PRECONDITION_FAILED);
		}

	}

}
