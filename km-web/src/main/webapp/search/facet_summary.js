var FacetSummary = FacetSummary || function($div, titles, $form, filter) {

    $div.on("click", "a.dim", function() {
        event.preventDefault();
        var dim = $(this).prop("id");
        filter(dim, "");
    });

    $div.on("click", "a.key", function() {
        event.preventDefault();
        var id = $(this).prop("id");
        var values = id.split(";");
        var dim = values[0];
        var key = values[1];
        var selection = $form.find("input[name=" + dim + "]").val();
        values = selection.split(";");
        var newValues = [];
        for (var i = 0; i < values.length; i++) {
            if (key != values[i]) {
                newValues.push(values[i]);
            }
        }
        filter(dim, newValues.join(";"));
    });

    this.render = function(facets) {
        var html = [];
        for (var i = 0; i < facets.length; i++) {
            var facet = facets[i];
            var dim = facet.dim;
            var title = titles[dim];
            var selection = $form.find("input[name=" + dim + "]").val();
            if (selection != "") {
                html.push("<div><b>{0}</b> <a class='dim' href='#' id='{1}'>X</a></div>".format([title, dim]));
                var items = facet.items;
                for (var j = 0; j < items.length; j++) {
                    var item = items[j];
                    html.push("<div>{0} <a class='key' href='#' id='{1}'>X</a></div>".format([item.name, dim + ";" + item.key]));
                }
            }
        }
        $div.html(html.join(""));
        $div.show();
    };
};
