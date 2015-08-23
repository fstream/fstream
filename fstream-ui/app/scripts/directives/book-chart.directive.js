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

   bookChart.$inject = ['lodash', 'booksService'];
   
   function bookChart(_, booksService) {
      return {
         restrict : 'E',
         scope: {
            symbol: '@'
         },
         replace: true,
         templateUrl: 'views/components/book-chart.html',         
         link: function($scope, $element, $attr) {
            $scope.paused = false; 
            // TODO: Fix
            window._ = _;
            var chart = new BookViewer({
                svgWidth: $element.width(),
                svgHeight: 500,
                width: $element.width() - 75,
                height: 400,
                windowSize: 1000 * 60 * 0.5
            });
            chart.init('order-book-chart');
            
            // This breaks stuff
            //load();
            
            $scope.$on('quote', function(e, quote) {
               if (quote.symbol == $scope.symbol) {
                  chart.addQuote(quote);
                  chart.guide(quote.dateTime);
               }
            });
            $scope.$on('trade', function(e, trade) {
               if (trade.symbol == $scope.symbol) {
                  chart.addTrade(trade);
                  chart.guide(trade.dateTime);
               }
            });
            $scope.$on('snapshot', function(e, snapshot) {
               if (snapshot.symbol == $scope.symbol) {
                  chart.addDepth(snapshot);
               }
            });
            
            $scope.pause = function() {
               $scope.paused = !$scope.paused;
               chart.togglePause();
            }
            $scope.rewind = function() {
               chart.rewind(1000 * 10)
            }
            $scope.forward = function() {
               chart.forward(1000 * 10)
            }            
            $scope.rescalePrice = function() {
               chart.rescalePriceRange(8, 12);
            }
            $scope.rescaleTime = function(scale) {
               if (scale == 30) {
                  chart.rescaleDateRange(1000 * 30);
               }
               if (scale == 1) {
                  chart.rescaleDateRange(1000 * 60);
               }
               if (scale == 5) {
                  chart.rescaleDateRange(1000 * 60 * 5);
               }                  
            }            
            
            function load() {
               booksService.getBook($scope.symbol).then(function(book){
                  var quotes = _.map(book.quotes, function(quote) {
                     quote.type = 'QUOTE';
                     quote.dateTime = quote.time;
                     return quote;
                  });
                  var trades = _.map(book.trades, function(trade) {
                     trade.type = 'TRADE';
                     trade.dateTime = trade.time;
                     return trade;
                  });
                  var snapshots = _.map(book.snapshots, function(snapshot) {
                     snapshot.type = 'SNAPSHOT';
                     snapshot.dateTime = snapshot.time;
                     snapshot.orders = JSON.parse(snapshot.orders);
                     snapshot.priceLevels = JSON.parse(snapshot.priceLevels);
                     return snapshot;
                  });                  
                  
                  chart.preload(quotes, trades, snapshots)               
               });
            }
         }
      };
   } 
})();