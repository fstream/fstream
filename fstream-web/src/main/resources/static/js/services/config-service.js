angular.module('FStreamApp.services').factory('configService', ['$http', function($http) {
	return {
		getConfig: function() {
			return $http.get('/config').then(function(response) {
				return response.data.symbols;
			});              
		}
	} 
}]);
