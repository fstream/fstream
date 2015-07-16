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
      .directive('touchSpin', touchSpin);

   /**
    * touchSpin - Directive for Bootstrap TouchSpin
    */
   function touchSpin() {
      return {
         restrict: 'A',
         scope: {
            spinOptions: '='
         },
         link: function (scope, element, attrs) {
            scope.$watch(scope.spinOptions, function () {
               render();
            });
            var render = function () {
               $(element).TouchSpin(scope.spinOptions);
            };
         }
      };
   }
})();