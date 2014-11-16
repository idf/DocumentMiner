package km.mallet.postprocess;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import km.common.util.MathUtil;

public class TermWithTopicWeight {
	private String term;
	private List<TopicWithWeight> topics;

	public TermWithTopicWeight(String term, TopicWithWeight topic) {
		this.term = term;
		this.topics = new ArrayList<>();
		addTopic(topic);
	}

	public void addTopic(TopicWithWeight topic) {
		this.topics.add(topic);
	}

	public void updateTopics(List<TopicWithWeight> topics) {
		this.topics.clear();
		this.topics.addAll(topics);
	}

	public void sort() {
		Collections.sort(topics, new Comparator<TopicWithWeight>() {
			@Override
			public int compare(TopicWithWeight t1, TopicWithWeight t2) {
				return t1.compareTo(t2);
			}
		});
	}

	public void normalize() {
		double totalWeight = 0;
		for (TopicWithWeight topic : topics) {
			totalWeight += topic.getWeight();
		}
		for (TopicWithWeight topic : topics) {
			topic.setWeight(MathUtil.round(topic.getWeight() / totalWeight, 4));
		}
	}

	public String getTerm() {
		return term;
	}

	public List<TopicWithWeight> getTopics() {
		return topics;
	}
}
