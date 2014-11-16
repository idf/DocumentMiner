$(document).ready(function() {
    var $form = $("#frmSearch");
    var facetTitles = {
        postMonth: "Post Months",
        postYear: "Post Years",
        topicId: "Topics",
        forumId: "Forums",
        threadId: "Threads",
        poster: "Poster"
    };
    var fws = {
        forumId: new FacetWidget($("#divForums"), "Forums", filter, showMore),
        postYear: new FacetWidget($("#divPostYears"), "Post Years", filter, showMore),
        postMonth: new FacetWidget($("#divPostMonths"), "Post Months", filter, showMore),
        poster: new FacetWidget($("#divPosters"), "Posters", filter, showMore),
        threadId: new FacetWidget($("#divThreads"), "Threads", filter, showMore),
        topicId: new FacetWidget($("#divTopics"), "Topics", filter, showMore)
    };
    var fd = new FacetDialog($("#divFacetDialog"), filter);
    var fs = new FacetSummary($("#divFilterSummary"), facetTitles, $form, filter);
    var charts = {
        postMonth: new Chart("divChartMonth", "Posts per month"),
        postYear: new Chart("divChartYear", "Posts per year"),
        topicId: new Chart("divChartTopic", "Posts per topic"),
        forumId: new Chart("divChartForum", "Posts per forum"),
        threadId: new Chart("divChartThread", "Posts per thread"),
        poster: new Chart("divChartPoster", "Posts per poster")
    };
    var currentDim = "";

    searchFacet();

    function filter(dim, selection) {
        currentDim = dim;
        $form.find("input[name=" + dim + "]").val(selection);
        searchFacet();
    }

    function showMore(dim, selection, title) {
        var data = $form.serialize();
        data += "&dim=" + dim;
        $.get(_root + "/s/facet", data, function(facet) {
            fd.dialog(facet, selection, title);
        });
    }

    $("#txtKeyword").keypress(function(event) {
        if (event.which == 13) {   //enter pressed
            search();
        }
    });

    $("#btnSearch").click(function() {
        search();
    });
    
    $("#btnClearAll").click(function() {
        for (var key in charts) {
            charts[key].reset();
        }
    });

    function search() {
        $form.find("input[name=keyword]").val($("#txtKeyword").val());
        $form.find("input[name=queryType]").val($("#sltQueryType").val());
        $form.find("input[name=dateFrom]").val($("#txtDateFrom").val());
        $form.find("input[name=dateTo]").val($("#txtDateTo").val());
        currentDim = "";
        searchFacet();
    }

    $("#btnReset").click(function() {
        currentDim = "";
        resetFilter();
        searchFacet();
    });

    $("#txtDateFrom").add($("#txtDateTo")).datepicker({
        showOn: "button",
        buttonImage: "../images/calendar.gif",
        buttonImageOnly: true,
        dateFormat: "yy-mm-dd",
        changeMonth: true,
        changeYear: true
    });

    function resetFilter() {
        $form.find("input[name=forumId]").val("");
        $form.find("input[name=threadId]").val("");
        $form.find("input[name=postYear]").val("");
        $form.find("input[name=postMonth]").val("");
        $form.find("input[name=poster]").val("");
        $form.find("input[name=topicId]").val("");
    }

    function searchFacet() {
        var data = $form.serialize();
        $.get(_root + "/s/facets", data, function(facets) {
            for (var i = 0; i < facets.length; i++) {
                var facet = facets[i];
                var dim = facet.dim;
                var selection = $form.find("input[name=" + dim + "]").val();
                if (dim == currentDim && selection != "") {
                    fws[dim].update(facet);
                } else {
                    fws[dim].render(facet, selection);
                }
                charts[dim].render(facet, data);
            }
            fs.render(facets);
        });
        $.notify("Refreshed.", "success");
    }
});
