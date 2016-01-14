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

/*
 * Controller that lays out the profile page
 */
photoApp.controller('XProfileController',
		function($scope, $window, $http, $timeout, $cookies, $location, $log,
				$rootScope, $routeParams, $animate) {

			angular.forEach($rootScope.buttons, function(btn) {
				$(btn).css("animation", "");
				$(btn).css("box-shadow", "");
			});

			$(".profileButton").css("box-shadow", "0px 2px 0px #004266");

			// $("#profileGalleryx").justifiedGallery();
			var galleryConfig = {
				rowHeight : window.screen.height * .25,
				lastRow : 'nojustify',
				margins : 10,
				captions : true,
				waitThumbnailsLoad : false
			};

			angular.element(document).ready(
					function() {
						var $profileGallery = $('#profileGalleryx');
						$profileGallery.justifiedGallery(galleryConfig).on(
								'jg.complete', function(e) {
									$scope.loading = false;
								});
						$profileGallery.removeClass('hidden');
						$profileGallery.addClass('resetOverflow');
					});


			// Updates to Profiles
			$scope.profile = {
				displayName : "",
				avatar : "",
			};


			$scope.data = [{"uid":"20971118","thumbnail":".\/api\/file?action=thumbnail&pid=7fdedc74-a9f4-46f1-acde-39bef9975847&lid=2597409c-b292-4059-bb4f-3c92c90f5c2e","lid":"2597409c-b292-4059-bb4f-3c92c90f5c2e","pid":"7fdedc74-a9f4-46f1-acde-39bef9975847"
				,"photographer":"ASIC ASIC","title":"Test32ab.jpeg"},{"uid":"20971118","thumbnail":".\/api\/file?action=thumbnail&pid=a9046490-dc17-43c1-8efb-c443c31a183c&lid=2597409c-b292-4059-bb4f-3c92c90f5c2e","lid":"2597409c-b292-4059-bb4f-3c92c90f5c2e"
				,"pid":"a9046490-dc17-43c1-8efb-c443c31a183c","photographer":"ASIC ASIC","title":"Test.png"},{"uid":"20971118","thumbnail":".\/api\/file?action=thumbnail&pid=987d9dc7-251e-41ee-b986-8e3329a4f47b&lid=2597409c-b292-4059-bb4f-3c92c90f5c2e"
				,"lid":"2597409c-b292-4059-bb4f-3c92c90f5c2e","pid":"987d9dc7-251e-41ee-b986-8e3329a4f47b","photographer"
				:"ASIC ASIC","title":"Test3.png"}]; 
				
				//[{"uid":"20971118","thumbnail":"http://raw.githubusercontent.com/miromannino/Justified-Gallery/master/test/photos/6791628438_affaa19e10.jpg","pid":"7fdedc74-a9f4-46f1-acde-39bef9975847","photographer":"ASIC ASIC","title":"Test32ab.jpeg"},{"uid":"20971118","thumbnail":"http://raw.githubusercontent.com/miromannino/Justified-Gallery/master/test/photos/6791628438_affaa19e10.jpg","lid":"2597409c-b292-4059-bb4f-3c92c90f5c2e","pid":"a9046490-dc17-43c1-8efb-c443c31a183c","photographer":"ASIC ASIC","title":"Test.png"},{"uid":"20971118","thumbnail":"http://raw.githubusercontent.com/miromannino/Justified-Gallery/master/test/photos/6791628438_affaa19e10.jpg","lid":"2597409c-b292-4059-bb4f-3c92c90f5c2e","pid":"987d9dc7-251e-41ee-b986-8e3329a4f47b","photographer":"ASIC ASIC","title":"Test3.png"}];
			
		});


/**
 * The controller for the Navigation Bar
 */
