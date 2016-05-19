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
    		method: 'PUT',
    		url: 'rest/crawler/start',
			data: {'fakeParam': 'fakeValue'},
			headers: {'Content-Type': 'application/x-www-form-urlencoded'},
		    transformRequest: function(obj) {
		        var str = [];
		        for(var p in obj)
		        str.push(encodeURIComponent(p) + "=" + encodeURIComponent(obj[p]));
		        return str.join("&");
		    }
    	}).success(function (response) {
    		/*
    		 * se tutto va bene si aggiorna la lista
    		 */
    		console.log("ok "+response);
    		$scope.crawlerStarting = false;
    		$scope.crawler = response;
    	}).error(function(data, status, headers, config, statusText) {
    		console.log("ko "+data);
    		$scope.crawlerStarting = false;
    	});
	}
	
    
}]);