var Pagination = Pagination || function(container, goto) {
    "use strict";
    var $pg = $(container);
    var span = 5;
    
    $pg.on("click", "a", function() {
        event.preventDefault();
        var page = $(this).prop("id");
        goto(page);
    });
    
    this.render = function(pageInfo) {
        var pageCount = pageInfo.pageCount;
        var page = pageInfo.page;
        
        var html = [];
        if (page > 1) {
            html.push("<a href='#' id='{0}'><span class='glyphicon glyphicon-chevron-left'></span></a>".build(page-1));
        }
        var start = page - span < 1 ? 1 : page - span;
        var end = page + span > pageCount ? pageCount : page + span;
        for (var i = start; i <= end; i++) {
            if (i === page) {
                html.push("<b>{0}</b>".build(i));
            } else {
                html.push("<a href='#' id='{0}'>{1}</a>".build(i, i));
            }
        }
        if (page < pageCount) {
            html.push("<a href='#' id='{0}'><span class='glyphicon glyphicon-chevron-right'></span></a>".build(page+1));
        }
        $pg.html(html.join(""));
        $pg.show();
    };
    
    this.hide = function() {
        $pg.hide();
    };
};
