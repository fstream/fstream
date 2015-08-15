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
      return {
         restrict : 'E',
         scope: {
            symbol: '@'
         },
         replace: true,
         template: '<div class="book-chart-wrapper"></div>',         
         link: function($scope, $element, $attr) {
            // TODO: Fix
            window._ = _;
            var chart = new BookViewer();
            chart.init('order-book-chart', new Date().getTime() - 15*1000);
            
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
         }
      };
   } 
})();