package photosharing.api.bss;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import photosharing.api.base.APIDefinition;

/**
 * Manages the login using OAuth 2.0 
 * 
 * @author Paul Bastide <pbastide@us.ibm.com>
 */
public class LoginDefinition implements APIDefinition {

	
	
	//TODO: Generate the base URL based on a configuration 
	public static final String accessUrl = "";
	public static final String tokenUrl = "";
	
	/**
	 * runs the login
	 */
	@Override
	public void run(HttpServletRequest request, HttpServletResponse response) {
 
	}

}
