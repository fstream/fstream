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
      .filter('propsFilter', propsFilter);

   function propsFilter() {
      return function (items, props) {
         var out = [];

         if (angular.isArray(items)) {
            items.forEach(function (item) {
               var itemMatches = false;

               var keys = Object.keys(props);
               for (var i = 0; i < keys.length; i++) {
                  var prop = keys[i];
                  var text = props[prop].toLowerCase();
                  if (item[prop].toString().toLowerCase().indexOf(text) !== -1) {
                     itemMatches = true;
                     break;
                  }
               }

               if (itemMatches) {
                  out.push(item);
               }
            });
         } else {
            // Let the output be the input untouched
            out = items;
         }

         return out;
      }
   }
})();