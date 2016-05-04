angular.module('Boser')
.controller('CrawlerCtrl', ['$scope','$http', function ($scope, $http) {
	
	/*
	 * inizializzazione
	 */
	$http({
		method: 'GET',
		url: 'rest/crawler'
	}).success(function(result) {
		$scope.crawler = result;
	});
	
    
}]);