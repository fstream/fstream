angular.module('fstream').directive('tickChart', ['historyService', 'lodash', function (historyService, lodash) {
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
      template: '<div class="tick-chart"></div>',
      link: function ($scope, $element, $attr) {
         $scope.maxTime = 0;
         $scope.loading = true;

         var chart,
             index = $scope.options && $scope.options.index || 0,
             colors = Highcharts.getOptions().colors,
             color = '#62cb31',
             opacity = 0.5 - (index / 6.0) * 0.5,
             maxTime = 0,
             enabled = true;

         chart = new Highcharts.StockChart({
            chart: {
               renderTo: $element[0],
               height: 325,
               animation: false
            },

            credits: {
               enabled: false
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
               shared: true
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
               },
            },

            series: [{
               name: 'Price',
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
            }],

            scrollbar: {
               barBackgroundColor: '#9fcc83',
            },

            navigator: {
               outlineColor: '#489125',
               maskFill: 'rgba(191, 220, 180, 0.5)',

               series: {
                  color: '#9fcc83',
                  lineColor: '#489125'
               }
            }
         });

         $scope.$on('rate', function (e, tick) {
            if ($scope.loading || tick.symbol !== $scope.options.symbol || tick.dateTime < $scope.maxTime) {
               return;
            }

            $scope.maxTime = tick.dateTime;

            var shift = false,
                animate = false;

            chart.series[0].addPoint([tick.dateTime, (tick.ask + tick.bid) / 2.0], false, shift, animate);
            chart.series[1].addPoint([tick.dateTime, tick.bid, tick.ask], enabled, shift, animate);
         });

         historyService.getTicks({
            symbol: $scope.options.symbol,
            interval: 'm'
         }).then(function (ticks) {
            var sorted = lodash.sortBy(ticks, 'time');

            var mids = lodash.map(sorted, function (tick) {
               return [tick.time, (tick.ask + tick.bid) / 2.0];
            });
            var rates = lodash.map(sorted, function (tick) {
               return [tick.time, tick.bid, tick.ask];
            });

            $scope.maxTime = ticks[ticks.length - 1].time;

            chart.series[0].setData(mids, false, false);
            chart.series[1].setData(rates, true, false);

            $scope.loading = false;
         });
      }
   }
}])