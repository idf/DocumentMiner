package km.crawler.entities;

public class Forum {

    private int id;
    private String title;
    private String desc;

    public Forum(int id, String title, String desc) {
        this.id = id;
        this.title = title;
        this.desc = desc;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDesc() {
        return desc;
    }
}
