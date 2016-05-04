angular.module('Boser')
.factory('SearchSrv', ['$http', function ($http) {
	
	return {
		getSearchConfigById: function(searchConfigId, callbackHandler, errorHandler) {
			$http({
				method: 'GET',
				url: 'rest/searchConfig/'+searchConfigId
			})
			.success(callbackHandler)
			.error(errorHandler);
		},
		insertKey: function(searchConfigId, text, callbackHandler, errorHandler) {
			$http({
	    		method: 'POST',
	    		url: 'rest/searchKey',
	    		data: {'searchConfigId': searchConfigId, 'text': text},
	    		headers: {'Content-Type': 'application/x-www-form-urlencoded'},
	    	    transformRequest: function(obj) {
	    	        var str = [];
	    	        for(var p in obj)
	    	        str.push(encodeURIComponent(p) + "=" + encodeURIComponent(obj[p]));
	    	        return str.join("&");
	    	    }
	    	})
	    	.success(callbackHandler)
	    	.error(errorHandler);
		},
		deleteKey: function(searchConfigId, keyId, callbackHandler, errorHandler) {
			$http({
	    		method: 'DELETE',
	    		url: 'rest/searchKey/'+keyId+'/searchConfig/'+searchConfigId
	    	})
	    	.success(callbackHandler)
	    	.error(errorHandler);
		},
		getRequests: function(callbackHandler, errorHandler) {
			$http({
				method: 'GET',
				url: 'rest/searchRequest'
			})
			.success(callbackHandler)
			.error(errorHandler);
		},
		insertRequest: function(searchConfigId, callbackHandler, errorHandler) {
			$http({
	    		method: 'POST',
	    		url: 'rest/searchRequest',
	    		data: {'searchConfigId': searchConfigId},
	    		headers: {'Content-Type': 'application/x-www-form-urlencoded'},
	    	    transformRequest: function(obj) {
	    	        var str = [];
	    	        for(var p in obj)
	    	        str.push(encodeURIComponent(p) + "=" + encodeURIComponent(obj[p]));
	    	        return str.join("&");
	    	    }
	    	})
	    	.success(callbackHandler)
			.error(errorHandler);
		},
		updateKey: function(searchConfigId, keyId, newValue, callbackHandler, errorHandler) {
			$http({
	    		method: 'PUT',
	    		url: 'rest/searchKey/'+keyId+'/searchConfig/'+searchConfigId,
	    		data: {'newValue': newValue},
	    		headers: {'Content-Type': 'application/x-www-form-urlencoded'},
	    	    transformRequest: function(obj) {
	    	        var str = [];
	    	        for(var p in obj)
	    	        str.push(encodeURIComponent(p) + "=" + encodeURIComponent(obj[p]));
	    	        return str.join("&");
	    	    }
	    	})
	    	.success(callbackHandler)
	    	.error(errorHandler);
		}
	};
}]);