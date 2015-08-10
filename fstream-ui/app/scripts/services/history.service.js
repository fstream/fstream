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
         getHistory: getHistory,
         getTicks: getTicks,
         getTrades: getTrades,
         getOrders: getOrders,
         getQuotes: getQuotes
      };

      return service;

      function getSymbols() {
         return executeQuery('SHOW TAG VALUES FROM quotes WITH KEY = symbol', function (result) {
            return _.get(result, 'data[0].series[0].values[0]');
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
         var series = params.interval ? 'quotes_1' + params.interval : 'quotes'
         var where = getWhere(params, ['time']);
         var limit = 1000;
         var query = 'SELECT * FROM "' + series + '" ' + where + ' LIMIT ' + limit;

         return executeQuery(query);
      }
      
      function getTrades(params) {
         return getHistory('trades', params);
      }
      
      function getOrders(params) {
         return getHistory('orders', params);
      }      
      
      function getQuotes(params) {
         return getHistory('quotes', params);
      }      

      function getHistory(series, params) {
         params = params || {};
         var where = getWhere(params, ['time', 'symbol']);
         var limit = getLimit(params);
         var offset = getOffset(params);
         var query = 'SELECT * FROM "' + series + '" ' + where + ' LIMIT ' + limit + ' OFFSET ' + offset;
         var column = series === 'quotes' ? 'ask' : 'amount';
         
         return executeQuery('SELECT COUNT(' + column + ') FROM "' + series + '" ' + where).then(function(count){
            return executeQuery(query).then(function(history){
               return {
                  start: offset,
                  size: limit,
                  count: count && count.length ? count[0].count : 0,
                  rows: history
               };
            });
         });
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
      
      function getOffset(params) {
         return params.offset || 0;
      }

      function executeQuery(query, transformer) {
         return $http.get('/history', {
            params: {query: query}
         }).then(transformer || transformPoints);
      }

      function transformPoints(result) {
         var data = _.get(result, 'data[0].series[0]', {columns:[], tags:{}, values: []});
         
         var points = [];
         if (data == null) {
            return points;
         }
        
         for (var i = 0; i < data.values.length; i++) {
            var values = data.values[i];
            
            var point = {};
            for(var j = 0; j < data.columns.length; j++) {
               point[data.columns[j]] = values[j];
            }
            
            points.push(_.assign(point, data.tags));
         }
         
         return points;
      }
   }
})();