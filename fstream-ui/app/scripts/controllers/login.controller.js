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
      .controller('loginController', loginController);

   loginController.$inject = ['$rootScope', '$scope', '$http', '$location'];

   function loginController($rootScope, $scope, $http, $location) {
      $scope.credentials = {};
      $scope.login = login;
      $scope.logout = logout;
      
      authenticate();
         
      function login() {
         authenticate($scope.credentials, function () {
            if ($rootScope.authenticated) {
               $scope.connect();
               $location.path("/dashboard");
               $scope.error = false;
            } else {
               $location.path("/");
               $scope.error = true;
            }
         });
      };
      
      function logout() {
        $http.post('/logout', {}).success(function() {
          $rootScope.authenticated = false;
          $location.path("/");
        }).error(function(data) {
          $rootScope.authenticated = false;
        });
      }

      function authenticate(credentials, callback) {
         var headers = credentials ? {
            authorization: "Basic " + btoa(credentials.username + ":" + credentials.password)
         } : {};

         $http.get('user', {
            headers: headers
         }).success(function (data) {
            if (data.name) {
               $rootScope.authenticated = true;
            } else {
               $rootScope.authenticated = false;
            }
            callback && callback();
         }).error(function () {
            $rootScope.authenticated = false;
            callback && callback();
         });
      }
   }
})();