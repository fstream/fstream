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
         } ,
         template: '<div class="book-chart-wrapper"></div>',
         link: function($scope, $element, $attr) {
            window._ = _;
            var chart = new OrderBook.Chart({element: $element[0]});
            
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
         }
      };
   } 
})();