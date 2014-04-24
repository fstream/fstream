var stompClient = null;

function setConnected(connected) {
    document.getElementById('connect').disabled = connected;
    document.getElementById('disconnect').disabled = !connected;
    document.getElementById('conversation').style.visibility = connected ? 'visible' : 'hidden';
    document.getElementById('events').innerHTML = '';
    document.getElementById('rates').innerHTML = '';
}

function connect() {
    var socket = new SockJS('/server');
    stompClient = Stomp.over(socket);
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
}

function disconnect() {
    stompClient.disconnect();
    setConnected(false);
    console.log("Disconnected");
}

function registerInstrument() {
    var instrument = document.getElementById('instrument').value;
    stompClient.send("/web/register", {}, JSON.stringify({ 'instrument': instrument }));
}

function showRate(message) {
    var response = document.getElementById('rates');
    var p = document.createElement('p');
    p.style.wordWrap = 'break-word';
    p.appendChild(document.createTextNode(message));
    response.appendChild(p);
}

function showEvent(message) {
    var response = document.getElementById('events');
    var p = document.createElement('p');
    p.style.wordWrap = 'break-word';
    p.appendChild(document.createTextNode(message));
    response.appendChild(p);
}
   
updateChart = null;

$(function() {
    (function() {
        function make_realtime(key) {
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
                add_callback: function(cb) {
                    callbacks.push(cb);
                }
            }
        };

        var realtime = {
            ask: make_realtime('ask'),
            bid: make_realtime('bid')
        };

        // This websocket sends homogenous messages in the form
        //{"dateTime":1398308418977,"symbol":"EUR/USD","bid":1.3818,"ask":1.38191}
        updateChart = function(data) {
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

                rt.add_callback(function(buf) {
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
                .attr('class', 'horizon')
                .call(context.horizon()
                    .extent(cht.extent)
                    .title(cht.title)
                    .format(function (n) { 
                        return num_fmt(n) + ' ' + cht.unit; 
                    })
                );
        });

        context.on('focus', function (i) {
            if (i !== null) {
                d3.selectAll('.value').style('right',
                                             context.size() - i + 'px');
            }
            else {
                d3.selectAll('.value').style('right', null)
            }
        });

    })();
});
       