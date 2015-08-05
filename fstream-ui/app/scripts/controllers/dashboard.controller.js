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
      .controller('dashboardController', dashboardController);

   dashboardController.$inject = ['$scope'];
   
   function dashboardController($scope, historyService) {
      var eventCount = 0;
      var startTime = new Date().getTime();
      
      // Initialize
      $scope.alertCount = 0;
      $scope.eventRate = 0;
      $scope.timeDelay = 0;
      
      // Update dashboard metrics on events
      $scope.$on('alert', function () {
         $scope.alertCount++;
      });
      $scope.$on('order', function(e, order) {
         var delay = (order.processedTime - order.dateTime) / 1000;
         $scope.timeDelay = delay;
      });
      ['alert', 'metric', 'trade', 'order', 'quote'].forEach(function(eventType) {
         $scope.$on(eventType, function() {
            eventCount++;
            var elapsed = (new Date().getTime() - startTime) / 1000;
            $scope.eventRate = eventCount / elapsed;
         });
      });
   }
})();