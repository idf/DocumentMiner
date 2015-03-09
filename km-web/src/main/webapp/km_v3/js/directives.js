(function(){
    'use strict';
    var app = angular.module('km_v3.directives', []);

    app.directive('postHits', function() {
       return {
           restrict: 'E',
           templateUrl: 'post-hits.html'
       };
    });
    
    app.directive('coOccurrenceTable', function() {
       return {
           restrict: 'E',
           templateUrl: 'co-occurrence-table.html',
           require: '^searchCtrl'
       };
    });

    app.directive('charts', function() {
        return {
            restrict: 'E',
            templateUrl: 'charts.html',
            require: '^searchCtrl'
        };
    });

    app.directive('pagination', function () {
       return {
           restrict: 'E',
           templateUrl: 'pagination.html',
           require: '^searchCtrl'
       };
    });
})();