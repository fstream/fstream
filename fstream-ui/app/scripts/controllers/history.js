/**
 * historyCtrl
 */

angular
   .module('fstream')
   .controller('historyCtrl', historyCtrl)

function historyCtrl($scope, historyService) {
   $scope.updateHistory = updateHistory;
   $scope.symbols = {
      selected: []
   };
   
   updateHistory();
   getAvailableSymbols();

   function updateHistory() {
      var params = {
         symbol:    $scope.symbols.selected.length && $scope.symbols.selected[0].name, 
         startTime: $scope.startTime, 
         endTime:   $scope.endTime
      };

      historyService.getHistory(params).then(function(history) {
         $scope.history = history;
      });
   }
   
   function getAvailableSymbols() {
      historyService.getAvailableSymbols().then(function(availableSymbols) {
         $scope.availableSymbols = availableSymbols;
      });
   }   
}