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
            startTime: $scope.startTime && (moment($scope.startTime, "YYYY-MM-DD hh:mm:ss").unix() - 1,
            endTime: $scope.endTime && moment($scope.endTime, "YYYY-MM-DD hh:mm:ss").unix()
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