/**
 * 
 */
package photosharing.api.conx;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.JSONObject;
import org.xml.sax.SAXException;

import photosharing.api.Configuration;
import photosharing.api.base.APIDefinition;
import photosharing.api.user.UserSession;

/**
 * The class calls the API for a Profile in IBM Connections <a
 * href="http://ibm.co/1K1rZZX">Profile API</a>
 * 
 * @author Paul Bastide <pbastide@us.ibm.com>
 *
 */
public class ProfileDefinition implements APIDefinition {

	/**
	 * generate the api url with a given userid
	 * 
	 * @param userid
	 * @return
	 */
	private String getApiUrl(String userid) {
		String server = Configuration
				.getConfigurationValue(Configuration.SERVER);
		String apiUrl = "https://" + server
				+ "/profiles/atom/profile.do?userid=" + userid;
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
		HttpSession session = request.getSession();
		UserSession user = (UserSession) session.getAttribute("User");
		String bearer = user.getBearer();

		/**
		 * The query should be cleansed before passing it to the backend
		 */
		Request get = Request.Get(getApiUrl(query));
		get.addHeader("Authorization", "Bearer " + bearer);

		try {
			Response apiResponse = get.execute();
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
				JSONObject jsonObj = new JSONObject(jsonString);

				JSONObject entry = jsonObj.getJSONArray("feed")
						.getJSONObject(0);

				//Check if the Entry exists for the given id
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
					while(!found && idx < len) {
						JSONObject link = links.getJSONObject(idx);
						
						String type = link.getString("type");
						if(type != null && !type.isEmpty() && type.compareTo("image")==0){
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
					
				}else{
					// There is no Entry for the user with the id.
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
			e.printStackTrace();
		} catch (JSONException e) {
			response.setHeader("X-Application-Error", e.getClass().getName());
			response.setStatus(500);
			e.printStackTrace();
		} catch (SAXException e) {
			response.setHeader("X-Application-Error", e.getClass().getName());
			response.setStatus(500);
			e.printStackTrace();
		}

	}

}
