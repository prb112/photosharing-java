/**
 * 
 */
package photosharing.api.conx;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.ByteArrayEntity;

import photosharing.api.Configuration;
import photosharing.api.base.APIDefinition;
import photosharing.api.user.UserSession;

/**
 * The class calls the API for a File Recommendation in IBM Connections <a
 * href="http://ibm.co/1i2beBn">File Recommendation (Unlike/Like) API</a>
 * 
 * @author Paul Bastide <pbastide@us.ibm.com>
 *
 */
public class RecommendationDefinition implements APIDefinition {

	/**
	 * generate the base api url for files
	 * 
	 * @param userid
	 * @return
	 */
	private String getApiUrl() {
		String server = Configuration
				.getConfigurationValue(Configuration.SERVER);
		StringBuilder builder = new StringBuilder();
		builder.append("https://");
		builder.append(server);
		builder.append("/files/oauth/api");
		return builder.toString();
	}

	/**
	 * creates the recommendation content
	 * 
	 * @return
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
	 * gets the nonce url 
	 * <a href="http://ibm.co/1fG83gY">Get a Cryptographic Key</a>
	 * @return
	 */
	private String getNonceUrl(){
		String server = Configuration
				.getConfigurationValue(Configuration.SERVER);
		StringBuilder builder = new StringBuilder();
		builder.append("https://");
		builder.append(server);
		builder.append("/files/oauth/api/nonce");
		return builder.toString();
	}
	
	/**
	 * get nonce as described with nonce
	 * <a href="http://ibm.co/1fG83gY">Get a Cryptographic Key</a>
	 * @param bearer
	 */
	private String getNonce(String bearer, HttpServletResponse response){
		String nonce = "";
		
		//Build the Request
		Request get = Request.Get(getNonceUrl());
		get.addHeader("Authorization", "Bearer " + bearer);
		
		try {
			Response apiResponse = get.execute();
			HttpResponse hr = apiResponse.returnResponse();

			/**
			 * Check the status codes and if 200, convert to String
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
			
			else if (code == 200) {
				InputStream in = hr.getEntity().getContent();
				nonce = IOUtils.toString(in);
			}
						
		} catch (IOException e) {
			response.setHeader("X-Application-Error", e.getClass().getName());
			response.setStatus(500);
			e.printStackTrace();
		} 
		
		return nonce;
	}
	
	/**
	 * like a file   
	 * 
	 * @param bearer
	 * @param lid
	 * @param uid
	 * @param nonce
	 * @param response
	 */
	public void unlike(String bearer, String pid, String lid, String nonce, HttpServletResponse response) {
		String apiUrl = getApiUrl() + "/library/" + lid + "/document/" + pid + "/feed";
		
		try {
						
			String recommendation = generateRecommendationContent();
			
			// Generate the 
			Request post = Request.Post(apiUrl);
			post.addHeader("Authorization", "Bearer " + bearer);
			post.addHeader("X-Update-Nonce",nonce);
			post.addHeader("X-METHOD-OVERRIDE","DELETE");
			post.addHeader("Content-Type","application/atom+xml");
			post.addHeader("Content-Length","" + recommendation.length());
			
			ByteArrayEntity entity = new ByteArrayEntity(recommendation.getBytes("UTF-8"));
			post.body(entity);
			
			Response apiResponse = post.execute();
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

			// Default to 200
			else {
				response.setStatus(200);
								
			}

		} catch (IOException e) {
			response.setHeader("X-Application-Error", e.getClass().getName());
			response.setStatus(500);
			e.printStackTrace();
		}
	}
	
	/**
	 * like a file   
	 * 
	 * @param bearer
	 * @param pid
	 * @param lid
	 * @param nonce
	 * @param response
	 */
	public void like(String bearer, String pid, String lid, String nonce, HttpServletResponse response) {
		String apiUrl = getApiUrl() + "/library/" + lid + "/document/" + pid + "/feed";
	
		try {
						
			String recommendation = generateRecommendationContent();
			
			// Generate the 
			Request post = Request.Post(apiUrl);
			post.addHeader("Authorization", "Bearer " + bearer);
			post.addHeader("X-Update-Nonce",nonce);
			post.addHeader("Content-Type","application/atom+xml");
			post.addHeader("Content-Length","" + recommendation.length());
			
			ByteArrayEntity entity = new ByteArrayEntity(recommendation.getBytes("UTF-8"));
			post.body(entity);
			
			Response apiResponse = post.execute();
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

			// Default to 200
			else {
				response.setStatus(200);
								
			}

		} catch (IOException e) {
			response.setHeader("X-Application-Error", e.getClass().getName());
			response.setStatus(500);
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
		 * get the users bearer token
		 */
		HttpSession session = request.getSession();
		UserSession user = (UserSession) session.getAttribute("User");
		String bearer = user.getBearer();		
		
		// Parameters to use
		String likeParam = request.getParameter("r"); //r = recommendation
		String lid = request.getParameter("lid");
		String uid = request.getParameter("uid");
	
		// Test is the key parameters are invalid
		if(likeParam == null || likeParam.isEmpty() || lid == null || uid == null || uid.isEmpty() || lid.isEmpty()){
			response.setStatus(412);
		}
		//Branches to removing a recommendation
		else if(likeParam.compareToIgnoreCase("off")==0){
			String nonce = getNonce(bearer,response);
			if(!nonce.isEmpty()){
				unlike(bearer,lid,uid,nonce,response);
			}
		}
		//Branches to creating a recommendation
		else if(likeParam.compareToIgnoreCase("on")==0){
			String nonce = getNonce(bearer,response);
			if(!nonce.isEmpty()){
				like(bearer,lid,uid,nonce,response);
			}			
		}
		//Catch all for Response Code 412
		else{
			response.setStatus(412);
		}
		
	}

}