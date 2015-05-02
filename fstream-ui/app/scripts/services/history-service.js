angular
   .module('fstream')
   .factory('historyService', historyService);

function historyService($http, lodash) {
   var config = {
      databaseName: 'fstream-events'
   }

   return {
      getHistory: function(options) {
         options = options || {}
         var limit = options.limit || 10;
         var series = options.symbol ? 'ticks.' + options.symbol : 'ticks';
         var where = options.symbol ? 'WHERE symbol = \'' + options.symbol + '\'' : '';
         var params = {u: 'root', p: 'root', q: 'SELECT * FROM "ticks" ' + where + ' GROUP BY symbol LIMIT ' + limit};

         return  $http.get('http://localhost:8086/db/' + config.databaseName + '/series', { params: params} ).then(transform);
      }
   };

   function transform(result) {
      var data = result.data && result.data[0] || {};
      return lodash.map(data.points, function(point) {
         var tick = {};
         for(var i = 0; i < data.columns.length; i++ ) {
            tick[data.columns[i]] = point[i];
         }

         return tick;
      });
   }
}