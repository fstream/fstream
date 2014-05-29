angular.module('FStreamApp.controllers').controller('mainController', function($scope, _, configService, eventService) {
	
	//
	// Methods
	//
	
	var queue = function(a, value, limit) {
			return a.length >= limit ? a.pop() : a.unshift(value);
		}, 
		
		reset = function() {
			$scope.rates = [];
			$scope.alerts = [];
			$scope.commands = [];
		},
		
		register = function() {
			
			// Scope
			$scope.connect = function() {
				eventService.connect();
			};
			$scope.disconnect = function() {
				eventService.disconnect();
			}
			$scope.register = function() {
				eventService.register($scope.instrument);
			}
			
			// Events
			$scope.$on('connected', function(e) {
				$scope.connected = true;
				reset();
			});
			$scope.$on('disconnected', function(e) {
				$scope.connected = false;
			});
			
		    $scope.$on('rate', function(e, rate) {
		    	queue($scope.rates, rate, 50);
		    });
		    $scope.$on('alert', function(e, alert) {
		    	queue($scope.alerts, alert, 50);
		    });
		    $scope.$on('command', function(e, command) {
		    	queue($scope.commands, command, 50);
		    });
		    
		};
	
		
	// Bootstrap configuration
	configService.getConfig().then(function(instruments) {
	  $scope.instruments = instruments;
	  
	  $scope.tickCharts = new Array(instruments.length);
	  for (var i = 0; i < instruments.length; i++) {
		  $scope.tickCharts[i] = {
			index: i,
			symbol: instruments[i]
		  };
	  }
	});

	// Initialize
	$scope.connected = false;
	reset();
	register();
});