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

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import photosharing.api.base.APIDefinition;

/**
 * Servlet implementation class APIServlet
 * 
 * @author Paul Bastide <pbastide@us.ibm.com>
 */
@WebServlet({"/api/*" })
public class APIServlet extends HttpServlet {
	private static final long serialVersionUID = 9999L;
       
    /**
     * constructor for the API Servlet 
     * @see HttpServlet#HttpServlet()
     */
    public APIServlet() {
        super();
    }

	/**
	 * intializes the servlet
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
				 
	}

	/**
	 * queries the registry for an API definition, and runs the definition for the given request and response
	 * REST request via GET method
	 * 
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		APIDefinition definition = ApiRegistry.getRegistry(request.getPathInfo());
		definition.run(request, response);
	}

	/**
	 * queries the registry for an API definition, and runs the definition for the given request and response
	 * REST request via POST method
	 * 
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		APIDefinition definition = ApiRegistry.getRegistry(request.getPathInfo());
		definition.run(request, response);
	}

	/**
	 * queries the registry for an API definition, and runs the definition for the given request and response
	 * REST request via PUT method
	 * 
	 * @see HttpServlet#doPut(HttpServletRequest, HttpServletResponse)
	 */
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		APIDefinition definition = ApiRegistry.getRegistry(request.getPathInfo());
		definition.run(request, response);
	}

	/**
	 * queries the registry for an API definition, and runs the definition for the given request and response
	 * REST request via DELETE method
	 * 
	 * @see HttpServlet#doDelete(HttpServletRequest, HttpServletResponse)
	 */
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		APIDefinition definition = ApiRegistry.getRegistry(request.getPathInfo());
		definition.run(request, response);
	}

}
