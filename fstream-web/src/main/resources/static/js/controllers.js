angular.module('FStreamApp.controllers', []).
controller('ratesController', function($scope, ratesService, chartService) {
	
	//
	// Private
	//
	
	var 
		setConnected = function setConnected(connected) {
		    document.getElementById('connect').disabled = connected;
		    document.getElementById('disconnect').disabled = !connected;
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
		chartService.init();
		setConnected(true);
	});
	$scope.$on('disconnected', function(e) {
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