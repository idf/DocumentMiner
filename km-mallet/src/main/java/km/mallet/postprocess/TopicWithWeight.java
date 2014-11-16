package km.mallet.postprocess;

public class TopicWithWeight implements Comparable<TopicWithWeight> {
	private int id;
	private double weight;
	
	public TopicWithWeight(int id, double weight) {
		this.id = id;
		this.weight = weight;
	}

	public int getId() {
		return id;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public double getWeight() {
		return weight;
	}

	@Override
	public int compareTo(TopicWithWeight o) {
		if (this.weight > o.weight) {
			return -1;
		} else if (this.weight < o.weight) {
			return 1;
		}
		return 0;
	}
}
