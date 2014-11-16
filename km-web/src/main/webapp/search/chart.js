var Chart = Chart || function(div, title) {

    var chart = null;
    var type = "line";
    var data = [];

    function getLegendText(query) {
        var legend = "";
        var fields = query.split("&");
        for (var i = 0; i < fields.length; i++) {
            var field = fields[i];
            var items = field.split("=");
            var name = items[0];
            var value = items[1];
            if (name == "keyword") {
                legend = value;
                break;
            }
        }
        if (legend == "") {
            legend = "all";
        }
        return legend;
    }

    this.render = function(facet, query) {
        var items = facet.items;
        var dataPoints = [];
        for (var i = 0; i < items.length; i++) {
            var item = items[i];
            dataPoints.push({x: i, y: item.count, label: item.name});
        }
        var dataItem = {
            type: type,
            showInLegend: true,
            legendText: getLegendText(query),
            dataPoints: dataPoints
        };
        data.push(dataItem);
        var options = {
            creditText: null,
            height: 240,
            width: 900,
            title: {text: title},
            legend: {
                cursor: "pointer",
                itemclick: function(e) {
                    if (typeof (e.dataSeries.visible) === "undefined" || e.dataSeries.visible) {
                        e.dataSeries.visible = false;
                    } else {
                        e.dataSeries.visible = true;
                    }
                    chart.render();
                },
                horizontalAlign: "right",
                verticalAlign: "center",
                fontSize: 14
            },
            data: data
        };
        chart = new CanvasJS.Chart(div, options);
        chart.render();
    };

    this.reset = function() {
        data = [];
        var options = {
            creditText: null,
            height: 10,
            width: 10,
            data: []
        };
        chart = new CanvasJS.Chart(div, options);
        chart.render();
    };
};
