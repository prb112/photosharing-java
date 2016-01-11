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

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

import photosharing.api.Configuration;
import photosharing.api.ExecutorUtil;

/**
 * <a href="http://ibm.co/1WOTZni">OAuth 2.0 APIs for web server flow</a>
 * 
 * @author Paul Bastide <pbastide@us.ibm.com>
 */
public class OAuth20Handler {

	// Logger
	private final static String className = OAuth20Handler.class.getName();
	private Logger logger = Logger.getLogger(className);

	/**
	 * Component Path for OAuth2.0 API on IBM Connections Cloud
	 */
	public final static String TOKENURL = "/manage/oauth2/token";
	public final static String AUTHURL = "/manage/oauth2/authorize";
	
	//Variable used in the Session Scope
	public static final String CREDENTIALS = "credentials";

	/**
	 * Enumeration of the various grant types
	 */
	private enum GrantType {
		Bearer, authorization_code
	};

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
	 * @param request
	 *            current http request
	 * @return url to redirected to for authorization
	 */
	public String generateRedirect(HttpServletRequest request) {
		Configuration config = Configuration.getInstance(request);

		// Builds the URL in a StringBuilder
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
	 * gets an access token based on the code
	 * 
	 * @param code
	 *            - the >254 character code representing temporary credentials
	 * @return the OAuth 20 configuration for the user requesting
	 * @throws IOException
	 */
	public OAuth20Data getAccessToken(String code) throws IOException {
		logger.info("getAccessToken activated");
		OAuth20Data oData = null;

		Configuration config = Configuration.getInstance(null);
		String body = this.generateAccessTokenRequestBody(
				config.getValue(Configuration.CLIENTID),
				config.getValue(Configuration.CLIENTSECRET),
				config.getValue(Configuration.CALLBACKURL), code);

		// Builds the URL in a StringBuilder
		StringBuilder builder = new StringBuilder();
		builder.append(config.getValue(Configuration.BASEURL));
		builder.append(TOKENURL);

		Request post = Request.Post(builder.toString());
		post.addHeader("Content-Type",
				ContentType.APPLICATION_FORM_URLENCODED.getMimeType());
		post.body(new StringEntity(body));

		/**
		 * Block is executed if there is a trace
		 */
		logger.info("URL Encoded body is " + body);
		logger.info("Token URL is " + builder.toString());

		/**
		 * Executes with a wrapped executor
		 */
		Executor exec = ExecutorUtil.getExecutor();
		Response apiResponse = exec.execute(post);
		HttpResponse hr = apiResponse.returnResponse();

		/**
		 * Check the status codes and if 200, convert to String and process the
		 * response body
		 */
		int statusCode = hr.getStatusLine().getStatusCode();

		if (statusCode == 200) {
			InputStream in = hr.getEntity().getContent();
			String x = IOUtils.toString(in);
			oData = OAuth20Data.createInstance(x);
		} else {
			logger.warning("OAuth20Data status code " + statusCode);
		}

		return oData;
	}

	/**
	 * generates request body for the access token
	 * 
	 * @param clientId
	 *            the client id of the third party application
	 * @param clientSecret
	 *            the confidential client secret for the third party application
	 * @param callbackURI
	 *            the callbackUri that is used by the third party application
	 * @param code
	 *            the >254 character code that is short lived
	 * @return {String} assembled request body
	 */
	public String generateAccessTokenRequestBody(String clientId,
			String clientSecret, String callbackURI, String code) {
		StringBuilder builder = new StringBuilder();
		builder.append("client_id=");
		builder.append(clientId);
		builder.append("&");

		builder.append("client_secret=");
		builder.append(clientSecret);
		builder.append("&");

		builder.append("callback_uri=");
		try {
			builder.append(URLEncoder.encode(callbackURI, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			logger.log(Level.WARNING, "Encoding issue " + callbackURI);
		}
		builder.append("&");

		builder.append("code=");
		builder.append(code);
		builder.append("&");

		builder.append("grant_type=");
		builder.append(GrantType.authorization_code.name());
		return builder.toString();
	}

	/**
	 * renews an access token with the user's data
	 * 
	 * @param oData
	 *            the current OAuth 2.0 data.
	 * @return {OAuth20Data} or null
	 * @throws IOException
	 */
	public OAuth20Data renewAccessToken(OAuth20Data oData) throws IOException {
		logger.finest("renewAccessToken activated");

		Configuration config = Configuration.getInstance(null);
		String body = this.generateRenewAccessTokenRequestBody(
				oData.getAccessToken(), oData.getRefreshToken(),
				oData.getIssuedOn(), oData.getExpiresIn());

		// Builds the URL in a StringBuilder
		StringBuilder builder = new StringBuilder();
		builder.append(config.getValue(Configuration.BASEURL));
		builder.append(TOKENURL);

		Request post = Request.Post(builder.toString());
		post.addHeader("Content-Type",
				ContentType.APPLICATION_FORM_URLENCODED.getMimeType());
		post.body(new StringEntity(body));

		/**
		 * Block is executed if there is a trace
		 */
		logger.info("URL Encoded body is " + body);
		logger.info("Token URL is " + builder.toString());

		/**
		 * Executes with a wrapped executor
		 */
		Executor exec = ExecutorUtil.getExecutor();
		Response apiResponse = exec.execute(post);
		HttpResponse hr = apiResponse.returnResponse();

		/**
		 * Check the status codes and if 200, convert to String and process the
		 * response body
		 */
		int statusCode = hr.getStatusLine().getStatusCode();

		if (statusCode == 200) {
			InputStream in = hr.getEntity().getContent();
			String x = IOUtils.toString(in);
			oData = OAuth20Data.createInstance(x);
		} else {
			logger.warning("OAuth20Data status code " + statusCode);
		}

		return oData;
	}

	/**
	 * generates request body for the renew access token operation
	 * 
	 * @param accessToken
	 *            The short-lived access token. The default life span of the
	 *            token is two hours. The maximum number of characters is 256.
	 * @param refreshToken
	 *            A long-lived refresh token that can be used to obtain a new
	 *            access token when the access token expires. The maximum number
	 *            of characters is 256.
	 * @param issuedOn
	 *            The details of when the access token was created.
	 * @param expiresIn
	 *            The amount of time in milliseconds that the access token is
	 *            valid.
	 * @return {String} assembled request body
	 */
	public String generateRenewAccessTokenRequestBody(String accessToken,
			String refreshToken, String issuedOn, String expiresIn) {
		StringBuilder builder = new StringBuilder();
		builder.append("access_token=");
		builder.append(accessToken);
		builder.append("&");

		builder.append("refresh_token=");
		builder.append(refreshToken);
		builder.append("&");

		builder.append("isseud_on=");
		builder.append(issuedOn);
		builder.append("&");

		builder.append("expires_in=");
		builder.append(expiresIn);
		builder.append("&");

		builder.append("grant_type=");
		builder.append(GrantType.Bearer.name());
		return builder.toString();
	}
}
