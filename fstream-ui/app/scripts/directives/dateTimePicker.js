(function () {
   "use strict";

   angular.module('fstream')
      .controller('dateTimeController', dateTimeController)
      .directive('dateTimePicker', dateTimePicker);

   dateTimeController.$inject = ['$scope', '$rootScope'];
   
   function dateTimeController($scope, $rootScope) {
      $scope.vm = {
         message: "Bootstrap DateTimePicker Directive",
         dateTime: {}
      };
   }

   dateTimePicker.$inject = ['$rootScope'];
   
   function dateTimePicker($rootScope) {
      return {
         require: '?ngModel',
         restrict: 'AE',
         scope: {
            pick12HourFormat: '@',
            language: '@',
            useCurrent: '@',
            location: '@'
         },
         link: function (scope, elem, attrs) {
            elem.datetimepicker({
               pick12HourFormat: scope.pick12HourFormat,
               language: scope.language,
               useCurrent: scope.useCurrent,
               format: 'YYYY-mm-DD HH:mm:ss'
            })
         }
      }
   }
})();