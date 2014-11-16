package km.lucene.entities;

public class Topic {
	private int id;
    private String[] terms;

    public Topic(int id, String[] terms) {
        this.id = id;
        this.terms = terms;
    }

    public int getId() {
        return id;
    }
    
    public String getTitle() {
    	return String.format("{%s, %s, %s}", terms[0], terms[1], terms[2]);
    }

	public String[] getTerms() {
		return terms;
	}
}
