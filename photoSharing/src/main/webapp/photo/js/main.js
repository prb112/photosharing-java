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
	$rootScope.loggedin = false;
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
	
	//Logs in the User
	$scope.login = function(){
		//if testing, $rootScope.loggedin = true;
		$rootScope.loginDialog = true;
	};
	
	//Logs out the User.  
	$scope.logout = function(){
		$rootScope.loggedin = false;
	};
	
	//General Button Controller 
	$scope.select = function(button) {
		angular.forEach($rootScope.buttons, function(btn) {
			$(btn).css("animation", "");
			$(btn).css("box-shadow", "");
		});
		$(button).css("animation", "navSelected .5s forwards");
		
		if(button === ".personalButton"){
			console.log("Private Button Clicked");
		} else if(button === ".publicButton"){
			console.log("Public Button Clicked");
		}else if(button === ".profileButton"){
			console.log("Profile Button Clicked");
		}else if(button === ".privateButton"){
			console.log("Messages Button Clicked");
		}else{
			console.log("NavigationButtonController: invalid click with " + button);
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
		console.log("open dialog for upload photo");
		
	};
		
});

/**
 * Manages the navigation bar search
 */
photoApp.controller('SearchController', function($location, $scope, $rootScope,
		$http, $route, $routeParams, $cookies, $uibModal, $log, $window) {
	
	$scope.peopleSearch = function(){
		console.log("People Search");
		
	};
	
	$scope.search = function(){
		console.log("Search");
	};
	
	$scope.goToProfile = function(id){
		console.log("Moving to profile " + id);
	};
	
	/**
	 * watches the login status in order to clear out the $scope.searchQuery
	 */
	$scope.$watch('loggedin', function(){
		if(!$rootScope.loggedin){
			console.log("changed logged in status to logged out");
			$scope.searchQuery = null;
		}
	});
		
});

/**
 * Manages 
 */
photoApp.controller('LoginModalDialogController', function($location, $scope, $rootScope,
		$http, $route, $routeParams, $cookies, $uibModal, $log, $window) {
	 $scope.items = ['item1', 'item2', 'item3'];

	  $scope.animationsEnabled = true;

	  /**
	   * launches the loginDialog when the loginDialog value changes 
	   */
	  $scope.$watch('loginDialog', function(){
		
		  var modalInstance = $uibModal.open({
		      animation: $scope.animationsEnabled,
		      templateUrl: './views/login-template.html',
		      controller: 'ModalInstanceController',
		      size: sm,
		      
		    });

		    modalInstance.result.then(function (selectedItem) {
		      $scope.selected = selectedItem;
		    }, function () {
		      $log.info('Modal dismissed at: ' + new Date());
		    });
		  
	  });
	  
	  $scope.toggleAnimation = function () {
	    $scope.animationsEnabled = !$scope.animationsEnabled;
	  };
});

photoApp.controller('ModalInstanceController', function ($scope, $uibModalInstance, items) {
	  $scope.selected = {
	    item: $scope.items[0]
	  };

	  $scope.ok = function () {
	    $uibModalInstance.close($scope.selected.item);
	  };

	  $scope.cancel = function () {
	    $uibModalInstance.dismiss('cancel');
	  };
});











