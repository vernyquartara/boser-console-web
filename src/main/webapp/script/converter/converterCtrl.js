angular.module('Boser')
.controller('ConverterCtrl', ['$scope','$http', '$location', function ($scope, $http, $location) {
	
	/*
	 * inizializzazione
	 */
	$scope.startBtnDisabled = false;
	$scope.converterStarting = false;
	$http({
		method: 'GET',
		url: 'rest/converter'
	}).success(function(result) {
		$scope.converter = result;
	});
	
	$scope.startInstance = function() {
		$scope.startBtnDisabled = true;
		$scope.converterStarting = true;
		$http({
    		method: 'PUT',
    		url: 'rest/converter/start'
    	}).success(function (response) {
    		/*
    		 * se tutto va bene si aggiorna la lista
    		 */
    		console.log("ok "+response);
    		$scope.converterStarting = false;
    		$scope.converter = response;
    	}).error(function(data, status, headers, config, statusText) {
    		console.log("ko");
    	});
	}
	
}]);