package photosharing.api;

/**
 * Manages the Configuration
 * @author Paul Bastide <pbastide@us.ibm.com>
 */
public class Configuration {
	
	public final static String SERVER = "SERVER";
	
	/**
	 * gets the configuration value based on a name
	 * 
	 * @param name
	 * @return
	 */
	public static String getConfigurationValue(String name){
		return "apps.na.collabserv.com";
		
	}
}
