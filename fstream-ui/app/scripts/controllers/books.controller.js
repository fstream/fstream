(function () {
   'use strict';
   
   angular
      .module('fstream')
      .controller('booksController', booksController);

   booksController.$inject = ['$scope', 'lodash'];
   
   function booksController($scope, _) {
      $scope.symbols = {
         selected: []
      };
      $scope.top = {
         values: _.times(20, function(i) {
            return {userId: 'user' + i, value: (20 - i)* 1000000};
         }),
         trades: _.times(20, function(i) {
            return {userId: 'user' + i, value: (20 - i)* 1000000};
         }),
         orders: _.times(20, function(i) {
            return {userId: 'user' + i, value: (20 - i)* 1000000};
         }),
         ratios: _.times(20, function(i) {
            return {userId: 'user' + i, value: (20 - i)* 1000000};
         })
      };      
   }
})();