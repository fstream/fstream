angular.module('FStreamApp.services', []).
factory('ratesService', function($rootScope, $timeout) {
	
	//
	// Private
	//
	
	var stompClient,
		publishEvent = function (eventName, frame){
			$timeout(function() {
				var event = frame && angular.fromJson(frame.body);
	        	$rootScope.$broadcast(eventName, event); 
			});
		};
 
	return {
		
		//
		// Methods
		//
		
		connect: function() {
			stompClient = Stomp.over(new SockJS('/server'));
			
			stompClient.connect({}, function(frame) {
				publishEvent("connected");
				
		        stompClient.subscribe('/topic/rates', function(frame){
		        	publishEvent("rate", frame);
		        });
		        
		        stompClient.subscribe('/topic/alerts', function(frame){
		        	publishEvent("alert", frame);
		        });
		        
		        stompClient.subscribe('/topic/commands', function(frame){
		        	publishEvent("command", frame);
		        });                
		    });
			
		},
		
	    disconnect: function() {
	        stompClient.disconnect();
	        
	        publishEvent("disconnected");
	    },
	    
	    register: function(instrument){
		    stompClient.send("/web/register", {}, angular.toJson({ 'instrument': instrument }));
	    }
	    
	} 
}).
factory('chartService', function($rootScope) {
	Highcharts.setOptions({
		global : {
			useUTC : false
		}
	});
	
	var size = 50, 
	    data = [], 
	    chart,
	    series;
	
	while(size--) data.push(0);
	
	return {
		init: function() {
			// Create the chart
			chart = new Highcharts.StockChart({
				
				credits: {
					enabled: false
				},
				
				chart : {
					type: 'spline',
					renderTo: 'charts-container',
					events : {
						load : function() {
							series = this.series;
						}
					},
					
					height: 600,
					zoomType: 'xy'
				},
				
				rangeSelector: {
					buttons: [{
						count: 1,
						type: 'minute',
						text: '1M'
					}, {
						count: 5,
						type: 'minute',
						text: '5M'
					}, {
						type: 'all',
						text: 'All'
					}],
					inputEnabled: false,
					selected: 0
				},
				
	            xAxis: {
	                type: 'datetime',
	                tickPixelInterval: 150
	            },
	            
		        tooltip: {
		            crosshairs: [true, true]
		        },			    
			    
				series : [{
					id: 'Ask',
					name : 'Ask',
					data : data
				}, {
					name : 'Bid',
					data : data
				}, {
					type: 'flags',
					color: '#C12E2A',
					onSeries: 'Ask',
					fillColor: '#D9534F',
			        width: 25,
			        style: {
			        	color: 'white'
			        },
			        states: {
			        	hover: {
			        		fillColor: '#C12E2A'
			        	}
			        }			        
				}]
			});
		},
		
    	addRate: function(rate) {
    		series[0].addPoint([rate.dateTime, rate.ask], true, true);
    		series[1].addPoint([rate.dateTime, rate.bid], true, true);
    	},
    	
		addAlert: function(alert) {
			series[2].addPoint({
				x: new Date().getTime(),
				title: 'Alert',
				text: angular.toJson(alert)
			}, true, false);
		}
    };	
});