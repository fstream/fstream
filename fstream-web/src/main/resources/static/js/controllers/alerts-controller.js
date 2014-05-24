angular.module('FStreamApp.controllers').controller('alertController', function($scope, $filter, ngTableParams) {
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
            var orderdAlerts = params.filter() ?
                   $filter('filter')(alerts, params.filter()) :
            	   alerts;

            $scope.alerts = orderdAlerts.slice((params.page() - 1) * params.count(), params.page() * params.count());

            params.total(orderdAlerts.length); // set total for recalc pagination
            $defer.resolve($scope.alerts);
        }
    });	
});
