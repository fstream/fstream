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