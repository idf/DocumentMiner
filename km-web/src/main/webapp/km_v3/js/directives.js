(function(){
    'use strict';
    var app = angular.module('km_v3.directives', []);

    app.directive("postHits", function() {
       return {
           restrict: "E",
           templateUrl: "post-hits.html"
       };
    });

})();