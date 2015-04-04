/**
 *
 * modalCtrl
 *
 */

angular
    .module('homer')
    .controller('modalCtrl', modalCtrl)

function modalCtrl($scope, $modal) {

    $scope.open = function () {

        var modalInstance = $modal.open({
            templateUrl: 'views/modal/modal_example.html',
            controller: ModalInstanceCtrl,
        });
    };

    $scope.open1 = function () {
        var modalInstance = $modal.open({
            templateUrl: 'views/modal/modal_example1.html',
            controller: ModalInstanceCtrl
        });
    };

    $scope.open3 = function (size) {
        var modalInstance = $modal.open({
            templateUrl: 'views/modal/modal_example3.html',
            size: size,
            controller: ModalInstanceCtrl,
        });
    };

    $scope.open2 = function () {
        var modalInstance = $modal.open({
            templateUrl: 'views/modal/modal_example2.html',
            controller: ModalInstanceCtrl,
            windowClass: "hmodal-info"
        });
    };

    $scope.open4 = function () {
        var modalInstance = $modal.open({
            templateUrl: 'views/modal/modal_example2.html',
            controller: ModalInstanceCtrl,
            windowClass: "hmodal-warning"
        });
    };

    $scope.open5 = function () {
        var modalInstance = $modal.open({
            templateUrl: 'views/modal/modal_example2.html',
            controller: ModalInstanceCtrl,
            windowClass: "hmodal-success"
        });
    };

    $scope.open6 = function () {
        var modalInstance = $modal.open({
            templateUrl: 'views/modal/modal_example2.html',
            controller: ModalInstanceCtrl,
            windowClass: "hmodal-danger"
        });
    };
};

function ModalInstanceCtrl ($scope, $modalInstance) {

    $scope.ok = function () {
        $modalInstance.close();
    };

    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };
};