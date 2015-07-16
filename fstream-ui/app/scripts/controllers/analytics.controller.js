(function () {
   'use strict';
   
   angular
      .module('fstream')
      .controller('analyticsController', analyticsController);

   analyticsController.$inject = ['$scope', 'lodash'];
   
   function analyticsController($scope, _) {
      $scope.results = _.times(20, function(i) {
         return {
            time: '2015-07-07', 
            buyUser: 'user' + i,
            sellUser: 'user' + i + 1,
            buyActive: i % 2 == 0,
            amount: (i + 1) * 10000,
            price: (i + 1) * 100.00
         };
      })
   }
})();