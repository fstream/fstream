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