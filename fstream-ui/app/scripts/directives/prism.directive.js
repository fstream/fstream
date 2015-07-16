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
      .directive('prism', prism);

   function prism() {
      return {
         restrict: "E",
         scope: {},
         transclude: true,
         replace: true,
         template: '<pre><code ng-transclude></code></pre>',
         link: function ($scope, element, attrs) {
            element.ready(function () {
               element.first().text(attrs.code);
               Prism.highlightElement(element[0]);
            });
         }
      };
   }
})();