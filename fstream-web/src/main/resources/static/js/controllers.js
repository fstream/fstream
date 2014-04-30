angular.module('FStreamApp.controllers', []).
controller('ratesController', function($scope, ratesService, chartService) {
	
	//
	// Private
	//
	
	var 
		setConnected = function setConnected(connected) {
		    document.getElementById('conversation').style.visibility = connected ? 'visible' : 'hidden';
		    document.getElementById('events').innerHTML = '';
		    document.getElementById('rates').innerHTML = '';
		}, 
		createElement = function(message) {
		    var p = document.createElement('p');
		    p.style.wordWrap = 'break-word';
		    p.appendChild(document.createTextNode(message));
		    
		    return p;
		}
	
		
	$scope.connected = false;
		
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
	    var instrument = document.getElementById('instrument').value;
		ratesService.register(instrument);
	}
	
	//
	// Events
	//
	
	$scope.$on('connected', function(e) {
		$scope.connected = true;
		
		chartService.init();
		setConnected(true);
	});
	$scope.$on('disconnected', function(e) {
		$scope.connected = false;
		
        setConnected(false);
	});
    $scope.$on('rate', function(e, rate) {
        document.getElementById('rates').appendChild(createElement(rate.toString()));
        chartService.updateChart(rate);
    });
    $scope.$on('event', function(e, event) {
    	document.getElementById('events').appendChild(createElement(event.toString()));
    });
    
});