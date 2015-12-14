package photosharing.api.base;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

/**
 * Mock Class to return API content if there is no API available 
 * 
 * @author Paul Bastide <pbastide@us.ibm.com>
 *
 */
public class MockDefinition implements APIDefinition{

	/**
	 * runs the mock api definition 
	 */
	@Override
	public void run(HttpServletRequest request, HttpServletResponse response) {
		InputStream is = MockDefinition.class.getResourceAsStream("feed.txt");
		try {
			response.setContentType("application/json");
			IOUtils.copy(is, response.getOutputStream());
			IOUtils.closeQuietly(is);
			IOUtils.closeQuietly(response.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
