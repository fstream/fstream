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
      .factory('userService', userService);

   userService.$inject = ['$http', 'lodash'];

   function userService($http, _) {
      var service = {
         login: login
      };

      return service;

      function login(params) {
      }
   }
})();