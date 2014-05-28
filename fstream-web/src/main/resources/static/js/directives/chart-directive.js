angular.module('FStreamApp.directives').directive('chart', function() {
	Highcharts.setOptions({
		global : {
			useUTC : false
		}
	});			
	
	return {
		restrict : 'E',
		scope: {
	      options: '='
	    },
	    replace: true,
		template : '<div class="chart"></div>',
		link: function($scope, $element, $attr){
			var chart,
			    size = 50,
			    enabled = true,
			    series;
			
			chart = new Highcharts.Chart({
		        chart: {
		            renderTo: $element[0],
		            height: 325,
		            width: 550,
		            animation: false
		        },
		        
				credits: {
					enabled: false
				},
		        
		        title: {
		        	text: $scope.options.symbol
		        },
		        
		        yAxis: {
		        	title: {
		        		text: "Price"
		        	}
		        },
		        
	            xAxis: {
	                type: 'datetime'
	            },
	            
		        tooltip: {
		            crosshairs: [true, true],
		            shared: true
		        },			    
		        
				series: [{
					id: 'Ask',
					name : 'Ask',
					step: true,
					data : []
				}, {
					name : 'Bid',
					step: true,
					data : []
				}]
			});
			
		    $scope.$on('rate', function(e, rate) {
		    	if (rate.symbol !== $scope.options.symbol) {
		    		return;
		    	}
		    	
	    		var shift = chart.series[0].data.length >= size,
    		    	animate = false;
	    		
	    		chart.series[0].addPoint([rate.dateTime, rate.ask], false, shift, animate);
	    		chart.series[1].addPoint([rate.dateTime, rate.bid], enabled, shift, animate);
		    });
		}
	}
})