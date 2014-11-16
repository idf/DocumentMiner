package km.mallet.postprocess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TermWithTopicWeightParser {
	private static final Logger LOG = Logger.getLogger(TopicWithTermWeightParser.class.getName());

	public static List<TermWithTopicWeight> parse(File file) {
		Map<String, TermWithTopicWeight> terms = new HashMap<>();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] values = line.split("\\s");
				int topicId = Integer.parseInt(values[0]);
				String term = values[1];
				double weight = Double.parseDouble(values[2]);
				TopicWithWeight topic = new TopicWithWeight(topicId, weight);
				if (terms.containsKey(term)) {
					TermWithTopicWeight termWTW = terms.get(term);
					termWTW.addTopic(topic);
					terms.put(term, termWTW);
				} else {
					TermWithTopicWeight termWTW = new TermWithTopicWeight(term, topic);
					terms.put(term, termWTW);
				}
				System.out.println(topicId + ", " + term);
			}
		} catch (IOException e) {
			LOG.log(Level.SEVERE, "Fail to parse " + file.getPath(), e);
		}

		List<TermWithTopicWeight> termList = new ArrayList<>();
		for (TermWithTopicWeight term : terms.values()) {
			term.sort();
			term.normalize();
			termList.add(term);
		}
		System.out.println(termList.size());

		return termList;
	}
}
