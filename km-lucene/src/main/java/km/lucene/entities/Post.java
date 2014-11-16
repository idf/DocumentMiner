package km.lucene.entities;

import java.util.ArrayList;
import java.util.List;

public class Post {

    private float score;
    private int forumId;
    private String forumTitle;
    private int threadId;
    private String threadTitle;
    private int id;
    private String postDate;
    private String content;
    private boolean full = false;
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

    public void setScore(float score) {
        this.score = score;
    }

    public void setForumTitle(String forumTitle) {
        this.forumTitle = forumTitle;
    }

    public void setThreadTitle(String threadTitle) {
        this.threadTitle = threadTitle;
    }

    public float getScore() {
        return score;
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

    public String getForumTitle() {
        return forumTitle;
    }

    public String getThreadTitle() {
        return threadTitle;
    }

    public boolean isFull() {
        return full;
    }

    public void setFull(boolean full) {
        this.full = full;
    }
}
