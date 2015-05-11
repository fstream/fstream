(function () {
   'use strict';

   angular
      .module('fstream')
      .controller('adminController', adminController);

   adminController.$inject = ['$scope', 'lodash', 'sweetAlert', 'eventService'];

   function adminController($scope, _, sweetAlert, eventService) {
      $scope.alert = {
         name: '',
         description: '',
         statement: ''
      };

      $scope.aceLoaded = function (editor) {
         // Options
         editor.completers.push({
            getCompletions: function (editor, session, pos, prefix, callback) {
               var activeSymbols = _.map($scope.state.symbols, function (symbol) {
                  return {
                     value: "'" + symbol + "'",
                     score: 1000,
                     meta: "symbol"
                  }
               });

               callback(null, activeSymbols);
            }
         });
      };

      $scope.registerAlert = function (alert) {
         $scope.isSaving = true;
         eventService.register(alert).then(function(){
            sweetAlert.swal({
               title: "Registration Successful",
               text: 'Alert "' + alert.name + '" was registered',
               type: "success",
               confirmButtonColor: "#62cb31"
            })
            $scope.isSaving = false;
         });
      }
   }
})();