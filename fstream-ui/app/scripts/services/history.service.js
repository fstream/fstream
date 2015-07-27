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
      .factory('historyService', historyService);

   historyService.$inject = ['$http', 'lodash'];

   function historyService($http, _) {
      var service = {
         getSymbols: getSymbols,
         getMetrics: getMetrics,
         getLastMetric: getLastMetric,
         getAlerts: getAlerts,
         getTicks: getTicks,
         getHistory: getHistory
      };

      return service;

      function getSymbols() {
         return executeQuery('LIST SERIES').then(function (result) {
            return _.compact(_.map(result, function (series) {
               var match = /^quotes\.([^.]+)/.exec(series.name);
               return match && {
                  name: match[1]
               };
            }));
         });
      }

      function getMetrics(params) {
         var series = 'metrics';
         var limit = 1000;
         var where = params.id ? ' WHERE id = ' + params.id + ' ' : '';
         var query = 'SELECT * FROM "' + series + '"' + where + ' LIMIT ' + limit;

         return executeQuery(query);
      }
      
      function getLastMetric(params) {
         var series = 'metrics';
         var limit = 1;
         var where = ' WHERE id = ' + params.id + ' ';
         var query = 'SELECT * FROM "' + series + '"' + where + ' LIMIT ' + limit;

         return executeQuery(query, function(result) {
            return JSON.parse(_.get(result, 'data[0].rows[0].data', '[]'));
         });
      }      

      function getAlerts(params) {
         var series = 'alerts';
         var limit = 50;
         var where = getWhere(params, ['id', 'time']);
         var query = 'SELECT * FROM "' + series + '" ' + where + ' LIMIT ' + limit;

         return executeQuery(query);
      }

      function getTicks(params) {
         var series = getSeries(params);
         var where = getWhere(params, ['time']);
         var limit = 1000;
         var query = 'SELECT * FROM "' + series + '" ' + where + ' LIMIT ' + limit;

         return executeQuery(query);
      }

      function getHistory(params) {
         params = params || {};
         var series = 'quotes';
         var where = getWhere(params, ['time', 'symbol']);
         var groupBy = 'symbol';
         var limit = getLimit(params);
         var query = 'SELECT * FROM "' + series + '" ' + where + ' GROUP BY ' + groupBy + ' LIMIT ' + limit;

         return executeQuery(query);
      }

      function getSeries(params) {
         var prefix = params.interval ? 'rollups.1' + params.interval + '.' : ''
         var suffix = params.symbol ? 'quotes.' + params.symbol : 'quotes';

         return prefix + suffix;
      }

      function getWhere(params, columns) {
         var conditions = [];
         if (params.id && _.contains(columns, 'id')) {
            conditions.push('id = \'' + params.id + '\'');
         }
         if (params.symbol && _.contains(columns, 'symbol')) {
            conditions.push('symbol = \'' + params.symbol + '\'');
         }
         if (params.startTime && _.contains(columns, 'time')) {
            conditions.push('time > ' + params.startTime + 's');
         }
         if (params.endTime && _.contains(columns, 'time')) {
            conditions.push('time < ' + params.endTime + 's');
         }

         return conditions.length ? 'WHERE ' + conditions.join(' AND ') : '';
      }

      function getLimit(params) {
         return params.limit || 50;
      }

      function executeQuery(query, transformer) {
         return $http.get('/history', {
            params: {query: query}
         }).then(transformer || transformPoints);
      }

      function transformPoints(result) {
         var data = _.get(result, 'data[0]', {});

         return data.rows;
      }
   }
})();