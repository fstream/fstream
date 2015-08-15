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
      .config(configState)
      .run(function ($rootScope, $location, $state, editableOptions) {
      $rootScope.$state = $state;
      editableOptions.theme = 'bs3';

      $rootScope.$on("$locationChangeStart", function(event, next) {
         if (!$rootScope.authenticated && next.indexOf("login") === -1) {
             $location.path("/login");
             event.preventDefault();
         }         
      });      
   });

   configState.$inject = ['$stateProvider', '$urlRouterProvider', '$compileProvider', '$httpProvider'];

   function configState($stateProvider, $urlRouterProvider, $compileProvider, $httpProvider) {
      // Prevent authentication form popups
      $httpProvider.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest';

      // Optimize load start with remove binding information inside the DOM element
      $compileProvider.debugInfoEnabled(true);

      // Set default state
      $urlRouterProvider.otherwise("/common/login");

      $stateProvider

      // Dashboard
         .state('dashboard', {
         url: "/dashboard",
         templateUrl: "views/dashboard.html",
         data: {
            pageTitle: 'Dashboard'
         }
      })

      // Charts
         .state('charts', {
         url: "/charts",
         templateUrl: "views/charts.html",
         data: {
            pageTitle: 'Charts',
         }
      })

      // Analytics
         .state('analytics', {
         url: "/analytics",
         templateUrl: "views/analytics.html",
         data: {
            pageTitle: 'Analytics',
         }
      })      

      // Books
         .state('books', {
         url: "/books",
         templateUrl: "views/books.html",
         data: {
            pageTitle: 'Books',
         }
      })

      // History
         .state('history', {
         url: "/history",
         templateUrl: "views/history.html",
         data: {
            pageTitle: 'History',
         }
      })

      // Alerts
         .state('alerts', {
         url: "/alerts",
         templateUrl: "views/alerts.html",
         data: {
            pageTitle: 'Alerts',
         }
      })

      // Admin
         .state('admin', {
         url: "/admin",
         templateUrl: "views/admin.html",
         data: {
            pageTitle: 'Admin',
         }
      })

      // Common views
         .state('common', {
         abstract: true,
         url: "/common",
         templateUrl: "views/common/content_empty.html",
         data: {
            pageTitle: 'Common'
         }
      })
         .state('common.login', {
         url: "/login",
         templateUrl: "views/common_app/login.html",
         data: {
            pageTitle: 'Login page',
            specialClass: 'blank'
         }
      })
         .state('common.register', {
         url: "/register",
         templateUrl: "views/common_app/register.html",
         data: {
            pageTitle: 'Register page',
            specialClass: 'blank'
         }
      })
         .state('common.error_one', {
         url: "/error_one",
         templateUrl: "views/common_app/error_one.html",
         data: {
            pageTitle: 'Error 404',
            specialClass: 'blank'
         }
      })
         .state('common.error_two', {
         url: "/error_two",
         templateUrl: "views/common_app/error_two.html",
         data: {
            pageTitle: 'Error 505',
            specialClass: 'blank'
         }
      })
         .state('common.lock', {
         url: "/lock",
         templateUrl: "views/common_app/lock.html",
         data: {
            pageTitle: 'Lock page',
            specialClass: 'blank'
         }
      });
   }
})();