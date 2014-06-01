angular.module('FStreamApp.directives').directive('metricChart', function() {
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
		template : '<div class="metric-chart"></div>',
		link: function($scope, $element, $attr){
			var chart,
			    index = $scope.options.index,
			    colors = Highcharts.getOptions().colors,
			    color = colors[0],
			    opacity =  0.5,
			    size = 50,
			    enabled = true;
			
			chart = new Highcharts.Chart({
		        chart: {
		            renderTo: $element[0],
		            height: 325,
		            width: 550,
		            type: 'area',
		            animation: false
		        },
		        
				credits: {
					enabled: false
				},
		        
		        title: {
		        	text: $scope.options.title
		        },
		        
		        yAxis: {
		        	title: {
		        		text: $scope.options.units
		        	},
	                alternateGridColor: '#FDFDfD'
		        },
		        
	            xAxis: {
	                type: 'datetime'
	            },
	            
		        tooltip: {
		            crosshairs: [true, true],
		            shared: true
		        },	
		        
				series: [{
			    	name: $scope.options.name,
			    	data: [],
			    	zIndex: 1,
			    	color: color,
			    	lineColor: color,
			    	marker: {
			    		fillColor: 'white',
			    		lineWidth: 2,
			    		radius: 3,
			    		lineColor: color
			    	}
				}]
			});
			
		    $scope.$on('metric', function(e, metric) {
		    	if (metric.id !== $scope.options.id) {
		    		return;
		    	}
		    	
	    		var shift = chart.series[0].data.length >= size,
    		    	animate = false;
	    		
	    		chart.series[0].addPoint([metric.dateTime, metric.data.count], enabled, shift, animate);
		    });
		}
	}
})