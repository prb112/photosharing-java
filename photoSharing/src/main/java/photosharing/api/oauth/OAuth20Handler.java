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

import photosharing.api.Configuration;

/**
 * <a href="http://ibm.co/1WOTZni">OAuth 2.0 APIs for web server flow</a>
 * 
 * @author Paul Bastide <pbastide@us.ibm.com>
 */
public class OAuth20Handler {

	// Logger
	private final static String className = CallbackDefinition.class.getName();
	private Logger logger = Logger.getLogger(className);

	/**
	 * Component Path for OAuth2.0 API on IBM Connections Cloud
	 */
	public final static String TOKENURL = "/manage/oauth2/token";
	public final static String AUTHURL = "/manage/oauth2/authorize";

	/**
	 * Only one instance of this class is needed.
	 */
	private static OAuth20Handler _handler;

	/**
	 * private constructor
	 */
	private OAuth20Handler() {

	}

	/**
	 * gets the single instance of the OAuth20 Handler
	 * 
	 * @return {OAuth20Handler} single instance of handler
	 */
	public static OAuth20Handler getInstance() {
		if (_handler == null) {
			_handler = new OAuth20Handler();
		}
		return _handler;
	}

	/**
	 * builds the authorization url for OAuth 2.0 redirect
	 * 
	 * For instance, the URL could be:
	 * https://apps.na.collabserv.com/manage/oauth2
	 * /authorize?response_type=code&
	 * client_id=app_example&callback_uri=http://localhost/callback
	 * 
	 * @param request current http request
	 * @return url to redirected to for authorization
	 */
	public String generateRedirect(HttpServletRequest request) {
		Configuration config = Configuration.getInstance(request);
		
		//Builds the URL in a StringBuilder
		StringBuilder builder = new StringBuilder();
		builder.append(config.getValue(Configuration.BASEURL));
		builder.append(AUTHURL);
		builder.append("?");
		builder.append("response_type=code");
		builder.append("&");
		builder.append("client_id=");
		builder.append(config.getValue(Configuration.CLIENTID));
		builder.append("&");
		builder.append("callback_uri=");
		builder.append(config.getValue(Configuration.CALLBACKURL));
		
		return builder.toString();
	}

	/**
	 * 
	 * @param code - the 256 character code representing temporary credentials
	 * @return the OAuth 20 configuration for the user requesting
	 */
	public OAuth20Data getAccessToken(String code) {
		logger.finest("getAccessToken activated");
		return new OAuth20Data();
	}

	/**
	 * 
	 * @return
	 */
	public OAuth20Data renewAccessToken() {
		logger.finest("renewAccessToken activated");
		return new OAuth20Data();
	}
}
