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

import java.io.IOException;
import java.security.Principal;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpStatus;

/**
 * Servlet implementation class LoginServlet
 * 
 * @author Paul Bastide <pbastide@us.ibm.com>
 */
@WebServlet({"/login" })
public class LoginServlet extends HttpServlet {
	private static final long serialVersionUID = 9999L;
       
    /**
     * constructor for the Auth Servlet 
     * @see HttpServlet#HttpServlet()
     */
    public LoginServlet() {
        super();
    }

	/**
	 * intializes the servlet
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
				 
	}

	/**
	 * Manages the authorization for a given user, creates a session or returns session invalid
	 * 
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		/*
		 * Checks to see if the User is logged in forces logout for any existing user, you wouldn't actually do this in production
		 */
		Principal user = request.getUserPrincipal();
		if(user != null){
			HttpSession session = request.getSession(false);
			if(session != null){
				session.invalidate();
			}
			
			request.logout();
			
		}
		
		/*
		 * Authorizes the User
		 */
		String auth = request.getHeader("Authorization");
		
		if(auth != null && !auth.isEmpty() ){
			auth = auth.replace("Basic ","");
			
			String authDecoded = new String(Base64.decodeBase64(auth));
			
			String[] creds = authDecoded.split(":");
			String username = creds[0];
			String password = creds[1];
			try{
				request.login(username, password);
				request.getSession(true);
			}catch(Exception e){
				response.setStatus(HttpStatus.SC_UNAUTHORIZED);
			}
						
			
		}else{
			response.setStatus(HttpStatus.SC_BAD_REQUEST);
		}
		
		
	}
}
