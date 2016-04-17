<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %> 
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>PDFConverter Manager</title>
<link rel="stylesheet" href="style/console.css"/>
</head>
<body>
<div id="barra">
	<div id="logo">boser</div>
</div>
<div id="legenda">
	<p>Si &egrave verificato un errore che ha impedito di conoscere lo stato del convertitore.</p>
	<p>Indirizzo email per la segnalazione: webny23@gmail.com</p>
	<c:if test="${running}">
		<p><a href="http://boser.quartara.it/conversionHome">Accedi</a> (potrebbe essere spento)</p>
	</c:if>
</div>
</body>
</html>