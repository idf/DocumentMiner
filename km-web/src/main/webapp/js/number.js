Number.prototype.toFix = function(dig) {
    return this.toString().toFixLengthWithPrefix(dig, "0");
};

Number.prototype.toRound = function(dig) {
    var factor = Math.pow(10, dig);
    return Math.round(this * factor) / factor;
};

Number.prototype.toMonthName = function() {
    var months = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];
    return months[this - 1];
};
