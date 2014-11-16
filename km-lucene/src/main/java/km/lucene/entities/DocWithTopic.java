package km.lucene.entities;

import java.util.ArrayList;
import java.util.List;

public class DocWithTopic {
	private int id;
	private List<TopicWithProp> topics;

	public DocWithTopic(int id, List<TopicWithProp> topics) {
		this.id = id;
		this.topics = new ArrayList<>();
		this.topics.addAll(topics);
	}

	public int getId() {
		return id;
	}

	public List<TopicWithProp> getTopics() {
		return topics;
	}
}
