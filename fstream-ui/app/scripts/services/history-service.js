angular
   .module('fstream')
   .factory('historyService', historyService);

function historyService($http, lodash) {
   var databaseName =  'fstream-events';
   var url = 'http://localhost:8086/db/' + databaseName + '/series';

   return {
      getHistory: getHistory
   };

   function getHistory(options) {
      options = options || {}
      var series = 'ticks';
      var where = getWhere(options);
      var groupBy = 'symbol'
      var limit = getLimit(options);
      var params = {u: 'root', p: 'root', q: 'SELECT * FROM "' + series + '" ' + where + ' GROUP BY ' + groupBy + ' LIMIT ' + limit};

      return $http.get(url, {params: params}).then(transformPoints);
   };
   
   function getLimit(options) {
      return options.limit || 50;
   }
   
   function getWhere(options) {
      var conditions = [];
      if (options.symbol) {
         conditions.push('symbol = \'' + options.symbol + '\'');
      }
      if (options.startTime) {
         conditions.push('time > \'' + options.startTime + '\'');
      }
      if (options.endTime) {
         conditions.push('time < \'' + options.endTime + '\'');
      }      
      
      return conditions.length ? 'WHERE ' + conditions.join(' AND ') : '';
   }

   function getSeries(symbol) {
      return symbol ? 'ticks.' + symbol : 'ticks';
   }

   function transformPoints(result) {
      var data = result.data && result.data[0] || {};
      
      return lodash.map(data.points, function(point) {
         return lodash.zipObject(data.columns, point);
      });
   }
}