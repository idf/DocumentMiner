package km.lucene.indexing;

import km.common.Settings;
import km.common.json.JsonReader;
import km.common.util.DateUtil;
import km.lucene.analysis.CustomAnalyzer;
import km.lucene.constants.FieldName;
import km.lucene.entities.Post;
import km.lucene.entities.Quote;
import km.lucene.services.DocWithTopicParser;
import km.lucene.services.ThreadService;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

public class ThreadIndexer {
	private final static ThreadService ts = new ThreadService();

	public static void main(String[] args) throws IOException {
		ThreadService.init(Settings.THREADS_PATH);
		Map<Integer, Integer> docTopics = DocWithTopicParser.parseSimple(Settings.MalletSettings.TOPICS_PATH);

		String postPath = Settings.SORTED_POSTS_PATH;
		String indexPath = Settings.THINDEX_PATH;

		System.out.println(String.format("Indexing to directory '%s'...", indexPath));
		Directory dir = FSDirectory.open(new File(indexPath));

		Analyzer analyzer = new CustomAnalyzer(Version.LUCENE_48);
        // Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_48);
		IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_48, analyzer);
		iwc.setOpenMode(OpenMode.CREATE);
		IndexWriter indexWriter = new IndexWriter(dir, iwc);

		JsonReader<Post> jr = new JsonReader<Post>(postPath, Post.class);
		Post post;
		int i = 1;
		int prevThreadId = 0;
		Document doc = null;
		while ((post = jr.next()) != null) {
			if (post.getThreadId() != prevThreadId) {
				if (doc != null) {
					indexWriter.addDocument(doc);
				}

				doc = new Document(); // Shared Thread data
				doc.add(new IntField(FieldName.FORUM_ID, post.getForumId(), Field.Store.NO));
				doc.add(new IntField(FieldName.THREAD_ID, post.getThreadId(), Field.Store.YES));
				doc.add(new TextField(FieldName.THREAD_TITLE, ts.getTitle(post.getThreadId()), Field.Store.NO));
				prevThreadId = post.getThreadId();
			}

			doc.add(new IntField(FieldName.ID, post.getId(), Field.Store.YES));

			String postDateStr = post.getPostDate();
			Date postDate = DateUtil.parse(postDateStr, "dd-MM-yyyy, hh:mm a");
			if (postDate == null) {
				throw new IOException("Invalid date format of " + postDateStr);
			}
			long postDateLong = postDate.getTime();
			doc.add(new LongField(FieldName.POST_DATE, postDateLong, Field.Store.NO));
			postDateStr = DateUtil.format(postDate, "yyyy-MM");
			String yearStr = postDateStr.substring(0, 4);
			String monthStr = postDateStr.substring(5, 7);
			doc.add(new IntField(FieldName.POST_YEAR, Integer.parseInt(yearStr), Field.Store.NO));
			doc.add(new IntField(FieldName.POST_MONTH, Integer.parseInt(monthStr), Field.Store.NO));

			// Daniel: add posting list
			FieldType fieldTypeDoc = new FieldType();
			fieldTypeDoc.setStoreTermVectors(true);
			fieldTypeDoc.setStoreTermVectorPositions(true);
			fieldTypeDoc.setStoreTermVectorOffsets(true); // for highlighting, rake4j
			fieldTypeDoc.setIndexed(true);
			fieldTypeDoc.setStored(true);  //for Rake to re-analyze the content

			doc.add(new Field(FieldName.CONTENT, post.getContent(), fieldTypeDoc));  // multiple contents under the same field
			doc.add(new IntField(FieldName.STOREY, post.getStorey(), Field.Store.YES));

			String poster = post.getPoster().isEmpty() ? "Anonymous" : post.getPoster();
			doc.add(new StringField(FieldName.POSTER, poster, Field.Store.NO));
			doc.add(new StringField(FieldName.POSTER_LEVEL, post.getPosterLevel(), Field.Store.NO));

			for (Quote q : post.getQuotes()) {
				doc.add(new IntField(FieldName.QUOTE_ID, q.getId(), Field.Store.NO));
				doc.add(new IntField(FieldName.QUOTE_REF_ID, q.getRefId(), Field.Store.NO));
				doc.add(new StringField(FieldName.QUOTE_POSTER, (q.getPoster().isEmpty() ? "Anonymous" : q.getPoster()), Field.Store.NO));
				doc.add(new TextField(FieldName.QUOTE_CONTENT, q.getContent(), Field.Store.NO));
			}

			doc.add(new IntField(FieldName.TOPIC_ID, docTopics.get(post.getId()), Field.Store.NO));

			System.out.println(String.format("added post %d, %d", (i++), post.getId()));
		}  // END for posts
		jr.close();

		indexWriter.addDocument(doc); // one last dangling

		System.out.println(String.format("Number of files indexed: %d", indexWriter.numDocs()));
		indexWriter.close();
	}
}
