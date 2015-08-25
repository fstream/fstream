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
      // Export
      $scope.updateHistory = updateHistory;
      $scope.updateTimeRange = updateTimeRange;
      $scope.updateSymbol = updateSymbol;
      $scope.resetParams = resetParams;
      
      var eventTypes = ['trades', 'orders', 'quotes'];

      // Init
      activate();

      function activate() {
         $scope.history = {trades:{}, orders:{}, quotes:{}};

         // Order matters
         createTables();
         resetParams();
         updateAvailableSymbols();
      }

      function createTables() {
         eventTypes.forEach(function(eventType){
            $scope.history[eventType].tableParams = new ngTableParams({
               page: 1,            
               count: 10
            }, {
               total: 0,
               getData: function($defer, table) {
                  var params = _.assign(getParams(), {
                     offset: (table.page() -  1)  * table.count(), 
                     limit: table.count()
                  });
                  
                  historyService.getHistory(eventType, params).then(function (result) {
                     table.total(result.count);
                     $defer.resolve(result.rows);
                  });
               }
            });
         });
      }
      
      function getParams() {
         return {
            symbol: _.get($scope, 'symbols.selected[0].name'),
            startTime: $scope.startTime && moment($scope.startTime, "YYYY-MM-DD hh:mm:ss").unix(),
            endTime: $scope.endTime && moment($scope.endTime, "YYYY-MM-DD hh:mm:ss").unix() + 1
         };
      }      
      
      function resetParams() {
         $scope.startTime = null;
         $scope.endTime = null;
         $scope.symbols = {
            selected: []
         };
         eventTypes.forEach(function(eventType) {
            $scope.history[eventType].tableParams.parameters({page: 1});
         })
         
         updateHistory();
      }

      function updateTimeRange(time) {
         $scope.startTime = $scope.endTime = $filter('date')(time, 'yyyy-MM-dd HH:mm:ss');
         updateHistory();
      }
      
      function updateHistory() {
         eventTypes.forEach(function(eventType){
            $scope.history[eventType].tableParams.reload();
         });
      }
      
      /**
       * Symbols
       */

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