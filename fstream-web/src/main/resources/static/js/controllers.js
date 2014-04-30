angular.module('FStreamApp.controllers', []).
controller('ratesController', function($scope, ratesService, chartService) {
	
	//
	// Private
	//
	
	var 
		setConnected = function setConnected(connected) {
			$('#commands').html('');
			$('#rates').html('');
		}, 
		createElement = function(message) {
		    var p = document.createElement('code');
		    p.style.wordWrap = 'break-word';
		    p.style.display = 'block'
		    p.appendChild(document.createTextNode(angular.toJson(message)));
		    
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
	    var instrument = $('#instrument').val();
		ratesService.register(instrument);
	}
	
	//
	// Events
	//
	
	$scope.$on('connected', function(e) {
		$scope.connected = true;

		setConnected(true);
	});
	$scope.$on('disconnected', function(e) {
		$scope.connected = false;
		
        setConnected(false);
	});
    $scope.$on('rate', function(e, rate) {
    	$('#rates').append(createElement(rate));
        chartService.updateChart(rate);
    });
    $scope.$on('command', function(e, command) {
    	$('#commands').append(createElement(command));
    });
    
}).
controller('chartController', function($scope, chartService) {
	chartService.init();
});
