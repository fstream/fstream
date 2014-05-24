angular.module('FStreamApp.controllers').controller('ratesController', function($scope, $http, ratesService, chartService) {
	
	// Bootstrap configuration
	$http.get('/config').success(function(data) {
	  $scope.instruments = data;
	});

	// Initialize
	$scope.connected = false;
	$scope.rates = [];
	$scope.alerts = [];
	$scope.commands = [];
	$scope.instruments = [];
		
	//
	// Methods
	//
	
	$scope.connect = function() {
		ratesService.connect();
	};
	$scope.disconnect = function() {
		ratesService.disconnect();
	}
	$scope.register = function() {
	    var instrument = $('#instrument').val();
		ratesService.register(instrument);
	}
	
	//
	// Events
	//
	
	$scope.$on('connected', function(e) {
		$scope.connected = true;
		$scope.rates = [];
		$scope.alerts = [];
		$scope.commands = [];
	});
	$scope.$on('disconnected', function(e) {
		$scope.connected = false;
	});
    $scope.$on('rate', function(e, rate) {
    	$scope.rates.unshift(rate);
        chartService.addRate(rate);
    });
    $scope.$on('alert', function(e, alert) {
    	chartService.addAlert(alert);
    });
    $scope.$on('command', function(e, command) {
    	$scope.commands.unshift(command);
    });
    
});