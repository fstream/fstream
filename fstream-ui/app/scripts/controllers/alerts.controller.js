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
      .controller('alertsCtrl', alertsCtrl);

   alertsCtrl.$inject = ['$scope', '$filter', 'ngTableParams', 'lodash', 'stateService', 'historyService'];

   function alertsCtrl($scope, $filter, ngTableParams, _, stateService, historyService) {
      $scope.updateAlerts = updateAlerts;
      $scope.updateTimeRange = updateTimeRange;
      
      $scope.resetParams = resetParams;
      $scope.alertDefs = _.indexBy(stateService.getCachedState().alerts, 'id');

      $scope.chartIncomeData = [{
         label: "line",
         data: [[1, 10], [2, 26], [3, 16], [4, 36], [5, 32], [6, 51]]
      }];
      $scope.chartIncomeOptions = {
         series: {
            lines: {
               show: true,
               lineWidth: 0,
               fill: true,
               fillColor: "#64cc34"

            }
         },
         colors: ["#62cb31"],
         grid: {
            show: false
         },
         legend: {
            show: false
         }
      };
      $scope.pieChartDataDas = [
         { label: "Mispricing", data: 16, color: "#62cb31", },
         { label: "Arbitrage", data: 6, color: "#A4E585", },
         { label: "Bursty Price", data: 22, color: "#368410", },
         { label: "Stale Price", data: 32, color: "#8DE563", }
      ];
      $scope.pieChartOptions = {
         series: {
            pie: {
               show: true
            }
         },
         grid: {
            hoverable: true
         },
         tooltip: true,
         tooltipOpts: {
            content: "%p.0%, %s", // show percentages, rounding to 2 decimal places
            shifts: {
               x: 20,
               y: 0
            },
            defaultTheme: false
         }
      };
      
      $scope.tableParams = new ngTableParams({
            page: 1,            
            count: 10,          
            sorting: {
                time: 'asc'     
            }
        }, {
            total: 0,
            getData: function($defer, table) {
               historyService.getHistory('alerts', _.assign(getParams(), {offset: table.page() * table.count(), limit: table.count() })).then(function (result) {
                  table.total(result.count);
                  
                  var rows = _.map(result.rows, function(alert) {
                     alert.name = ($scope.alertDefs[alert.id] || {}).name || 'Unknown';
                     return alert;
                  });
                  
                  $defer.resolve(rows);
               });
            }
        });      
      
      function resetParams() {
         $scope.startTime = null;
         $scope.endTime = null;
         $scope.tableParams.parameters({page: 1});
         updateAlerts();
      }      

      function getParams() {
         return {
            symbol: _.get($scope, 'symbols.selected[0].name', null),
            startTime: $scope.startTime && moment($scope.startTime, "YYYY-MM-DD hh:mm:ss").unix(),
            endTime: $scope.endTime && moment($scope.endTime, "YYYY-MM-DD hh:mm:ss").unix() + 1
         };
      }
      
      function updateAlerts() {
         $scope.tableParams.reload();
      }

      function updateTimeRange(time) {
         resetParams();
         $scope.startTime = $scope.endTime = $filter('date')(time, 'yyyy-MM-dd HH:mm:ss');
         updateAlerts();
      }
   }
})();