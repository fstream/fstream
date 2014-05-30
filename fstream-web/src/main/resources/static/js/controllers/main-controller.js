angular.module('FStreamApp.controllers').controller('mainController', function($scope, _, configService, eventService) {
	function registerEvents() {
		// Events
		$scope.$on('connected', function(e) {
			$scope.connected = true;
		});
		$scope.$on('disconnected', function(e) {
			$scope.connected = false;
		});
		
		// TODO: Rename
		$scope.$on('rate', function(e, rate) {
			queueEvent($scope.rates, rate, 50);
		});
		$scope.$on('alert', function(e, alert) {
			queueEvent($scope.alerts, alert, 50);
		});
		$scope.$on('metric', function(e, metric) {
			queueEvent($scope.metrics, metric, 60);
		});
		$scope.$on('command', function(e, command) {
			queueEvent($scope.commands, command, 50);
		});
	};
	
	function connect() {
		configService.getConfig().then(updateInstruments);
		initScope();
		resetModel();
		
		eventService.connect();
	}
	
	function disconnect() {
		eventService.disconnect();
	}
	
	function initScope() {
		$scope.connected = false;
		$scope.instruments = [];
		$scope.views = [];
		$scope.connect = connect;
		$scope.disconnect = disconnect;
		$scope.register = registerInstrument;
	}
		
	function resetModel() {
		$scope.rates = [];
		$scope.alerts = [];
		$scope.commands = [];
		$scope.metrics = [];
	}
	
	function registerInstrument() {
		eventService.register($scope.instrument);
	}
		
	function updateInstruments(instruments) {
		$scope.instruments = instruments;
		$scope.views.push({
			type: 'metric',
			title: 'Events per Minute',
			name: "Events",
			units: "Count"
		});
		_.each(instruments, function(symbol, i) {
			$scope.views.push({
				type: 'tick',
				index: i,
				symbol: symbol
		    });
		});
	};
		
	function queueEvent(a, value, limit) {
		return a.length >= limit ? a.pop() : a.unshift(value);
	};
		
	// Initialize
	registerEvents();
	connect();
});