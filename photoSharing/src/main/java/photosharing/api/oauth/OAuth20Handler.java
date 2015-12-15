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

/**
 * The API is described at: 
 * <a href="http://ibm.co/1WOTZni">OAuth 2.0 APIs for web server flow</a>
 */
public class OAuth20Handler {
	
	private static OAuth20Handler _handler;
	
	/**
	 * private constructor
	 */
	private OAuth20Handler(){
		
	}
	
	/**
	 * gets the single instance of the OAuth20 Handler
	 * @return {OAuth20Handler} single instance of handler
	 */
	public static OAuth20Handler getInstance(){
		if(_handler == null){
			_handler = new OAuth20Handler();
		}
		return _handler;
	}
	
	
	public OAuth20Data getAccessToken(){
		return new OAuth20Data();
	}
	
	public OAuth20Data renewAccessToken(){
		return new OAuth20Data();
	}
}
