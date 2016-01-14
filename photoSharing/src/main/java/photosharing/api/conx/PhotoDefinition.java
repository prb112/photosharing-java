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

import photosharing.api.Configuration;
import photosharing.api.ExecutorUtil;
import photosharing.api.base.APIDefinition;
import photosharing.api.oauth.OAuth20Data;
import photosharing.api.oauth.OAuth20Handler;

/**
 * The class calls the API for a Profile Photo in IBM Connections <a
 * href="http://ibm.co/1PgnHua">Working with Profile Photos API</a>
 * 
 * @author Paul Bastide <pbastide@us.ibm.com>
 * 
 */
public class PhotoDefinition implements APIDefinition {

	// Logger
	private final static String className = PhotoDefinition.class.getName();
	private Logger logger = Logger.getLogger(className);

	/**
	 * generate the api url with a given key for the user
	 * 
	 * @param key
	 * @return url of the api
	 */
	private String getApiUrl(String key) {
		Configuration config = Configuration.getInstance(null);
		String apiUrl = config.getValue(Configuration.BASEURL)
				+ "/profiles/photo.do?key=" + key;
		return apiUrl;
	}

	/**
	 * retrieves a profile photo based on the person's key
	 * 
	 * @see photosharing.api.base.APIDefinition#run(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void run(HttpServletRequest request, HttpServletResponse response) {

		/**
		 * check if query is empty, send SC_PRECONDITION_FAILED - 412
		 */
		String query = request.getParameter("key");
		if (query == null || query.isEmpty()) {
			response.setStatus(HttpStatus.SC_PRECONDITION_FAILED);
		} else {

			/**
			 * get the users bearer token
			 */
			HttpSession session = request.getSession(false);

			Object oData = session.getAttribute(OAuth20Handler.CREDENTIALS);
			if (oData == null) {
				logger.warning("OAuth20Data is null");
			}

			OAuth20Data data = (OAuth20Data) oData;
			String bearer = data.getAccessToken();

			try {

				/**
				 * Example URL:
				 * http://localhost:9080/photoSharing/api/photo?uid=key maps to
				 * https://apps.collabservnext.com/profiles/photo.do
				 * 
				 * and results in an image
				 */
				String apiUrl = getApiUrl(query);
				
				logger.info("api url is " + apiUrl);

				Request get = Request.Get(apiUrl);
				get.addHeader("Authorization", "Bearer " + bearer);

				Executor exec = ExecutorUtil.getExecutor();
				Response apiResponse = exec.execute(get);

				HttpResponse hr = apiResponse.returnResponse();

				/**
				 * Check the status codes
				 */
				int code = hr.getStatusLine().getStatusCode();

				// Session is no longer valid or access token is expired - 403
				if (code == HttpStatus.SC_FORBIDDEN) {
					response.sendRedirect("./api/logout");
				}

				// User is not authorized
				// SC_UNAUTHORIZED (401)
				else if (code == HttpStatus.SC_UNAUTHORIZED) {
					response.setStatus(HttpStatus.SC_UNAUTHORIZED);
				}

				// Content is returned
				// OK (200)
				else if (code == HttpStatus.SC_OK) {
					
					//Headers
					response.setContentType(hr.getFirstHeader("Content-Type").getValue());
					response.setHeader("content-length", hr.getFirstHeader("content-length").getValue());
					
					// Streams
					InputStream in = hr.getEntity().getContent();
					IOUtils.copy(in, response.getOutputStream());
					IOUtils.closeQuietly(in);
					IOUtils.closeQuietly(response.getOutputStream());

				} else {
					// Unexpected Result
					response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
				}

			} catch (IOException e) {
				response.setHeader("X-Application-Error", e.getClass()
						.getName());
				response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
				logger.severe("Photo Definition - IOException " + e.toString());
			} 
		}

	}

}
