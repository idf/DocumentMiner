/**
 * Created by Danyang on 2/3/2015.
 */
(function(){
    'use strict';
    var app = angular.module('km_v3.controllers', ['chart.js']);

    app.controller('SearchController', ['$http', function($http) {
        var vm = this;  // view model
        var debug = true;
        vm.results = {};
        vm.query = {};

        vm.pagination = pagination;
        vm.filtering = filtering;
        vm.searchQuery = searchQuery;

        function pagination(pageInfo) {
            var pageCount = pageInfo.pageCount;
            var page = pageInfo.page;
            // TODO: pagination
        }

        function filtering(){
            // TODO
        }

        function searchQuery() {
            var param = [];
            param.push('query='+vm.query.str);
            param.push('filter='+'postMonth:,postYear:,topicId:,forumId:,threadId:,poster:');
            param.push('page='+1);
            param.push('sort='+2);
            $http.get('/s/v2/posts?'+param.join('&')).success(function(data) {
                console.log(data);
                vm.results.posts = data;
                vm.pagination(data.pageInfo);
            });

            $http.get('/s/v2/collocations?query='+vm.query.str).success(function(data) {
                console.log(data);
                vm.results.collocations = data;
            });
        }
    }]);

    app.controller('BarController', function() {
        var vm = this;
        vm.labels =["nice place", "felicia chin", "bear bear", "pika kor kor", "pulau ubin", "happy valentine", "merry christmas", "nus forum", "haut ahh", "world class university"];

        vm.series = ["Phrases"];

        vm.data = [
            [65, 59, 90, 81, 56, 55, 40, 56, 55, 40]
        ];

        var steps = 10;
        vm.options = {
            scaleOverride: true,
            scaleSteps: steps,
            scaleStepWidth: Math.ceil(vm.data[0].max()/steps),
            scaleStartValue: 0
        };
    });
})();