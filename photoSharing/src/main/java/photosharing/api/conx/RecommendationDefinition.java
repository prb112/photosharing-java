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

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.ByteArrayEntity;

import photosharing.api.Configuration;
import photosharing.api.ExecutorUtil;
import photosharing.api.base.APIDefinition;
import photosharing.api.oauth.OAuth20Data;
import photosharing.api.oauth.OAuth20Handler;

/**
 * The class calls the API for a File Recommendation in IBM Connections <a
 * href="http://ibm.co/1i2beBn">File Recommendation (Unlike/Like) API</a>
 * 
 * @author Paul Bastide <pbastide@us.ibm.com>
 * 
 */
public class RecommendationDefinition implements APIDefinition {

	// Logger
	private final static String className = RecommendationDefinition.class
			.getName();
	private Logger logger = Logger.getLogger(className);

	/**
	 * generate the base api url for files
	 * 
	 * you can use basic or oauth in the path of the api url
	 * 
	 * @param userid
	 * @return apiUrl
	 */
	private String getApiUrl() {
		Configuration config = Configuration.getInstance(null);
		String apiUrl = config.getValue(Configuration.BASEURL);
		StringBuilder builder = new StringBuilder();
		builder.append(apiUrl);
		builder.append("/files/basic/api");
		return builder.toString();
	}

	/**
	 * creates the recommendation content
	 * 
	 * @return xml representing the recommendation
	 */
	private String generateRecommendationContent() {
		StringBuilder builder = new StringBuilder();
		builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		builder.append("<entry xmlns=\"http://www.w3.org/2005/Atom\">");
		builder.append("<category term=\"recommendation\" scheme=\"tag:ibm.com,2006:td/type\" label=\"recommendation\"/>");
		builder.append("</entry>");
		return builder.toString();
	}

	/**
	 * gets the nonce url <a href="http://ibm.co/1fG83gY">Get a Cryptographic
	 * Key</a>
	 * 
	 * you can use basic or oauth in the path of the api url
	 * 
	 * @return apiUrl
	 */
	private String getNonceUrl() {
		Configuration config = Configuration.getInstance(null);
		String apiUrl = config.getValue(Configuration.BASEURL);
		StringBuilder builder = new StringBuilder();
		builder.append(apiUrl);
		builder.append("/files/basic/api/nonce");
		return builder.toString();
	}

	/**
	 * get nonce as described with nonce <a href="http://ibm.co/1fG83gY">Get a
	 * Cryptographic Key</a>
	 * 
	 * @param bearer
	 */
	private String getNonce(String bearer, HttpServletResponse response) {
		String nonce = "";

		// Build the Request
		Request get = Request.Get(getNonceUrl());
		get.addHeader("Authorization", "Bearer " + bearer);

		try {
			Executor exec = ExecutorUtil.getExecutor();
			Response apiResponse = exec.execute(get);

			HttpResponse hr = apiResponse.returnResponse();

			/**
			 * Check the status codes and if SC_OK (200), convert to String
			 */
			int code = hr.getStatusLine().getStatusCode();

			// Session is no longer valid or access token is expired
			if (code == HttpStatus.SC_FORBIDDEN) {
				response.sendRedirect("./api/logout");
			}

			// User is not authorized
			else if (code == HttpStatus.SC_UNAUTHORIZED) {
				response.setStatus(HttpStatus.SC_UNAUTHORIZED);
			}

			else if (code == HttpStatus.SC_OK) {
				InputStream in = hr.getEntity().getContent();
				nonce = IOUtils.toString(in);
			}else{
				//Given a bad proxied request
				response.setStatus(HttpStatus.SC_BAD_GATEWAY);
			}

		} catch (IOException e) {
			response.setHeader("X-Application-Error", e.getClass().getName());
			response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			logger.severe("IOException " + e.toString());
		}

		return nonce;
	}

	/**
	 * unlike a file
	 * 
	 * Example URL
	 * http://localhost:9080/photoSharing/api/like?r=off&lid=f8ad2a54
	 * -4d20-4b3b-ba3f-834e0b0cf90b&uid=bec24e93-8165-431d-bf38-0c668a5e6727
	 * maps to
	 * https://apps.collabservdaily.swg.usma.ibm.com/files/basic/api/library/00c129c9-f3b6-4d22-9988-99e69d16d7a7/document/bf33a9b5-3042-46f0-a96e-b8742fced7a4/feed
	 
	 * 
	 * @param bearer
	 * @param lid
	 * @param uid
	 * @param nonce
	 * @param response
	 */
	public void unlike(String bearer, String pid, String lid, String nonce,
			HttpServletResponse response) {
		String apiUrl = getApiUrl() + "/library/" + lid + "/document/" + pid
				+ "/feed";

		try {

			String recommendation = generateRecommendationContent();
			logger.info("like -> " + apiUrl + " " + recommendation);
			
			// Generate the
			Request post = Request.Post(apiUrl);
			post.addHeader("Authorization", "Bearer " + bearer);
			post.addHeader("X-Update-Nonce", nonce);
			post.addHeader("X-METHOD-OVERRIDE", "DELETE");
			post.addHeader("Content-Type", "application/atom+xml");
			
			ByteArrayEntity entity = new ByteArrayEntity(
					recommendation.getBytes("UTF-8"));
			post.body(entity);

			Executor exec = ExecutorUtil.getExecutor();
			Response apiResponse = exec.execute(post);
			HttpResponse hr = apiResponse.returnResponse();

			/**
			 * Check the status codes
			 */
			int code = hr.getStatusLine().getStatusCode();

			// Session is no longer valid or access token is expired
			if (code == HttpStatus.SC_FORBIDDEN) {
				response.sendRedirect("./api/logout");
			}

			// User is not authorized
			else if (code == HttpStatus.SC_UNAUTHORIZED) {
				response.setStatus(HttpStatus.SC_UNAUTHORIZED);
			}

			// Default to SC_NO_CONTENT (204)
			else {
				response.setStatus(HttpStatus.SC_NO_CONTENT);

			}

		} catch (IOException e) {
			response.setHeader("X-Application-Error", e.getClass().getName());
			response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			logger.severe("IOException " + e.toString());
		}
	}

