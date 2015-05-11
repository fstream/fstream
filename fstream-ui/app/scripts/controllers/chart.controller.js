(function () {
   'use strict';
   
   angular
      .module('fstream')
      .controller('chartController', chartController);

   chartController.$inject = ['$scope', 'historyService'];
   
   function chartController($scope, historyService) {
      // TODO: Implement
      $scope.enableChart = angular.noop;
      $scope.disableChart = angular.noop;
   }
})();