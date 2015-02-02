var Dropdown = Dropdown || function(container, update, more) {
        "use strict";
    var instance = this;
    var $dropdown = $(container);
    var currentDim = "";

    // hide dropdown if click on document
    $(document).click(function() {
        if ($(event.target).hasClass("menu_panel") == false 
                && $(event.target).parents("div.menu_panel").length == 0) {
            instance.hide();
        }
    });

    // disable select text
    $dropdown.on("selectstart", function() {
        return false;
    });

    $dropdown.on("click", function() {
        event.stopPropagation();
    });

    $dropdown.on("click", "div#any", function() {
        update(currentDim, "");
        instance.hide();
    });

    $dropdown.on("click", "div#more", function() {
        more(currentDim);
        instance.hide();
    });

    $dropdown.on("click", "div.dd_cell_panel", function() {
        if ($(this).hasClass("dd_cell_panel_active")) {
            $(this).removeClass("dd_cell_panel_active");
        } else {
            $(this).addClass("dd_cell_panel_active");
        }
    });

    $dropdown.on("click", "a#all", function() {
        event.preventDefault();
        $dropdown.find("div.dd_cell_panel").addClass("dd_cell_panel_active");
    });

    $dropdown.on("click", "a#none", function() {
        event.preventDefault();
        $dropdown.find("div.dd_cell_panel").removeClass("dd_cell_panel_active");
    });

    $dropdown.on("click", "button#update", function() {
        var selection = $dropdown.find("div.dd_cell_panel_active").map(function() {
            return $(this).prop("id");
        }).get().join(";");
        update(currentDim, selection);
        instance.hide();
    });

    this.hide = function() {
        currentDim = "";
        $dropdown.hide();
    };

    this.visible = function(dim) {
        if (currentDim == dim) {
            return true;
        }
        return false;
    };

    this.render = function(facet) {
        currentDim = facet.dim;

        var html = [];
        html.push("<div class='dd_row_panel' id='any'>{0}</div>".format([facet.title]));
        html.push("<div class='hr'></div>");

        var max = 0;
        var items = facet.items;
        for (var i = 0; i < items.length; i++) {
            var item = items[i];
            if (item.count > max) {
                max = item.count;
            }
        }
        var selected = facet.selected;
        var cols = facet.cols;
        if (typeof cols === "undefined") {
            cols = 1;
        }
        var clsCols = "dd_cell_cols_" + cols;
        for (var i = 0; i < items.length; i++) {
            var item = items[i];
            var clsName = "dd_cell_panel";
            if (selected.include(item.key, ";")) {
                clsName += " dd_cell_panel_active";
            }
            clsName += " " + clsCols;
            html.push("<div class='{0}' id='{1}'>".format([clsName, item.key]));
            html.push("<div class='dd_item_text'>{0} <small>({1})</small></div>".format([item.name, item.count.toString().toNumberWithCommas()]));
            var width = item.count / max * 100;
            html.push("<div class='dd_item_bar' style='width:{0}%'></div>".format([width]));
            html.push("</div>");
            if ((i + 1) % cols == 0) {
                html.push("<br/>");
            }
        }

        html.push("<div class='hr'></div>");
        html.push("<div class='dd_op_panel'>");
        html.push("<a href='#' id='all'>All</a>");
        html.push("<a href='#' id='none'>None</a>");
        html.push("<button id='update'>Update</button>");
        html.push("</div>");

        if (facet.childCount > items.length) {
            html.push("<div class='hr'></div>");
            html.push("<div class='dd_row_panel' id='more'>");
            html.push("<div class='dd_row_text'>More options...</div>");
            html.push("</div>");
        }
        $dropdown.html(html.join(""));

        var position = facet.position;
        $dropdown.css("left", position.left);
        $dropdown.css("top", position.top);
        $dropdown.show();
    };
};
