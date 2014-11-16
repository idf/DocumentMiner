(function($) {
    $.fn.bindMouseEvent = function() {
        $(this).find("tr:odd").css("background-color", "#f3f3f3");
        $(this).find("tr").mouseover(function() {
            $(this).css("background-color", "#cfcfcf");
        });
        $(this).find("tr:even").mouseout(function() {
            $(this).css("background-color", "#ffffff");
        });
        $(this).find("tr:odd").mouseout(function() {
            $(this).css("background-color", "#f3f3f3");
        });
    };
})(jQuery);

(function($) {
    $.redirect = function(url) {
        $(location).attr("href", url);
    };
})(jQuery);

jQuery.fn.extend({
    serializeForm: function() {
        var formData = this.serializeArray();
        var data = {};
        for (var i = 0; i < formData.length; i++) {
            var item = formData[i];
            if ($.type(data[item.name]) == "string") {
                data[item.name] += "," + item.value;
            } else {
                data[item.name] = item.value.toString();
            }
        }
        var str = [];
        for (var name in data) {
            str.push(name + "=" + data[name]);
        }
        return str.join("&");
    }
});

jQuery.getNoCache = function() {
    var url, data, callback, type;
    if (arguments.length == 0) {
        return null;
    }
    if (arguments.length == 1) {
        url = arguments[0];
        data = "t=" + Math.random();
        return $.get(url, data);
    }
    if (arguments.length == 2) {
        url = arguments[0];
        if ($.type(arguments[1]) == "string") {
            data = arguments[1] + "&t=" + Math.random();
            return $.get(url, data);
        } else {
            data = "t=" + Math.random();
            callback = arguments[1];
            return $.get(url, data, callback);
        }
    }
    if (arguments.length == 3) {
        url = arguments[0];
        if ($.type(arguments[1]) == "string") {
            data = arguments[1] + "&t=" + Math.random();
            callback = arguments[2];
            return $.get(url, data, callback);
        } else {
            data = "t=" + Math.random();
            callback = arguments[1];
            type = arguments[2];
            return $.get(url, data, callback, type);
        }
    }
    if (arguments.length == 4) {
        url = arguments[0];
        data = arguments[1] + "&t=" + Math.random();
        callback = arguments[2];
        type = arguments[3];
        return $.get(url, data, callback, type);
    }
    return null;
};

jQuery.fn.center = function() {
    this.css("position", "absolute");
    this.css("top", Math.max(0, (($(window).height() - $(this).outerHeight()) / 2) +
            $(window).scrollTop()) + "px");
    this.css("left", Math.max(0, (($(window).width() - $(this).outerWidth()) / 2) +
            $(window).scrollLeft()) + "px");
    return this;
};

jQuery.listen = function(event, callback) {
    $(document).on(event, callback);
};

jQuery.forget = function(event) {
    $(document).unbind(event);
};

jQuery.publish = function(event, param) {
    $(document).trigger(event, param);
};
