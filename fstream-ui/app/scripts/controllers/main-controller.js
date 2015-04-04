angular.module('homer').controller('mainController', function($scope, lodash, stateService, eventService) {
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
		$scope.$on('state', function(e, state) {
			updateState(state);
		});
	};
	
	function connect() {
      console.log("connecting...");
		stateService.getState().then(updateState);
		initScope();
		resetModel();
		
		eventService.connect();
	}
	
	function disconnect() {
		eventService.disconnect();
	}
	
	function initScope() {
		$scope.connected = false;
		$scope.state = {};
		$scope.views = [];
		$scope.newAlert = {};
		$scope.connect = connect;
		$scope.disconnect = disconnect;
		$scope.registerAlert = registerAlert;
	}
		
	function resetModel() {
		$scope.rates = [];
		$scope.alerts = [];
		$scope.commands = [];
		$scope.metrics = [];
	}
	
	function registerAlert() {
		eventService.register($scope.newAlert);
	}
		
	function updateState(state) {
		$scope.state = state;
		
		// This could be smarter to allow not updating things that haven't changed
		$scope.views = [];
		
		lodash.each(state.metrics, function(metric, i) {
			$scope.views.push({
				type: 'metric-chart',
				id: metric.id,
				title: metric.name,
				name: metric.units,
				units: metric.units
			});
		});
		
//		$scope.views.push({
//			type: 'alert-log',
//		});
	
		lodash.each(state.symbols, function(symbol, i) {
			$scope.views.push({
				type: 'tick-chart',
				index: i,
				symbol: symbol
		    });
		});
	};
		
	function queueEvent(a, value, limit) {
		return a.length >= limit ? a.pop() : a.unshift(value);
	};
    
    // temporary workaround for Highcahrts opacity animation bug
    jQuery.cssProps.opacity = 'opacity';
	// Initialize
	registerEvents();
	connect();
});