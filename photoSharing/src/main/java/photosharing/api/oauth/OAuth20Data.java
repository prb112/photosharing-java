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
package photosharing.api.oauth;

import java.io.Serializable;
import java.util.HashMap;

/**
 * OAuth20Data is a serializable class which stores the OAuth Data
 * The data is in the x-www-form-urlencoded format, and looks like: 
 * access_token=16
 * c7f772427f367be615ffaefd8293cff73646e246e1d094a63dd914c43b9e3efd84809378199815886d83a740
 * &refresh_token=5
 * b1e334d4de0b8e39c3dff8fd1c88dc8d9169fdbe7a0759b155dcd2b3a0479c47f5b
 * &issued_on=1399488937103&expires_in=7200000&token_type=Bearer
 * 
 * @author Paul Bastide <pbastide@us.ibm.com>
 * 
 */
public class OAuth20Data implements Serializable {

	/**
	 * generated serial version id
	 */
	private static final long serialVersionUID = 7528264765427082943L;

	// access_token The access token that is used as a bearer token to access
	// the protected resource, and is valid for two hours from time it is
	// granted. The maximum number of characters is 256.
	private String access_token = null;

	// refresh_token A long-lived refresh token that can be used to obtain a new
	// access token when the access token expires. The maximum number of
	// characters is 256.
	// Note: The value of the refresh token is confidential and should be
	// protected.
	private String refresh_token = null;

	// issued_on The details of when the access token was created. The created
	// timestamp is based in epochs.
	private String issued_on = null;

	// expires_in The amount of time in milliseconds that the access token is
	// valid.
	private String expires_in = null;

	// token_type The default value is Bearer.
	private String token_type = null;
	
	/**
	 * creates an unpopulated object representing OAuth20Data
	 */
	public OAuth20Data(){
		
	}
	
	/**
	 * creates a new object representing OAuth20Data
	 * 
	 * @param access_token
	 * @param refresh_token
	 * @param issued_on
	 * @param expires_in
	 * @param token_type
	 */
	public OAuth20Data(String access_token, String refresh_token,
			String issued_on, String expires_in, String token_type) {
		super();
		this.access_token = access_token;
		this.refresh_token = refresh_token;
		this.issued_on = issued_on;
		this.expires_in = expires_in;
		this.token_type = token_type;
	}
	
	/**
	 * creates a new object representing OAuth20Data from the responseData
	 * @param responseData
	 * @return a new instance of OAuth20Data
	 */
	public static OAuth20Data createInstance(String responseData){
		String[] params = responseData.split("&");
		
		// Build a temp map to avoid repeated if calls
		HashMap<String,String> paramMap = new HashMap<String,String>();
		for(String param : params){
			String[] paramValue = param.split("=");
			paramMap.put(paramValue[0],paramValue[1]);
		}
		
		// Creates a new instance
		OAuth20Data oData = new OAuth20Data(paramMap.get("access_token"), paramMap.get("refresh_token"),
				paramMap.get("issued_on"), paramMap.get("expires_in"), paramMap.get("token_type"));
		
		return oData;
	}

	/**
	 * @return the access token which is valid for 2 hours
	 */
	public String getAccessToken() {
		return access_token;
	}

	/**
	 * sets the given access token
	 * 
	 * @param access_token
	 */
	public void setAccessToken(String access_token) {
		this.access_token = access_token;
	}

	/**
	 * gets the refresh token
	 * 
	 * @return the binary data for the refresh token
	 */
	public String getRefreshToken() {
		return refresh_token;
	}

	/**
	 * sets the refresh token
	 * 
	 * @param refresh_token
	 */
	public void setRefreshToken(String refresh_token) {
		this.refresh_token = refresh_token;
	}

	/**
	 * gets the date in milliseconds that the refresh token was issued
	 * 
	 * @return milliseconds since epoch
	 */
	public String getIssuedOn() {
		return issued_on;
	}

	/**
	 * sets the issued on date for the refresh token
	 * 
	 * @param issued_on
	 */
	public void setIssuedOn(String issued_on) {
		this.issued_on = issued_on;
	}

	/**
	 * @return gets the time at which the refresh token expires
	 */
	public String getExpiresIn() {
		return expires_in;
	}

	/**
	 * sets the time in which the refresh token expires
	 * 
	 * @param expires_in
	 */
	public void setExpiresIn(String expires_in) {
		this.expires_in = expires_in;
	}

	/**
	 * @return the token type
	 */
	public String getTokenType() {
		return token_type;
	}

	/**
	 * sets the token type, it should only be bearer
	 * 
	 * @param token_type
	 */
	public void setTokenType(String token_type) {
		this.token_type = token_type;
	}
}
