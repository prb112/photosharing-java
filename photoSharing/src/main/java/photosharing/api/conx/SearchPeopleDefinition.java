/**
 * 
 */
package photosharing.api.conx;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.JSONObject;

import photosharing.api.Configuration;
import photosharing.api.base.APIDefinition;
import photosharing.api.user.UserSession;

/**
 * The class calls the API for searching people in IBM Connections 
 * <a href="http://ibm.co/1KKSXzm">Social People Finder API</a>
 * 
 * @author Paul Bastide <pbastide@us.ibm.com>
 *
 */
public class SearchPeopleDefinition implements APIDefinition {

	/**
	 * generate the api url with a given query 
	 * The query should have a minimum of 3 characters to be successful
	 * 
	 * @param query
	 * @return
	 */
	private String getApiUrl(String query){
		String server = Configuration.getConfigurationValue(Configuration.SERVER);
		String apiUrl = "https://" + server + "/search/oauth/people/typeahead?=" + query;
		return apiUrl;
	}
	
	/**
	 * searches for people 
	 * @see photosharing.api.base.APIDefinition#run(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void run(HttpServletRequest request, HttpServletResponse response) {
		
		/**
		 * check if query is empty, send 412
		 */
		String query = request.getParameter("q");
		if(query == null || query.isEmpty()){
			response.setStatus(412);
		}				
		
		/**
		 * get the users bearer token 
		 */
		HttpSession session = request.getSession();
		UserSession user = (UserSession)session.getAttribute("User");
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
			if(code == 403){
				response.sendRedirect("./api/logout");
			}	
			
			// User is not authorized
			else if(code == 401){
				response.setStatus(401);
			}
			
			// Content is returned 
			else if(code == 200){
				ServletOutputStream out = response.getOutputStream();
				InputStream in = hr.getEntity().getContent();
				IOUtils.copy(in, out);
				IOUtils.closeQuietly(in);
				IOUtils.closeQuietly(out);
			}
			
			// Unexpected status 
			else{
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
		}
		
	}

}
