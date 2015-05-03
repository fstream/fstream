angular
   .module('fstream')
   .factory('stateService', ['$http', stateService]);

function stateService($http) {
	return {
		getState: function() {
			return $http.get('/state').then(function(response) {
				return response.data;
			});              
		}
	} 
}
