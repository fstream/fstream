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
      .factory('analyticsService', analyticsService);

   analyticsService.$inject = ['$http', '$q', 'lodash'];

   function analyticsService($http, $q, _) {
      var service = {
         executeQuery: executeQuery
      };

      return service;

      function executeQuery(query) {
         // Simulate query
         var results = _.times(20, function(i) {
            return {
               time: '2015-07-07', 
               buyUser: 'user' + i,
               sellUser: 'user' + i + 1,
               buyActive: i % 2 == 0,
               amount: (i + 1) * 10000,
               price: (i + 1) * 100.00
            };
         });

         // Simulate deferred
         return $q.when(results);
      }
   }
})();