# PhotoJava

This project provides a sample application that leverages the [IBM Connections Cloud APIs](https://developer.ibm.com/social/) to create a social photo sharing experience. The application can be deployed to IBM Bluemix or it can be deployed on it's own. 

[![Deploy to Bluemix](https://bluemix.net/deploy/button.png)](https://bluemix.net/deploy?repository=https://github.com/ibmcnxdev/photosharing-java.git/example/photoSharing)

# Dependencies

A listing of the major components used in the application.

###### Server and Package Management
* [IBM WebSphere Liberty](https://developer.ibm.com/wasdev/websphere-liberty/)

###### Javascript and CSS Libraries
* [Angular](https://github.com/angular/angular.js)
* [Bootstrap](https://github.com/twbs/bootstrap)
* [Justified-Gallery](https://github.com/miromannino/Justified-Gallery)

###### Maven Module Dependencies 
The following libraries are used in the project:
fluent-hc
wink-json4j
httpclient
httpmime
commons-io
was-liberty
commons-collections4

# Installation

*Prerequisite*: 
Ensure you have the latest version of [IBM WebSphere Liberty](https://developer.ibm.com/wasdev/websphere-liberty/) installed in your environment.

1. Clone the github repository.  
`git clone https://github.com/ibmcnxdev/photosharing-java.git`  

2. Move into the cloned directory.
`cd photosharing`  

3.  Update your Liberty server.xml 
`
	<basicRegistry id="basic" realm="BasicRealm"> 
		<!-- <user name="yourUserName" password="" />  -->
		<user password="{xor}DUMMYDATA==" name="paul" id="paul"/>
	</basicRegistry>
`

4. Add your web application to the Liberty Server

5. Update your server.xml for the webapplication, and add a binding for ALL_AUTHENTICATED_USERS.
`
	<webApplication id="photoSharing" location="photoSharing.war"
    	name="photoSharing">
    	<application-bnd>
    		<security-role name="Users">
    			<special-subject type="ALL_AUTHENTICATED_USERS" />
    		</security-role>
    	</application-bnd>
    </webApplication>
`   

6. Register your OAuth 2.0 Credentials with the call back `http://localhost:9080/photoSharing/api/callback` [OAuth Flow](https://www-10.lotus.com/ldd/appdevwiki.nsf/xpAPIViewer.xsp?lookupName=API+Reference#action=openDocument&res_title=OAuth_2.0_APIs_for_web_server_flow_sbt&content=apicontent)

7. Update your /WEB-INF/appconfig.properties file with the new credentials and URL for the environment you registered the credentials. 

8. Restart your Web Application

9. The PhotoSharing application is now running! Direct browser to `http://localhost:9080/photosharing`.  

# License

This code is licensed under Apache License v2.0. See the LICENSE file in the root directory of this repository for more details.
