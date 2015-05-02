angular
   .module('fstream')
   .factory('historyService', historyService);

function historyService($http, lodash) {
   return {
      getHistory: getHistory,
      getTicks: getTicks
   };

   function getHistory(options) {
      options = options || {}
      var series = 'ticks';
      var where = getWhere(options, ['time', 'symbol']);
      var groupBy = 'symbol'
      var limit = getLimit(options);
      var params = {u: 'root', p: 'root', q: 'SELECT * FROM "' + series + '" ' + where + ' GROUP BY ' + groupBy + ' LIMIT ' + limit};

      return get(params).then(transformPoints);
   };
   
   function getTicks(options) {
      var series = getSeries(options);
      var where = getWhere(options, ['time']);
      var limit = 1000;
      var params = {u: 'root', p: 'root', q: 'SELECT * FROM "' + series + '" ' + where + ' LIMIT ' + limit}; 
      
      return get(params).then(transformPoints);
   }
   
   function getSeries(options) {
      var prefix = options.interval ? 'rollups.1' + options.interval + '.' : ''
      var suffix = options.symbol ? 'ticks.' + options.symbol : 'ticks';
      
      return prefix + suffix;
   }
   
   function getWhere(options, columns) {
      var conditions = [];
      if (options.symbol && lodash.contains(columns, 'symbol')) {
         conditions.push('symbol = \'' + options.symbol + '\'');
      }
      if (options.startTime && lodash.contains(columns, 'time')) {
         conditions.push('time > ' + options.startTime + '');
      }
      if (options.endTime && lodash.contains(columns, 'time')) {
         conditions.push('time < ' + options.endTime + '');
      }      
      
      return conditions.length ? 'WHERE ' + conditions.join(' AND ') : '';
   }

   function getLimit(options) {
      return options.limit || 50;
   }
   
   function transformPoints(result) {
      var data = result.data && result.data[0] || {};
      
      return lodash.map(data.points, function(point) {
         return lodash.zipObject(data.columns, point);
      });
   }
   
   function get(params) {
      var databaseName =  'fstream-events';
      var url = 'http://localhost:8086/db/' + databaseName + '/series';
      
      return $http.get(url, {params: params});
   }
}