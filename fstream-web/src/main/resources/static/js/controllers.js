angular.module('FStreamApp.controllers', []).
controller('ratesController', function($scope, ratesService) {
	$scope.connect = function() {
		init();
		ratesService.connect();
	};
	
	$scope.disconnect = function() {
		ratesService.disconnect();
	}
	
	$scope.register = function() {
		ratesService.register();
	}
});