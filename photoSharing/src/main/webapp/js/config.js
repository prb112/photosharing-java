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
 * Angular Module - photoApp 
 * photoApp is the main application module with depdencies as identified
 */
var photoApp = angular.module('photoApp', ['ngSanitize', 'ngRoute', 'ngAnimate', 'ngCookies', 'ui.bootstrap', 'infinite-scroll']);

/**
 * Configures the routes for the photoApp
 */
photoApp.config(function ($routeProvider) {
  $routeProvider
      .when('/:type', {// route for the home page
    templateUrl : './partials/page-home',
    resolve     : {
      feedData  : function($rootScope, $route, $window, apiService){

        var feed;
        var type = 'public';
        var tags = '';

        if($route.current.params.type){
          type = $route.current.params.type;
        }

        $rootScope.state = type;

        document.title = type.charAt(0).toUpperCase() + type.slice(1);

        if($route.current.params.tags){
          tags = $route.current.params.tags;
        }
        
        var apiUrl = '?type=' + type + '&q=' + tags + '&ps=20&si=1';
        console.log(apiUrl);
        return apiService.getFeed(apiUrl, successCallback, errorCallback)
        .then(function(){
        	
        	
          var images = [];
          angular.forEach(feed, function(image){
            images.push(image.thumbnail);
          });
          
          return apiService.resolveImages(images)
            .then(function(){
            	console.log(feed);
              return feed;
            }, function(){
              console.log("Done, but with errors, check image source");
              return feed;
            });
        });


        function successCallback(data, status){
          feed = data;
        }

        function errorCallback(data, status){
          if(status === 401 || status === 403){
            $window.location.assign('/');
          }
        }

      }
    },
    controller  : 'homeController'
  })
      .when('/photo/:lid/:pid', {// route for the about page
    templateUrl : './partials/page-photo',

    resolve     :  {
    photoData : function($route, apiService){

      var resolveData = {};
      var uid = '';

      var apiUrl = '?pid=' + $route.current.params.pid + '&lid=' + $route.current.params.lid;
      console.log(apiUrl);
      
      return apiService.getPhoto(apiUrl, photoCallback, errorCallback)

        .then(function(){

          return apiService.getProfile('?uid=' + uid, profileCallback, errorCallback)

            .then(function(){
            	var apiUrl = '?pid=' + $route.current.params.pid + '&uid=' + uid;
            	console.log(apiUrl);
              return apiService.getComments(apiUrl, commentCallback, errorCallback)

                .then(function(){

                  return apiService.getProfiles(resolveData.comments, profilesCallback, errorCallback)

                    .then(function(){

                      var images = [];
                      images.push(resolveData.photo.link);
                      images.push(resolveData.profile.img);
                      angular.forEach(resolveData.comments, function(comment){
                        images.push(comment.profileImg);
                      });

                      return apiService.resolveImages(images)

                        .then(function(){

                          return resolveData;

                          });
                        });
                      });
                    });
                  });


        //Promise Callbacks
        function photoCallback(data, status){
          resolveData.photo = data;
          uid = data.uid;
          resolveData.photo.published = new Date(resolveData.photo.published);
          resolveData.photo.published = resolveData.photo.published.toLocaleDateString();
          document.title = data.title;
        }

        function commentCallback(data, status){
          resolveData.comments = data;
          resolveData.comments.reverse();
          resolveData.comments.forEach(function(comment, index){
            comment.date = new Date(comment.date);
            comment.date = comment.date.toLocaleDateString();
          })
        }

        function profileCallback(data, status){
          resolveData.profile = data;
        }

        function profilesCallback(data, status, comment){
          comment.profileImg = data.img;
        }

        function errorCallback(data, status){
          if(status === 401 || status === 403){
            $window.location.assign('/');
          }
        }

      }
    },
    controller  : 'photoController'
  })
      .when('/profile/:uid', {// route for the contact page
    templateUrl : './partials/page-profile',
    resolve     :  {
      profileData : function($route, apiService){
        var resolveData = {};

        var tags = '';

        if($route.current.params.tags){
          tags = $route.current.params.tags;
        }

        return apiService.getProfile('?uid=' + $route.current.params.uid, profileCallback, errorCallback)
        .then(function(){
        	var apiUrl = '?type=user&uid=' + $route.current.params.uid + '&q=' + tags + '&ps=20&si=1';
        	console.log(apiUrl);
          return apiService.getFeed(apiUrl, feedCallback, errorCallback)
          .then(function(){
            return resolveData;
          }, function(){
        	  console.log("error getting the API");
          });
        });

        function profileCallback(data, status){
          resolveData.profile = data;
          document.title = data.name;
        }
        function feedCallback(data, status){
          resolveData.feed = data;
        }
        function errorCallback(data, status){
          if(status === 401 || status === 403){
            $window.location.assign('/');
          }
        }
      }
    },
    controller  : 'profileController'
  })
      .otherwise({
    redirectTo: './public'
  })
});
