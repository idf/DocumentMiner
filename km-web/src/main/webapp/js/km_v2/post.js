var Post = Post || function(container) {
    "use strict";
    var $post = $(container);

    $post.on("click", "#more", function() {
        var postId = $(this).parents("div.post_panel:eq(0)").prop("id");
        var $content = $(this).parents("div.content:eq(0)");
        var data = "postId=" + postId;
        $.get("/s/content", data, function(result) {
            $content.html(result.content);
        });
    });

    $post.on("click", "#more_quote", function() {
        var postId = $(this).parents("div.post_panel:eq(0)").prop("id");
        var quoteId = $(this).parents("div.quote_panel:eq(0)").prop("id");
        var $quoteContent = $(this).parents("div.quote:eq(0)");
        var data = "postId=" + postId + "&quoteId=" + quoteId;
        $.get("/s/quoteContent", data, function(result) {
            $quoteContent.html(result.quoteContent);
        });
    });

    $post.on("click", "div.up", function() {
        event.preventDefault();
        var $up = $(this);
        var $thread = $(this).parents("div.thread_panel:eq(0)");
        var threadId = $thread.prop("id");
        var $post = $(this).parents("div.post_panel:eq(0)");
        var storey = $(this).next("div.storey:eq(0)").prop("id");
        var data = "threadId=" + threadId + "&storey=" + storey;
        $.get("/s/postsInFrontOfStorey", data, function(posts) {
            if (posts.length===0) {
                $.notify("No more posts.", "info");
                $up.hide();
            } else {
                var html = [];
                for (var i = 0; i < posts.length; i++) {
                    var post = posts[i];
                    if ($thread.find("div.post_panel#" + post.id).length > 0) {
                        break;
                    }
                    html.push(getPostHTML(post));
                }
                html.reverse();
                $post.before(html.join(""));
                cleanUpDown($thread);
            }
        });
    });

    $post.on("click", "div.down", function() {
        event.preventDefault();
        var $up = $(this);
        var $thread = $(this).parents("div.thread_panel:eq(0)");
        var threadId = $thread.prop("id");
        var $post = $(this).parents("div.post_panel:eq(0)");
        var storey = $(this).prev("div.storey:eq(0)").prop("id");
        var data = "threadId=" + threadId + "&storey=" + storey;
        $.get("/s/postsBackOfStorey", data, function(posts) {
            if (posts.length == 0) {
                $.notify("No more posts.", "info");
                $up.hide();
            } else {
                var html = [];
                for (var i = 0; i < posts.length; i++) {
                    var post = posts[i];
                    if ($thread.find("div.post_panel#" + post.id).length > 0) {
                        break;
                    }
                    html.push(getPostHTML(post));
                }
                $post.after(html.join(""));
                cleanUpDown($thread);
            }
        });
    });

    function cleanUpDown($thread) {
        var $prevPost = null;
        var prevStorey = 0;
        $thread.find("div.post_panel").each(function() {
            var storey = parseInt($(this).find("div.storey:eq(0)").prop("id"));
            if (storey == 1) {
                $(this).find("div.up").hide();
            } else if (storey == prevStorey + 1) {
                $prevPost.find("div.down").hide();
                $(this).find("div.up").hide();
            }
            $prevPost = $(this);
            prevStorey = storey;
        });
    }

    this.render = function(posts) {
        var html = [];
        var prevThreadId = 0;
        for (var i = 0; i < posts.length; i++) {
            var post = posts[i];
            if (post.threadId !== prevThreadId) {
                if (prevThreadId !== 0) {
                    html.push("</div>");  // end of previous thread
                }
                html.push("<div class='thread_panel' id='{0}'>".build(post.threadId));
                html.push(getThreadTitleHTML(post));
                prevThreadId = post.threadId;
            }
            html.push(getPostHTML(post));
        }
        html.push("</div>");  // end of last thread
        $post.html(html.join(""));
        $post.show();
    };
    
    this.hide = function() {
        $post.hide();
    };

    function getThreadTitleHTML(post) {
        var html = [];
        html.push("<div>");
        html.push("<div class='forum_title'>{0}</div>".build(post.forumTitle));
        html.push("<div class='thread_title'>{0}</div>".build(post.threadTitle));
        html.push("</div>");
        return html.join("");
    }

    function getPostHTML(post) {
        var html = [];
        html.push("<div class='post_panel' id='{0}'>".build(post.id));
        var quotes = post.quotes;
        if (quotes.length > 0) {
            for (var j = 0; j < quotes.length; j++) {
                var quote = quotes[j];
                html.push(getQuoteHTML(quote));
            }
        }
        html.push("<div class='content'>{0}".build(post.content));
        if (post.full === false) {
            html.push(" <small id='more'><span class='glyphicon glyphicon-forward'></span></small>");
        }
        html.push("</div>");
        html.push("<div class='remark'>by {0} {1}</div>".build(post.poster, moment(post.postDate).fromNow()));
        html.push("<div class='up'><span class='glyphicon glyphicon-chevron-up'></span></div>");
        html.push("<div class='storey' id='{0}'># {1}</div>".build(post.storey, post.storey.toString().toNumberWithCommas()));
        html.push("<div class='down'><span class='glyphicon glyphicon-chevron-down'></span></div>");
        html.push("</div>");
        return html.join("");
    }

    function getQuoteHTML(quote) {
        var html = [];
        html.push("<div class='quote_panel' id='{0}'>".build(quote.id));
        html.push("<div class='quote'>{0}".build(quote.content));
        if (quote.full === false) {
            html.push(" <small id='more_quote'><span class='glyphicon glyphicon-forward'></span></small>");
        }
        html.push("</div>");
        html.push("</div>");
        return html.join("");
    }
};
