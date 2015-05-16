(function () {
   'use strict';

   angular
      .module('fstream')
      .factory('stateService', stateService);

   stateService.$inject = ['$http'];

   function stateService($http) {
      return {
         getState: function () {
            return $http.get('/state').then(function (response) {
               return response.data;
            });
         }
      }
   }
})();