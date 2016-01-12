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

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;

import org.apache.http.client.fluent.Executor;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;

/**
 * utility that generates a custom executor which sets the SSL Trust
 * 
 * @author Paul Bastide <pbastide@us.ibm.com>
 */
public class ExecutorUtil {

	// Logger
	private static String className = ExecutorUtil.class.getName();
	private static Logger logger = Logger.getLogger(className);

	/**
	 * helper method that returns an HTTPClient executor with credentials
	 * available.
	 * 
	 * Also enables the test case to connect to ANY SSL Certificate
	 * valid/invalid
	 * 
	 * @return {Executor} or Null if there is an issue
	 */
	public static Executor getExecutor(){
    	Executor executor = null;
    	        
        /*
         * if using one of the environments without a trusted CA chain or
         * you are using Fiddler, you want to set TRUST=TRUE in appconfig.properties
         */
        Configuration config = Configuration.getInstance(null);
        String sTrust = config.getValue(Configuration.TRUST);
        boolean trusted = Boolean.parseBoolean(sTrust);
        if (trusted) {
        	try{
        	HttpClientBuilder builder = HttpClients.custom();
        	
            // Setup the SSL Context to Trust Any SSL Certificate
            SSLContextBuilder sslBuilder = new SSLContextBuilder();
            sslBuilder.loadTrustMaterial(null, new TrustStrategy() {
                /**
                 * override for fiddler proxy
                 */
                public boolean isTrusted(X509Certificate[] certs, String host)
                        throws CertificateException {
                    return true;
                }
            });
            SSLContext sslContext = sslBuilder.build();
            builder.setHostnameVerifier(new AllowAllHostnameVerifier());
            builder.setSslcontext(sslContext);
            
            CloseableHttpClient httpClient = builder.build();
            executor = Executor.newInstance(httpClient);
        	}catch (NoSuchAlgorithmException e) {
				logger.log(Level.SEVERE,"Issue with No Algorithm " + e.toString());
			} catch (KeyStoreException e) {
				logger.log(Level.SEVERE,"Issue with KeyStore " + e.toString());
			} catch (KeyManagementException e) {
				logger.log(Level.SEVERE,"Issue with KeyManagement  " + e.toString());
			}
        }
        
        return executor;
    }
}
