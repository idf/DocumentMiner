var PageWidget = PageWidget || function($div, gotoPage, changePageSize) {
    var _PageCount = 0;
    var _CurrentPage = 1;

    $div.on("click", "a", function() {
        event.preventDefault();
        var page = $(this).text();
        if (page == "Prev") {
            page = _CurrentPage - 1;
        } else if (page == "Next") {
            page = _CurrentPage + 1;
        }
        gotoPage(page);
    });

    $div.on("change", "select#sltPageSize", function() {
        var pageSize = $(this).val();
        changePageSize(pageSize);
    });

    this.render = function(pageInfo) {
        var pageSize = pageInfo.pageSize;
        _PageCount = pageInfo.pageCount;
        _CurrentPage = pageInfo.page;

        var html = [];
        html.push("Page: ");
        if (_CurrentPage > 1) {
            html.push("<a href='#'>Prev</a> ");
        }
        var start = _CurrentPage - 5 < 1 ? 1 : _CurrentPage - 5;
        var end = _CurrentPage + 5 > _PageCount ? _PageCount : _CurrentPage + 5;
        for (var page = start; page <= end; page++) {
            if (page == _CurrentPage) {
                html.push("<b>" + page + "</b> ");
            } else {
                html.push("<a href='#'>" + page + "</a> ");
            }
        }
        if (_CurrentPage < _PageCount) {
            html.push("<a href='#'>Next</a> ");
        }

        var pageSizeOptions = [10, 25, 50, 100];
        html.push("<select id='sltPageSize'>");
        for (var i = 0; i < pageSizeOptions.length; i++) {
            if (pageSizeOptions[i] == pageSize) {
                html.push("<option selected>" + pageSizeOptions[i] + "</option>");
            } else {
                html.push("<option>" + pageSizeOptions[i] + "</option>");
            }
        }
        html.push("</select>");
        $div.html(html.join(""));
    };
};
