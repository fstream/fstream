var app = angular.module('prism',[]);

app.directive('prism',[function () {
   "use strict";
   return {
      restrict: "E",
      scope: {},
      transclude: true,
      replace: true,
      template: '<pre><code ng-transclude></code></pre>',
      link: function ($scope, element, attrs) {
         element.ready(function() {
            element.first().text(attrs.code);
            Prism.highlightElement(element[0]);
         });
      }
   };
}]);