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

   dashboardController.$inject = ['$scope', 'historyService'];
   
   function dashboardController($scope, historyService) {
      var eventCount = 0;
      var startTime = new Date().getTime();
      var averageLast20Delay = movingAverage(100 /* events */);
      
      // Initialize
      $scope.orderCount = 0;
      $scope.alertCount = 0;
      $scope.eventRate = 0;
      $scope.timeDelay = 0;
      
      historyService.getTodaysAlertCount().then(function(alertCount){
         $scope.alertCount = alertCount || 0;
      });
      
      // Update dashboard metrics on events
      $scope.$on('alert', function () {
         $scope.alertCount++;
      });
      $scope.$on('order', function(e, order) {
         $scope.orderCount++;
         var delay = (order.processedTime - order.dateTime) / 1000.0;
         $scope.timeDelay = averageLast20Delay(delay);
      });
      ['alert', 'metric', 'trade', 'order', 'quote'].forEach(function(eventType) {
         $scope.$on(eventType, function() {
            eventCount++;
            var elapsed = (new Date().getTime() - startTime) / 1000;
            $scope.eventRate = eventCount / elapsed;
         });
      });
   }
   
   function movingAverage(period) {
      var nums = [];
      return function(num) {
         nums.push(num);
         if (nums.length > period)
            nums.splice(0,1);  // remove the first element of the array
         var sum = 0;
         for (var i in nums)
            sum += nums[i];
         var n = period;
         if (nums.length < period)
            n = nums.length;
         return(sum/n);
      }
   }
})();