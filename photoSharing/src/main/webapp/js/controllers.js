/*
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

/**
 * Angular Module - photoApp photoApp is the main module with dependencies as
 * identified
 */
var photoApp = angular.module('photoApp', [ 'ngSanitize', 'ngRoute',
		'ngAnimate', 'ngCookies', 'ui.bootstrap', 'infinite-scroll' ]);

/**
 * The controller for the Navigation Bar
 */
photoApp.controller('NavbarController', function($location, $scope, $rootScope,
		$http, $route, $routeParams, $cookies, $uibModal, $log, $window) {
	
	//Default State is not logged in, switch to true only during testing
	
	/*
	 * Checks to see if there is a Session based on the LtpaToken2
	 * If the cookie exists, the login is true, else false
	 */
	var cookie = $cookies.get('LtpaToken2');
	$log.debug("Cookie Details: " + cookie);
	if(cookie !== 'undefined'){
		$rootScope.loggedin = true;
	}else{
		$rootScope.loggedin = false;
		$location.url("");
	}
		
	$rootScope.state = 'public';

	$rootScope.loading = false;
	$rootScope.loadingHues = [ '#fdc162', '#7bb7fa', '#60d7a9', '#fd6a62' ];
	$rootScope.hueIndex = 0;

	$rootScope.buttons = [ ".profileButton", ".publicButton", ".privateButton",
			".personalButton", ".uploadButton" ];

	$scope.animationsEnabled = false;

	$scope.$on('$locationChangeStart', function(event, next, current) {
		$rootScope.loading = true;

		if ($rootScope.hueIndex > 2) {
			$rootScope.hueIndex = 0;
		} else {
			$rootScope.hueIndex++;
		}
		
		// Reset to login Page
		if(!$rootScope.loggedin){
			$location.url("");
		}
		
	});

	// Turns animation to the inverse state
	$scope.toggleAnimation = function() {
		$scope.animationsEnabled = !$scope.animationsEnabled;
	};

	// Default processing of the Resize
	$scope.mediumScreen = true;
	$(document).ready(function() {
		if ($(window).width() < 1218 || $(window).width() > 767) {
			$scope.mediumScreen = false;
		}
		if ($(window).width() >= 1218 || $(window).width() <= 767) {
			$scope.mediumScreen = true;
		}
		$scope.uploadSideA = $(window).width() >= 905;
		if ($(window).width() < 746) {
			$('#searchText').css('width', '100%');
		} else {
			$('#searchText').css('width', '100px');
		}
	});

	// Adds resize operation in order to set the hamburger menu
	$(window).resize(function() {
		if ($(window).width() < 1218 || $(window).width() > 767) {
			$scope.mediumScreen = false;
		}
		if ($(window).width() >= 1218 || $(window).width() <= 746) {
			$scope.mediumScreen = true;
		}
		$scope.uploadSideA = $(window).width() >= 905;
		if ($(window).width() < 746) {
			$('#searchText').css('width', '100%');
		} else {
			$('#searchText').css('width', '100px');
		}
		$scope.$digest();
	});

});

/**
 * Manages the navigation bar's buttons
 */
photoApp.controller('NavigationButtonController', function($location, $scope, $rootScope,
		$http, $route, $routeParams, $cookies, $uibModal, $log, $window) {
	
	//Sets the Default to logged out
	$rootScope.loggedin = false;
	
	//Logs in the User
	$scope.login = function(){
		$log.info("Logging in the User");
		$rootScope.loggedin = false;
		$location.url('/oauth');
	};
	
	//Logs out the User.  
	$scope.logout = function(){
		$log.info("Logging out the User and destroying the server session");
		$rootScope.loggedin = false;
		$location.url("");
		
		/**
		 * Calls the Logout Method
		 */
		$http({
			method: 'GET',
			url: "./logout"
		}).then(function success(response){
			if(status == 200){
				//Success, the logged in flip happened
			}
			else $log.debug(status);
		}, function error(response, status){
			$log.debug("User not logged in? " + status);
		});
	};
	
	//General Button Controller 
	$scope.select = function(button) {
		angular.forEach($rootScope.buttons, function(btn) {
			$(btn).css("animation", "");
			$(btn).css("box-shadow", "");
		});
		$(button).css("animation", "navSelected .5s forwards");
		
		if(button === ".personalButton"){
			$log.log("Private Button Clicked");
		} else if(button === ".publicButton"){
			$log.log("Public Button Clicked");
		}else if(button === ".profileButton"){
			$log.log("Profile Button Clicked");
		}else if(button === ".privateButton"){
			$log.log("Messages Button Clicked");
		}else{
			$log.log("NavigationButtonController: invalid click with " + button);
		}
		
	};
	
});

