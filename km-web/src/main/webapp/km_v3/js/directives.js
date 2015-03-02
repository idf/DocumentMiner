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
           templateUrl: 'co-occurrence-table.html'
       };
    });

    app.directive('charts', function() {
        return {
            restrict: 'E',
            templateUrl: 'charts.html'
        };
    });

    app.directive('pagination', function () {
       return {
           restrict: 'E',
           templateUrl: 'pagination.html'
       };
    });
})();