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
      .directive('alertLog', alertLog);

   alertLog.$inject = ['ngTableParams'];  

   function alertLog(ngTableParams) {
      return {
         restrict : 'E',
         template:
         '<table ng-table="tableParams" class="table alert-log"> ' +
         '	<tr ng-repeat="alert in data"> ' +
         '		<td data-title="\'Time\'">{{ alert.dateTime | date:\'yyyy-MM-dd HH:mm:ss Z\' }}</td> ' +
         '		<td data-title="\'ID\'">#{{ alert.id }}</td> ' +
         '	</tr> '+
         '</table>',
         link: function($scope, $element, $attr){
            $scope.$on('alert', function(e, alert) {
               $scope.tableParams.reload();
            });

            $scope.tableParams = new ngTableParams({page: 1, count: 5}, {
               total: $scope.alerts.length,
               counts: [], // hides page sizes
               getData: function($defer, params) {
                  var data = $scope.alerts;

                  params.total(data.length);
                  $scope.data = data.slice((params.page() - 1) * params.count(), params.page() * params.count());
                  $defer.resolve($scope.data);
               }
            });			
         }
      }
   }
})();