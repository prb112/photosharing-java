<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html> s
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>PhotoSharing Java</title>
<head>
  <link rel="shortcut icon" href="<% out.println(request.getContextPath()); %>/img/favicon.png" />
  <!-- Fonts -->
  <link href='//fonts.googleapis.com/css?family=Sue+Ellen+Francisco' rel='stylesheet' type='text/css'>

  <!-- CSS Libraries -->
  <link rel='stylesheet' href='//cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/3.3.4/css/bootstrap.css'/>
  <link rel='stylesheet' href='//cdnjs.cloudflare.com/ajax/libs/justifiedGallery/3.6.0/justifiedGallery.css'/>
  <link rel='stylesheet' href='./css/spinkit/spinkit.css'/>

  <!-- CSS -->
  <link rel='stylesheet' href='./css/style.css'/>

  <!-- Angular -->
  <script src="//cdnjs.cloudflare.com/ajax/libs/angular.js/1.4.4/angular.js"></script>
  <script src="//cdnjs.cloudflare.com/ajax/libs/angular.js/1.4.4/angular-route.js"></script>
  <script src="//cdnjs.cloudflare.com/ajax/libs/angular.js/1.4.4/angular-animate.js"></script>
  <script src="//cdnjs.cloudflare.com/ajax/libs/angular.js/1.4.4/angular-cookies.js"></script>
  <script src="//cdnjs.cloudflare.com/ajax/libs/ngInfiniteScroll/1.2.1/ng-infinite-scroll.js"></script>
  <script src="//cdnjs.cloudflare.com/ajax/libs/angular.js/1.4.4/angular-sanitize.js"></script>
  <script src="./js/config.js"></script>
  <script src="./js/controllers/miscctrl.js"></script>
  <script src="./js/controllers/homectrl.js"></script>
  <script src="./js/controllers/photoctrl.js"></script>
  <script src="./js/controllers/profilectrl.js"></script>
  <script src="./js/services.js"></script>
  <script src="./js/directives.js"></script>

  <!-- Bootstrap Mobile Optimization -->
  <meta name="viewport" content="width=device-width, initial-scale=1">

  <!-- jQuery -->
  <script src="//cdnjs.cloudflare.com/ajax/libs/jquery/3.0.0-alpha1/jquery.js"></script>

  <!-- Bootstrap Javascript -->
  <script src="//cdnjs.cloudflare.com/ajax/libs/angular-ui-bootstrap/0.13.3/ui-bootstrap.js"></script>
  <script src="//cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/3.3.4/js/bootstrap.js"></script>

  <!-- Justified-Gallery -->
  <script src="//cdnjs.cloudflare.com/ajax/libs/justifiedGallery/3.6.0/jquery.justifiedGallery.js"></script>


</head>
<body ng-app="photoApp" ng-init="root.displayName=''">


