angular.module('FStreamApp.directives').directive('chart', function() {
	Highcharts.setOptions({
		global : {
			useUTC : false
		}
	});
	
	var hashCode = function(value) {
		var hash = 0;
		if (value.length == 0) return hash;
		for (i = 0; i < value.length; i++) {
			char = value.charCodeAt(i);
			hash = ((hash<<5)-hash)+char;
			hash = hash & hash; // Convert to 32bit integer
		}
		
		return hash;
	}
	
	return {
		restrict : 'E',
		scope: {
	      options: '='
	    },
	    replace: true,
		template : '<div class="chart"></div>',
		link: function($scope, $element, $attr){
			var colors = Highcharts.getOptions().colors;
			var chart,
			    color = colors[hashCode($scope.options.symbol) % colors.length],
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
					id: 'Ask',
					name : 'Ask',
					step: true,
					data : [],
					color: color
				}, {
					name : 'Bid',
					step: true,
					data : [],
					color: "#000"
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