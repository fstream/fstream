angular.module('FStreamApp.services').factory('ratesService', ['$rootScope', '$timeout', function($rootScope, $timeout) {
	
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
		        
		        stompClient.subscribe('/topic/metrics', function(frame){
		        	publishEvent("metric", frame);
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
}]);
