<%@page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div class="row">
	<div class="col-md-6 col-md-offset-3">
		<div class="panel panel-success">
			<div class="panel-heading">Il crawler &egrave; <strong>attivo</strong></div>
			<div class="panel-body">
				<button type="button" class="btn btn-success btn-block">
					Clicca per accedere  <span class="badge"><span class="glyphicon glyphicon-share-alt"></span></span>
				</button>
			</div>
		</div>
	</div>
</div>

<div class="row">
	<div class="col-md-6 col-md-offset-3">
		<div class="panel panel-warning">
			<div class="panel-heading">Il crawler &egrave; <strong>in stand-by</strong></div>
			<div class="panel-body">
				<button type="button" class="btn btn-warning btn-block">
					Clicca per avviare  <span class="badge"><span class="glyphicon glyphicon-play"></span></span>
				</button>
			</div>
		</div>
	</div>
</div>

<div class="row">
	<div class="col-md-6 col-md-offset-3">
		<div class="panel panel-primary">
			<div class="panel-heading">Il crawler &egrave; <strong>in fase di avvio</strong></div>
			<div class="panel-body">
				<div class="progress">
					<div class="progress-bar" role="progressbar" aria-valuenow="60"
						aria-valuemin="0" aria-valuemax="100" style="width: 60%;">
						<span class="sr-only">60% Complete</span>
					</div>
				</div>
			</div>
		</div>
	</div>
</div>


<div class="row">
	<div class="col-md-6 col-md-offset-3">
		<ul class="list-group">
			<li class="list-group-item list-group-item-info">Info e	statistiche</li>
			<li class="list-group-item">URL: http://ec2-52-58-70-245.eu-central-1.compute.amazonaws.com</li>
			<li class="list-group-item">Ultimo avvio: 16-05-2014</li>
			<li class="list-group-item">Ultimo stand-by: 16-05-2014</li>
			<li class="list-group-item">Ore di attivit&agrave; mese corrente: 14</li>
			<li class="list-group-item">Ore di stand-by mese corrente: 722</li>
		</ul>
	</div>
</div>
