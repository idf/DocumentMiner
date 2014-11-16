package km.crawler.entities;

public class Quote {

    private int id;
    private int refId;
    private String poster;
    private String content;

    public Quote(int id, int refId, String poster, String content) {
        this.id = id;
        this.refId = refId;
        this.poster = poster;
        this.content = content;
    }

    public int getId() {
        return id;
    }

    public int getRefId() {
		return refId;
	}

	public String getPoster() {
        return poster;
    }

    public String getContent() {
        return content;
    }
}
