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
      .controller('chartController', chartController);

   chartController.$inject = ['$scope', 'lodash', 'historyService'];
   
   function chartController($scope, _, historyService) {
      // TODO: Implement
      $scope.enableChart = angular.noop;
      $scope.disableChart = angular.noop;
      
      $scope.quoteSymbols = ['EUR/USD', 'USD/JPY','USD/CAD', 'RY', 'BMO', 'TD'];
   }
})();