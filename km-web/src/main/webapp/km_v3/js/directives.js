(function(){
    'use strict';
    var app = angular.module('directives', []);

    app.directive("postHits", function() {
       return {
           restrict: "E",
           templateUrl: "post-hits.html"
       };
    });

})();