/**
 * Created by Danyang on 2/3/2015.
 */
(function(){
    'use strict';
    var app = angular.module('controllers', []);

    app.controller('SearchController', ['$http', function($http) {
        var ctrl = this;
        var debug = true;
        ctrl.results = {};
        ctrl.query = {};

        ctrl.pagination = function(pageInfo) {
            var pageCount = pageInfo.pageCount;
            var page = pageInfo.page;
            // TODO: page
        };

        ctrl.filtering = function() {

        };

        ctrl.searchQuery = function() {
            var param = [];
            param.push('query='+ctrl.query.str);
            param.push('filter='+'postMonth:,postYear:,topicId:,forumId:,threadId:,poster:');
            param.push('page='+1);
            param.push('sort='+2);
            $http.get('/s/v2/posts?'+param.join('&')).success(function(data) {
                console.log(data);
                ctrl.results.posts = data;
                ctrl.pagination(data.pageInfo);
            });

            $http.get('/s/v2/collocations?query='+ctrl.query.str).success(function(data) {
                console.log(data);
                ctrl.results.collocations = data;
            });
        };
    }]);
})();