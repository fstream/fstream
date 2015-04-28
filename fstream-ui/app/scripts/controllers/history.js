/**
 *
 * alertsCtrl
 *
 */

angular
    .module('homer')
    .controller('historyCtrl', historyCtrl)


angular
    .module('homer')
    .factory('historyService', function($http) {
   return {
        getHistory: function(symbol, start, end, limit) {
            symbol = (symbol || '.*').replace('/', '\\/');
            limit = limit || 10;
            return $http.get('http://localhost:8086/db/ticks/series', {
                params: {u: 'root', p: 'root', q: 'select * from /' + symbol + '/ LIMIT ' + limit}
            });
        }
   };
});

function historyCtrl($scope, historyService) {
    historyService.getHistory().then(function(result) {
        $scope.history = result.data;
    });
    
    $scope.submit = function() {
         historyService.getHistory($scope.symbol).then(function(result) {
            $scope.history = result.data;
        });
    }
}