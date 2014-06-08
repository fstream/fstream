angular.module('FStreamApp.services').factory('eventService', ['$rootScope', '$timeout', function($rootScope, $timeout) {
	
	var stompClient,
		publishEvent = function (eventName, frame){
			$timeout(function() {
				var event = frame && angular.fromJson(frame.body);
	        	$rootScope.$broadcast(eventName, event); 
			});
		};
 
	return {
		
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
		        
		        stompClient.subscribe('/topic/metrics', function(frame){
		        	publishEvent("metric", frame);
		        });
		                     
		        stompClient.subscribe('/topic/state', function(frame){
		        	publishEvent("state", frame);
		        });                
		    });
			
		},
		
	    disconnect: function() {
	        stompClient.disconnect();
	        
	        publishEvent("disconnected");
	    },
	    
	    register: function(alert){
		    stompClient.send("/web/register", {}, angular.toJson(alert));
	    }
	    
	} 
}]);
