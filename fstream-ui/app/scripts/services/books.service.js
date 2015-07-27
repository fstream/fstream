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
      .factory('booksService', booksService);

   booksService.$inject = ['$http', '$q', 'lodash', 'historyService'];

   function booksService($http, $q, _, historyService) {
      var service = {
         getTop: getTop
      };

      var metricIds = {
         topUserValues: 10
      };

      return service;

      function getTop() {
         return historyService.getLastMetric({
            id: metricIds.topUserValues
         }).then(function (values) {
            // TODO: Hook up other metrics!
            return {
               values: values,
               trades: _.times(20, function (i) {
                  return {
                     userId: 'user' + i + 1,
                     value: (20 - i) * 10000
                  };
               }),
               orders: _.times(20, function (i) {
                  return {
                     userId: 'user' + i + 2,
                     value: (20 - i) * 100000
                  };
               }),
               ratios: _.times(20, function (i) {
                  return {
                     userId: 'user' + i + 3,
                     value: (20 - i) * 1
                  };
               })
            };
         });
      }
   }
})();