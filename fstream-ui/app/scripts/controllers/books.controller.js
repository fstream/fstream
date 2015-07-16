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
      $scope.symbols = {
         selected: []
      };
      $scope.top = {};
      
      booksService.getTop().then(function(top) {
         $scope.top = top;
      });
   }
})();