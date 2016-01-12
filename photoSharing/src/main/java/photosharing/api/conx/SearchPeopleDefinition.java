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

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.JSONObject;

import photosharing.api.Configuration;
import photosharing.api.ExecutorUtil;
import photosharing.api.base.APIDefinition;
import photosharing.api.oauth.OAuth20Data;
import photosharing.api.oauth.OAuth20Handler;

/**
 * The class calls the API for searching people in IBM Connections <a
 * href="http://ibm.co/1KKSXzm">Social People Finder API</a>
 * 
 * @author Paul Bastide <pbastide@us.ibm.com>
 * 
 */
public class SearchPeopleDefinition implements APIDefinition {

	// Logger
	private final static String className = SearchPeopleDefinition.class.getName();
	private Logger logger = Logger.getLogger(className);

	/**
	 * generate the api url with a given query The query should have a minimum
	 * of 3 characters to be successful
	 * 
	 * @param query
	 * @return url
	 */
	private String getApiUrl(String query) {
		Configuration config = Configuration.getInstance(null);
		String apiUrl = config.getValue(Configuration.BASEURL)
				+ "/search/oauth/people/typeahead?query=" + query;
		return apiUrl;
	}

	/**
	 * searches for people
	 * 
	 * @see photosharing.api.base.APIDefinition#run(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void run(HttpServletRequest request, HttpServletResponse response) {

		/**
		 * check if query is empty, send SC_PRECONDITION_FAILED - 412
		 */
		String query = request.getParameter("q");
		if (query == null || query.isEmpty()) {
			response.setStatus(HttpStatus.SC_PRECONDITION_FAILED);
		}

		/**
		 * get the users bearer token
		 */
		HttpSession session = request.getSession(false);
		OAuth20Data data = (OAuth20Data) session.getAttribute(OAuth20Handler.CREDENTIALS);
		String bearer = data.getAccessToken();

		/**
		 * The query should be cleansed before passing it to the backend
		 * 
		 * Example API Url
		 * http://localhost:9080/photoSharing/api/searchPeople?q=sub
		 * maps to 
		 * https://apps.collabservnext.com/search/oauth/people/typeahead?query=sub
		 * 
		 * Response Data
		 * {
  		 *	"totalResults": 1,
  		 *	"startIndex": 1,
  		 *	"numResultsInCurrentPage": 1,
  		 *	"persons": [
    	 *		{
      	 *		"id": "20000397",
      	 *		"name": "John Doe2",
      	 *		"userType": "EMPLOYEE",
      	 *		"jobResponsibility": "Stooge",
      	 *		"confidence": "medium",
      	 *		"score": 10997.0
    	 *		}
  		 *	]
		 *	}
		 * 
		 */
		Request get = Request.Get(getApiUrl(query));
		get.addHeader("Authorization", "Bearer " + bearer);

		try {
			Executor exec = ExecutorUtil.getExecutor();
			Response apiResponse = exec.execute(get);
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

			// Content is returned
			else if (code == HttpStatus.SC_OK) {
				ServletOutputStream out = response.getOutputStream();
				InputStream in = hr.getEntity().getContent();
				IOUtils.copy(in, out);
				IOUtils.closeQuietly(in);
				IOUtils.closeQuietly(out);
			}

			// Unexpected status
			else {
				JSONObject obj = new JSONObject();
				obj.put("error", "unexpected content");

			}

		} catch (IOException e) {
			response.setHeader("X-Application-Error", e.getClass().getName());
			response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			logger.severe("IOException " + e.toString());
		} catch (JSONException e) {
			response.setHeader("X-Application-Error", e.getClass().getName());
			response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			logger.severe("JSONException " + e.toString());
		}

	}

}
