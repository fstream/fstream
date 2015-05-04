(function () {
   'use strict';

   angular
      .module('fstream')
      .controller('historyCtrl', historyCtrl);

   historyCtrl.$inject = ['$scope', 'historyService'];

   function historyCtrl($scope, historyService) {
      $scope.updateHistory = updateHistory;
      $scope.symbols = {
         selected: []
      };

      activate();

      function activate() {
         updateHistory();
         getAvailableSymbols();
      }

      function updateHistory() {
         var params = {
            symbol: $scope.symbols.selected.length && $scope.symbols.selected[0].name,
            startTime: $scope.startTime,
            endTime: $scope.endTime
         };

         historyService.getHistory(params).then(function (history) {
            $scope.history = history;
         });
      }

      function getAvailableSymbols() {
         historyService.getSymbols().then(function (symbols) {
            $scope.availableSymbols = symbols;
         });
      }
   }
})();