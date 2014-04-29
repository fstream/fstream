angular.module('FStreamApp.services', []).
  factory('ratesService', function() {
	var stompClient;
	 
    return {
    	connect: function() {
    		stompClient = Stomp.over(new SockJS('/server'));
    		stompClient.connect({}, function(frame) {
    	        setConnected(true);
    	        console.log('Connected: ' + frame);
    	        
    	        stompClient.subscribe('/topic/rates', function(rate){
    	            showRate(rate.body.toString());
    	            updateChart(eval("(" + rate.body + ")"));
    	        });
    	        stompClient.subscribe('/topic/events', function(event){
    	        	showEvent(event.body.toString());
    	        });                
    	    });
    		
		},
	    disconnect: function() {
	        stompClient.disconnect();
	        setConnected(false);
	        console.log("Disconnected");
	    },
	    register: function(){
    	    var instrument = document.getElementById('instrument').value;
    	    stompClient.send("/web/register", {}, JSON.stringify({ 'instrument': instrument }));
	    }
    } 
  });