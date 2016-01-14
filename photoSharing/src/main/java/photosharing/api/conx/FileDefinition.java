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
import java.util.Iterator;
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
import org.apache.http.entity.ContentType;
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
 * The class calls the API for managing Files in IBM Connections<a
 * href="http://ibm.co/1RlASQK">Working with Files API</a>
 * 
 * @author Paul Bastide <pbastide@us.ibm.com>
 * 
 */
public class FileDefinition implements APIDefinition {

	// Logger
	private final static String className = FileDefinition.class.getName();
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
	 * returns the data from the user library
	 * 
	 * @param userId
	 * 
	 * @return {String}
	 */
	private String getUserLibraryApiUrl(String userId) {
		return getApiUrl() + "/userlibrary/" + userId + "/feed?tag=photojava";
	}

	/**
	 * returns the data from the user's organization's public feed
	 * 
	 * @return {String}
	 */
	private String getPublicFilesApiUrl() {
		return getApiUrl()
				+ "/documents/feed?tag=photojava&sK=created&sO=dsc&visibility=public";
	}

	/**
	 * returns the data from the user's private feed
	 * 
	 * @return {String}
	 */
	private String getPrivateFilesApiUrl() {

		return getApiUrl()
				+ "/myuserlibrary/feed?sK=modified&sO=dsc&tag=photojava";
	}

	/**
	 * returns the data from the user's private feed
	 * 
	 * @return {String}
	 */
	private String getSharedFiles() {

		return getApiUrl()
				+ "/documents/shared/feed?sK=created&sO=dsc&sC=docshare&direction=inboundtag=photojava";
	}

	/**
	 * returns the user apiUrl to get a entry
	 * 
	 * @param pid
	 *            file id
	 * @param lid
	 *            library id
	 * @return {String}
	 */
	private String getFileMetadata(String pid, String lid) {
		return getApiUrl()
				+ "/library/"
				+ lid
				+ "/document/"
				+ pid
				+ "/entry?includeRecommendation=true&includeTags=true&includeShare=true";
	}

	/**
	 * returns the user apiUrl to get a thumbnail
	 * 
	 * @param pid
	 *            file id
	 * @param lid
	 *            library id
	 * @return {String}
	 */
	private String getThumbnailApiUrl(String pid, String lid) {
		// http://localhost:9080/photoSharing/api/file?action=thumbnail&pid=a9046490-dc17-43c1-8efb-c443c31a183c&lid=2597409c-b292-4059-bb4f-3c92c90f5c2e
		// maps to
		// https://<SERVER>/files/basic/api/library/<LID>/document/<PID>/thumbnail?renditionKind=mediumview
		return getApiUrl() + "/library/" + lid + "/document/" + pid + "/media";
	}

