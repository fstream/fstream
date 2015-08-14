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
      .factory('settingsService', settingsService);

   settingsService.$inject = ['$http'];

   function settingsService($http) {
      return {
         getSettings: function () {
            return $http.get('/settings').then(function (response) {
               return response.data;
            });
         }
      }
   }
})();