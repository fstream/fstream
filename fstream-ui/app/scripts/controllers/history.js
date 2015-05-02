/**
 * historyCtrl
 */

angular
   .module('fstream')
   .controller('historyCtrl', historyCtrl)

function historyCtrl($scope, historyService) {
   updateHistory();

   $scope.updateHistory = updateHistory;

   function updateHistory() {
      var params = {
         symbol:    $scope.symbol, 
         startTime: $scope.startTime, 
         endTime:   $scope.endTime
      };

      historyService.getHistory(params).then(function(history) {
         $scope.history = history
      });
   }
}