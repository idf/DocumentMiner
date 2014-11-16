package km.mallet.postprocess;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import km.common.util.MathUtil;

public class TopicWithDoc {
	private int id;
	private List<DocWithProp> docs;

	public TopicWithDoc(int id, TreeMap<Integer, Double> sortedDocs) {
		this.id = id;
		docs = new ArrayList<>();
		int i = 0;
		for (Map.Entry<Integer, Double> entry : sortedDocs.entrySet()) {
			int docId = entry.getKey();
			double weight = MathUtil.round(entry.getValue(), 4);
			DocWithProp doc = new DocWithProp(docId, weight);
			docs.add(doc);

			i++;
			if (i >= 100) { // filter top 100 documents
				break;
			}
		}
	}

	public int getId() {
		return id;
	}

	public List<DocWithProp> getDocs() {
		return docs;
	}
}
