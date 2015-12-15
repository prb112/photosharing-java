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
import org.apache.http.entity.ByteArrayEntity;
import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.JSONObject;
import org.xml.sax.SAXException;

import photosharing.api.Configuration;
import photosharing.api.base.APIDefinition;
import photosharing.api.user.UserSession;

/**
 * The class calls the API for a File Comments in IBM Connections <a
 * href="">File Comments API</a>
 * 
 * @author Paul Bastide <pbastide@us.ibm.com>
 *
 */
public class CommentsDefinition implements APIDefinition {

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
		builder.append(server);
		builder.append("/files/oauth/api");
		return builder.toString();
	}

	/**
	 * creates the formatted comment
	 * 
	 * @param content
	 * @return
	 */
	private String generateComment(String content) {
		StringBuilder builder = new StringBuilder();
		builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?><entry xmlns=\"http://www.w3.org/2005/Atom\" xmlns:app=\"http://www.w3.org/2007/app\" xmlns:snx=\"http://www.ibm.com/xmlns/prod/sn\"><category scheme=\"tag:ibm.com,2006:td/type\" term=\"comment\" label=\"comment\"/><content type=\"text\">");
		builder.append(content);
		builder.append("</content></entry>");
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
	 * deletes a comment with the given comments api url 
	 * uses the HTTP method delete 
	 * 
	 * @param bearer
	 * @param cid
	 * @param pid
	 * @param uid
	 * @param response
	 * @param nonce
	 */
	public void deleteComment(String bearer, String cid, String pid, String uid, HttpServletResponse response, String nonce){
		String apiUrl = getApiUrl() + "/userlibrary/" + uid + "/document/" + pid + "/comment/" + cid + "/entry";
		
		Request delete = Request.Delete(apiUrl);
		delete.addHeader("Authorization", "Bearer " + bearer);
		delete.addHeader("X-Update-Nonce",nonce);

		try {
			Response apiResponse = delete.execute();
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
			else{
				response.setStatus(200);
			}

		} catch (IOException e) {
			response.setHeader("X-Application-Error", e.getClass().getName());
			response.setStatus(500);
			e.printStackTrace();
		} 

	}

	/**
	 * updates a given comment 
	 * 
	 * @param bearer
	 * @param cid
	 * @param pid
	 * @param uid
	 * @param body
	 * @param nonce
	 * @param response
	 */
	public void updateComment(String bearer, String cid, String pid, String uid, String body, String nonce, HttpServletResponse response){
		String apiUrl = getApiUrl() + "/userlibrary/" + uid + "/document/" + pid + "/comment/" + cid + "/entry";
		
		String comment = generateComment(body);
		
		// Generate the 
		Request put = Request.Put(apiUrl);
		put.addHeader("Authorization", "Bearer " + bearer);
		put.addHeader("X-Update-Nonce",nonce);
		put.addHeader("Content-Type","application/atom+xml");
		put.addHeader("Content-Length","" + comment.length());
		
		try {
			ByteArrayEntity entity = new ByteArrayEntity(comment.getBytes("UTF-8"));
			put.body(entity);
			
			Response apiResponse = put.execute();
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
			else{
				response.setStatus(200);
			}

		} catch (IOException e) {
			response.setHeader("X-Application-Error", e.getClass().getName());
			response.setStatus(500);
			e.printStackTrace();
		} 
		
	}
	
	/**
	 * creates a new comment with a given library id and document id  
	 * 
	 * @param bearer
	 * @param pid
	 * @param uid
	 * @param body
	 * @param nonce
	 * @param response
	 */
	public void createComment(String bearer, String pid, String uid,
			String body, String nonce, HttpServletResponse response) {
		String apiUrl = getApiUrl() + "/userlibrary/" + uid + "/document/" + pid + "/feed";
	
		try {
			JSONObject obj = new JSONObject(body);
			
			String comment = generateComment(obj.getString("comment"));
			
			// Generate the 
			Request post = Request.Post(apiUrl);
			post.addHeader("Authorization", "Bearer " + bearer);
			post.addHeader("X-Update-Nonce",nonce);
			post.addHeader("Content-Type","application/atom+xml");
			post.addHeader("Content-Length","" + comment.length());
			
			ByteArrayEntity entity = new ByteArrayEntity(comment.getBytes("UTF-8"));
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
			else if(code == 201){
				response.setStatus(200);
				
				InputStream in = hr.getEntity().getContent();
				String jsonString = org.apache.wink.json4j.utils.XML.toJson(in);
				
				JSONObject base = new JSONObject(jsonString);
				JSONObject entry = base.getJSONObject("entry");
				JSONObject author = entry.getJSONObject("author");
				
				String name = author.getString("name");
				String userid = author.getString("snx:userid");
				String date = entry.getString("date");
				String content = entry.getString("content");
				String cid = entry.getString("td:uuid");
				
				//Build the JSON object
				JSONObject commentJSON = new JSONObject();
				commentJSON.put("uid", userid);
				commentJSON.put("author", name);
				commentJSON.put("date", date);
				commentJSON.put("content", content);
				commentJSON.put("cid", cid);
				
				// Flush the Object to the Stream with content type
				response.setHeader("Content-Type","application/json");
				ServletOutputStream out = response.getOutputStream();
				commentJSON.write(out);
				
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
	
	/**
	 * reads the comments from the comments feed 
	 * 
	 * @param bearer
	 * @param pid
	 * @param uid
	 * @param response
	 */
	public void readComments(String bearer, String pid, String uid, HttpServletResponse response){
		String apiUrl = getApiUrl() + "/userlibrary/" + uid + "/document/" + pid + "/feed?category=comment&sortBy=created&sortOrder=desc";
		
		Request get = Request.Get(apiUrl);
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

			// Default to 200
			else if(code == 200){
				response.setStatus(200);
				
				InputStream in = hr.getEntity().getContent();
				String jsonString = org.apache.wink.json4j.utils.XML.toJson(in);
				JSONObject feed = new JSONObject(jsonString);
				
				JSONArray comments = new JSONArray();
				
				JSONArray entries = feed.getJSONArray("entry");
				int len = entries.length();
				for(int i = 0; i < len; i++){
					JSONObject entry = entries.getJSONObject(i);
					JSONObject author = entry.getJSONObject("author");
					
					String name = author.getString("name");
					String userid = author.getString("snx:userid");
					String date = entry.getString("date");
					String content = entry.getString("content");
					String cid = entry.getString("td:uuid");

					//Build the JSON object
					JSONObject commentJSON = new JSONObject();
					commentJSON.put("uid", userid);
					commentJSON.put("author", name);
					commentJSON.put("date", date);
					commentJSON.put("content", content);
					commentJSON.put("cid", cid);
					
					comments.add(commentJSON);
					
				}
				
				// Flush the Object to the Stream with content type
				response.setHeader("Content-Type","application/json");
				ServletOutputStream out = response.getOutputStream();
				comments.write(out);
				
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
	
	/**
	 * manages interactions with comments based on method
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
		HttpSession session = request.getSession();
		UserSession user = (UserSession) session.getAttribute("User");
		String bearer = user.getBearer();		
		
		// Create a Comment
		if (method.compareTo("POST") == 0) {
			//Extract the URL parameters from the request
			String pid = request.getParameter("pid");
			String uid = request.getParameter("uid");
			
			try {
				String body = IOUtils.toString(request.getInputStream());
				
				//Checks the State of the URL parameters 
				if( pid == null || uid == null || body == null || body.isEmpty() || pid.isEmpty() || uid.isEmpty()){
					response.setStatus(412);
				}else{
					String nonce = getNonce(bearer,response);
					if(!nonce.isEmpty()){
						createComment(bearer,pid,uid,body,nonce,response);
					}
				}
								
			} catch (IOException e) {
				response.setHeader("X-Application-Error", e.getClass().getName());
				response.setStatus(500);
				e.printStackTrace();
			}		
		}
		// Update a Comment
		else if (method.compareTo("PUT") == 0) {
			//Extract the URL parameters from the request
			String cid = request.getParameter("cid");
			String pid = request.getParameter("pid");
			String uid = request.getParameter("uid");
			
			try {
				String body = IOUtils.toString(request.getInputStream());
				
				//Checks the State of the URL parameters 
				if(cid == null || pid == null || uid == null || body == null || body.isEmpty() || cid.isEmpty() || pid.isEmpty() || uid.isEmpty()){
					response.setStatus(412);
				}else{
					String nonce = getNonce(bearer,response);
					if(!nonce.isEmpty()){
						updateComment(bearer,cid,pid,uid,body,nonce,response);
					}
				}
								
			} catch (IOException e) {
				response.setHeader("X-Application-Error", e.getClass().getName());
				response.setStatus(500);
				e.printStackTrace();
			}
			
		}
		// Delete a Comment
		else if (method.compareTo("DELETE") == 0) {
			//Extract the URL parameters from the request
			String cid = request.getParameter("cid");
			String pid = request.getParameter("pid");
			String uid = request.getParameter("uid");
			
			//Checks the State of the URL parameters 
			if(cid == null || pid == null || uid == null || cid.isEmpty() || pid.isEmpty() || uid.isEmpty()){
				response.setStatus(412);
			}else{
				String nonce = getNonce(bearer,response);
	
				if(!nonce.isEmpty()){
					deleteComment(bearer,cid,pid,uid,response,nonce);
				}
			}
			
		}
		// Read a Comment and default to a GET
		else {
			//Extract the URL parameters from the request
			String pid = request.getParameter("pid");
			String uid = request.getParameter("uid");
			
			if( pid == null || uid == null || pid.isEmpty() || uid.isEmpty()){
				response.setStatus(412);
			}else{
				readComments(bearer,pid,uid,response);
			}
				
		}

	}

}