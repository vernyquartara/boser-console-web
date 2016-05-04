<!DOCTYPE html>
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<html>
<head lang="en">
	<title>BOSER</title>
	<meta charset="utf-8">
	<meta name="viewport" content="width=device-width, initial-scale=1">
	
	<link rel="stylesheet" href="style/bootstrap.css">
	<link rel="stylesheet" href="style/boser.css">
	<script type="text/javascript" src="<c:url value="/script/angular.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/script/i18n/angular-locale_it-it.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/script/angular-route.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/script/boser-app.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/script/boser-routes.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/script/crawler/crawlerCtrl.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/script/search/searchSrv.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/script/search/searchCtrl.js"/>"></script>
	
	<script type="text/javascript" src="<c:url value="/script/angular-strap.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/script/angular-strap.tpl.js"/>"></script>
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