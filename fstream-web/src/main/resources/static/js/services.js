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
	function makeRealtime(key) {
	    var buf = [], callbacks = [];
	    return {
	        data: function(ts, val) {
	            buf.push({ts: ts, val: val});
	            callbacks = callbacks.reduce(function(result, cb) {
	                if (!cb(buf))
	                    result.push(cb);
	                return result
	            }, []);
	        },
	        addCallback: function(cb) {
	            callbacks.push(cb);
	        }
	    }
	};
	
    var realtime = {
        ask: makeRealtime('ask'),
        bid: makeRealtime('bid')
    };

    // This websocket sends homogenous messages in the form
    //{"dateTime":1398308418977,"symbol":"EUR/USD","bid":1.3818,"ask":1.38191}
    var updateChart = function(data) {
    	console.log(data);
    	realtime['ask'].data(data.dateTime, data.ask);
    	realtime['bid'].data(data.dateTime, data.bid);
    };

    var context = cubism.context().step(1000).size(960);

    var metric = function (key, title) {
        var rt = realtime[key];

        return context.metric(function (start, stop, step, callback) {
            start = start.getTime();
            stop = stop.getTime();

            rt.addCallback(function(buf) {
                if (!(buf.length > 1 && 
                      buf[buf.length - 1].ts > stop + step)) {
                    // Not ready, wait for more data
                    return false;
                }

                var r = d3.range(start, stop, step);

                /* Don't like using a linear search here, but I don't
                 * know enough about cubism to really optimize. I had
                 * assumed that once a timestamp was requested, it would
                 * never be needed again so I could drop it. That doesn't
                 * seem to be true!
                 */
                var i = 0;
                var point = buf[i];

                callback(null, r.map(function (ts) {
                    if (ts < point.ts) {
                        // We have to drop points if no data is available
                        return null;
                    }
                    for (; buf[i].ts < ts; i++);
                    return buf[i].val;
                }));

                // opaque, but this tells the callback handler to
                // remove this function from its queue
                return true;
            });
        }, title);
    };

    var init = function() {
	    ['top', 'bottom'].map(function (d) {
	        d3.select('#charts').append('div')
	            .attr('class', d + ' axis')
	            .call(context.axis().ticks(12).orient(d));
	
	    });
	
	    d3.select('#charts').append('div').attr('class', 'rule')
	        .call(context.rule());
	
	    charts = {
	        ask: {
	            title: 'Ask',
	            unit: '$',
	            extent: [0, 2]
	        },
	        bid: {
	            title: 'Bid',
	            unit: '$',
	            extent: [0, 2]
	        }
	    };
	
	    Object.keys(charts).map(function (key) {
	        var cht = charts[key];
	        var num_fmt = d3.format('.5r');
	        d3.select('#charts')
	            .insert('div', '.bottom')
	            .datum(metric(key, cht.title))
	            .attr('class', 'horizon chart-container')
	            .call(context.horizon()
	            	.colors(["#bae4b3","#74c476","#31a354","#006d2c", "#08519c","#3182bd","#6baed6","#bdd7e7"])
            		.height(200)
	                .extent(cht.extent)
	                .title(cht.title)
	                .format(function (n) { 
	                    return n && num_fmt(n) + ' ' + cht.unit; 
	                })
	            );
	    });
	
	    context.on('focus', function (i) {
            d3.selectAll('.value').style('right', i && context.size() - i + 'px');
	    });
    }
    
    return {
    	init: init,
    	updateChart: updateChart
    };
});