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
         templateUrl: 'views/components/book.html',         
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
            chart.init('order-book-chart', new Date().getTime() - 15*1000);
            
            booksService.getBook($scope.symbol).then(function(book){
               for (var i = 0; i < book.quotes.length; i++) {
                  var quote = book.quotes[i];
                  quote.dateTime = quote.time;
                  chart.addQuote(quote);
               }
            });
            
            $scope.$on('quote', function(e, quote) {
               if (quote.symbol == $scope.symbol) {
                  chart.addQuote(quote);
               }
            });
            $scope.$on('trade', function(e, trade) {
               if (trade.symbol == $scope.symbol) {
                  chart.addTrade(trade);
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
         }
      };
   } 
})();