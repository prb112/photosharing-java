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
package photosharing.api;

import java.util.HashMap;
import java.util.logging.Logger;

import photosharing.api.base.APIDefinition;
import photosharing.api.base.ImageDefinition;
import photosharing.api.base.MockDefinition;
import photosharing.api.bss.LogoutDefinition;
import photosharing.api.conx.CommentsDefinition;
import photosharing.api.conx.ProfileDefinition;
import photosharing.api.conx.RecommendationDefinition;
import photosharing.api.conx.SearchPeopleDefinition;
import photosharing.api.conx.SearchTagsDefinition;
import photosharing.api.conx.UploadFileDefinition;
import photosharing.api.oauth.CallbackDefinition;
import photosharing.api.oauth.OAuthDefinition;
import photosharing.api.oauth.PollingDefinition;

/**
 * API Registry to isolate the how in each call 
 * @author Paul Bastide <pbastide@us.ibm.com>
 * 
 */
public class ApiRegistry {
	
	//Logger
	private static String className = ApiRegistry.class.getName();
	private static Logger logger = Logger.getLogger(className);
	
	//Defintions
	private static HashMap<String,APIDefinition> definitions = new HashMap<String,APIDefinition>();
	
	/**
	 * API Definitions
	 */
	static{
		definitions.put("*", (APIDefinition)new MockDefinition());
		definitions.put("/auth", (APIDefinition) new OAuthDefinition());
		definitions.put("/poll", (APIDefinition) new PollingDefinition());
		definitions.put("/logout", (APIDefinition) new LogoutDefinition());
		definitions.put("/image*", (APIDefinition) new ImageDefinition());
		definitions.put("/searchPeople", (APIDefinition) new SearchPeopleDefinition());
		definitions.put("/searchTags", (APIDefinition) new SearchTagsDefinition());	
		definitions.put("/profile", (APIDefinition) new ProfileDefinition());
		definitions.put("/comments", (APIDefinition) new CommentsDefinition());
		definitions.put("/like", (APIDefinition) new RecommendationDefinition());
		definitions.put("/upload", (APIDefinition) new UploadFileDefinition());
		definitions.put("/callback", (APIDefinition) new CallbackDefinition());
	}
	
	/**
	 * hides the constructor
	 */
	private ApiRegistry(){
		
	}
	
	/**
	 * finds the best API based on method and URI
	 * 
	 * @param method
	 * @param uri
	 * @return {APIDefinition}
	 */
	public static APIDefinition getRegistry(String uri){
		APIDefinition defintion = definitions.get(uri);
		
		//If no API exists, it returns a MockAPI
		if(defintion == null){
			defintion = definitions.get("*");
		}
		
		//Logs out the Class Name, if Finest is set
		logger.finest(defintion.getClass().getName());
		
		if(uri.contains("/image/")){
			defintion = definitions.get("/image*");
		}
				
		return defintion;
	}
}
