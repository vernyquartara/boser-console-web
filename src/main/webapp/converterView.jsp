<%@page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div class="row starting-panel" ng-show="converterStarting">
	<div class="col-md-6 col-md-offset-3">
		<div class="panel panel-primary">
			<div class="panel-heading">Il convertitore &egrave; <strong>in fase di avvio</strong></div>
			<div class="panel-body">
				<div id="loading-bar-container"></div>
			</div>
		</div>
	</div>
</div>

<div class="row active-panel" ng-show="converter.state == 'running'">
	<div class="col-md-6 col-md-offset-3">
		<div class="panel panel-success">
			<div class="panel-heading">Il convertitore &egrave; <strong>attivo</strong></div>
			<div class="panel-body">
				<div>
					<a href="http://{{converter.publicDNSName}}" target="_blank" class="btn btn-success btn-block" role="button">
						Clicca per accedere  <span class="badge"><span class="glyphicon glyphicon-share-alt"></span></span>
					</a>
				</div>
			</div>
		</div>
	</div>
</div>

<div class="row standby-panel" ng-show="converter.state == 'stopped'">
	<div class="col-md-6 col-md-offset-3">
		<div class="panel panel-warning">
			<div class="panel-heading">Il convertitore &egrave; <strong>in stand-by</strong></div>
			<div class="panel-body">
				<button type="button" class="btn btn-warning btn-block" ng-click="startInstance()" ng-disabled="startBtnDisabled">
					Clicca per avviare  <span class="badge"><span class="glyphicon glyphicon-play"></span></span>
				</button>
			</div>
		</div>
	</div>
</div>




<!-- <div class="row">
	<div class="col-md-6 col-md-offset-3">
		<ul class="list-group">
			<li class="list-group-item list-group-item-info">Info e	statistiche</li>
			<li class="list-group-item">Converter URL: http://{{converter.publicDNSName}}</li>
			<li class="list-group-item">Ultimo avvio: 13-05-2014</li>
			<li class="list-group-item">Ultimo stand-by: 13-05-2014</li>
			<li class="list-group-item">Ore di attivit&agrave; mese corrente: 11</li>
			<li class="list-group-item">Ore di stand-by mese corrente: 723</li>
		</ul>
	</div>
</div> -->
