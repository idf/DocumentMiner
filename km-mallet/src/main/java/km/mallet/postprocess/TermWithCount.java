package km.mallet.postprocess;

public class TermWithCount {
	private int id;
	private String term;
	private int count;

	public TermWithCount(int id, String term, int count) {
		this.id = id;
		this.term = term;
		this.count = count;
	}

	public int getId() {
		return id;
	}

	public String getTerm() {
		return term;
	}

	public int getCount() {
		return count;
	}
}
