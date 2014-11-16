package km.lucene.services;

import km.lucene.entities.Topic;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TopicService implements TitleService {

	private static final Logger LOG = Logger.getLogger(TopicService.class.getName());
	private static Map<Integer, Topic> topics;
	
	public static void init(String filename) {
		loadTopics(filename);
	}
	
	@Override
	public String getTitle(int id) {
		if (topics.containsKey(id)) {
			return topics.get(id).getTitle();
		}
		return null;
	}

	private static void loadTopics(String filename) {
		topics = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filename)), StandardCharsets.UTF_8))) {
            String line;
            br.readLine(); // skip the first line: "#doc name topic proportion ..."
            while ((line = br.readLine()) != null) {
                String[] values = line.split("\\s");
                int id = Integer.parseInt(values[0]);
                // String[] terms = Arrays.copyOfRange(values, 2, values.length);  // terms, index of of range error
                String[] terms = Arrays.copyOfRange(values, 1, values.length);
                Topic topic = new Topic(id, terms);
                topics.put(id, topic);
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Fail to load topic data.", e);
        }
	}
}