	/**
	 * manages files api definition
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
		Object o = session.getAttribute(OAuth20Handler.CREDENTIALS);
		OAuth20Data data = (OAuth20Data) o;

		if (o != null) {

			String bearer = data.getAccessToken();

			if (method.compareTo("GET") == 0) {
				String action = request.getParameter("action");
				if (action == null || action.isEmpty()) {
					response.setStatus(HttpStatus.SC_PRECONDITION_FAILED);
				} else {

					if (action.compareTo("public") == 0) {
						// get files data for public
						getOrgPublicFeed(bearer, request, response);

					} else if (action.compareTo("private") == 0) {
						// get files data for private
						getPrivateFeed(bearer, request, response);

					} else if (action.compareTo("messages") == 0) {
						// get files data for messages
						getSharedFeed(bearer, request, response);

					} else if (action.compareTo("file") == 0) {
						// get file for image
						getThumbnail(bearer, request, response);

					} else if (action.compareTo("thumbnail") == 0) {
						// get thumbnail for an image
						getThumbnail(bearer, request, response);

					} else if (action.compareTo("userlibrary") == 0) {
						// get file for a specific userlibrary
						// action -> userlibrary
						getUserLibraryResults(bearer, request, response);

					} else if (action.compareTo("info") == 0) {
						// get extended file data
						getFileMetadata(bearer, request, response);

					} else {
						response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
						logger.warning("did not find the action " + action);
					}
				}

			} else {
				response.setStatus(HttpStatus.SC_PRECONDITION_FAILED);
			}
		} else {
			response.setStatus(HttpStatus.SC_BAD_REQUEST);
		}

	}

	/**
	 * manages the thumbnail access
	 * 
	 * @param bearer
	 * @param request
	 * @param response
	 */
	public void getThumbnail(String bearer, HttpServletRequest request,
			HttpServletResponse response) {
		String pid = request.getParameter("pid");
		String lid = request.getParameter("lid");

		if (pid == null || lid == null || pid.isEmpty() || lid.isEmpty()) {
			logger.warning("bad parameters");
			response.setStatus(HttpStatus.SC_BAD_REQUEST);
		} else {

			String apiUrl = getThumbnailApiUrl(pid, lid);

			Request get = Request.Get(apiUrl);
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

				// Default to SC_OK (200)
				else if (code == HttpStatus.SC_OK) {
					response.setContentType(hr.getFirstHeader("Content-Type")
							.getValue());
					response.setHeader("content-length",
							hr.getFirstHeader("content-length").getValue());
					response.setStatus(HttpStatus.SC_OK);

					// Streams
					InputStream in = hr.getEntity().getContent();
					IOUtils.copy(in, response.getOutputStream());
					IOUtils.closeQuietly(in);
					IOUtils.closeQuietly(response.getOutputStream());

				}

			} catch (IOException e) {
				response.setHeader("X-Application-Error", e.getClass()
						.getName());
				response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
				logger.severe("Issue with read file " + e.toString());
			}

		}

	}

	/**
	 * gets the user file metadata results
	 * 
	 * Payload
	 *  {"uid":"20971118","thumbnail":
	 * ".\/api\/file?action=thumbnail&pid=7fdedc74-a9f4-46f1-acde-39bef9975847&lid=2597409c-b292-4059-bb4f-3c92c90f5c2e",
	 * "like":true,"lid":"2597409c-b292-4059-bb4f-3c92c90f5c2e","pid":"7fdedc74-a9f4-46f1-acde-39bef9975847","photographer":"ASIC
	 * ASIC","title":"Test32ab.jpeg","tags":["abcd","photojava"]}
	 * 
	 * @param request
	 * @param response
	 */
	public void getFileMetadata(String bearer, HttpServletRequest request,
			HttpServletResponse response) {

		String library = request.getParameter("lid");
		String file = request.getParameter("pid");
		if (library == null || library.isEmpty() || file == null
				|| file.isEmpty()) {
			logger.warning("library or file is null");
			response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
		} else {
			// sets the content type - application/json
			response.setContentType(ContentType.APPLICATION_JSON.getMimeType());
			String apiUrl = getFileMetadata(file, library);

			logger.info(apiUrl);

			Request get = Request.Get(apiUrl);
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

				// Default to SC_OK (200)
				else if (code == HttpStatus.SC_OK) {
					response.setStatus(HttpStatus.SC_OK);

					InputStream in = hr.getEntity().getContent();
					String jsonString = org.apache.wink.json4j.utils.XML
							.toJson(in);

					// Logging out the JSON Object
					logger.info(jsonString);

					JSONObject result = new JSONObject(jsonString);
					JSONObject entry = result.getJSONObject("entry");

					logger.info(entry.toString());

					JSONObject author = entry.getJSONObject("author");
					String photographer = author.getString("name");
					String uid = author.getString("userid");
					String date = entry.getString("published");

					String title = entry.getJSONObject("title").getString(
							"content");

					String lid = entry.getString("libraryId");

					String pid = entry.getString("uuid");

					String thumbnail = "./api/file?action=thumbnail&pid=" + pid
							+ "&lid=" + lid;

					JSONObject res = new JSONObject(createPhoto(lid, pid,
							title, uid, photographer, thumbnail));

					JSONArray links = entry.getJSONArray("link");
					@SuppressWarnings("rawtypes")
					Iterator iter = links.iterator();
					while (iter.hasNext()) {
						JSONObject obj = (JSONObject) iter.next();
						String rel = obj.getString("rel");
						if (rel != null && rel.compareTo("recommendation") == 0) {
							res.put("like", true);
						}
					}

					JSONArray categories = entry.getJSONArray("category");
					iter = categories.iterator();
					JSONArray tags = new JSONArray();
					while (iter.hasNext()) {
						JSONObject obj = (JSONObject) iter.next();
						if (!obj.has("scheme")) {
							tags.put(obj.getString("term"));
						}
					}
					res.put("tags", tags);
					res.put("published", date);

					// Flush the Object to the Stream with content type
					response.setHeader("Content-Type", "application/json");
					PrintWriter out = response.getWriter();
					out.println(res.toString());
					out.flush();

				}

			} catch (IOException e) {
				response.setHeader("X-Application-Error", e.getClass()
						.getName());
				response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
				logger.severe("Issue with read userlibrary " + e.toString());
			} catch (JSONException e) {
				response.setHeader("X-Application-Error", e.getClass()
						.getName());
				response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
				logger.severe("Issue with read userlibrary " + e.toString());
				e.printStackTrace();
			} catch (SAXException e) {
				response.setHeader("X-Application-Error", e.getClass()
						.getName());
				response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
				logger.severe("Issue with read userlibrary " + e.toString());
			}

		}

	}

	/**
	 * gets the user library results
	 * 
	 * @param request
	 * @param response
	 */
	public void getUserLibraryResults(String bearer,
			HttpServletRequest request, HttpServletResponse response) {

		String userId = request.getParameter("userid");
		if (userId == null || userId.isEmpty()) {
			logger.warning("userId is null");
			response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
		} else {
			// sets the content type - application/json
			response.setContentType(ContentType.APPLICATION_JSON.getMimeType());
			String apiUrl = getUserLibraryApiUrl(userId);

			logger.info(apiUrl);

			Request get = Request.Get(apiUrl);
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

				// Default to SC_OK (200)
				else if (code == HttpStatus.SC_OK) {
					response.setStatus(HttpStatus.SC_OK);

					InputStream in = hr.getEntity().getContent();
					String jsonString = org.apache.wink.json4j.utils.XML
							.toJson(in);

					// Logging out the JSON Object
					logger.info(jsonString);

					JSONObject feed = new JSONObject(jsonString)
							.getJSONObject("feed");

					logger.info(feed.toString());

					JSONArray files = new JSONArray();

					JSONArray entries = feed.getJSONArray("entry");
					int len = entries.length();
					for (int i = 0; i < len; i++) {

						JSONObject entry = entries.getJSONObject(i);
						logger.info(entry.toString());

						JSONObject author = entry.getJSONObject("author");
						String photographer = author.getString("name");
						String uid = author.getString("userid");

						String title = entry.getJSONObject("title").getString(
								"content");

						String lid = entry.getString("libraryId");

						String pid = entry.getString("uuid");

						String thumbnail = "./api/file?action=thumbnail&pid="
								+ pid + "&lid=" + lid;

						files.add(createPhoto(lid, pid, title, uid,
								photographer, thumbnail));

					}

					// Flush the Object to the Stream with content type
					response.setHeader("Content-Type", "application/json");
					PrintWriter out = response.getWriter();
					out.println(files.toString());
					out.flush();

				}

			} catch (IOException e) {
				response.setHeader("X-Application-Error", e.getClass()
						.getName());
				response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
				logger.severe("Issue with read userlibrary " + e.toString());
			} catch (JSONException e) {
				response.setHeader("X-Application-Error", e.getClass()
						.getName());
				response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
				logger.severe("Issue with read userlibrary " + e.toString());
				e.printStackTrace();
			} catch (SAXException e) {
				response.setHeader("X-Application-Error", e.getClass()
						.getName());
				response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
				logger.severe("Issue with read userlibrary " + e.toString());
			}

		}

	}

	/**
	 * gets the user's organization's public files results
	 * 
	 * @param request
	 * @param response
	 */
	public void getOrgPublicFeed(String bearer, HttpServletRequest request,
			HttpServletResponse response) {

		String apiUrl = getPublicFilesApiUrl();
		logger.info(apiUrl);

		Request get = Request.Get(apiUrl);
		get.addHeader("Authorization", "Bearer " + bearer);

		try {
			// sets the content type - application/json
			response.setContentType(ContentType.APPLICATION_JSON.getMimeType());

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

			// Default to SC_OK (200)
			else if (code == HttpStatus.SC_OK) {
				response.setStatus(HttpStatus.SC_OK);

				InputStream in = hr.getEntity().getContent();
				String jsonString = org.apache.wink.json4j.utils.XML.toJson(in);

				// Logging out the JSON Object
				logger.info(jsonString);

				JSONObject feed = new JSONObject(jsonString)
						.getJSONObject("feed");

				logger.info(feed.toString());

				JSONArray files = new JSONArray();

				JSONArray entries = feed.getJSONArray("entry");
				int len = entries.length();
				for (int i = 0; i < len; i++) {

					JSONObject entry = entries.getJSONObject(i);
					logger.info(entry.toString());

					JSONObject author = entry.getJSONObject("author");
					String photographer = author.getString("name");
					String uid = author.getString("userid");

					String title = entry.getJSONObject("title").getString(
							"content");

					String lid = entry.getString("libraryId");

					String pid = entry.getString("uuid");

					String thumbnail = "./api/file?action=file&pid=" + pid
							+ "&lid=" + lid;

					String share = "0";
					String like = "0";
					JSONArray rank = entry.getJSONArray("rank");

					@SuppressWarnings("rawtypes")
					Iterator r = rank.iterator();
					while (r.hasNext()) {
						JSONObject temp = (JSONObject) r.next();
						String scheme = temp.getString("scheme");
						if (scheme.contains("share")) {
							share = temp.getString("content");
						} else if (scheme.contains("recommendations")) {
							like = temp.getString("content");
						}
					}

					JSONObject e = createPhoto(lid, pid, title, uid,
							photographer, thumbnail);
					e.put("likes", like);
					e.put("shares", share);
					files.add(e);

				}

				// Flush the Object to the Stream with content type
				response.setHeader("Content-Type", "application/json");
				PrintWriter out = response.getWriter();
				out.println(files.toString());
				out.flush();

			}

		} catch (IOException e) {
			response.setHeader("X-Application-Error", e.getClass().getName());
			response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			logger.severe("Issue with read user's org feed " + e.toString());
		} catch (JSONException e) {
			response.setHeader("X-Application-Error", e.getClass().getName());
			response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			logger.severe("Issue with read user's org feed " + e.toString());
			e.printStackTrace();
		} catch (SAXException e) {
			response.setHeader("X-Application-Error", e.getClass().getName());
			response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			logger.severe("Issue with read user's org feed " + e.toString());
		}

	}

	/**
	 * gets the user's organization's public files results
	 * 
	 * @param request
	 * @param response
	 */
	public void getPrivateFeed(String bearer, HttpServletRequest request,
			HttpServletResponse response) {

		String apiUrl = getPrivateFilesApiUrl();
		logger.info(apiUrl);

		Request get = Request.Get(apiUrl);
		get.addHeader("Authorization", "Bearer " + bearer);

		try {
			// sets the content type - application/json
			response.setContentType(ContentType.APPLICATION_JSON.getMimeType());

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

			// Default to SC_OK (200)
			else if (code == HttpStatus.SC_OK) {
				response.setStatus(HttpStatus.SC_OK);

				InputStream in = hr.getEntity().getContent();
				String jsonString = org.apache.wink.json4j.utils.XML.toJson(in);

				// Logging out the JSON Object
				logger.info(jsonString);

				JSONObject feed = new JSONObject(jsonString)
						.getJSONObject("feed");

				logger.info(feed.toString());

				JSONArray files = new JSONArray();

				JSONArray entries = feed.getJSONArray("entry");
				int len = entries.length();
				for (int i = 0; i < len; i++) {

					JSONObject entry = entries.getJSONObject(i);
					logger.info(entry.toString());

					JSONObject author = entry.getJSONObject("author");
					String photographer = author.getString("name");
					String uid = author.getString("userid");

					String title = entry.getJSONObject("title").getString(
							"content");

					String lid = entry.getString("libraryId");

					String pid = entry.getString("uuid");

					String thumbnail = "./api/file?action=file&pid=" + pid
							+ "&lid=" + lid;

					String share = "0";
					String like = "0";
					JSONArray rank = entry.getJSONArray("rank");

					@SuppressWarnings("rawtypes")
					Iterator r = rank.iterator();
					while (r.hasNext()) {
						JSONObject temp = (JSONObject) r.next();
						String scheme = temp.getString("scheme");
						if (scheme.contains("share")) {
							share = temp.getString("content");
						} else if (scheme.contains("recommendations")) {
							like = temp.getString("content");
						}
					}

					JSONObject e = createPhoto(lid, pid, title, uid,
							photographer, thumbnail);
					e.put("likes", like);
					e.put("shares", share);
					files.add(e);

				}

				// Flush the Object to the Stream with content type
				response.setHeader("Content-Type", "application/json");
				PrintWriter out = response.getWriter();
				out.println(files.toString());
				out.flush();

			}

		} catch (IOException e) {
			response.setHeader("X-Application-Error", e.getClass().getName());
			response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			logger.severe("Issue with read user's private feed " + e.toString());
		} catch (JSONException e) {
			response.setHeader("X-Application-Error", e.getClass().getName());
			response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			logger.severe("Issue with read user's private feed " + e.toString());
			e.printStackTrace();
		} catch (SAXException e) {
			response.setHeader("X-Application-Error", e.getClass().getName());
			response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			logger.severe("Issue with read user's private feed " + e.toString());
		}

	}

	/**
	 * gets the user's shared files results
	 * 
	 * @param request
	 * @param response
	 */
	public void getSharedFeed(String bearer, HttpServletRequest request,
			HttpServletResponse response) {

		String apiUrl = getSharedFiles();
		logger.info(apiUrl);

		Request get = Request.Get(apiUrl);
		get.addHeader("Authorization", "Bearer " + bearer);

		try {
			// sets the content type - application/json
			response.setContentType(ContentType.APPLICATION_JSON.getMimeType());

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

			// Default to SC_OK (200)
			else if (code == HttpStatus.SC_OK) {
				response.setStatus(HttpStatus.SC_OK);

				InputStream in = hr.getEntity().getContent();
				String jsonString = org.apache.wink.json4j.utils.XML.toJson(in);

				// Logging out the JSON Object
				logger.info(jsonString);

				JSONObject feed = new JSONObject(jsonString)
						.getJSONObject("feed");

				logger.info(feed.toString());

				JSONArray files = new JSONArray();

				JSONArray entries = feed.getJSONArray("entry");
				int len = entries.length();
				for (int i = 0; i < len; i++) {

					JSONObject entry = entries.getJSONObject(i);
					logger.info(entry.toString());

					JSONObject author = entry.getJSONObject("author");
					String photographer = author.getString("name");
					String uid = author.getString("userid");

					String title = entry.getJSONObject("title").getString(
							"content");

					String lid = entry.getString("libraryId");

					String pid = entry.getString("uuid");

					String thumbnail = "./api/file?action=file&pid=" + pid
							+ "&lid=" + lid;

					String share = "0";
					String like = "0";
					JSONArray rank = entry.getJSONArray("rank");

					@SuppressWarnings("rawtypes")
					Iterator r = rank.iterator();
					while (r.hasNext()) {
						JSONObject temp = (JSONObject) r.next();
						String scheme = temp.getString("scheme");
						if (scheme.contains("share")) {
							share = temp.getString("content");
						} else if (scheme.contains("recommendations")) {
							like = temp.getString("content");
						}
					}

					JSONObject e = createPhoto(lid, pid, title, uid,
							photographer, thumbnail);
					e.put("likes", like);
					e.put("shares", share);
					files.add(e);

				}

				// Flush the Object to the Stream with content type
				response.setHeader("Content-Type", "application/json");
				PrintWriter out = response.getWriter();
				out.println(files.toString());
				out.flush();

			}

		} catch (IOException e) {
			response.setHeader("X-Application-Error", e.getClass().getName());
			response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			logger.severe("Issue with read user's shared feed " + e.toString());
		} catch (JSONException e) {
			response.setHeader("X-Application-Error", e.getClass().getName());
			response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			logger.severe("Issue with read user's shared feed " + e.toString());
			e.printStackTrace();
		} catch (SAXException e) {
			response.setHeader("X-Application-Error", e.getClass().getName());
			response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			logger.severe("Issue with read user's shared feed " + e.toString());
		}

	}

	/**
	 * creates the JSON for a single photograph
	 * 
	 * @param libraryId
	 *            the library where the file is stored
	 * @param pid
	 *            identifier of the given file
	 * @param title
	 *            of the given file
	 * @param userId
	 *            of the person owning the file
	 * @param photographer
	 *            displayname of the file
	 * @param thumbnail
	 *            generated url for downloading file
	 * 
	 * @return {JSONObject}
	 * 
	 * @throws JSONException
	 */
	public JSONObject createPhoto(String libraryId, String pid, String title,
			String userId, String photographer, String thumbnail)
			throws JSONException {
		JSONObject json = new JSONObject();
		json.put("lid", libraryId);
		json.put("pid", pid);
		json.put("title", title);
		json.put("uid", userId);
		json.put("photographer", photographer);
		json.put("thumbnail", thumbnail);
		return json;
	}

}
