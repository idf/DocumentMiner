package km.lucene.entities;

public class Thread {

    private int id;
    private int forumId;
    private String title;

    public Thread(int id, int forumId, String title) {
        this.id = id;
        this.forumId = forumId;
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public int getForumId() {
        return forumId;
    }

    public String getTitle() {
        return title;
    }
}
