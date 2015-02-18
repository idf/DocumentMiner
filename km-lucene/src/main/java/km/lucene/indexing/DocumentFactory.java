package km.lucene.indexing;

import io.deepreader.java.commons.util.DateUtils;
import km.lucene.constants.FieldName;
import km.lucene.entities.DocWithTopic;
import km.lucene.entities.Post;
import km.lucene.entities.Quote;
import km.lucene.entities.TopicWithProp;
import km.lucene.services.ThreadService;
import org.apache.lucene.document.*;
import org.apache.lucene.facet.FacetField;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

public class DocumentFactory {

	private final static ThreadService ts = new ThreadService();

	public static Document newInstance(Post post, Map<Integer, DocWithTopic> docTopics) throws IOException {
		Document doc = new Document();
		doc.add(new FacetField(FieldName.FORUM_ID, Integer.toString(post.getForumId())));
		doc.add(new StoredField(FieldName.FORUM_ID, post.getForumId()));
		doc.add(new FacetField(FieldName.THREAD_ID, Integer.toString(post.getThreadId())));
		doc.add(new StoredField(FieldName.THREAD_ID, post.getThreadId()));
		doc.add(new TextField(FieldName.THREAD_TITLE, ts.getTitle(post.getThreadId()), Field.Store.YES));
		doc.add(new IntField(FieldName.ID, post.getId(), Field.Store.YES));

		String postDateStr = post.getPostDate();
		Date postDate = DateUtils.parse(postDateStr, "dd-MM-yyyy, hh:mm a");
		if (postDate == null) {
			throw new IOException("Invalid date format of " + postDateStr);
		}
		long postDateLong = postDate.getTime();
		doc.add(new LongField(FieldName.POST_DATE, postDateLong, Field.Store.YES));
		postDateStr = DateUtils.format(postDate, "yyyy-MM");
		String yearStr = postDateStr.substring(0, 4);
		String monthStr = postDateStr.substring(5, 7);
		doc.add(new FacetField(FieldName.POST_YEAR, yearStr));
		doc.add(new FacetField(FieldName.POST_MONTH, monthStr));


		// Daniel: add posting list
		FieldType fieldTypeDoc = new FieldType();
		fieldTypeDoc.setStoreTermVectors(true);
		fieldTypeDoc.setStoreTermVectorPositions(true);
		fieldTypeDoc.setStoreTermVectorOffsets(true); // for highlighting, rake4j
		fieldTypeDoc.setIndexed(true);
		fieldTypeDoc.setStored(true);  //for Rake to re-analyze the content

        doc.add(new Field(FieldName.CONTENT, post.getContent(), fieldTypeDoc));
		doc.add(new IntField(FieldName.STOREY, post.getStorey(), Field.Store.YES));

		String poster = post.getPoster().isEmpty() ? "Anonymous" : post.getPoster();
		doc.add(new FacetField(FieldName.POSTER, poster));
		doc.add(new StoredField(FieldName.POSTER, poster));
		doc.add(new StringField(FieldName.POSTER_LEVEL, post.getPosterLevel(), Field.Store.YES));

		for (Quote q : post.getQuotes()) {
			doc.add(new IntField(FieldName.QUOTE_ID, q.getId(), Field.Store.YES));
			doc.add(new IntField(FieldName.QUOTE_REF_ID, q.getRefId(), Field.Store.YES));
			doc.add(new StringField(FieldName.QUOTE_POSTER, (q.getPoster().isEmpty() ? "Anonymous" : q.getPoster()), Field.Store.YES));
			doc.add(new TextField(FieldName.QUOTE_CONTENT, q.getContent(), Field.Store.YES));
		}

		TopicWithProp topic = docTopics.get(post.getId()).getTopics().get(0);
		doc.add(new FacetField(FieldName.TOPIC_ID, Integer.toString(topic.getId())));
		doc.add(new DoubleField(FieldName.TOPIC_PROP, topic.getProp(), Field.Store.YES));

		return doc;
	}
}
