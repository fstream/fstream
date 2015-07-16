/* 
 * Copyright (c) 2015 fStream. All Rights Reserved.
 * 
 * Project and contact information: https://bitbucket.org/fstream/fstream
 * 
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

(function () {
   'use strict';

   angular
      .module('fstream')
      .controller('historyController', historyController);

   historyController.$inject = ['$scope', '$filter', 'historyService'];

   function historyController($scope, $filter, historyService) {
      $scope.updateHistory = updateHistory;
      $scope.updateTimeRange = updateTimeRange;
      $scope.updateSymbol = updateSymbol;
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
            startTime: $scope.startTime && moment($scope.startTime, "YYYY-MM-DD hh:mm:ss").unix(),
            endTime: $scope.endTime && moment($scope.endTime, "YYYY-MM-DD hh:mm:ss").unix() + 1
         }

         historyService.getHistory(params).then(function (history) {
            $scope.history = history;
         });
      }

      function updateTimeRange(time) {
         $scope.startTime = $scope.endTime = $filter('date')(time, 'yyyy-MM-dd HH:mm:ss');
      }

      function updateSymbol(name) {
         $scope.symbols = {
            selected: [{
               name: name
            }]
         };
      }

      function getAvailableSymbols() {
         historyService.getSymbols().then(function (symbols) {
            $scope.availableSymbols = symbols;
         });
      }
   }
})();