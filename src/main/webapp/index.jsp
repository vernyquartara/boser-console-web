<!DOCTYPE html>
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<html>
<head lang="en">
	<title>BOSER</title>
	<meta charset="utf-8">
	<meta name="viewport" content="width=device-width, initial-scale=1">
	
	<!--
		https://github.com/chieffancypants/angular-loading-bar 
	-->
	
	<link rel="stylesheet" href="style/bootstrap.css" type='text/css' media='all'>
	<link rel="stylesheet" href="style/boser.css" type='text/css' media='all'>
	<link rel="stylesheet" href="style/animate.css" type='text/css' media='all'>
	<link rel='stylesheet' href='//cdnjs.cloudflare.com/ajax/libs/angular-loading-bar/0.9.0/loading-bar.min.css' type='text/css' media='all' />
	
	<script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/angularjs/1.5.5/angular.min.js"></script>
	<script type="text/javascript" src="https://code.angularjs.org/1.5.5/i18n/angular-locale_it-it.js"></script>
	<script type="text/javascript" src="https://code.angularjs.org/1.5.5/angular-route.min.js"></script>
	<script type="text/javascript" src="<c:url value="/script/boser-app.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/script/boser-routes.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/script/crawler/crawlerCtrl.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/script/search/searchSrv.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/script/search/searchCtrl.js"/>"></script>
	
	<script type="text/javascript" src="<c:url value="/script/angular-strap.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/script/angular-strap.tpl.js"/>"></script>
	<script type="text/javascript" src="https://code.angularjs.org/1.5.5/angular-animate.min.js"></script>
	<script type='text/javascript' src='//cdnjs.cloudflare.com/ajax/libs/angular-loading-bar/0.9.0/loading-bar.min.js'></script>
</head>

<body>

	<div ng-app="Boser" class="container">
	
		<nav class="navbar navbar-default" bs-navbar>
		  <div class="container-fluid">
		    <div class="navbar-header">
		      <a class="navbar-brand" href="#">boser-console</a>
		    </div>
		    <div>
		      <ul class="nav navbar-nav">
		        <li data-match-route="/crawler"><a href="#/crawler">Crawler</a></li>
		        <li data-match-route="/converter"><a href="#/converter">Converter</a></li>
		      </ul>
		    </div>
		  </div>
		</nav>
	
		<ng-view></ng-view>
	</div>

</body>
</html>