package km.mallet.postprocess;

public class DocWithProp {
	private int id;
	private double prop;

	public DocWithProp(int id, double prop) {
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
