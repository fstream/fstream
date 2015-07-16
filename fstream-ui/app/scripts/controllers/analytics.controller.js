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
      .controller('analyticsController', analyticsController);

   analyticsController.$inject = ['$scope', 'analyticsService'];
   
   function analyticsController($scope, analyticsService) {
      analyticsService.executeQuery({}).then(function(results) {
         $scope.results = results;
      });
   }
})();