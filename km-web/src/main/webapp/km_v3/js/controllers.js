/**
 * Created by Danyang on 2/3/2015.
 */
(function(){
    'use strict';
    var app = angular.module('km_v3.controllers', ['chart.js']);

    app.controller('SearchController', ['$http', '$scope', 'collocationDataService', function($http, $scope, collocService) {
        var vm = this;  // view model
        vm.show = false;
        vm.searching = false;
        vm.results = {};
        vm.query = {'str': ''};  // predefined structure
        vm.pages = {};

        vm.pagination = pagination;
        vm.goto = goto;
        vm.filtering = filtering;
        vm.searchQuery = searchQuery;
        vm.search = search;

        function pagination(page, pageCount) {
            var span = 3;
            vm.pages = {};
            if(page>1) {
                vm.pages.left = {};
                vm.pages.left.link = page-1;
            }
            if(page<pageCount) {
                vm.pages.right = {};
                vm.pages.right.link = page+1;
            }

            vm.pages.nums = [];
            var start = page-span;
            var end = page+span;
            start = start<1? 1: start;
            end = end>pageCount? pageCount: end;
            for(var i=start; i<=end; i++) {
                var num = {};
                num.page = i;
                if(i!==page) {
                    num.link = i;
                }
                vm.pages.nums.push(num);
            }
        }

        function goto(page) {
            // console.log("goto page "+page);
            var param = [];
            param.push('query='+vm.query.str);
            param.push('filter='+'postMonth:,postYear:,topicId:,forumId:,threadId:,poster:');
            param.push('page='+page);
            param.push('sort='+2);
            $http.get('/s/v2/posts?'+param.join('&')).success(function(data) {
                pushReturnDataToAttributes(data);
            });
        }

        function filtering(){
            // TODO
        }


        /**
         * @param data  {{ pageInfo: * }}
         */
        function pushReturnDataToAttributes(data) {
            // console.log(data);
            vm.results.posts = data;
            vm.pagination(data.pageInfo.page, data.pageInfo.pageCount);
        }

        function search(query) {
            vm.query.str = query;
            searchQuery();
        }

        function searchQuery() {
            vm.searching = true;
            var param = [];
            param.push('query='+vm.query.str);
            param.push('filter='+'postMonth:,postYear:,topicId:,forumId:,threadId:,poster:');
            param.push('page='+1);
            param.push('sort='+2);
            $http.get('/s/v2/posts?'+param.join('&')).success(function(data) {
                pushReturnDataToAttributes(data);
            });

            collocService.prepForBroadcast(vm.query.str);
            $scope.$on('collocationDataReady', function() {
                vm.results.collocations = collocService.msg;
                vm.results.collocations.order = ["terms", "phrases"];
                if(collocService.msg.results.hasOwnProperty("phrases_excluded")) {
                    vm.results.collocations.order.push("phrases_excluded");
                }
                vm.searching = false;
                vm.show = true;
                $scope.$broadcast('shown');
            });
            $scope.$on('prepForBroadcast', function() {
                vm.searching = true;
                vm.show = false;
            });

        }
    }]);

    app.factory('collocationDataService', ["$http", "$rootScope", function($http, $rootScope) {
        var sharedService = {};
        sharedService.prepForBroadcast = prepForBroadcast;

        function prepForBroadcast(str) {
            $rootScope.$broadcast('prepForBroadcast');
            $http.get('/s/v2/collocations?query='+str).success(function(data) {
                // console.log(data);
                sharedService.msg = data;
                $rootScope.$broadcast('collocationDataReady');
            });
        }

        return sharedService;
    }]);

    app.controller('BarController', ['$scope', 'collocationDataService', function($scope, collocService) {
        var vm = this;
        vm.labels =[];
        vm.data = []; // push
        vm.options = {};
        vm.chart = null;
        vm.show = false;

        var steps = 10;
        vm.init = function (target) {
            vm.target = target;
            loadData();
        };

        function setUp(rankedList) {
            cleanup();
            var scores = [];
            for(var i=0; i<rankedList.length; i++) {
                scores.push(rankedList[i].score*1000);
                //vm.labels.push(rankedList[i].coincidentalTerm);
                vm.labels.push(i+1);
            }

            var l = scores.length;
            while(l<10) {
                scores.push(0);
                vm.labels.push('');
                l++;
            }

            vm.data.push(scores);

            vm.options =  {
                scaleOverride: true,
                scaleSteps: steps,
                scaleStepWidth: Math.ceil(vm.data[0].max()/steps),
                scaleStartValue: 0,
                scaleShowLabels: false,
                responsive: true,
                maintainAspectRatio: true
            };
            vm.show = true;
        }

        function cleanup() {
            vm.data.length = 0;
            vm.labels.length = 0;
            vm.options = {};
            vm.show = false;
            if(vm.chart) { vm.chart.destroy(); }
        }

        function loadData() {
            $scope.$apply();
            if(collocService.msg.results.hasOwnProperty(vm.target)) {
                setUp(collocService.msg.results[vm.target]);
                // console.log("set up: "+vm.target);
            }
            $scope.$apply();
        }

        $scope.$on('prepForBroadcast', function () {
            cleanup();
        });

        $scope.$on('shown', function () {
            loadData();
        });


        $scope.$on('create', function (event, chart) {
            vm.chart = chart;
        });
    }]);
})();