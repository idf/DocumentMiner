$(document).ready(function() {
    var $form = $("#frmSearch");
    var pw = new PostWidget($("#divPosts"));
    var pgTop = new PageWidget($("#divPageUp"), gotoPage, changePageSize);
    var pgBtm = new PageWidget($("#divPageDown"), gotoPage, changePageSize);
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
    var currentDim = "";

    searchFacet();

    function gotoPage(page) {
        changePage(page);
        searchPost();
    }

    function changePage(page) {
        $form.find("input[name=page]").val(page);
    }

    function changePageSize(pageSize) {
        $form.find("input[name=pageSize]").val(pageSize);
        changePage(1);
        searchPost();
    }

    function filter(dim, selection) {
        currentDim = dim;
        $form.find("input[name=" + dim + "]").val(selection);
        searchPost();
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

    function search() {
        $form.find("input[name=keyword]").val($("#txtKeyword").val());
        $form.find("input[name=queryType]").val($("#sltQueryType").val());
        $form.find("input[name=dateFrom]").val($("#txtDateFrom").val());
        $form.find("input[name=dateTo]").val($("#txtDateTo").val());
        currentDim = "";
        changePage(1);
        searchPost();
        searchFacet();
    }

    $("#btnReset").click(function() {
        currentDim = "";
        resetFilter();
        searchPost();
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

    $("#sltSort").change(function() {
        var sortType = $(this).val();
        $form.find("input[name=sortType]").val(sortType);
        changePage(1);
        searchPost();
    });

    function searchPost() {
        var data = $form.serialize();
        var url = "";
        if ($("#chkGroupByThread").is(":checked") == true) {
            url = "/s/postsGBT";
        } else {
            url = "/s/posts";
        }
        $.get(_root + url, data, function(result) {
            renderSearchResult(result.pageInfo, result.elapsed);
            pw.render(result.pageInfo.start, result.posts);
            pgTop.render(result.pageInfo);
            pgBtm.render(result.pageInfo);
            $.notify("Posts refreshed.", "success");
        });
    }

    function renderSearchResult(pageInfo, elapsed) {
        var start = pageInfo.start;
        var end = pageInfo.end;
        var totalHits = pageInfo.totalHits;
        totalHits = totalHits.toString().toNumberWithCommas();
        var seconds = (elapsed/1000).toRound(2);
        var html = "{0} - {1} of about {2} results ({3} seconds)"
                .format([start, end, totalHits, seconds]);
        $("#spSearchResult").html(html);
    }

    function searchFacet() {
        var data = $form.serialize();
        $.get(_root + "/s/facets", data, function(facets) {
            for (var i = 0; i < facets.length; i++) {
                var facet = facets[i];
                var selection = $form.find("input[name=" + facet.dim + "]").val();
                if (facet.dim == currentDim && selection != "") {
                    fws[facet.dim].update(facet);
                } else {
                    fws[facet.dim].render(facet, selection);
                }
            }
            fs.render(facets);
        });
    }
});
