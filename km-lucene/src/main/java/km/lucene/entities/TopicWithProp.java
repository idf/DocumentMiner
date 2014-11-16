package km.lucene.entities;

public class TopicWithProp {
	private int id;
	private double prop;

	public TopicWithProp(int id, double prop) {
		this.id = id;
		this.prop = prop;
	}

	public int getId() {
		return id;
	}

	public double getProp() {
		return prop;
	}
}
