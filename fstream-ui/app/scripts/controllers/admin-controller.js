(function () {
   'use strict';

   angular
      .module('fstream')
      .controller('adminController', adminController);

   adminController.$inject = ['$scope', 'lodash'];

   function adminController($scope, _) {
      $scope.aceLoaded = function (editor) {
         // Options
         editor.completers.push({
            getCompletions: function(editor, session, pos, prefix, callback) {
               var activeSymbols = _.map($scope.state.symbols, function(symbol) {
                  return {value: "'" + symbol + "'", score: 1000, meta: "symbol"}
               });
               
               callback(null, activeSymbols);
            }
         });

      };
   }
})();