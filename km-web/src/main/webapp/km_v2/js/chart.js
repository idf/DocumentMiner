var Chart = Chart || function(container, title) {
    var chart = null;
    var type = "line";

    this.render = function(facets) {
        var data = [];
        for (var i = 0; i < facets.length; i++) {
            var facet = facets[i];
            var items = facet.items;
            var dataPoints = [];
            for (var j = 0; j < items.length; j++) {
                var item = items[j];
                dataPoints.push({x: j, y: item.count, label: item.name});
            }
            var dataItem = {
                type: type,
                showInLegend: true,
                legendText: facet.keyword,
                dataPoints: dataPoints
            };
            data.push(dataItem);
        }
        var width = 500;
        if (data.length > 1) {
            width = 800;
        }
        var options = {
            creditText: null,
            height: 300,
            width: width,
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
        chart = new CanvasJS.Chart(container, options);
        chart.render();
    };
};
