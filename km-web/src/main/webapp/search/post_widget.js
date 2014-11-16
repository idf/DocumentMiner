var PostWidget = PostWidget || function($div) {

    $div.on("click", "a.more", function() {
        event.preventDefault();
        var $content = $(this).parents("div.content:eq(0)");
        var postId = $(this).parents("div.post:eq(0)").prop("id");
        var data = "postId=" + postId;
        $.get(_root + "/s/content", data, function(result) {
            $content.html(result.content);
        });
    });

    $div.on("click", "a.more_quote", function() {
        event.preventDefault();
        var $quoteContent = $(this).parents("div.quote:eq(0)");
        var postId = $(this).parents("div.post:eq(0)").prop("id");
        var quoteId = $quoteContent.prop("id");
        var data = "postId=" + postId + "&quoteId=" + quoteId;
        $.get(_root + "/s/quoteContent", data, function(result) {
            $quoteContent.html(result.quoteContent);
        });
    });

    $div.on("click", "a.up", function() {
        event.preventDefault();
        var $a = $(this);
        var $storey = $a.parents("div.storey:eq(0)");
        var storey = $storey.prop("id");
        var $post = $storey.parents("div.post");
        var $thread = $post.parents("div.thread:eq(0)");
        var threadId = $thread.prop("id");
        var data = "threadId=" + threadId + "&storey=" + storey;
        $.get(_root + "/s/postsInFrontOfStorey", data, function(posts) {
            if (posts.length == 0) {
                $.notify("No more posts.", "info");
                $a.hide();
            } else {
                var html = [];
                for (var i=0; i<posts.length; i++) {
                    var post = posts[i];
                    if ($thread.find("div.post#" + post.id).length > 0) {
                        break;
                    }
                    html.push(getPostHTML(post));
                }
                html.reverse();
                $post.before(html.join(""));
                cleanUpDown();
            }
        });
    });

    $div.on("click", "a.down", function() {
        event.preventDefault();
        var $a = $(this);
        var $storey = $a.parents("div.storey:eq(0)");
        var storey = $storey.prop("id");
        var $post = $storey.parents("div.post");
        var $thread = $post.parents("div.thread:eq(0)");
        var threadId = $thread.prop("id");
        var data = "threadId=" + threadId + "&storey=" + storey;
        $.get(_root + "/s/postsBackOfStorey", data, function(posts) {
            if (posts.length == 0) {
                $.notify("No more posts.", "info");
                $a.hide();
            } else {
                var html = [];
                for (var i=0; i<posts.length; i++) {
                    var post = posts[i];
                    if ($thread.find("div.post#" + post.id).length > 0) {
                        break;
                    }
                    html.push(getPostHTML(post));
                }
                $post.after(html.join(""));
                cleanUpDown();
            }
        });
    });

    function cleanUpDown() {  // clean up show up and down options
        $div.find("div.thread").each(function() {
            var $prevStorey = null;
            var prevStorey = 0;
            $(this).find("div.storey").each(function() {
                var storey = parseInt($(this).prop("id"));
                if (storey == 1) {
                    $(this).find("a.up").hide();
                } else if (storey == prevStorey + 1) {
                    $prevStorey.find("a.down").hide();
                    $(this).find("a.up").hide();
                }
                $prevStorey = $(this);
                prevStorey = storey;
            });
        });
    }

    function getPostHTML(post) {
        var html = [];
        html.push("<div class='post' id='{0}'>".format([post.id]));  // start of post
        html.push("<div class='storey' id='{0}'>#{1} <a class='up' href='#'>Up</a> <a class='down' href='#'>Down</a></div>".format([post.storey, post.storey]));
        html.push("<div class='content_panel'>");  // start of content panel
        var quotes = post.quotes;
        if (quotes.length > 0) {
            for (var j = 0; j < quotes.length; j++) {
                var quote = quotes[j];
                html.push("<div class='quote' id='{0}'>".format([quote.id]));  // start of quote content
                html.push(quote.content);
                if (quote.full != true) {
                    html.push(" <a class='more_quote' href='#'>More</a>");
                }
                html.push("</div>");  // end of quote content
            }
        }
        html.push("<div class='content'>");  // start of content
        html.push(post.content);
        if (post.full != true) {
            html.push(" <a class='more' href='#'>More</a>");
        }
        html.push("</div>");  // end of content
        html.push("</div>");  // end of content panel
        html.push("<div class='poster'>By {0}, posted on {1}</div>".format([post.poster, moment(post.postDate).format("YYYY-MM-DD hh:mm")]));
        html.push("</div>");  // end of post
        return html.join("");
    }

    this.render = function(start, posts) {
        var html = [];
        var prevThreadId = 0;
        for (var i = 0; i < posts.length; i++) {
            var post = posts[i];
            if (post.threadId != prevThreadId) {
                if (prevThreadId != 0) {
                    html.push("</div>");  // end of previous thread
                }
                html.push("<div class='thread' id='", post.threadId, "'>");  // start of thread
                html.push("<div class='title'>#{0} <b>{1}</b> -> {2}</div>".format([start++, post.forumTitle, post.threadTitle]));
                prevThreadId = post.threadId;
            }
            html.push(getPostHTML(post));
        }
        html.push("</div>");  // end of last thread
        $div.html(html.join(""));

        cleanUpDown();
    };
};
