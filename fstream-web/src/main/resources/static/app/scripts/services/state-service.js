angular.module('homer').factory('stateService', ['$http', function($http) {
	return {
		getState: function() {
			return $http.get('/state').then(function(response) {
				return response.data;
			});              
		}
	} 
}]);
