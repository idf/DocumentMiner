/**
 * Created by Danyang on 2/2/2015.
 */
(function(){
    "use strict";
    var gem = { name: 'Azurite', price: 2.95 };
    var app = angular.module('gemStore', []);
    app.controller('StoreController', function() {
        this.product = gem;
    });
})();
