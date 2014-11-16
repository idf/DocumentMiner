package km.mallet.postprocess;

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

	public String[] getTerms() {
		return terms;
	}
}
