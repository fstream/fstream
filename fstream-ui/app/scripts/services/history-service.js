angular
   .module('fstream')
   .factory('historyService', ['$http', 'lodash', historyService]);

function historyService($http, _) {
   return {
      getMetrics: getMetrics,
      getHistory: getHistory,
      getTicks: getTicks,
      getAvailableSymbols: getAvailableSymbols
   };

   function getAvailableSymbols() {
      return executeQuery('LIST SERIES').then(function(result){
         return _.compact(_.map(result, function(series) {
            var match = /^ticks\.([^.]+)/.exec(series.name);
            return match && {name: match[1]};
         }));
      });
   }

   function getMetrics(params) {
      var series = 'metrics';
      var limit = 1000;
      var where = params.id ? ' WHERE id = ' + params.id + ' '  : '';
      var query = 'SELECT * FROM "' + series + '"' + where + ' LIMIT ' + limit;

      return executeQuery(query);      
   }

   function getHistory(params) {
      params = params || {};
      var series = 'ticks';
      var where = getWhere(params, ['time', 'symbol']);
      var groupBy = 'symbol'
      var limit = getLimit(params);
      var query = 'SELECT * FROM "' + series + '" ' + where + ' GROUP BY ' + groupBy + ' LIMIT ' + limit;

      return executeQuery(query);
   };

   function getTicks(params) {
      var series = getSeries(params);
      var where = getWhere(params, ['time']);
      var limit = 1000;
      var query = 'SELECT * FROM "' + series + '" ' + where + ' LIMIT ' + limit; 

      return executeQuery(query);
   }

   function getSeries(params) {
      var prefix = params.interval ? 'rollups.1' + params.interval + '.' : ''
      var suffix = params.symbol ? 'ticks.' + params.symbol : 'ticks';

      return prefix + suffix;
   }

   function getWhere(params, columns) {
      var conditions = [];
      if (params.symbol && _.contains(columns, 'symbol')) {
         conditions.push('symbol = \'' + params.symbol + '\'');
      }
      if (params.startTime && _.contains(columns, 'time')) {
         conditions.push('time > ' + params.startTime + '');
      }
      if (params.endTime && _.contains(columns, 'time')) {
         conditions.push('time < ' + params.endTime + '');
      }      

      return conditions.length ? 'WHERE ' + conditions.join(' AND ') : '';
   }

   function getLimit(params) {
      return params.limit || 50;
   }

   function executeQuery(query) {
      var databaseName =  'fstream-events';
      var url = 'http://localhost:8086/db/' + databaseName + '/series';
      var params = {u: 'root', p: 'root', q: query};

      return $http.get(url, {params: params}).then(transformPoints);
   }

   function transformPoints(result) {
      var data = result.data && result.data[0] || {};

      return _.map(data.points, function(point) {
         return _.zipObject(data.columns, point);
      });
   }
}