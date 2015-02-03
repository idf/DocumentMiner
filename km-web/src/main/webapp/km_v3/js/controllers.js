/**
 * Created by Danyang on 2/3/2015.
 */
(function(){
    'use strict';
    var app = angular.module('controllers', []);

    app.controller('SearchController', ["$http", function($http) {
        var ctrl = this;
        var debug = false;
        ctrl.results = [];
        ctrl.results.push({"snippet": "no results yet"});
        ctrl.queryTerm = {};

        ctrl.searchQuery = function() {
            console.log("query searched");
            ctrl.results.push({"snippet": "this is the test results: "+ctrl.queryTerm.content }); // something with ctrl.queryTerm
        };
    }]);
})();