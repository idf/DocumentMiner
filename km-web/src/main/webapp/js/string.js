String.prototype.toFixLengthWithPrefix = function(len, prefix) {
    var prefix_str = "";
    for (var i = 0; i < len; i++) {
        prefix_str += prefix;
    }
    var new_str = prefix_str + this.toString();
    new_str = new_str.substring(new_str.length - len);
    return new_str;
};

String.prototype.toNumberWithCommas = function() {
    return this.replace(/\B(?=(\d{3})+(?!\d))/g, ",");
};

String.prototype.include = function(str, delimiter) {
    var strs = this.split(delimiter);
    for (var i=0; i<strs.length; i++) {
        if (str == strs[i]) {
            return true;
        }
    }
    return false;
};

String.prototype.format = function(param) {
    var str = this;
    for (var i=0; i<param.length; i++) {
        str = str.replace("{" + i + "}", param[i]);
    }
    return str;
};

String.prototype.build = function() {
    var str = this;
    for (var i=0; i<arguments.length; i++) {
        str = str.replace("{" + i + "}", arguments[i]);
    }
    return str;
};
