(function () {
   "use strict";

   angular
      .module('fstream')
      .directive('metricChart', metricChart);

   metricChart.$inject = ['historyService', 'lodash'];

   function metricChart(historyService, _) {
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
         template: '<div class="metric-chart"></div>',
         link: function ($scope, $element, $attr) {
            $scope.maxTime = 0;
            $scope.loading = true;

            var chart,
                colors = Highcharts.getOptions().colors,
                color = '#62cb31',
                opacity = 0.5,
                size = 50,
                enabled = true;

            var highlightColor = "#62CB31";

            chart = new Highcharts.StockChart({
               chart: {
                  renderTo: $element[0],
                  height: 325,
                  type: 'area',
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
                     text: $scope.options.units
                  },
                  alternateGridColor: '#FDFDfD'
               },

               xAxis: {
                  type: 'datetime',
                  gridLineWidth: '1px',
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
                  name: $scope.options.name,
                  data: [],
                  zIndex: 1,
                  color: color,
                  lineColor: color,
                  marker: {
                     fillColor: 'white',
                     lineWidth: 2,
                     radius: 3,
                     lineColor: color
                  }
               }],

               scrollbar: {
                  barBackgroundColor: highlightColor,
                  barBorderColor: highlightColor,

                  buttonArrowColor: highlightColor,
                  buttonBorderColor: highlightColor,
                  buttonBackgroundColor: 'white',

                  rifleColor: 'white'
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

            $scope.$on('metric', function (e, metric) {
               if ($scope.loading || metric.id !== $scope.options.id || metric.dateTime < $scope.maxTime) {
                  return;
               }

               $scope.maxTime = metric.dateTime;

               var shift = false,
                   animate = false;

               chart.series[0].addPoint([metric.dateTime, metric.data.count], enabled, shift, animate);
            });

            historyService.getMetrics({
               id: $scope.options.id
            }).then(function (metrics) {
               var sorted = _.sortBy(metrics, 'time');

               var values = _.map(sorted, function (value) {
                  return [value.time, JSON.parse(value.data).count];
               });

               $scope.maxTime = _.last(sorted).time;

               chart.series[0].setData(values, true, false);

               $scope.loading = false;
            });
         }
      };
   }
})();