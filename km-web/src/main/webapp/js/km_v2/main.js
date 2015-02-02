$(document).ready(function() {
    var titles = {postMonth: "Any month", postYear: "Any year", topicId: "Any topic", forumId: "Any forum", threadId: "Any thread", poster: "Any poster"};
    var selecteds = {postMonth: "", postYear: "", topicId: "", forumId: "", threadId: "", poster: ""};
    var cols = {postMonth: 3, postYear: 3, topicId: 1, forumId: 1, threadId: 1, poster: 1};
    var menu = new Menu("#topMenu");
    for (var key in titles) {
        menu.addItem(new MenuItem(key, titles[key], getFacetByDim));
    }
    menu.render();
    var dd = new Dropdown("#topMenuDropdown", filter, getFacetMoreItems);
    var pp = new Popup("#topMenuPopup", filter);
    var post = new Post("#posts");
    var pg = new Pagination("#pagination", goto);
    var charts = {
        postMonth: new Chart("divChartMonth", "Posts per month"),
        postYear: new Chart("divChartYear", "Posts per year"),
        topicId: new Chart("divChartTopic", "Posts per topic"),
        forumId: new Chart("divChartForum", "Posts per forum"),
        threadId: new Chart("divChartThread", "Posts per thread"),
        poster: new Chart("divChartPoster", "Posts per poster")
    };
    var queryStr = "";
    var page = 1;
    var sort = 2;

    $("#txtQuery").focus();
    $("#txtQuery").keypress(function() {
        if (event.which == 13) {   //enter pressed
            search();
        }
    });
    $("#btnSearch").click(function() {
        search();
    });

    function search() {
        queryStr = $("#txtQuery").val();
        page = 1;
        var vs = queryStr.include("vs", " ");
        if (vs) {
            post.hide();
            pg.hide();
            $(".result").hide();
            searchFacet();
        } else {
            searchPost();
            searchFacet();
        }
    }

    function getFacetByDim($srcEle, dim) {
        var position = $srcEle.position();
        if (dd.visible(dim)) {
            dd.hide();
        } else {
            var filters = [];
            for (var key in selecteds) {
                filters.push(key + ":" + selecteds[key]);
            }
            var data = [];
            data.push("query=" + queryStr);
            data.push("filter=" + filters.join(","));
            data.push("dim=" + dim);
            $.get("/s/v2/facetTopItems", data.join("&"), function(facet) {
                facet.title = titles[dim];
                facet.selected = selecteds[dim];
                facet.position = {left: position.left - 10, top: position.top + 20};
                facet.cols = cols[dim];
                dd.render(facet);
            });
        }
    }

    function filter(dim, selected) {
        selecteds[dim] = selected;
        search();
        menu.highlight(dim, selected);
    }

    function goto(newPage) {
        page = newPage;
        searchPost();
    }

    function searchPost() {
        var filters = [];
        for (var key in selecteds) {
            filters.push(key + ":" + selecteds[key]);
        }
        var data = [];
        data.push("query=" + queryStr);
        data.push("filter=" + filters.join(","));
        data.push("page=" + page);
        data.push("sort=" + sort);
        $.get("/s/v2/posts", data.join("&"), function(result) {
            showResult(result.pageInfo, result.elapsed);
            post.render(result.posts);
            pg.render(result.pageInfo);
            $.notify("Posts refreshed.", "success");
        });
    }

    function searchFacet() {
        var filters = [];
        for (var key in selecteds) {
            filters.push(key + ":" + selecteds[key]);
        }
        var data = [];
        data.push("query=" + queryStr);
        data.push("filter=" + filters.join(","));
        $.get("/s/v2/facets", data.join("&"), function(facets) {
            for (var dim in facets) {
                charts[dim].render(facets[dim]);
            }
            $.notify("Facets refreshed.", "success");
        });
    }

    function getFacetMoreItems(dim) {
        var filters = [];
        for (var key in selecteds) {
            filters.push(key + ":" + selecteds[key]);
        }
        var data = [];
        data.push("query=" + queryStr);
        data.push("filter=" + filters.join(","));
        data.push("dim=" + dim);
        $.get("/s/v2/facet", data.join("&"), function(facet) {
            facet.title = titles[dim];
            facet.selected = selecteds[dim];
            facet.cols = 1;
            pp.render(facet);
        });
    }

    function showResult(pageInfo, elapsed) {
        var totalThreads = pageInfo.totalThreads.toString().toNumberWithCommas();
        var totalPosts = pageInfo.totalPosts.toString().toNumberWithCommas();
        var seconds = (elapsed / 1000).toRound(2);
        $(".result").html("Total {0} threads, {1} posts, ({2} seconds)".build(totalThreads, totalPosts, seconds));
        $(".result").show();
    }
});
