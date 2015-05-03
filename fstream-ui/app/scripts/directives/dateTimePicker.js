angular.module('fstream')
   .controller('dateTimeController', ['$scope', '$http', dateTimeController])
   .directive('dateTimePicker', dateTimePicker);

function dateTimeController($scope, $rootScope) {
   $scope.vm = {
      message: "Bootstrap DateTimePicker Directive",
      dateTime: {}
   };
}

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
            useCurrent: scope.useCurrent
         })
      }
   }
}