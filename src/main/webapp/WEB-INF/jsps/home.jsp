<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %> 
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>Boser Management Console</title>
<link rel="stylesheet" href="style/console.css"/>
</head>
<body>
<div id="barra">
	<div id="logo">boser</div>
</div>
<div id="legenda">
	<p>Il <i>crawler</i> <strong>&egrave; ${crawlerStatus}</strong></p>
	<c:if test="${not crawlerRunning}">
		<p>Premi il pulsante qui sotto per avviarlo (l'avvio dura circa due minuti)</p>
		<p>Il crawler sar&agrave; messo in standby automaticamente dopo 55 minuti di inattivit&agrave;</p>
		<form action="<c:url value="/crawler"/>" method="post">
			<input id="crawlerStartBtn" type="submit" value="AVVIO" onclick="javascript:getElementById('crawlerStartBtn').disabled=true;"/>
		</form>
	</c:if>
	<c:if test="${crawlerRunning}">
		<p><a href="http://boser.quartara.it/">Accedi</a></p>
	</c:if>
</div>
<hr/>
<div id="legenda">
	<p>Il <i>convertitore</i> <strong>&egrave; ${converterStatus}</strong></p>
	<c:if test="${not converterRunning}">
		<p>Premi il pulsante qui sotto per avviarlo (l'avvio dura circa un minuto)</p>
		<p>Il convertitore sar&agrave; messo in standby automaticamente dopo 55 minuti di inattivit&agrave;</p>
		<form action="<c:url value="/converter"/>" method="post">
			<input id="converterStartBtn" type="submit" value="AVVIO" onclick="javascript:getElementById('converterStartBtn').disabled=true;"/>
		</form>
	</c:if>
	<c:if test="${converterRunning}">
		<p><a href="http://boser-converter.quartara.it/conversionHome">Accedi</a></p>
	</c:if>
</div>
</body>
</html>