angular.module('FStreamApp.directives').directive('epl', function() {
	return {
		restrict : 'E',
		scope: {
	      options: '='
	    },
	    replace: true,
		template : '<pre><code class="sql"></code></pre>',
		link: function($scope, $element, $attr){
			$element.find("code").text($attr.statement);
			hljs.highlightBlock($element[0]);
		}
	}
})