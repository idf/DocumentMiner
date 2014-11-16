var Popup = Popup || function(container, update) {
    var instance = this;
    var $pp = $(container);
    var currentDim = "";

    // hide popup if click on document
    $(document).click(function() {
        if ($(event.target).hasClass("pp_panel") == false
                && $(event.target).parents("div.pp_panel").length == 0) {
            $pp.hide();
        }
    });

    // disable select text
    $pp.on("selectstart", function() {
        return false;
    });

    $pp.on("click", function() {
        event.stopPropagation();
    });

    $pp.on("click", "div#any", function() {
        update(currentDim, "");
        instance.hide();
    });
    
    $pp.on("click", "div.pp_cell_panel", function() {
        if ($(this).hasClass("pp_cell_panel_active")) {
            $(this).removeClass("pp_cell_panel_active");
        } else {
            $(this).addClass("pp_cell_panel_active");
        }
    });

    $pp.on("click", "a#all", function() {
        event.preventDefault();
        $pp.find("div.pp_cell_panel").addClass("pp_cell_panel_active");
    });

    $pp.on("click", "a#none", function() {
        event.preventDefault();
        $pp.find("div.pp_cell_panel").removeClass("pp_cell_panel_active");
    });

    $pp.on("click", "button#update", function() {
        var selection = $pp.find("div.pp_cell_panel_active").map(function() {
            return $(this).prop("id");
        }).get().join(";");
        update(currentDim, selection);
        instance.hide();
    });

    this.hide = function() {
        currentDim = "";
        $pp.hide();
    };
    
    this.render = function(facet) {
        currentDim = facet.dim;
        
        var html = [];
        html.push("<div class='pp_row_panel' id='any'>{0}</div>".format([facet.title]));
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
        var clsCols = "pp_cell_cols_" + cols;
        html.push("<div class='pp_cell_container'>");
        for (var i = 0; i < items.length; i++) {
            var item = items[i];
            var clsName = "pp_cell_panel";
            if (selected.include(item.key, ";")) {
                clsName += " pp_cell_panel_active";
            }
            clsName += " " + clsCols;
            html.push("<div class='{0}' id='{1}'>".format([clsName, item.key]));
            html.push("<div class='pp_item_text'>{0} <small>({1})</small></div>".format([item.name, item.count.toString().toNumberWithCommas()]));
            var width = item.count / max * 100;
            html.push("<div class='pp_item_bar' style='width:{0}%'></div>".format([width]));
            html.push("</div>");
            if ((i + 1) % cols == 0) {
                html.push("<br/>");
            }
        }
        html.push("</div>");
        
        html.push("<div class='hr'></div>");
        html.push("<div class='pp_op_panel'>");
        html.push("<a href='#' id='all'>All</a>");
        html.push("<a href='#' id='none'>None</a>");
        html.push("<button id='update'>Update</button>");
        html.push("</div>");
        $pp.html(html.join(""));

        $pp.center();
        $pp.css("top", 80);
        $pp.show();
    };
};
