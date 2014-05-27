angular.module('FStreamApp.controllers').controller('mainController', function($scope, configService, ratesService, chartService) {
	
	// Bootstrap configuration
	configService.getConfig().then(function(instruments) {
	  $scope.instruments = instruments;
	});

	// Initialize
	$scope.connected = false;
	$scope.instrument = 'EUR/USD';
	$scope.rates = [];
	$scope.alerts = [];
	$scope.commands = [];
	$scope.instruments = [];
	$scope.charts = [
        {
			type: "event",
			symbol: "EUR/USD"
		}, {
			type: "event",
			symbol: "EUR/GBP"
		}, {
			type: "event",
			symbol: "EUR/JPY"
		}, {
			type: "event",
			symbol: "AUD/JPY"
		}
	];
		
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
		ratesService.register($scope.instrument);
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