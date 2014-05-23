angular.module('FStreamApp.controllers', ['ngTable']).
controller('ratesController', function($scope, $http, ratesService, chartService) {
	
	// Bootstrap configuration
	$http.get('/config').success(function(data) {
	  $scope.instruments = data;
	});
	
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
        chartService.addRate(rate);
    });
    $scope.$on('alert', function(e, alert) {
    	chartService.addAlert(alert);
    });
    $scope.$on('command', function(e, command) {
    	$('#commands').append(createElement(command));
    });
    
}).
controller('chartController', function($scope, $timeout, chartService) {
	$timeout(function(){
		chartService.init();
	}, 0);
}).
controller('alertController', function($scope, $filter, ngTableParams) {
    var alerts = [];
    $scope.$on('alert', function(e, alert) {
    	alerts.unshift(alert);
    	
    	// Limit to 50 events
    	if (alerts.length >= 50) {
    		alerts.pop();
    	}
    	
    	$scope.tableParams.reload();
    });
    
    $scope.tableParams = new ngTableParams({
        page: 1,
        count: 10
    }, {
        total: alerts.length, // length of data
        getData: function($defer, params) {
            // use build-in angular filter
            var orderedData = params.filter() ?
                   $filter('filter')(alerts, params.filter()) :
            	   alerts;

            $scope.alerts = orderedData.slice((params.page() - 1) * params.count(), params.page() * params.count());

            params.total(orderedData.length); // set total for recalc pagination
            $defer.resolve($scope.alerts);
        }
    });	
});
