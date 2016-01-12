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
package photosharing.api.conx;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.logging.Logger;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.JSONObject;
import org.xml.sax.SAXException;

import photosharing.api.Configuration;
import photosharing.api.ExecutorUtil;
import photosharing.api.base.APIDefinition;
import photosharing.api.oauth.OAuth20Data;
import photosharing.api.oauth.OAuth20Handler;

/**
 * The class calls the API for a Profile in IBM Connections <a
 * href="http://ibm.co/1K1rZZX">Profile API</a>
 * 
 * @author Paul Bastide <pbastide@us.ibm.com>
 * 
 */
public class ProfileDefinition implements APIDefinition {

	// Logger
	private final static String className = ProfileDefinition.class.getName();
	private Logger logger = Logger.getLogger(className);

	/**
	 * generate the api url with a given userid the format is atom and supports
	 * json (change atom to json)
	 * 
	 * @param userid
	 * @return url of the api
	 */
	private String getApiUrl(String userid) {
		Configuration config = Configuration.getInstance(null);
		String apiUrl = config.getValue(Configuration.BASEURL)
				+ "/profiles/atom/profile.do?userid=" + userid;
		return apiUrl;
	}

	/**
	 * generates the api url to retrieve the logged in user id the format is
	 * atom/xml
	 * 
	 * @return url of the service api
	 */
	private String getApiUrlForServiceDoc() {
		Configuration config = Configuration.getInstance(null);
		String apiUrl = config.getValue(Configuration.BASEURL)
				+ "/profiles/atom/profileService.do";
		return apiUrl;
	}

	/**
	 * retrieves a profile based on the person's userid
	 * 
	 * @see photosharing.api.base.APIDefinition#run(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void run(HttpServletRequest request, HttpServletResponse response) {

		/**
		 * check if query is empty, send 412
		 */
		String query = request.getParameter("uid");
		if (query == null || query.isEmpty()) {
			response.setStatus(412);
		}

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
			 * http://localhost:9080/photoSharing/api/profile?uid=self maps to
			 * https://apps.collabservnext.com/profiles/atom/profileService.do
			 * 
			 * example response
			 * 
			 */
			if (query.compareTo("self") == 0) {
				String apiUrl = getApiUrlForServiceDoc();

				Request get = Request.Get(apiUrl);
				get.addHeader("Authorization", "Bearer " + bearer);

				Executor exec = ExecutorUtil.getExecutor();
				Response apiResponse = exec.execute(get);

				HttpResponse hr = apiResponse.returnResponse();

				/**
				 * Check the status codes
				 */
				int code = hr.getStatusLine().getStatusCode();

				// Session is no longer valid or access token is expired
				if (code == 403) {
					response.sendRedirect("./api/logout");
				}

				// User is not authorized
				else if (code == 401) {
					response.setStatus(401);
				}

				// Content is returned
				else if (code == 200) {
					InputStream in = hr.getEntity().getContent();

					// Converts the XML to JSON
					// Alternatively, one can parse the XML using XPATH
					String jsonString = org.apache.wink.json4j.utils.XML.toJson(in);
					logger.info("json string is " + jsonString);
					
					JSONObject jsonObj = new JSONObject(jsonString);
					JSONObject workspace = jsonObj.getJSONObject("service").getJSONObject("workspace").getJSONObject("collection");
					String id = workspace.getString("userid");
					
					query = id;
				} else {
					JSONObject obj = new JSONObject();
					obj.put("error", "unexpected content");
				}

			}

			/**
			 * The query should be cleansed before passing it to the backend
			 * cleansing can incorporate checking that the id is a number
			 * 
			 * example URL
			 * http://localhost:9080/photoSharing/api/profile?uid=20131674 maps
			 * to https
			 * ://apps.collabservnext.com/profiles/atom/profile.do?userid
			 * =20131674
			 * 
			 * example response {"img":
			 * "https:\/\/apps.collabservnext.com\/profiles\/photo.do?key=fef1b5f3-586f-4470-ab0a-a9d4251fe1ec&lastMod=1443607729019","name":"P
			 * a u l Demo","email":"demo@us.ibm.com"}
			 * 
			 */
			String apiUrl = getApiUrl(query);
			Request get = Request.Get(apiUrl);
			get.addHeader("Authorization", "Bearer " + bearer);

			Executor exec = ExecutorUtil.getExecutor();
			Response apiResponse = exec.execute(get);

			HttpResponse hr = apiResponse.returnResponse();

			/**
			 * Check the status codes
			 */
			int code = hr.getStatusLine().getStatusCode();

			// Session is no longer valid or access token is expired
			if (code == 403) {
				response.sendRedirect("./api/logout");
			}

			// User is not authorized
			else if (code == 401) {
				response.setStatus(401);
			}

			// Content is returned
			else if (code == 200) {
				InputStream in = hr.getEntity().getContent();

				// Converts the XML to JSON
				// Alternatively, one can parse the XML using XPATH
				String jsonString = org.apache.wink.json4j.utils.XML.toJson(in);
				logger.info("json string is " + jsonString);

				JSONObject jsonObj = new JSONObject(jsonString);

				JSONObject entry = jsonObj.getJSONObject("feed").getJSONObject(
						"entry");
				logger.info("entry" + entry);

				// Check if the Entry exists for the given id
				if (entry != null) {
					// Start Building the Response
					String name = "";
					String image = "";
					String email = "";

					JSONObject contributor = entry.getJSONObject("contributor");
					name = contributor.getString("name");
					email = contributor.getString("email");

					JSONArray links = entry.getJSONArray("link");

					// Scans through the links and finds the profile image
					// XPath is much more efficient
					boolean found = false;
					int idx = 0;
					int len = links.length();
					while (!found && idx < len) {
						JSONObject link = links.getJSONObject(idx);

						String type = link.getString("type");
						if (type != null && !type.isEmpty()
								&& type.compareTo("image") == 0) {
							found = true;
							image = link.getString("href");
						}

						idx++;
					}

					// Build the json to send back
					JSONObject profile = new JSONObject();
					profile.put("name", name);
					profile.put("email", email);
					profile.put("img", image);

					// Write output streams
					ServletOutputStream out = response.getOutputStream();
					profile.write(out);

				} else {
					// There is no Entry for the user with the id.
					response.setStatus(HttpStatus.SC_NOT_FOUND);
					PrintWriter out = response.getWriter();
					out.println("User does not exist");

				}

			}

			// Unexpected status
			else {
				JSONObject obj = new JSONObject();
				obj.put("error", "unexpected content");

			}

		} catch (IOException e) {
			response.setHeader("X-Application-Error", e.getClass().getName());
			response.setStatus(500);
			logger.severe("IOException " + e.toString());
		} catch (JSONException e) {
			response.setHeader("X-Application-Error", e.getClass().getName());
			response.setStatus(500);
			logger.severe("JSONException " + e.toString());
		} catch (SAXException e) {
			response.setHeader("X-Application-Error", e.getClass().getName());
			response.setStatus(500);
			logger.severe("SAXException  " + e.toString());
		}

	}

}
