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

   historyController.$inject = ['$scope', '$filter', 'lodash', 'historyService'];

   function historyController($scope, $filter, _, historyService) {
      $scope.history = {};
      $scope.updateHistory = updateHistory;
      $scope.updateTimeRange = updateTimeRange;
      $scope.updateSymbol = updateSymbol;
      $scope.symbols = {
         selected: []
      };

      activate();

      function activate() {
         updateHistory();
         updateAvailableSymbols();
      }

      function updateHistory() {
         ['trades', 'orders', 'quotes'].forEach(function(type){
            var params = {
               symbol: _.get($scope, 'symbols.selected[0].name'),
               startTime: $scope.startTime && moment($scope.startTime, "YYYY-MM-DD hh:mm:ss").unix(),
               endTime: $scope.endTime && moment($scope.endTime, "YYYY-MM-DD hh:mm:ss").unix() + 1,
               limit: 10
            }

            historyService.getHistory(type, params).then(function (history) {
               $scope.history[type] = history;
            });
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

      function updateAvailableSymbols() {
         historyService.getSymbols().then(function (symbols) {
            $scope.availableSymbols = _.map(_.union(symbols, $scope.state.symbols), function(name) {
               return {
                  name: name
               };
            });
         });
      }
   }
})();