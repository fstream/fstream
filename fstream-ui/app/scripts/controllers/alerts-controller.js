angular.module('fstream').controller('alertController', function($scope, $filter, ngTableParams) {
    $scope.$on('alert', function(e, alert) {
    	$scope.tableParams.reload();
    });
    
    $scope.tableParams = new ngTableParams({page: 1, count: 10}, {
        total: $scope.alerts.length,
        getData: function($defer, params) {
        	var alerts = $scope.alerts,
            	data = params.filter() ? $filter('filter')(alerts, params.filter()) : alerts;

            params.total(data.length);
            $scope.data = data.slice((params.page() - 1) * params.count(), params.page() * params.count());
            $defer.resolve($scope.data);
        }
    });	
});
