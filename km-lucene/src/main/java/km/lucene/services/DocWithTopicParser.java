package km.lucene.services;

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

import km.lucene.entities.DocWithTopic;
import km.lucene.entities.TopicWithProp;

public class DocWithTopicParser {
	public static Map<Integer, DocWithTopic> parse(String filename) throws IOException {
    	Map<Integer, DocWithTopic> docTopics = new HashMap<>();
    	try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filename)), StandardCharsets.UTF_8))) {
			String line = br.readLine(); // ignore the first line
			while ((line = br.readLine()) != null) {
				String[] values = line.split("\\s");
				int docId = Integer.parseInt(values[1]);
				
				List<TopicWithProp> topics = new ArrayList<>();
				for (int i = 2; i < values.length; i += 2) {
					int topicId = Integer.parseInt(values[i]);
					double weight = Double.parseDouble(values[i + 1]);
					TopicWithProp topic = new TopicWithProp(topicId, weight);
					topics.add(topic);
				}
				DocWithTopic doc = new DocWithTopic(docId, topics);
				docTopics.put(docId, doc);
			}
		}
    	return docTopics;
    }

	public static Map<Integer, Integer> parseSimple(String filename) throws IOException {
    	Map<Integer, Integer> docTopics = new HashMap<>();
    	try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filename)), StandardCharsets.UTF_8))) {
			String line = br.readLine(); // ignore the first line
			while ((line = br.readLine()) != null) {
				String[] values = line.split("\\s");
				int docId = Integer.parseInt(values[1]);
				int topicId = Integer.parseInt(values[2]);
				docTopics.put(docId, topicId);
			}
		}
    	return docTopics;
    }
}
