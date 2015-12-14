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
