package km.lucene.search;

import java.io.IOException;
import java.util.Date;
import km.common.util.DateUtil;
import km.lucene.analysis.CustomAnalyzer;
import km.lucene.constants.FieldName;
import km.lucene.entities.Post;
import km.lucene.entities.Quote;
import km.lucene.services.ForumService;
import km.lucene.services.ThreadService;
import km.lucene.services.TitleService;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.util.Version;

public class PostFactory {

    private static final TitleService fs = new ForumService();
    private static final TitleService ts = new ThreadService();

    public static Post create(Query query, IndexSearcher searcher, ScoreDoc scoreDoc, Document doc) throws IOException {
        Analyzer analyzer = new CustomAnalyzer(Version.LUCENE_48);

        int forumId = Integer.parseInt(doc.get(FieldName.FORUM_ID));
        String forumTitle = fs.getTitle(forumId);
        int threadId = Integer.parseInt(doc.get(FieldName.THREAD_ID));

        String threadTitle = ts.getTitle(threadId);
        threadTitle = getFragment(query, FieldName.THREAD_TITLE, analyzer, threadTitle);

        int id = Integer.parseInt(doc.get(FieldName.ID));
        Long postDateLong = Long.parseLong(doc.get(FieldName.POST_DATE));
        Date postDate = new Date(postDateLong);
        String postDateStr = DateUtil.format(postDate, "yyyy-MM-dd hh:mm");

        String content = doc.get(FieldName.CONTENT);
        String fragment = getFragment(query, FieldName.CONTENT, analyzer, content);
        boolean full = false;
        if (fragment.length() >= content.length()) {
            full = true;
        }

        int storey = Integer.parseInt(doc.get(FieldName.STOREY));
        String poster = doc.get(FieldName.POSTER);
        String posterLevel = "";
        String joinDateStr = "";
        int totalPosts = 0;
        Post post = new Post(forumId, threadId, id, postDateStr, fragment, storey, poster, posterLevel, joinDateStr, totalPosts);
        post.setForumTitle(forumTitle);
        post.setThreadTitle(threadTitle);
        post.setFull(full);
        post.setScore(scoreDoc.score);

        IndexableField[] quoteIds = doc.getFields(FieldName.QUOTE_ID);
        if (quoteIds.length > 0) {
            IndexableField[] quoteRefIds = doc.getFields(FieldName.QUOTE_REF_ID);
            IndexableField[] quotePosters = doc.getFields(FieldName.QUOTE_POSTER);
            IndexableField[] quoteContents = doc.getFields(FieldName.QUOTE_CONTENT);
            for (int i = 0; i < quoteIds.length; i++) {
                int quoteId = (Integer) quoteIds[i].numericValue();
                int quoteRefId = (Integer) quoteRefIds[i].numericValue();
                String quotePoster = quotePosters[i].stringValue();

                String quoteContent = quoteContents[i].stringValue();
                String quoteFragment = getFragment(query, FieldName.QUOTE_CONTENT, analyzer, quoteContent);
                boolean quoteFull = false;
                if (quoteFragment.length() >= quoteContent.length()) {
                    quoteFull = true;
                }

                Quote quote = new Quote(quoteId, quoteRefId, quotePoster, quoteFragment);
                quote.setFull(quoteFull);
                post.addQuuote(quote);
            }
        }
        return post;
    }

    private static String getFragment(Query query, String field, Analyzer analyzer, String content) {
        QueryScorer scorer = new QueryScorer(query, field);
        Highlighter highlighter = new Highlighter(scorer);
        String fragment = null;
        try {
            fragment = highlighter.getBestFragment(analyzer, field, content);
        } catch (IOException | InvalidTokenOffsetsException e) {
        }
        if (fragment == null) {
            if (content.length() < 200) {
                fragment = content;
            } else {
                fragment = content.substring(0, 150);
            }
        }
        return fragment;
    }
}