/**
 * Manages the navigation bar upload
 */
photoApp.controller('UploadController', function($location, $scope, $rootScope,
		$http, $route, $routeParams, $cookies, $uibModal, $log, $window) {
	
	//Changes the Selection 
	$scope.select = function(button) {
		angular.forEach($rootScope.buttons, function(btn) {
			$(btn).css("animation", "");
			$(btn).css("box-shadow", "");
		});
		$(button).css("animation", "navSelected .5s forwards");
	};
	
	//Logs in the User
	$scope.openUploadDialog = function(){
		$log.log("open dialog for upload photo");
		$location.url("/upload");
		
	};
		
});

/**
 * Manages the navigation bar search
 */
photoApp.controller('SearchController', function($location, $scope, $rootScope,
		$http, $route, $routeParams, $cookies, $uibModal, $log, $window) {
	
	$scope.peopleSearch = function(){
		$log.log("People Search");
		
	};
	
	$scope.search = function(){
		$log.log("Search");
	};
	
	$scope.goToProfile = function(id){
		$log.log("Moving to profile " + id);
	};
	
	/**
	 * watches the login status in order to clear out the $scope.searchQuery
	 */
	$scope.$watch('loggedin', function(){
		if(!$rootScope.loggedin){
			$log.log("changed logged in status to logged out");
			$scope.searchQuery = null;
		}
	});
		
});

/**
 * Manages the Login
 */
photoApp.controller('LoginController', function($location, $scope, $rootScope,
		$http, $route, $routeParams, $log) {
	
	// Login Function
	$scope.login = function(userx){
		//Logs details only if info is setup. 
		$log.info("Submitted Login Details");
		$log.info("- " + $scope.user.userloginname);
		$log.info("- " + $scope.user.userpassword);
		
		//Calls the api Login
		var url = "./login";
		var basicAuth = "Basic " + window.btoa($scope.user.userloginname + ":" + $scope.user.userpassword);
		var config = {
				method : "GET",
				headers : {
					'Authorization' :  basicAuth ,
					'X-Requested-With' : 'XMLHttpRequest'
				}
		};
		
		$http.get(url, config).then(function(response){
			$rootScope.loggedin = true;
			$location.url("/oauth");
		}, function(response){
			$log.info("issue logging in");
			// If there is an error, set the alert
			$scope.alert = true;
		});
		
	};
		
});

/**
 * launches the OAuth flow in a new window
 */
photoApp.controller('OAuthController', function($scope,$window,$http,$timeout,$cookies,$location,$log) {
	$scope.polling = function(){
		
		$log.warn("Cookie: " + $cookies.get('JSESSIONID'));
		
		var config = {
				method: "GET",
				withCredentials: true
		};
		
		$http.get('./api/poll',config).then(function(response){
			if(response.status == '200'){
				$location.url("/public");
			}
		}, function(error){
			if(error.status == '204'){
				$log.info("Waiting on Credentials");
				$timeout($scope.polling, 3000);
			}
		});
	};
	
	$scope.launch = function(){
		$window.open('./api/auth','newwindow','width=740, height=480');
		$timeout($scope.polling, 3000);
	};
		
});

/**
 * Configuration for the photoApp
 */
photoApp.config([ '$routeProvider', 
                  function($routeProvider, $locationProvider) {

	$routeProvider.when('/', {
		// Defaults to the Login View
		templateUrl : "views/login.html"
	}).when('/public', {
		templateUrl : "views/home.html"
	}).when('/private', {
		templateUrl : "views/private.html"
	}).when('/upload', {
		templateUrl : "views/modal-upload.html"
	}).when('/myphotos', {
		templateUrl : "views/photo.html"
	}).when('/oauth', {
		templateUrl : "views/oauth.html"
	}).otherwise({ //Defaults to / 
		redirectTo : "/"
	});
	
}]);

