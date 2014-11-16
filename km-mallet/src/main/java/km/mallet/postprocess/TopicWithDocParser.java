package km.mallet.postprocess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TopicWithDocParser {
	private static final Logger LOG = Logger.getLogger(TopicWithDocParser.class.getName());

	/*
	 * assumption: 50 topics, 327454 documents
	 */
	private static final int totalTopics = 50;
	private static final int totalDocs = 327454;

	public static List<TopicWithDoc> parse(File file) {
		int[] docIds = new int[totalDocs];
		double[][] weights = new double[totalDocs][totalTopics];

		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
			String line = br.readLine(); // ignore the first line
			while ((line = br.readLine()) != null) {
				String[] values = line.split("\\s");
				int idx = Integer.parseInt(values[0]);
				int docId = Integer.parseInt(values[1]);
				docIds[idx] = docId;
				for (int i = 2; i < values.length; i += 2) {
					int topicId = Integer.parseInt(values[i]);
					double weight = Double.parseDouble(values[i + 1]);
					weights[idx][topicId] = weight;
				}
				System.out.println("parsed " + idx);
			}
		} catch (IOException e) {
			LOG.log(Level.SEVERE, "Fail to parse " + file.getPath(), e);
		}

		List<TopicWithDoc> topics = new ArrayList<>();
		for (int topicId = 0; topicId < totalTopics; topicId++) {
			Map<Integer, Double> docs = new HashMap<>();
			for (int idx = 0; idx < totalDocs; idx++) {
				int docId = docIds[idx];
				double weight = weights[idx][topicId];
				if (weight > 0.8) { // must ensure 100 documents
					docs.put(docId, weight);
				}
			}
			ValueComparator vc = new ValueComparator(docs);
			TreeMap<Integer, Double> sortedDocs = new TreeMap<>(vc);
			sortedDocs.putAll(docs);
			TopicWithDoc topic = new TopicWithDoc(topicId, sortedDocs);
			topics.add(topic);
		}
		return topics;
	}

	public static class ValueComparator implements Comparator<Integer> {

		Map<Integer, Double> map;

		public ValueComparator(Map<Integer, Double> map) {
			this.map = map;
		}

		@Override
		public int compare(Integer key1, Integer key2) {
			if (map.get(key1) >= map.get(key2)) {
				return -1;
			} else {
				return 1;
			}
		}
	}
}
