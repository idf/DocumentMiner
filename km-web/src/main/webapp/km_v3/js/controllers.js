/**
 * Created by Danyang on 2/3/2015.
 */
(function(){
    'use strict';
    var app = angular.module('km_v3.controllers', ['chart.js']);

    app.controller('SearchController', ['$http', '$scope', 'collocationDataService', function($http, $scope, sharedService) {
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

            //$http.get('/s/v2/collocations?query='+vm.query.str).success(function(data) {
            //    console.log(data);
            //    vm.results.collocations = data;
            //});

            sharedService.prepForBroadcast(vm.query.str);
            $scope.$on('collocationDataReady', function() {
               vm.results.collocations = sharedService.msg;
            });
        }
    }]);

    app.factory('collocationDataService', ["$http", "$rootScope", function($http, $rootScope) {
        var sharedService = {};
        sharedService.prepForBroadcast = prepForBroadcast;
        sharedService.broadcastItem = broadcastItem;

        function broadcastItem() {
            $rootScope.$broadcast('collocationDataReady');
        }

        function prepForBroadcast(str) {
            $http.get('/s/v2/collocations?query='+str).success(function(data) {
                console.log(data);
                sharedService.msg = data;
                sharedService.broadcastItem();
            });
        }

        return sharedService;
    }]);

    // TODO service for phrases and phrases excluded
    app.controller('BarController', ['$scope', 'collocationDataService', function($scope, sharedService) {
        var vm = this;
        vm.labels =[];
        vm.series = [""];
        vm.data = []; // push
        var steps = 10;
        vm.options = {};
        vm.setUp = setUp;

        function setUp(rankedList) {
            vm.data = [];
            vm.labels =[];

            var scores = [];
            for(var i=0; i<rankedList.length; i++) {
                scores.push(rankedList[i].score*1000);
                vm.labels.push(rankedList[i].coincidentalTerm);
            }
            vm.data.push(scores);

            vm.options =  {
                scaleOverride: true,
                scaleSteps: steps,
                scaleStepWidth: Math.ceil(vm.data[0].max()/steps),
                scaleStartValue: 0
            };
        }

        $scope.$on('collocationDataReady', function() {
            vm.setUp(sharedService.msg.results.terms);
        });
    }]);
})();