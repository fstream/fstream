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

   alertsCtrl.$inject = ['$scope', '$filter', 'historyService'];

   function alertsCtrl($scope, $filter, historyService) {
      $scope.updateAlerts = updateAlerts;
      $scope.updateTimeRange = updateTimeRange;

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

      activate();

      function activate() {
         updateAlerts();
      }

      function updateAlerts() {
         var params = {
            startTime: $scope.startTime && moment($scope.startTime, "YYYY-MM-DD hh:mm:ss").unix(),
            endTime: $scope.endTime && moment($scope.endTime, "YYYY-MM-DD hh:mm:ss").unix() + 1
         }

         historyService.getAlerts(params).then(function (alerts) {
            $scope.alerts = alerts;
         });
      }

      function updateTimeRange(time) {
         $scope.startTime = $scope.endTime = $filter('date')(time, 'yyyy-MM-dd HH:mm:ss');
      }
   }
})();