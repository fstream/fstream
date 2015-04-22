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
        getHistory: function() {
             return $http.get('http://localhost:8086/db/ticks/series', {params: {u: 'root', p: 'root', q: 'select * from /.*/ LIMIT 10'}
})
        }
   }
});

function historyCtrl($scope, historyService) {
    historyService.getHistory().then(function(result) {
        $scope.history = result.data;
    });
}