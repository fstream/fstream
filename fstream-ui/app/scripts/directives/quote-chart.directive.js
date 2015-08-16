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
      .directive('quoteChart', quoteChart);

   quoteChart.$inject = ['historyService', 'stateService', 'lodash'];

   function quoteChart(historyService, stateService, _) {
      Highcharts.setOptions({
         global: {
            useUTC: false
         }
      });

      return {
         restrict: 'E',
         scope: {
            options: '='
         },
         replace: true,
         template: '<div class="quote-chart"></div>',
         link: function ($scope, $element, $attr) {
            // Private
            var index = $scope.options && $scope.options.index || 0,
                maxTime = 0,
                maxAlertTime = 0,
                lastQuote,
                enabled = true;
            
            // Binding
            $scope.loading = true;
            $scope.$on('quote', onQuote);
            $scope.$on('alert', onAlert);

            // Activate
            var chart = createChart($element[0]);
            loadHistory();
            
            function createChart(container) { 
               var color = '#62cb31',
                   highlightColor = "#62CB31",
                   opacity = 0.5 - (index / 6.0) * 0.5;
                   
               return new Highcharts.StockChart({
                  chart: {
                     renderTo: container,
                     height: 325,
                     animation: false
                  },

                  credits: {
                     enabled: false
                  },

                  lang: {
                     noData: 'EMPTY'
                  },
                  noData: {
                     style: {
                        fontWeight: 'bold',
                        fontSize: '3em',
                        color: 'rgba(82, 132, 78, 0.29)'
                     }
                  },

                  yAxis: {
                     title: {
                        text: "Price"
                     },
                     alternateGridColor: '#FDFDfD'
                  },

                  xAxis: {
                     type: 'datetime',
                     minRange: 1000
                  },

                  tooltip: {
                     crosshairs: [true, true],
                     shared: true,
                     valueDecimals: 4,
                     useHTML: true
                  },

                  rangeSelector: {
                     inputEnabled: false,

                     selected: 1,

                     buttons: [
                        {
                           type: 'minute',
                           count: 1,
                           text: '1m'
                        }, {
                           type: 'hour',
                           count: 1,
                           text: '1h'
                        }, {
                           type: 'day',
                           count: 1,
                           text: '1d'
                        }, {
                           type: 'All',
                           text: 'all'
                        }
                     ],

                     buttonTheme: {
                        states: {
                           hover: {
                              fill: 'rgba(192, 192, 192, 0.5)'
                           },
                           select: {
                              fill: 'rgba(191, 220, 180, 0.5)'
                           }
                        }
                     }
                  },

                  series: [{
                     name: 'Price',
                     id: 'price',
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
                  }, {
                     name: 'Spread',
                     data: [],
                     step: true,
                     type: 'arearange',
                     lineWidth: 0.5,
                     linkedTo: ':previous',
                     color: color,
                     fillOpacity: opacity,
                     zIndex: 0
                  }, {
                     type: 'flags',
                     name: 'Alerts',
                     data: [],
                     onSeries: 'price',
                     shape: 'circlepin',
                     color: "#FFF", // Text
                     style: {
                        color: 'white'
                     },
                     states: {
                        hover: {
                           fillColor: '#b51414' // Darker
                        }
                     },
                     fillColor: "rgba(181, 20, 20, 0.63)" // Opacity
                  }],

                  scrollbar: {
                     barBackgroundColor: highlightColor
                  },

                  navigator: {
                     outlineColor: highlightColor,
                     maskFill: 'rgba(191, 220, 180, 0.5)',

                     series: {
                        color: '#9fcc83',
                        lineColor: '#489125'
                     }
                  }
               });
            }

            function onQuote(e, quote) {
               if ($scope.loading || quote.symbol !== $scope.options.symbol || quote.dateTime < maxTime) {
                  return;
               }

               maxTime = quote.dateTime;
               lastQuote = quote;

               var shift = false,
                   animate = false;

               chart.series[0].addPoint([quote.dateTime, (quote.ask + quote.bid) / 2.0], false, shift, animate);
               chart.series[1].addPoint([quote.dateTime, quote.bid, quote.ask], enabled, shift, animate);
            }

            function onAlert(e, alert) {
               if ($scope.loading || alert.symbol !== $scope.options.symbol || alert.dateTime < maxAlertTime) {
                  return;
               }

               if (lastQuote) {
                  // Forward fill quote so that the alert will have something to sit on
                  lastQuote.dateTime = alert.dateTime;
                  onQuote(e, lastQuote);
               }

               var shift = false,
                   animate = false;
               
               maxAlertTime = alert.dateTime;

               var alertDef = getAlertDefinition(alert.id);               
               
               chart.series[2].addPoint({
                  x: alert.dateTime,
                  title: " " + alert.id + " ",
                  text: alertDef.name,
               }, false, shift, animate);
            }

            function loadHistory() {
               historyService.getTicks({
                  symbol: $scope.options.symbol,
                  interval: 'm'
               }).then(function (quotes) {
                  var sorted = _.sortBy(quotes, 'time');

                  var mids = _.map(sorted, function (quote) {
                     return [quote.time, (quote.ask + quote.bid) / 2.0];
                  });
                  var values = _.map(sorted, function (quote) {
                     return [quote.time, quote.bid, quote.ask];
                  });

                  lastQuote = _.last(sorted);
                  maxTime = lastQuote.time;

                  chart.series[0].setData(mids, false, false);
                  chart.series[1].setData(values, true, false);

                  $scope.loading = false;
               });
            }

            function getAlertDefinition(id) {
               var definitions = stateService.getCachedState().alerts;
               return _.findWhere(definitions, {
                  id: id
               });
            }
         }
      };
   }
})();