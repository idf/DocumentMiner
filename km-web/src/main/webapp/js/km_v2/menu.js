var Menu = Menu || function(container) {
    var $menu = $(container);
    var items = {};

    // disable select text
    $menu.on("selectstart", function() {
        return false;
    });

    this.addItem = function(menuItem) {
        items[menuItem.dim] = menuItem;
        $menu.on("click", "div.menu_item_panel#" + menuItem.dim, function() {
            menuItem.callback($(this), menuItem.dim);
        });
    };

    this.render = function() {
        var html = [];
        for (var key in items) {
            var menuItem = items[key];
            html.push(menuItem.html());
        }
        $menu.html(html.join(""));
    };
    
    this.highlight = function(dim, selected) {
        var $e = $menu.find("div.menu_item_panel#" + dim + ">div.menu_item_text");
        var item = items[dim];
        if (selected.length == 0) {
            $e.text(item.title);
            $e.css("font-weight", "normal");
        } else {
            $e.text(item.title.replace("Any", "Filted by"));
            $e.css("font-weight", "bold");
        }
    };
};

var MenuItem = MenuItem || function(dim, title, callback) {
    this.dim = dim;
    this.title = title;
    this.callback = callback;
    
    this.html = function() {
        var html = [];
        html.push("<div class='menu_item_panel' id='{0}'>".format([dim]));
        html.push("<div class='menu_item_text'>{0}</div>".format([title]));
        html.push("<div class='menu_item_arrow'><span class='mn-dwn-arw'></span></div>");
        html.push("</div>");
        return html.join("");
    };
};