	/**
	 * like a file
	 * 
	 * Example URL
	 * http://localhost:9080/photoSharing/api/like?r=on&lid=f8ad2a54-
	 * 4d20-4b3b-ba3f-834e0b0cf90b&uid=bec24e93-8165-431d-bf38-0c668a5e6727 maps
	 * to
	 * https://apps.collabservdaily.swg.usma.ibm.com/files/basic/api/library/00c129c9-f3b6-4d22-9988-99e69d16d7a7/document/bf33a9b5-3042-46f0-a96e-b8742fced7a4/feed
	 * 
	 * 
	 * @param bearer
	 * @param pid
	 * @param lid
	 * @param nonce
	 * @param response
	 */
	public void like(String bearer, String pid, String lid, String nonce,
			HttpServletResponse response) {
		String apiUrl = getApiUrl() + "/library/" + lid + "/document/" + pid
				+ "/feed";

		try {

			String recommendation = generateRecommendationContent();
			logger.info("like -> " + apiUrl + " " + recommendation);

			// Generate the apiUrl for like
			Request post = Request.Post(apiUrl);
			post.addHeader("Authorization", "Bearer " + bearer);
			post.addHeader("X-Update-Nonce", nonce);
			post.addHeader("Content-Type", "application/atom+xml");

			ByteArrayEntity entity = new ByteArrayEntity(
					recommendation.getBytes("UTF-8"));
			post.body(entity);

			Executor exec = ExecutorUtil.getExecutor();
			Response apiResponse = exec.execute(post);
			HttpResponse hr = apiResponse.returnResponse();

			/**
			 * Check the status codes
			 */
			int code = hr.getStatusLine().getStatusCode();
			logger.info("code " + code);

			// Session is no longer valid or access token is expired
			if (code == HttpStatus.SC_FORBIDDEN) {
				response.sendRedirect("./api/logout");
			}

			// User is not authorized
			else if (code == HttpStatus.SC_UNAUTHORIZED) {
				response.setStatus(HttpStatus.SC_UNAUTHORIZED);
			}

			// Default to SC_NO_CONTENT (204)
			else {
				response.setStatus(HttpStatus.SC_NO_CONTENT);

			}

		} catch (IOException e) {
			response.setHeader("X-Application-Error", e.getClass().getName());
			response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			logger.severe("IOException " + e.toString());
			e.printStackTrace();
		}
	}

	/**
	 * manages the recommendations for a file
	 * 
	 * @see photosharing.api.base.APIDefinition#run(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void run(HttpServletRequest request, HttpServletResponse response) {

		/**
		 * get the users bearer token from the session object
		 */
		HttpSession session = request.getSession(false);
		Object o = session.getAttribute(OAuth20Handler.CREDENTIALS);
		if (o == null) {
			logger.warning("Credentials can't be found");
			response.setStatus(HttpStatus.SC_BAD_REQUEST);
		} else {

			OAuth20Data data = (OAuth20Data) o;
			String bearer = data.getAccessToken();

			// Parameters to use
			String likeParam = request.getParameter("r"); // r = recommendation
			String lid = request.getParameter("lid");
			String uid = request.getParameter("uid");

			// Test is the key parameters are invalid
			if (likeParam == null || likeParam.isEmpty() || lid == null
					|| uid == null || uid.isEmpty() || lid.isEmpty()) {
				response.setStatus(HttpStatus.SC_PRECONDITION_FAILED);
			}
			// Branches to removing a recommendation
			else if (likeParam.compareToIgnoreCase("off") == 0) {
				String nonce = getNonce(bearer, response);
				if (!nonce.isEmpty()) {
					unlike(bearer, uid, lid, nonce, response);
				}
			}
			// Branches to creating a recommendation
			else if (likeParam.compareToIgnoreCase("on") == 0) {
				String nonce = getNonce(bearer, response);
				if (!nonce.isEmpty()) {
					like(bearer, uid, lid, nonce, response);
				}
			}
			// Catch all for Response Code SC_PRECONDITION_FAILED (412)
			else {
				response.setStatus(HttpStatus.SC_PRECONDITION_FAILED);
			}

		}
	}
}
