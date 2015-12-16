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
package photosharing.api.base;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import photosharing.api.oauth.CallbackDefinition;

/**
 * Mock Class to return API content if there is no API available 
 * 
 * @author Paul Bastide <pbastide@us.ibm.com>
 *
 */
public class MockDefinition implements APIDefinition{

	// Logger
	private final static String className = MockDefinition.class.getName();
	private Logger logger = Logger.getLogger(className);
	
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
			logger.severe("IOException " + e.toString());
		}
	}
	
}
