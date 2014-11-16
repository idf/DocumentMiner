package km.crawler.entities;

import java.util.ArrayList;
import java.util.List;

public class Post {

    private int forumId;
    private int threadId;
    private int id;
    private String postDate;
    private String content;
    private int storey;
    private String poster;
    private String posterLevel;
    private String joinDate;
    private int totalPost;
    private List<Quote> quotes;

    public Post(int forumId, int threadId, int id, String postDate, String content,
            int storey, String poster, String posterLevel, String joinDate, int totalPost) {
        this.forumId = forumId;
        this.threadId = threadId;
        this.id = id;
        this.postDate = postDate;
        this.content = content;
        this.storey = storey;
        this.poster = poster;
        this.posterLevel = posterLevel;
        this.joinDate = joinDate;
        this.totalPost = totalPost;
        this.quotes = new ArrayList<>();
    }

    public void addQuuote(Quote quote) {
        this.quotes.add(quote);
    }

    public void removeQuoteStringFromContent(String quoteString) {
        this.content = this.content.replace(quoteString, "").trim();
    }

    public int getForumId() {
        return forumId;
    }

    public int getThreadId() {
        return threadId;
    }

    public int getId() {
        return id;
    }

    public String getPostDate() {
        return postDate;
    }

    public String getContent() {
        return content;
    }

    public int getStorey() {
        return storey;
    }

    public String getPoster() {
        return poster;
    }

    public String getPosterLevel() {
        return posterLevel;
    }

    public String getJoinDate() {
        return joinDate;
    }

    public int getTotalPost() {
        return totalPost;
    }

    public List<Quote> getQuotes() {
        return quotes;
    }
}
