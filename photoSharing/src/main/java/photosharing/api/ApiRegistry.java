/**
 * 
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
		
		if(defintion == null){
			defintion = definitions.get("*");
		}
		
		logger.info(defintion.getClass().getName());
		
		if(uri.contains("/image/")){
			defintion = definitions.get("/image*");
		}
				
		return defintion;
	}
}
