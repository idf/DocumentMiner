package km.mallet.postprocess;

import km.common.Config;
import km.common.json.JsonWriter;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class App {

	public static void main(String[] args) throws IOException {
		File keysFile = new File(Config.settings.getMalletSettings().getRootFolder(), "keys_50.txt");
		File topicsFile = new File(Config.settings.getMalletSettings().getRootFolder(), "topics_50.txt");
		File weightsFile = new File(Config.settings.getMalletSettings().getRootFolder(), "topic_word_weights_50.txt");
		File countsFile = new File(Config.settings.getMalletSettings().getRootFolder(), "word_topic_counts_50.txt");

//		 List<Topic> topics = TopicParser.parse(keysFile);
		// JSONUtil.save(topics, "E:/project/kd/data/topic/topic_terms.json");

//		 List<TopicWithTermWeight> topicsWithTermWeight = TopicWithTermWeightParser.parse(weightsFile);
//		 JSONUtil.save(topicsWithTermWeight, "E:/project/kd/data/topic/topic_terms_weight.json");

//		 List<TopicWithTermCount> topicsWithCount = TopicWithTermCountParser.parse(countsFile);
//		 JSONUtil.save(topicsWithCount, "E:/project/kd/data/topic/topic_termcount.json");

		// List<TopicWithDocs> topics = TopicWithDocsParser.parse(topicsFile);
		// JSONUtil.save(topics, "E:/project/kd/data/topic/topic_docs.json");

//		List<DocWithTopic> docs = DocWithTopicParser.parse(topicsFile);
//		for (int start = 0; start < docs.size(); start += 50000) {
//			int end = start + 50000;
//			if (end > docs.size()) {
//				end = docs.size();
//			}
//			JSONUtil.save(docs.subList(start, end), "E:/project/kd/data/topic/doc_topics_" + start + ".json");
//		}
		
//		List<TermWithTopicWeight> terms = TermWithTopicWeightParser.parse(weightsFile);
//		JSONUtil.save(terms, "E:/project/kd/data/topic/term_topics.json");
		
		List<TermWithCount> terms = TermWithCountParser.parse(countsFile);
		JsonWriter.saveList(terms, Config.settings.getTopicSettings().getTermsPath());
	}
}