photoApp.controller('NavbarController', function($location, $scope, $rootScope,
		$http, $route, $routeParams, $cookies, $uibModal, $log, $window) {

	/*
	 * Controls the ability to make calls without having existing oauth
	 * credentials
	 */
	$rootScope.oauth = false;

	// Default State is not logged in, switch to true only during testing

	/*
	 * Checks to see if there is a Session based on the LtpaToken2 If the cookie
	 * exists, the login is true, else false
	 */
	var cookie = $cookies.get('LtpaToken2');
	$log.debug("Cookie Details: " + cookie);
	if (cookie !== 'undefined') {
		$rootScope.loggedin = true;
	} else {
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
		if (!$rootScope.loggedin) {
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
photoApp.controller('NavigationButtonController', function($location, $scope,
		$rootScope, $http, $route, $routeParams, $cookies, $uibModal, $log,
		$window) {

	// Sets the Default to logged out
	$rootScope.loggedin = false;

	// Logs in the User
	$scope.login = function() {
		$log.info("Logging in the User");
		$rootScope.loggedin = false;
		$location.url('/oauth');
	};

	// Logs out the User.
	$scope.logout = function() {
		$log.info("Logging out the User and destroying the server session");
		$rootScope.loggedin = false;
		$location.url("");

		/**
		 * Calls the Logout Method
		 */
		$http({
			method : 'GET',
			url : "./logout"
		}).then(function success(response) {
			if (status == 200) {
				// Success, the logged in flip happened
			} else
				$log.debug(status);
		}, function error(response, status) {
			$log.debug("User not logged in? " + status);
		});
	};

	// General Button Controller
	$scope.select = function(button) {
		angular.forEach($rootScope.buttons, function(btn) {
			$(btn).css("animation", "");
			$(btn).css("box-shadow", "");
		});
		$(button).css("animation", "navSelected .5s forwards");

		if (button === ".personalButton") {
			$log.log("Private Button Clicked");
		} else if (button === ".publicButton") {
			$log.log("Public Button Clicked");
		} else if (button === ".profileButton") {
			$log.log("Profile Button Clicked");
			$location.url("/profile/200000");

		} else if (button === ".privateButton") {
			$log.log("Messages Button Clicked");
		} else {
			$log
					.log("NavigationButtonController: invalid click with "
							+ button);
		}

	};

});

/**
 * Manages the navigation bar upload
 */
photoApp
		.controller(
				'UploadController',
				function($location, $scope, $rootScope, $http, $route,
						$routeParams, $cookies, $uibModal, $log, $window,
						$rootScope) {

					/**
					 * Defines the Data to Upload
					 */
					$scope.uploadModel = {
						visibility : 'public',
						appliedTags : [ 'photojava' ],
						// items : items,
						shares : '',
						tags : '',
						select : {
						// item : $scope.uploadModel.items[0]
						},
						file : "",
						appliedPeople : []
					};

					/**
					 * monitors the file and grabs it
					 */
					$scope.fileNameChanged = function(filedata) {

						var file = document.querySelector('input[type=file]').files[0];
						var reader = new FileReader();
						reader.onload = function() {
							$scope.preview = reader.result;
							$scope.uploadModel.file = reader.result;
							$scope.$apply();
						};
						reader.readAsDataURL(file);
					};

					/**
					 * add tag from typeahead
					 */
					$scope.addTag = function(index) {
						$scope.uploadModel.appliedTags
								.push($scope.uploadModel.tagsList[index].name);
						$scope.uploadModel.tags = '';
						$scope.uploadModel.tagsList = [];
					};

					/**
					 * checks given keypress
					 */
					$scope.checkPress = function(event, context) {
						// Checks the maximum size of the title
						if (context == 'title' && $scope.uploadModel.title) {
							if ($scope.uploadModel.title.length > 38) {
								event.preventDefault();
								$scope.message = "Too long of a title";
							}
						}

						// Checks the maximum length of a tag
						if (context == 'tags' && $scope.uploadModel.tags) {
							if ($scope.uploadModel.tags.length > 20) {
								event.preventDefault();
								$scope.message = "Too long of a tag name";
							}
						}

						// If Keycode is one of the following types, then add it
						// to tag list
						// Key Code
						// > ENTER (13)
						// > SPACE (32)
						if (event.keyCode == 13 || event.keyCode == 32) {

							if (context == 'tags') {

								$log.debug("Tags-" + $scope.uploadModel.tags
										+ "-");

								if ($scope.uploadModel.appliedTags
										.indexOf($scope.uploadModel.tags) == -1
										&& $scope.uploadModel.tags != null) {
									var tag = $scope.uploadModel.tags;
									tag = tag.replace(" ", "");
									$scope.uploadModel.appliedTags.push(tag);
								}

								clearTimeout($scope.lastSent);
								$scope.uploadModel.tags = null;
								$scope.uploadModel.tagsList = [];
							}
							// -> Requires Checks
							else if (context == 'people') {
								$scope.addPerson(0);
							}
						}
					};

					// Adds a person from drop down list
					$scope.addPerson = function(index) {
						var person = $scope.peopleList[index];
						$log.log(index + " " + person.name);
						if ($scope.uploadModel.appliedPeople.indexOf(person) == -1) {
							person.name = person.name;
							$scope.uploadModel.appliedPeople.push(person);
						}
						$scope.uploadModel.people = '';
						$scope.peopleList = [];
					};

					// Removes the Person from the index
					$scope.removePerson = function(index) {
						$scope.uploadModel.appliedPeople.splice(index, 1);
					};

					// People Search queries
					$scope.peopleSearch = function() {
						var people = $scope.uploadModel.people;
						if (people == '') {
							$scope.peopleList = [];
						} else {
							$http
									.get('./api/searchPeople?q=' + people)
									.then(
											function(response) {
												$scope.peopleList = response.data.persons;
											});
						}
					};

					// Executed to get the list / data - initiateSearch
					$scope.initiateSearch = function() {
						if ($scope.uploadModel.tags.length > 0) {

							$http
									.get(
											"./api/searchTags?q="
													+ $scope.uploadModel.tags)
									.then(
											function(response) {
												$scope.uploadModel.tagsList = response.data.items;
												// $log.log(response);
												// $log.log($scope.tagsList);
											});
						}
					};

					$scope.search = function() {
						if ($scope.tags == '') {
							clearTimeout($scope.lastSent);
							$scope.tagsList = [];
							$scope.$digest();
						}
						clearTimeout($scope.lastSent);
						$scope.lastSent = setTimeout(function() {
							$scope.initiateSearch($scope.tags);
						}, 100);
					};

					// Removes a Given Tag
					$scope.removeTag = function(index) {
						$scope.uploadModel.appliedTags.splice(index, 1);
					};

					// Changes the Selection
					$scope.select = function(button) {
						angular.forEach($rootScope.buttons, function(btn) {
							$(btn).css("animation", "");
							$(btn).css("box-shadow", "");
						});
						$(button).css("animation", "navSelected .5s forwards");
					};

					$scope.openUploadDialog = function() {
						$log.log("open dialog for upload photo");
						$location.url("/upload");

					};

					// Returns back to Public when canceled
					$scope.cancel = function() {
						$location.url("/public");
					};

					// Upload File
					$scope.uploadFile = function() {
						/*
						 * Sets the Spinner Icons during the upload
						 */
						$scope.message = '';
						$('#uploadButton').attr('disabled', '');
						$('#uploadText').css('display', 'none');
						$('#uploadSpinner').css('display', 'inline');

						var url = './api/upload?visibility='
								+ $scope.uploadModel.visibility;

						// Shares
						if ($scope.uploadModel.appliedPeople != '') {
							var shares = [];
							angular.forEach($scope.uploadModel.appliedPeople,
									function(person) {
										shares.push(person.id);
									});
							url += '&share=' + shares.join();
						}

						// Applied Tags
						if ($scope.uploadModel.appliedTags) {
							url += '&q='
									+ $scope.uploadModel.appliedTags.join();
						}

						// Title
						url += '&title=' + $scope.uploadModel.title;

						// Caption
						if ($scope.uploadModel.caption) {
							url += '&caption=' + $scope.uploadModel.caption;
						}

						// Grabs the Base64 coded image file
						var data = $scope.uploadModel.file.split(",");
						var body = data[1];

						// Cleans up the content type
						var contentType = data[0];
						contentType = contentType.replace("data:", "");
						contentType = contentType.replace(";base64", "");

						// Executes only if the OAuth Credentials are retrieved.
						if ($rootScope.oauth) {
							$http
									.post(url, body, {
										headers : {
											'Content-Type' : contentType,
											'X-Content-Length' : body.length
										},
										transformRequest : angular.identity
									})
									.success(function(data, status) {

										$location.url('/public');

									})
									.error(
											function(data, status) {
												/*
												 * Resets the Spinner Icons
												 * during the upload
												 */
												if (status == 409) {
													$scope.message = 'You already have a photo with this name; please select another name.';

												} else {
													var exception = data
															.headers('X-Application-Error');
													$scope.message = 'Issue with upload '
															+ exception;

												}
												$('#uploadButton').removeAttr(
														'disabled');
												$('#uploadText').css('display',
														'inline');
												$('#uploadSpinner').css(
														'display', 'none');
											});
						}

					};

				});

/**
 * Manages the navigation bar search
 */
photoApp.controller('SearchController', function($location, $scope, $rootScope,
		$http, $route, $routeParams, $cookies, $uibModal, $log, $window) {

	$scope.peopleSearch = function() {
		$log.log("People Search");

	};

	$scope.search = function() {
		$log.log("Search");
	};

	$scope.goToProfile = function(id) {
		$log.log("Moving to profile " + id);
	};

	/**
	 * watches the login status in order to clear out the $scope.searchQuery
	 */
	$scope.$watch('loggedin', function() {
		if (!$rootScope.loggedin) {
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
	$scope.login = function(userx) {
		// Logs details only if info is setup.
		$log.info("Submitted Login Details");
		$log.info("- " + $scope.user.userloginname);
		$log.info("- " + $scope.user.userpassword);

		// Calls the api Login
		var url = "./login";
		var basicAuth = "Basic "
				+ window.btoa($scope.user.userloginname + ":"
						+ $scope.user.userpassword);
		var config = {
			method : "GET",
			headers : {
				'Authorization' : basicAuth,
				'X-Requested-With' : 'XMLHttpRequest',
			}
		};

		$http.get(url, config).then(function(response) {
			$rootScope.loggedin = true;
			$location.url("/oauth");
		}, function(response) {
			$log.info("issue logging in");
			// If there is an error, set the alert
			$scope.alert = true;
		});

	};

});

/**
 * launches the OAuth flow in a new window
 */
photoApp.controller('OAuthController', function($scope, $window, $http,
		$timeout, $cookies, $location, $log, $rootScope) {
	$scope.polling = function() {

		// Only logs if debug is configured
		$log.debug("Cookie: " + $cookies.get('JSESSIONID'));

		var config = {
			method : "GET",
			withCredentials : true
		};

		$http.get('./api/poll', config).then(function(response) {
			if (response.status == '200') {
				$location.url("/public");
				$rootScope.oauth = true;

				// Gets the User's Details
				var apiurl = './api/profile?uid=self';
				$http.get(apiurl).then(function(response) {
					$rootScope.displayName = response.data.name;

					/*
					 * Retrieves the Image Profile
					 */
					var image = response.data.img;
					var imgSrc = "./api/photo?" + image.split("?")[1];
					$rootScope.avatar = imgSrc;

					$rootScope.userid = response.data.userid;

					$scope.$digest();
				}, function(response) {
					$log.log("Data " + response.data);
					$log.log("status " + response.status);
				});

			} else if (response.status == '204') {
				$log.info("Waiting on Credentials");
				$timeout($scope.polling, 3000);
			}
		}, function(error) {
			$log.info("Issue getting the credentials and failing silently");
		});
	};

	$scope.launch = function() {
		$window.open('./api/auth', 'newwindow', 'width=740, height=480');
		$timeout($scope.polling, 3000);
	};

});

/*
 * Controller that lays out the profile page
 */
photoApp.controller('XProfileController2',
		function($scope, $window, $http, $timeout, $cookies, $location, $log,
				$rootScope, $routeParams, $animate) {

			angular.forEach($rootScope.buttons, function(btn) {
				$(btn).css("animation", "");
				$(btn).css("box-shadow", "");
			});

			$(".profileButton").css("box-shadow", "0px 2px 0px #004266");

			// $("#profileGalleryx").justifiedGallery();
			var galleryConfig = {
				rowHeight : window.screen.height * .25,
				lastRow : 'nojustify',
				margins : 10,
				captions : true,
				waitThumbnailsLoad : false
			};

			var $profileGallery = $('#profileGalleryx');
			$profileGallery.justifiedGallery(galleryConfig);

			/**
			 * loads more
			 */
			$scope.loadMore = function() {
				// TODO Needs to be Reimplemented
			};

			var userid = $routeParams.userid;

			// Updates to Profiles
			$scope.profile = {
				displayName : "",
				avatar : "",
			};

			// Gets the User's Details
			var apiurl = './api/profile?uid=' + userid;
			$http.get(apiurl).then(function(response) {
				$scope.profile.displayName = response.data.name;

				/*
				 * Retrieves the Image Profile
				 */
				var image = response.data.img;
				var imgSrc = "./api/photo?" + image.split("?")[1];
				$scope.profile.avatar = imgSrc;

			}, function(response) {
				$log.log("Data " + response.data);
				$log.log("status " + response.status);
			});

			// http://localhost:9080/photoSharing/api/file?action=userlibrary&userid=20971118

			var apiurlx = './api/file?action=userlibrary&userid=' + userid;
			$http.get(apiurlx).then(function(response) {
				$scope.data = response.data;

			}, function(response) {
				$log.log("Data " + response.data);
				$log.log("status " + response.status);
			});
		});

/**
 * Configuration for the photoApp
 */
photoApp.config([ '$routeProvider', '$locationProvider',
		function($routeProvider, $locationProvider) {

			$locationProvider.html5Mode(false);

			$routeProvider.when('/', {
				// Defaults to the Login View
				title : "Login",
				templateUrl : "views/login.html"
			}).when('/public', {
				title : "Public Photos",
				templateUrl : "views/home.html"
			}).when('/private', {
				title : "Private Photos",
				templateUrl : "views/private.html"
			}).when('/upload', {
				title : "Upload Photos",
				templateUrl : "views/modal-upload.html"
			}).when('/myphotos', {
				title : "My Photos",
				templateUrl : "views/photo.html"
			}).when('/oauth', {
				title : "Connect with IBM Connections Cloud",
				templateUrl : "views/oauth.html"
			}).when('/profile/:userid', {
				title : "Your Profile",
				templateUrl : "views/profile.html",
				controller : "XProfileController"
			}).otherwise({ // Defaults to /
				redirectTo : "/"
			});

		} ]);

