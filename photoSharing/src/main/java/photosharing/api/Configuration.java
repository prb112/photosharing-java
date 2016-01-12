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
package photosharing.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

/**
 * Manages the Configuration
 * @author Paul Bastide <pbastide@us.ibm.com>
 */
public class Configuration {
	
	// Logger
	private final static String className = Configuration.class.getName();
	private Logger logger = Logger.getLogger(className);
	
	/**
	 * Configuration Property Names are exposed so consumers can get the values of the given properties
	 */
	public final static String HOSTNAME = "HOSTNAME";
	public final static String BASEURL = "URL";
	public final static String CLIENTID = "ClientId";
	public final static String CLIENTSECRET = "ClientSecret";
	public final static String CALLBACKURL = "CallbackUrl";
	public final static String TRUST = "TRUST";
	
	// App Config
	public final static String APPCONFIG = "AppConfig";
	
	// Single Instance
	private static Configuration config = null;
	
	// Properties File read from the ContextParameter
	private Properties props = null;
	
	/**
	 * constructs the single configuration object
	 * @param request - only activated for one request during the lifecycle of the application
	 */
	private Configuration(HttpServletRequest request){
		ServletContext ctx = request.getServletContext();
		String param = ctx.getInitParameter(APPCONFIG);
		
		InputStream is = ctx.getResourceAsStream(param);
		props = new Properties();
		try {
			props.load(is);
		} catch (IOException e) {
			logger.severe("Issue loading the configuration " + e.toString());
		}
	}
	
	/**
	 * gets a single instance of the configuration object
	 * @param request can be null or a given request object
	 * @return the single Configuration object
	 */
	public static Configuration getInstance(HttpServletRequest request){
		if(config == null){
			config = new Configuration(request);
		}
		return config;
	}
	
	/**
	 * gets the configuration value based on a name
	 * @param name
	 * @return the property or null
	 */
	public String getValue(String name){
		return props.getProperty(name);
		
	}
}