<!--Calls back to navbarController, and creates the navbar fixed at the top of the screen-->
<nav ng-controller="navbarController" id="navStyle" class="navbar navbar-fixed-top" role="navigation">
    <!--Displays brand in center of navbar-->
    <a style="line-height:15px;" class="navbar-brand" ng-click="select();" ng-show="!loading" href="#/public">
      Photo<span style="font-weight:bolder">Node</span>
      <span style="line-height:15px;" class="glyphicon glyphicon-picture"></span>
    </a>

    <div class="navbar-brand" ng-show="loading">
      <div class="sk-spinner sk-spinner-three-bounce" style="width:100px;">
        <div class="sk-bounce1" style="background-color: #004266"></div>
        <div class="sk-bounce2" style="background-color: #004266"></div>
        <div class="sk-bounce3" style="background-color: #004266"></div>
      </div>
    </div>

    <!-- Toggle is displayed for better mobile display -->
    <div class="navbar-header">
      <button style="border-radius: 2px; border: 1px solid black;" type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar-collapse" aria-expanded="false">
        <span class="sr-only">Toggle navigation</span>
        <span style="background-color: black" class="icon-bar"></span>
        <span style="background-color: black" class="icon-bar"></span>
        <span style="background-color: black" class="icon-bar"></span>
      </button>

    </div>

    <!-- Collect the nav links, forms, and other content for toggling -->
    <div class="collapse navbar-collapse" id="navbar-collapse">
      <ul class="nav navbar-nav">

        <!--Displays the profile button-->
        <li class="profile" ng-click="select('.profileButton')">
          <a href="#/profile/{{uid}}"><img id="profilePic" ng-src="{{$root.avatar}}" height=40p>
            <!--Display on glyphicon when screen gets smaller-->
            <text class="profileButton" ng-show="mediumScreen">{{$root.displayName}}</text>
          </a>
        </li>

        <!--Displays the public button-->
        <li class="navbarButton" ng-click="select('.publicButton')">
          <a href="#/public"><span class="glyphicon glyphicon-globe"></span>
            <!--Display on glyphicon when screen gets smaller-->
            <text class="publicButton" ng-show="mediumScreen">Public</text>
          </a>
        </li>

        <!--Displays the private button-->
        <li class="navbarButton" ng-click="select('.privateButton')">
          <a href="#/private"><span class="glyphicon glyphicon-envelope"></span>
            <!--Display on glyphicon when screen gets smaller-->
            <text class="privateButton" ng-show="mediumScreen">Messages</text>
          </a>
        </li>

        <li class="navbarButton" ng-click="select('.personalButton')">
          <a href="#/myphotos"><span class="glyphicon glyphicon-user"></span>
            <text class="personalButton" ng-show="mediumScreen">Private</text>
          </a>
        </li>

        <li ng-show="!uploadSideA" class="navbarButton" ng-click="select('.uploadButton');">
          <a ng-click="open()" href=""><span class="glyphicon glyphicon-upload"></span>
            <!--Display on glyphicon when screen gets smaller-->
            <text class="uploadButton" ng-show="mediumScreen">Upload</text>
          </a>
        </li>

      </ul>

      <!--Places the following to the right of the navbar-->
      <ul class="nav navbar-nav navbar-right">

        <!--Displays the upload button-->
        <li ng-show="uploadSideA" class="navbarButton" ng-click="select('.uploadButton');">
          <a ng-click="open()" href=""><span class="glyphicon glyphicon-upload"></span>
            <!--Display on glyphicon when screen gets smaller-->
            <text class="uploadButton" ng-show="mediumScreen">Upload</text>
          </a>
        </li>

        <li>
          <!--Displays the search bar and defines its functionality-->
          <form class="searchBar navbar-form" role="search">
            <div style="position: relative" class="form-group input-group stylish-input-group">
              <input type="text" id="searchText" ng-change="peopleSearch()" ng-model="searchQuery" class="form-control" placeholder="Search">
              <span class="input-group-addon">
                <button type="submit" id="searchButton" ng-click="search()">
                  <span class="glyphicon glyphicon-search"></span>
                </button>
              </span>
              <ul ng-show="resultList.length > 0" class="dropdown-menu" style="display:block">
                <li ng-repeat="result in resultList"><a ng-bind-html="result.name" href="" ng-click="goToProfile(result.id)"></a></li>
              </ul>

              <!--Allows the user to submit search by hitting enter-->
              <script>
              $("#searchText").keyup(function(event){
                if(event.keyCode == 13 || event.which == 13){
                  $("#searchButton").click();
                }
              });
              </script>

            </div>
          </form>
        </li>

        <!--Displays the logout button-->
        <li class="navbarButton logOut">
          <a ng-click="logout()" href=""><span class="glyphicon glyphicon-log-out"></span>
            <!--Display on glyphicon when screen gets smaller-->
            <text ng-show="mediumScreen">Logout</text>
          </a>
        </li>

      </ul>

    </div>
</nav>

<div align="middle" class="loadingSplash" ng-show="loading" ng-style="{'background-color':loadingHues[hueIndex]}" style="opacity:1.0; width:100%;height:100%; z-index:10;top:0; left:0; position: fixed;">

  <div class="container">
      <div class="row photonode">

        <div class="col-lg-12 col-md-12 col-sm-12 col-xs-12">

          <span class="brandPagePhoto" style="color:white">Photo</span>
          <span class="brandPageNode" style="color:white">Node </span>
          <span class="glyphicon glyphicon-picture brandPageLogo"></span>

        </div>

      </div>

  </div>
</div>
<div id="main">

    <div class="page {{pageClass}}" ng-view></div>

</div>

</body>

</html>
