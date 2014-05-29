angular.module('FStreamApp', [
  'FStreamApp.controllers',
  'FStreamApp.directives',
  'FStreamApp.services',
  'ngSanitize',
  'ui.bootstrap',
  'ui.select',
  'underscore'
]);
angular.module('FStreamApp.controllers', ['ngTable']);
angular.module('FStreamApp.services', []);
angular.module('FStreamApp.directives', []);
angular.module('underscore', []).factory('_', function() {
	return window._; // assumes underscore has already been loaded on the page
});  