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
      .controller('booksController', booksController);

   booksController.$inject = ['$scope', 'lodash', 'booksService'];

   function booksController($scope, _, booksService) {
      $scope.symbol = 'RY';
      $scope.symbols = {
         selected: []
      };
      $scope.snapshot = {};
      $scope.top = {};

      booksService.getTop().then(function(top) {
         $scope.top = top;
      });
      
      $scope.$on('snapshot', function (e, snapshot) {
         if ($scope.symbol == snapshot.symbol) {
            $scope.snapshot = snapshot;
         }
      });

      $scope.$on('metric', function (e, metric) {
         if (metric.id == 10) {
            $scope.top.values = metric.data
         }
         if (metric.id == 11) {
            $scope.top.trades = metric.data
         }    
         if (metric.id == 12) {
            $scope.top.orders = metric.data
         }            
         if (metric.id == 13) {
            $scope.top.ratios = metric.data
         }         
      });
   }
})();