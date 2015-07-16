(function () {
   'use strict';
   
   angular
      .module('fstream')
      .controller('booksController', booksController);

   booksController.$inject = ['$scope'];
   
   function booksController($scope) {
      $scope.symbols = {
         selected: []
      };
   }
})();