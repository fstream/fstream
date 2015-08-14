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
      .controller('mainController', mainController);

   mainController.$inject = ['$scope', 'lodash', 'stateService', 'settingsService', 'eventService'];

   function mainController($scope, _, stateService, settingsService, eventService) {
      activate();

      function activate() {
         // Temporary workaround for Highcahrts opacity animation bug
         jQuery.cssProps.opacity = 'opacity';

         // Initialize
         registerEvents();
         connect();
      }

      function registerEvents() {
         // Events
         $scope.$on('connected', function (e) {
            $scope.connected = true;
         });
         $scope.$on('disconnected', function (e) {
            $scope.connected = false;
         });

         var limit = 50;
         $scope.$on('trade', function (e, trade) {
            queueEvent($scope.trades, trade, limit);
         });
         $scope.$on('order', function (e, order) {
            queueEvent($scope.orders, order, limit);
         });
         $scope.$on('quote', function (e, quote) {
            queueEvent($scope.quotes, quote, limit);
         });
         $scope.$on('alert', function (e, alert) {
            queueEvent($scope.alerts, alert, limit);
         });
         $scope.$on('metric', function (e, metric) {
            queueEvent($scope.metrics, metric, limit);
         });
         $scope.$on('state', function (e, state) {
            updateState(state);
         });
      }

      function connect() {
         console.log("Connecting...");
         stateService.getState().then(updateState);
         settingsService.getSettings().then(updateSettings);
         initScope();
         resetModel();

         eventService.connect();
      }

      function disconnect() {
         eventService.disconnect();
      }

      function initScope() {
         $scope.connected = false;
         $scope.state = {};
         $scope.settings = {
            analyticsUrl: "http://localhost:8081"
         };
         $scope.views = [];
         $scope.newAlert = {};
         $scope.connect = connect;
         $scope.disconnect = disconnect;
         $scope.registerAlert = registerAlert;
      }

      function resetModel() {
         $scope.trades = [];
         $scope.orders = [];
         $scope.quotes = [];
         
         $scope.alerts = [];
         $scope.metrics = [];
         $scope.commands = [];
      }

      function registerAlert() {
         eventService.register($scope.newAlert);
      }

      function updateState(state) {
         $scope.state = state;

         // This could be smarter to allow not updating things that haven't changed
         $scope.views = [];

         _.each(state.metrics, function (metric, i) {
            $scope.views.push({
               type: 'metric-chart',
               id: metric.id,
               title: metric.name,
               name: metric.units,
               units: metric.units
            });
         });

         _.each(state.symbols, function (symbol, i) {
            $scope.views.push({
               type: 'tick-chart',
               index: i,
               symbol: symbol
            });
         });
      }
      
      function updateSettings(settings) {
         $scope.settings = settings;
      }

      function queueEvent(a, value, limit) {
         return a.length >= limit ? a.pop() : a.unshift(value);
      }
   }
})();