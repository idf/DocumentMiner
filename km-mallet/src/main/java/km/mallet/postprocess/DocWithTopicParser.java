package km.mallet.postprocess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DocWithTopicParser {
	private static final Logger LOG = Logger.getLogger(DocWithTopicParser.class.getName());

	public static List<DocWithTopic> parse(File file) {
		List<DocWithTopic> docs = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
			String line = br.readLine(); // ignore the first line
			while ((line = br.readLine()) != null) {
				String[] values = line.split("\\s");
				int idx = Integer.parseInt(values[0]);
				int docId = Integer.parseInt(values[1]);
				
				List<TopicWithProp> topics = new ArrayList<>();
				for (int i = 2; i < values.length; i += 2) {
					int topicId = Integer.parseInt(values[i]);
					double weight = Double.parseDouble(values[i + 1]);
					TopicWithProp topic = new TopicWithProp(topicId, weight);
					topics.add(topic);
				}
				Collections.sort(topics, comparator);
				DocWithTopic doc = new DocWithTopic(docId, topics);
				docs.add(doc);
				System.out.println("parsed " + idx);
			}
		} catch (IOException e) {
			LOG.log(Level.SEVERE, "Fail to parse " + file.getPath(), e);
		}
		return docs;
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
	
	private static Comparator<TopicWithProp> comparator = new Comparator<TopicWithProp>() {
        @Override
        public int compare(TopicWithProp t1, TopicWithProp t2) {
            return t1.compareTo(t2);
        }
    };
}
