(function () {
   'use strict';
   
   angular
      .module('fstream')
      .controller('adminController', adminController);

   adminController.$inject = ['$scope'];
   
   function adminController($scope) {
      $scope.test = 'foo'
   }
})();