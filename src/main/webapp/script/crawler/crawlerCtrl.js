angular.module('Boser')
.controller('CrawlerCtrl', ['$scope','$http', function ($scope, $http) {
	
	/*
	 * inizializzazione
	 */
	$scope.startBtnDisabled = false;
	$scope.crawlerStarting = false;
	$http({
		method: 'GET',
		url: 'rest/crawler'
	}).success(function(result) {
		$scope.crawler = result;
	});
	
	$scope.startInstance = function() {
		$scope.startBtnDisabled = true;
		$scope.crawlerStarting = true;
		$http({
    		method: 'POST',
    		url: 'rest/crawler/start'
    	}).success(function (response) {
    		/*
    		 * se tutto va bene si aggiorna la lista
    		 */
    		console.log("ok "+response);
    		$scope.crawlerStarting = false;
    		$scope.crawler = response;
    	}).error(function(data, status, headers, config, statusText) {
    		console.log("ko");
    	});
	}
	
    
}]);