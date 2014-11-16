package km.mallet.postprocess;

public class TopicWithProp implements Comparable<TopicWithProp> {
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

	@Override
	public int compareTo(TopicWithProp o) {
		if (this.prop >= o.prop) {
			return -1;
		} else {
			return 1;
		}
	}
}
