/* 
 * Copyright (c) 2015 fStream. All Rights Reserved.
 * 
 * Project and contact information: https://bitbucket.org/fstream/fstream
 * 
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

/**
 * Sweet Alert Directive
 * Official plugin - http://tristanedwards.me/sweetalert
 * Angular implementation inspiring by https://github.com/oitozero/ngSweetAlert
 */

(function () {
   "use strict";
   
   angular
      .module('fstream')
      .factory('sweetAlert', sweetAlert);

   sweetAlert.$inject = ['$timeout', '$window'];

   function sweetAlert($timeout, $window) {
      var swal = $window.swal;
      return {
         swal: function (arg1, arg2, arg3) {
            $timeout(function () {
               if (typeof (arg2) === 'function') {
                  swal(arg1, function (isConfirm) {
                     $timeout(function () {
                        arg2(isConfirm);
                     });
                  }, arg3);
               } else {
                  swal(arg1, arg2, arg3);
               }
            }, 200);
         },
         success: function (title, message) {
            $timeout(function () {
               swal(title, message, 'success');
            }, 200);
         },
         error: function (title, message) {
            $timeout(function () {
               swal(title, message, 'error');
            }, 200);
         },
         warning: function (title, message) {
            $timeout(function () {
               swal(title, message, 'warning');
            }, 200);
         },
         info: function (title, message) {
            $timeout(function () {
               swal(title, message, 'info');
            }, 200);
         }
      };
   }
})();