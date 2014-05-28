angular.module('FStreamApp.controllers').controller('mainController', function($scope, configService, eventService, chartService) {
	
	// Bootstrap configuration
	configService.getConfig().then(function(instruments) {
	  $scope.instruments = instruments;
	  $scope.charts = new Array(instruments.length);
	  for (var i = 0; i < instruments.length; i++) {
		  $scope.charts[i] = {
			type: "event",
			symbol: instruments[i]
		  };
	  }
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
		eventService.connect();
	};
	$scope.disconnect = function() {
		eventService.disconnect();
	}
	$scope.register = function() {
		eventService.register($scope.instrument);
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
        //chartService.addRate(rate);
    });
    $scope.$on('alert', function(e, alert) {
    	//chartService.addAlert(alert);
    });
    $scope.$on('command', function(e, command) {
    	$scope.commands.unshift(command);
    });
});