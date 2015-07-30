/* 
 * Copyright (c) 2015 fStream. All Rights Reserved.
 * 
 * Project and contact information: https://bitbucket.org/fstream/fstream
 * 
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

(function () {
   'use strict';

   angular
      .module('fstream')
      .directive('bookChart', bookChart);

   bookChart.$inject = ['lodash'];
   
   function bookChart(_) {
      // Data
      var quotesData = [], tradesData = [];

      var svg;
      var dateScale, priceScale, midScale, brush;
      var volumeScale;
      var interval = 1;

      var config = {
        width: 1030,
        quote_height: 420,
        volume_height: 80,
        offset: 10,
        colour_bid: '#0A0',
        colour_ask: '#00A'
      }
      
      return {
         restrict : 'E',
         template:
         '<svg id="canvas" width="100%" height="500"></svg>',
         scope: {
            symbol: '@'
         } ,
         link: function($scope, $element, $attr) {
            $scope.$on('quote', function(e, quote) {
               if (quote.symbol == $scope.symbol) {
                  console.log("Quote: ", quote);
                  quotesData.unshift(quote);

                  // Buffer at least 2  events
                  if (quotesData.length < 2) {
                     return;
                  }

                  // FIXME: This removes the DOM every quote
                  $element.children().empty();
                  init(quotesData);
                  initBrush();
                  
                  processQuotes(quotesData);
                  processTradeVolumes(tradesData);
                  processTrades(tradesData);
               }
            });
            $scope.$on('trade', function(e, trade) {
               if (trade.symbol == $scope.symbol) {
                  console.log("Trade: ", trade);
                  tradesData.unshift(trade);
               }
            });            
         }
      };

      /**
       * Initialize common scales and things
       */
      function init(data) {
        // Remove first one for now so it doesn't have huge range ????
        // data.shift();

        // Setup scales
        var dateRange, pMin, pMax;

        dateRange = d3.extent( _.pluck(data, 'dateTime'));
        dateScale = d3.scale.linear().domain(dateRange).range( [0, config.width] );

        pMin = d3.min(_.pluck(data, 'bid'));
        pMax = d3.max(_.pluck(data, 'ask'));
        midScale = d3.scale.linear()
          .domain([pMin, pMax])
          .range( [config.quote_height-config.offset, config.offset]);

        // Set up svg handles and render area
        svg = d3.select('#canvas');
        svg.append('rect').attr({
          x: 0,
          y: 0,
          width: config.width,
          height: config.quote_height + config.volume_height,
        }).style('fill', 'none').style('stroke', '#BBB');


        // Axis
        var yAxis = d3.svg.axis().scale(midScale)
          .orient('left')
          .ticks(5);
        svg.append('g').classed('axis', true).attr('transform', _translate(config.width-10, 0)).call(yAxis);
      }


      /**
       * Show individual trades as circles, where the area is propotional to the amount
       */
      function processTrades(data) {
        svg.selectAll('.trade-point')
          .data(data)
          .enter()
          .append('circle')
          .classed('trace-point', true)
          .attr('cx', function(d) { return dateScale(d.dateTime); })
          .attr('cy', function(d) { return midScale(d.price); })
          .attr('r', function(d) { return Math.sqrt(d.amount) * 0.12; })
          .style('fill', '#F80')
          .style('fill-opacity', 0.1);
      }


      /**
       * Brush event
       */
      function brushed() {
        var min = config.width*brush.extent()[0];
        var max = config.width*brush.extent()[1];
        var start = dateScale.invert(min);
        var end = dateScale.invert(max);

        svg.selectAll('.volume, .quote').style('stroke', '#CCC');
        svg.selectAll('.volume, .quote').filter(function(d) {
          return d.dateTime >= start && d.dateTime <= end;
        }).style('stroke', '#369');


        // Show ask/bid volume over time at price
        var filtered = _.filter(quotesData, function(q) {
          return q.dateTime >= start && q.dateTime <= end;
        });

        var bids = _.groupBy(filtered, 'bid');
        var bidsArray = [];
        Object.keys(bids).forEach(function(key) {
          bidsArray.push({
            price: key,
            quotes: bids[key]
          });
        });

        var asks = _.groupBy(filtered, 'ask');
        var asksArray = [];
        Object.keys(asks).forEach(function(key) {
          asksArray.push({
            price: key,
            quotes: asks[key]
          });
        });

      console.log(asksArray.length);

        // Just testing for now
        svg.selectAll('.ask-volume').remove();
        svg.selectAll('.bid-volume').remove();

        svg.selectAll('.bid-volume')
          .data(bidsArray)
          .enter()
          .append('rect')
          .classed('bid-volume', true)
          .attr('x', config.width+5)
          .attr('y', function(d) { return midScale(d.price) - 2; })
          .attr('height', 3)
          .attr('width', function(d) { return _.sum(_.pluck(d.quotes, 'bidAmount')) * 0.001;})
          .style('fill', config.colour_bid);

        svg.selectAll('.ask-volume')
          .data(asksArray)
          .enter()
          .append('rect')
          .classed('ask-volume', true)
          .attr('x', config.width+5)
          .attr('y', function(d) { return midScale(d.price) + 2; })
          .attr('height', 3)
          .attr('width', function(d) { return _.sum(_.pluck(d.quotes, 'askAmount')) * 0.001;})
          .style('fill', config.colour_ask);

      }

      /*
      function brushed_not_used() {
        var min = config.width*brush.extent()[0];
        var max = config.width*brush.extent()[1];
        var start = dateScale.invert(min);
        var end = dateScale.invert(max);

        svg.selectAll('.volume, .quote').style('stroke', '#CCC');
        svg.selectAll('.volume, .quote').filter(function(d) {
          return d.dateTime >= start && d.dateTime <= end;
        }).style('stroke', '#369');


        // Show ask/bid volume over time at price
        var filtered = _.filter(tradesData, function(q) {
          return q.dateTime >= start && q.dateTime <= end;
        });

        var prices = _.groupBy(filtered, 'price');
        var pricesArray = [];
        Object.keys(prices).forEach(function(key) {
          pricesArray.push({
            price: key,
            trades: prices[key]
          });
        });


        // Just testing for now
        svg.selectAll('.temp').remove();
        svg.selectAll('.temp')
          .data(pricesArray)
          .enter()
          .append('rect')
          .classed('temp', true)
          .attr('x', config.width+5)
          .attr('y', function(d) { return midScale(d.price); })
          .attr('height', 1)
          .attr('width', function(d) { return _.sum(_.pluck(d.trades, 'amount')) * 0.001;})
          .style('stroke', '#CCC');
      }
      */

      function initBrush() {
        var test = d3.scale.linear().range( [0, config.width]);
        var brushLine = svg.append('g').classed('brush', true).classed('x', true);
        brush = d3.svg.brush()
          .x( test )
          .on('brush', brushed);

        brushLine.append('path').attr('d', _line(0, config.quote_height-1, config.width, config.quote_height-1)).style('stroke', '#CCC');
        brushLine.append('path').attr('d', _line(0, config.quote_height+1, config.width, config.quote_height+1)).style('stroke', '#CCC');
        brushLine
          .call(brush)
          .selectAll('rect')
          .attr('y', 0)
          .attr('height', 500);
      }

      /**
       * Renders total trade volume at a specific time point
       */
      function processTradeVolumes(data) {

        var volumeRange = [0, 12000];
        var offset = 5;

        console.log(config.volume_height + config.quote_height);

        volumeScale = d3.scale.linear()
          .domain(volumeRange).range( [config.volume_height+config.quote_height, config.quote_height + offset] );

        var range = _.groupBy(data, 'dateTime');
        var rangeArray = [];
        Object.keys(range).forEach(function(key) {
          rangeArray.push({
            dateTime: key,
            trades: range[key]
          });
        });

        svg.selectAll('.volume')
          .data(rangeArray)
          .enter()
          .append('path')
          .classed('volume', true)
          .attr('d', function(d) {
             var total = _.sum(d.trades, 'amount');
             var x = dateScale(d.dateTime);
             var y = volumeScale(total);
             return _line(x, y, x, config.volume_height + config.quote_height);
          })
          .style('opacity', 0.5)
          .style('stroke', '#CCC');

        /*
        var range = _.uniq(_.pluck(data, 'dateTime'));


        _.each(range, function(r) {
          var t = _.filter(data, function(d) { return d.dateTime === r; })
          var total = _.sum(_.pluck(t, 'amount'));

          var x1 = dateScale(r);
          var y1 = volumeScale(total);
          svg.append('path')
            .datum({
              dateTime: r
            })
            .classed('volume', true)
            .attr('d', _line(x1, y1, x1, 490))
            .style('opacity', 0.5)
            .style('stroke', '#CCC');

        });
        */
      }

      /**
       * Render ask, bid and mid
       */
      function processQuotes(data) {
        for (var idx=interval; idx < data.length; idx+=interval) {
          var prev = data[idx-interval];
          var curr = data[idx];
          var x1 = dateScale(prev.dateTime);
          var x2 = dateScale(curr.dateTime);
          var y1 = midScale(prev.mid);
          var y2 = midScale(curr.mid);

          svg.append('path')
            .classed('quote', true)
            .datum({
              dateTime: curr.dateTime
            })
            .attr('d', _line(x1, y1, x2, y2))
            .style('stroke-dasharray', '3,3')
            .style('stroke', '#CCC');

          // Render ask
          svg.append('path')
            .attr('d', _line(x1, midScale(prev.ask), x2, midScale(curr.ask)))
            .style('stroke', config.colour_ask)
            .style('stroke-width', 2)
            .style('opacity', 0.45);

          // Render bid
          svg.append('path')
            .attr('d', _line(x1, midScale(prev.bid), x2, midScale(curr.bid)))
            .style('stroke', config.colour_bid)
            .style('stroke-width', 2)
            .style('opacity', 0.45);
        }
      }
   }
   
   // SVG helpers
   
   function _line(x1, y1, x2, y2) {
     return 'M' + x1 + ',' + y1 + 'L' + x2 + ',' + y2;
   }

   function _translate(x, y) {
     return 'translate(' + x + ',' + y + ')';
   }   
})();