angular.module('Boser')
.config(function($routeProvider) {
    $routeProvider
        .when('/home', {
        	templateUrl: 'mainMenu.jsp'
        })
        .when('/crawler', {
        	templateUrl: 'crawlerView.jsp',
        	controller: 'CrawlerCtrl'
        })
        .when('/converter', {
        	templateUrl: 'converterView.jsp',
        	controller: 'ConverterCtrl'
        })
        .otherwise({
            redirectTo: '/home'
        });
});