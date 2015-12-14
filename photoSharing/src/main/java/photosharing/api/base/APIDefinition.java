/**
 * 
 */
package photosharing.api.base;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * API Definition 
 * 
 * @author Paul Bastide <pbastide@us.ibm.com>
 *
 */
public interface APIDefinition {
	
	/**
	 * runs the API with the given request 
	 * 
	 * @param uri
	 * @param method
	 */
	public void run(HttpServletRequest request, HttpServletResponse response);
}
