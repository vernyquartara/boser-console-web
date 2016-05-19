angular.module("Boser", [
                         'ngRoute',
                         'mgcrea.ngStrap',
                         'angular-loading-bar',
                         'ngAnimate'
                         ])
.config(['cfpLoadingBarProvider', function(cfpLoadingBarProvider) {
    cfpLoadingBarProvider.parentSelector = '#loading-bar-container';
    cfpLoadingBarProvider.spinnerTemplate = '<div><span class="fa fa-spinner">Avvio in corso, per favore attendi ...</div>';
    cfpLoadingBarProvider.latencyThreshold = 1000;
  }]);
 