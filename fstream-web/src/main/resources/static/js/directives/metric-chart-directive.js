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
			    color = '#FF0000',
			    opacity =  0.5 - (index / 6.0)* 0.5,
			    size = 50,
			    enabled = true;
			
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
			    	step: true,
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
			
		    $scope.$on('rate', function(e, rate) {
		    	if (rate.symbol !== $scope.options.symbol) {
		    		return;
		    	}
		    	
	    		var shift = chart.series[0].data.length >= size,
    		    	animate = false;
	    		
	    		chart.series[0].addPoint([rate.dateTime, (rate.ask + rate.bid)/2.0], false, shift, animate);
	    		chart.series[1].addPoint([rate.dateTime, rate.bid, rate.ask], enabled, shift, animate);
		    });
		}
	}
})