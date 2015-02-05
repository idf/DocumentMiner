/**
 * Created by Danyang on 2/3/2015.
 */
(function(){
    'use strict';
    var app = angular.module('km_v3.controllers', []);

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
})();