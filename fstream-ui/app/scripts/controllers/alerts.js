(function () {
   'use strict';

   angular
      .module('fstream')
      .controller('alertsCtrl', alertsCtrl);

   alertsCtrl.$inject = ['$scope', 'historyService'];

   function alertsCtrl($scope, historyService) {
      $scope.updateAlerts = updateAlerts;

      activate();

      function activate() {
         updateAlerts();
      }

      function updateAlerts() {
         var params = {};
         historyService.getAlerts(params).then(function (alerts) {
            $scope.alerts = alerts;
         });
      }
   }
})();