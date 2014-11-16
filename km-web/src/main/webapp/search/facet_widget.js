var FacetWidget = FacetWidget || function($div, title, filter, showMore) {
    $div.on("change", "input:checkbox", function() {
        var dim = $div.prop("dim");
        if ($(this).prop("checked") == true) {
            if ($(this).hasClass("all")) {
                $div.find("input:checkbox.sub").prop("checked", false);
            } else {
                $div.find("input:checkbox.all").prop("checked", false);
            }
            var selection = getCheckedOptions($div);
            filter(dim, selection);
        } else {
            if ($div.find("input:checkbox:checked").length == 0) {
                $(this).prop("checked", true);
            } else {
                var selection = getCheckedOptions($div);
                filter(dim, selection);
            }
        }
    });

    function getCheckedOptions($div) {
        var selection = $div.find("input:checkbox:checked").map(function() {
            return this.value;
        }).get().join(";");
        return selection;
    }

    $div.on("click", "a#showMore", function() {
        event.preventDefault();
        var dim = $div.prop("dim");
        var selection = getCheckedOptions($div);
        showMore(dim, selection, title);
    });

    this.render = function(facet, selection) {
        var html = [];
        html.push("<div><b>{0}</b></div>".format([title]));
        html.push("<div><label><input type='checkbox' value='' class='all' {0}>All Results</label></div>".format([(selection == "" ? "checked" : "")]));
        var items = facet.items;
        html.push("<div class='child'>");
        var option = "<div><label><input type='checkbox' class='sub' value='{0}' {1} />{2} ({3})</label></div>";
        for (var i = 0; i < items.length; i++) {
            var item = items[i];
            var checked = selection.include(item.key, ";") ? "checked" : "";
            html.push(option.format([item.key, checked, item.name, item.count]));
        }
        html.push("</div>");

        var remaining = facet.childCount - items.length;
        if (remaining > 0) {
            html.push("<div class='right'><a id='showMore' href='#'>Show More ({0})</a></div>".format([remaining]));
        }

        $div.prop("dim", facet.dim);
        $div.prop("remaining", remaining);
        $div.html(html.join(""));
        $div.show();
    }

    this.update = function(facet) {
        var $divChild = $div.find("div.child:eq(0)");
        var $divSM = $divChild.next();
        var items = facet.items;
        var html = [];
        var appended = 0;
        var selection = [];
        var option = "<div><label><input type='checkbox' class='sub' value='{0}' {1} />{2} ({3})</label></div>";
        $div.find("input:checkbox").prop("checked", false);
        for (var i = 0; i < items.length; i++) {
            var item = items[i];
            selection.push(item.key);
            var $element = $divChild.find("input:checkbox[value='" + item.key + "']");
            if ($element.length == 0) {
                html.push(option.format([item.key, "checked", item.name, item.count]));
                appended++;
            } else {
                $element.prop("checked", true);
            }
        }
        $divChild.append(html.join(""));

        var remaining = $div.prop("remaining");
        remaining -= appended;
        $div.prop("remaining", remaining);
        if (remaining > 0) {
            $divSM.html("<a id='showMore' href='#'>Show More ({0})</a>".format([remaining]));
        } else {
            $divSM.hide();
        }
    }
};
