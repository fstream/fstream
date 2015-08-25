/* 
 * Copyright (c) 2015 fStream. All Rights Reserved.
 * 
 * Project and contact information: https://bitbucket.org/fstream/fstream
 * 
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

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
         link: function (scope, elem, attrs, ngModelCtrl) {
            var picker = elem.datetimepicker({
               format: 'YYYY-MM-DD HH:mm:ss'
            });
            ngModelCtrl.$render(function() {
               picker.data('DateTimePicker').date(ngModelCtrl.$modelValue || '');
            });
            picker.on('dp.change', function(e) {
               scope.$apply(function(){
                  ngModelCtrl.$setViewValue(moment(e.date).format('YYYY-MM-DD HH:mm:ss'));
               });
            });         
         }
      }
   }
})();