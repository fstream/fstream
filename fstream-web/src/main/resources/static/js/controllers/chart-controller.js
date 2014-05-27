angular.module('FStreamApp.controllers').controller('chartController', function($scope, $timeout, chartService) {
	$timeout(function(){
		//chartService.init();
	});
	
	$scope.enableChart = chartService.enable;
	$scope.disableChart = chartService.disable;
});
