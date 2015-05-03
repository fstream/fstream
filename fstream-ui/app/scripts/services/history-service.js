angular
   .module('fstream')
   .factory('historyService', historyService);

function historyService($http, lodash) {
   return {
      getMetrics: getMetrics,
      getHistory: getHistory,
      getTicks: getTicks
   };


   function getMetrics(params) {
      var series = 'metrics';
      var limit = 1000;
      var query = 'SELECT * FROM "' + series + '"' + ' LIMIT ' + limit;

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
      if (params.symbol && lodash.contains(columns, 'symbol')) {
         conditions.push('symbol = \'' + params.symbol + '\'');
      }
      if (params.startTime && lodash.contains(columns, 'time')) {
         conditions.push('time > ' + params.startTime + '');
      }
      if (params.endTime && lodash.contains(columns, 'time')) {
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

      return lodash.map(data.points, function(point) {
         return lodash.zipObject(data.columns, point);
      });
   }
}