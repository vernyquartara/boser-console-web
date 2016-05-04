angular.module('Boser')
.controller('SearchCtrl', ['$scope','SearchSrv',function ($scope, SearchSrv) {
	$scope.bntListActive = true;
	$scope.submenu = function(selected) {
		$scope.bntNewActive = (selected == 'new' && !$scope.bntNewActive);
		$scope.bntListActive = (selected == 'list' && !$scope.bntListActive);
	}
	
	$scope.searchConfigId = 1;
	
	/*
	 * inizializzazione lista chiavi
	 */
	SearchSrv.getSearchConfigById(
			$scope.searchConfigId,
			function(response){
				$scope.keys = response.keys;
				for (i = 0; i < $scope.keys.length; i++) {
					var key = $scope.keys[i];
					$scope.editableKeys[key.id] = false;
				}
			},
			function(data, status, headers, config, statusText){
				console.log(data);
			}
	);
	/*
	 * gestione chiavi
	 */
	$scope.addNewKey = function() {
		SearchSrv.insertKey(
			$scope.searchConfigId,
			$scope.newKey,
			function(response){
				$scope.keys = response.keys;
				$scope.newKey = '';
				$scope.keysForm.newKey.$setValidity("duplicate", true);
			},
			function(data, status, headers, config, statusText){
				console.log(data);
				$scope.keysForm.newKey.$setValidity("duplicate", false);
			}
		);
	}
	$scope.removeKey = function(keyId) {
		SearchSrv.deleteKey(
			$scope.searchConfigId,
			keyId,
			function(response){
				$scope.keys = response.keys;
			},
			function(data, status, headers, config, statusText){
				console.log(data);
			}
		);
	}
	$scope.updateKey = function(keyId) {
		var inputId = 'key'.concat(keyId);
		var inputVal = document.getElementById(inputId).value;
		SearchSrv.updateKey(
				$scope.searchConfigId,
				keyId,
				inputVal,
				function(response){
					$scope.editableKeys[keyId] = false;
					$scope.keys = response.keys;
					updateKeyValidity = true;
				},
				function(data, status, headers, config, statusText){
					updateKeyValidity = false;
				}
		);
	}
	
	/*
	 * ricerca
	 */
	$scope.startSearch = function() {
		console.log("start search");
		
		SearchSrv.insertRequest(
			$scope.searchConfigId,
			function(response){
				$scope.submenu('list');
				$scope.getList();
			},
			function(data, status, headers, config, statusText){
				console.log(data);
			}
		);
	}
	/*
	 * lista
	 */
	$scope.getList = function() {
		SearchSrv.getRequests(
			function(response){
				$scope.requests = response;
			},
			function(data, status, headers, config, statusText){
				console.log(data);
			}
		);
	}
	$scope.getList();
	
	/*
	 * gestione edit singole chiavi
	 */
	var updateKeyValidity = true;
	$scope.updateKeyValid = function() {
		/*
		 * true o false per indicare se a seguito di un submit la chiave è risultata valida
		 * o meno (e quindi si deve mostrare il msg di errore).
		 * è unico e non array perché si edita una sola chiave alla volta (quindi il msg di errore
		 * su tutte le altri chiavi è attivo ma non visibile)
		 */
		return updateKeyValidity; 
	}
	$scope.editableKeys = []; //true o false per ogni chiave
	$scope.editable = function(id) {
		//resituisce l'editabilità del singolo elemento
		return $scope.editableKeys[id];
	};

	$scope.editKey = function(id, $event) {
		//modifica l'editabilità del singolo elemento
		for (i = 0; i < $scope.editableKeys.length; i++) {
			$scope.editableKeys[i] = false;
		}
		updateKeyValidity = true;
		$scope.editableKeys[id] = true;
	};
	
	$scope.undoEditKey = function($event, id) {
		//si stoppa la propagazione perché se no riscatterebbe l'edit key (che è definito sull'elemento padre <li>)
		updateKeyValidity = true;
		$scope.editableKeys[id] = false;
		$event.cancelBubble = true;
		$event.stopPropagation();
	};
	
	$scope.dontSubmit = function(event) {
		//soppressione tasto invio per evitare il submit del form
		var keyCode = event.which || event.keyCode;
		if (keyCode === 13) {
			event.preventDefault();
			return false;
		}
	}
}]);