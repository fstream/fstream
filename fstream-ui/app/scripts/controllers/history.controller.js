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

   historyController.$inject = ['$scope', '$filter', 'lodash', 'ngTableParams', 'historyService'];

   function historyController($scope, $filter, _, ngTableParams, historyService) {
      $scope.history = {trades:{}, orders:{}, quotes:{}};
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
               endTime: $scope.endTime && moment($scope.endTime, "YYYY-MM-DD hh:mm:ss").unix() + 1
            }

            $scope.history[type].options = new ngTableParams({
                  page: 1,            
                  count: 10,          
                  sorting: {
                      time: 'asc'     
                  }
              }, {
                  total: 0,
                  getData: function($defer, table) {
                     historyService.getHistory(type, _.assign(params, {offset: table.page() * table.count(), limit: table.count() })).then(function (result) {
                        table.total(result.count);
                        $defer.resolve(result.rows);
                     });
                  }
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