/**
 *
 * alertsCtrl
 *
 */

angular
   .module('fstream')
   .controller('historyCtrl', historyCtrl)

function historyCtrl($scope, historyService) {
   updateHistory();

   $scope.updateHistory = updateHistory;

   function updateHistory() {
      historyService.getHistory({symbol: $scope.symbol}).then(function(history) {
         $scope.history = history
      });
   }
}