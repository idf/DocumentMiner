var FacetDialog = FacetDialog || function($div, filter) {
    $div.dialog({
        autoOpen: false,
        resizable: true,
        height: 360,
        width: 640,
        position: {my: "center top", at: "center top+50", of: window},
        modal: true,
        buttons: {
            "Update": function() {
                var dim = $div.prop("dim");
                var selection = getCheckedOptions($div);
                filter(dim, selection);
                $div.dialog("close");
            },
            Cancel: function() {
                $div.dialog("close");
            }
        }
    });

    $div.on("change", "input[type=checkbox]", function() {
        if ($(this).prop("checked") == true) {
            if ($(this).hasClass("all")) {
                $div.find("input:checkbox.sub").prop("checked", false);
            } else {
                $div.find("input:checkbox.all").prop("checked", false);
            }
        } else {
            if ($div.find("input:checkbox:checked").length == 0) {
                $(this).prop("checked", true);
            }
        }
    });

    function getCheckedOptions($div) {
        var selection = $div.find("input:checkbox:checked").map(function() {
            return this.value;
        }).get().join(";");
        return selection;
    }

    this.dialog = function(facet, selection, title) {
        var html = [];
        html.push("<div><label><input type='checkbox' class='all' value='' {0}>All Results</label></div>".format([(selection == "" ? "checked" : "")]));
        var items = facet.items;
        var option = "<div><label><input type='checkbox' class='sub' value='{0}' {1} />{2} ({3})</label></div>";
        html.push("<div class='child'>");
        for (var i = 0; i < items.length; i++) {
            var item = items[i];
            var checked = selection.include(item.key, ";") ? "checked" : "";
            html.push(option.format([item.key, checked, item.name, item.count]));
        }
        html.push("</div>");
        
        $div.dialog("option", "title", title);
        $div.prop("dim", facet.dim);
        $div.html(html.join(""));
        $div.dialog("open");
    };
};
