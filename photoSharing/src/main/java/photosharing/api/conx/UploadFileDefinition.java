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
import java.io.PrintWriter;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.InputStreamEntity;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.JSONObject;
import org.xml.sax.SAXException;

import photosharing.api.Configuration;
import photosharing.api.ExecutorUtil;
import photosharing.api.base.APIDefinition;
import photosharing.api.oauth.OAuth20Data;
import photosharing.api.oauth.OAuth20Handler;

/**
 * The class calls the API for a Uploading a File to IBM Connections<a
 * href="http://ibm.co/1PTMmqO">File Upload API</a>
 * 
 * @author Paul Bastide <pbastide@us.ibm.com>
 * 
 */
public class UploadFileDefinition implements APIDefinition {

	// Logger
	private final static String className = UploadFileDefinition.class.getName();
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
	 * gets the nonce url <a href="http://ibm.co/1fG83gY">Get a Cryptographic
	 * Key</a>
	 * 
	 * you can use basic or oauth in the path of the api url
	 * 
	 * @return the api url for the nonce
	 */
	private String getNonceUrl() {
		Configuration config = Configuration.getInstance(null);
		StringBuilder builder = new StringBuilder();
		builder.append(config.getValue(Configuration.BASEURL));
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
			 * Check the status codes and if 200, convert to String
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
			}

		} catch (IOException e) {
			response.setHeader("X-Application-Error", e.getClass().getName());
			response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			logger.severe("IOException " + e.toString());
		}

		return nonce;
	}

	/**
	 * uploads a file to the IBM Connections Cloud using the Files Service
	 * 
	 * @param bearer token
	 * @param nonce 
	 * @param request
	 * @param response
	 */
	public void uploadFile(String bearer, String nonce,
			HttpServletRequest request, HttpServletResponse response) {

		// Extracts from the Request Parameters
		String visibility = request.getParameter("visibility");
		String title = request.getParameter("title");
		String share = request.getParameter("share");
		String tagsUnsplit = request.getParameter("q");

		// Check for the Required Parameters
		if (visibility == null || title == null || title.isEmpty()
				|| visibility.isEmpty()) {
			response.setStatus(HttpStatus.SC_PRECONDITION_FAILED);

		} else {

			/*
			 * Builds the URL Parameters 
			 */
			StringBuilder builder = new StringBuilder();
			builder.append("visibility=" + visibility+ "&");
			builder.append("title=" + title+ "&");

			// The Share parameters for the URL
			if (share != null && !share.isEmpty()) {
				builder.append("shared=true&");
				builder.append("shareWith=" + share + "&");
			}

			// Splits the TagString into Indvidual Tags
			// - Technically this API is limited to 3 tags at most. 
			String[] tags = tagsUnsplit.split(",");
			for (String tag : tags) {
				logger.info("Tag-> " + tag);
				builder.append("tag=" + tag + "&");
			}
			
			// Build the apiURL
			String apiUrl = getApiUrl() + "/myuserlibrary/feed?"
					+ builder.toString();
			
			//API Url
			logger.info(apiUrl);

			// Add the Headers
			//String length = request.getHeader("X-Content-Length");
			String contentType = request.getHeader("Content-Type");
			String fileext = contentType.split("/")[1];
			String slug = title+ "." + fileext;
			

			Request post = Request.Post(apiUrl);
			post.addHeader("Authorization", "Bearer " + bearer);
			post.addHeader("X-Update-Nonce", nonce);
			post.addHeader("Slug", slug);
			//post.addHeader("Content-Length", length);

			try {
				//
				InputStream in = request.getInputStream();
				Base64InputStream bis = new Base64InputStream(in);
				
				InputStreamEntity entity = new InputStreamEntity(bis);
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

				// Duplicate Item
				else if (code == HttpStatus.SC_CONFLICT) {
					response.setStatus(HttpStatus.SC_CONFLICT);
				}

				// Checks if Created
				else if(code == HttpStatus.SC_CREATED) {
					response.setStatus(HttpStatus.SC_OK);
					/**
					 * Do Extra Processing Here to process the body
					 */
					InputStream inRes = hr.getEntity().getContent();

					// Converts XML to JSON String
					String jsonString = org.apache.wink.json4j.utils.XML
							.toJson(inRes);
					JSONObject obj = new JSONObject(jsonString);
					
					response.setContentType("application/json");
					PrintWriter writer = response.getWriter();
					writer.append(obj.toString());
					writer.close();

				}else{
					// Catch All
					response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
				}

			} catch (IOException e) {
				response.setHeader("X-Application-Error", e.getClass()
						.getName());
				response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
				logger.severe("IOException " + e.toString());
			} catch (SAXException e) {
				response.setHeader("X-Application-Error", e.getClass()
						.getName());
				response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
				logger.severe("IOException " + e.toString());
			} catch (JSONException e) {
				response.setHeader("X-Application-Error", e.getClass()
						.getName());
				response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
				logger.severe("IOException " + e.toString());
			}
		}
	}

	/**
	 * manages upload file definition
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
		if (method.compareTo("POST") == 0) {
			String nonce = getNonce(bearer, response);
			if (!nonce.isEmpty()) {
				uploadFile(bearer, nonce, request, response);
			}
		} else {
			response.setStatus(HttpStatus.SC_PRECONDITION_FAILED);
		}

	}

}
