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
			    index = $scope.options.index,
			    colors = Highcharts.getOptions().colors,
			    color = colors[(index + 5)  % colors.length],
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
			    	name: 'Price',
			    	data: [],
			    	zIndex: 1,
			    	lineColor: color,
			    	marker: {
			    		fillColor: 'white',
			    		lineWidth: 2,
			    		lineColor: color
			    	}
				}, {
			        name: 'Spread',
			        data: [],
			        type: 'arearange',
			        lineWidth: 0.5,
			    	linkedTo: ':previous',
			    	color: color,
			    	fillOpacity: 0.3,
			    	zIndex: 0
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