/**
 *
 * alertsCtrl
 *
 */

angular
   .module('fstream')
   .controller('historyCtrl', historyCtrl)


angular
   .module('fstream')
   .factory('historyService', function($http) {
   var config = {
      databaseName: 'fstream-events'
   }
   return {
      getHistory: function(symbol, start, end, limit) {
         limit = limit || 10;
         return $http.get('http://localhost:8086/db/' + config.databaseName + '/series', {
            params: {u: 'root', p: 'root', q: 'SELECT * FROM "ticks" GROUP BY symbol LIMIT ' + limit}
         });
      }
   };
});

function historyCtrl($scope, historyService) {
   getHistory();

   $scope.submit = getHistory();

   function getHistory() {
      historyService.getHistory($scope.symbol).then(function(result) {
         $scope.history = result.data[0];
      });
   }
}