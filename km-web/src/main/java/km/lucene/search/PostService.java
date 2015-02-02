package km.lucene.search;

import km.lucene.constants.FieldName;
import km.lucene.entities.Post;
import org.apache.lucene.document.Document;
import org.apache.lucene.facet.*;
import org.apache.lucene.facet.taxonomy.FastTaxonomyFacetCounts;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyReader;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PostService {

    private static final Logger LOG = Logger.getLogger(PostService.class.getName());
    private static final FacetsConfig config = new FacetsConfig();
    private static final boolean doDocScores = true;
    private static final boolean doMaxScore = false;
    private static final int pageSize = 10;
    private static final int maxPostsPerThread = 3;
    private IndexSearcher searcher;
    private TaxonomyReader taxoReader;

    public PostService(String indexPath) {
        try {
            IndexReader indexReader = DirectoryReader.open(FSDirectory.open(new File(indexPath)));
            searcher = new IndexSearcher(indexReader);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Fail to create searcher.", e);
        }
    }

    public PostService(String indexPath, String taxoPath) {
        this(indexPath);
        try {
            taxoReader = new DirectoryTaxonomyReader(FSDirectory.open(new File(taxoPath)));
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Fail to create searcher.", e);
        }
    }
    
    public Map<String, Object> getPosts(String queryStr, String filterStr, int page, int sortType) throws IOException, ParseException {
        Map<String, Object> ret = new HashMap<>();
        Map<String, BooleanQuery> queries = QueryParser.parse(queryStr, filterStr);
        Query query;
        if (queries.isEmpty()) {
            query = new MatchAllDocsQuery();
        } else {
            query = queries.values().iterator().next();
        }
        Filter filter = null;
        FacetsCollector fc = new FacetsCollector();
        searcher.search(query, filter, fc);
        Facets facets = new FastTaxonomyFacetCounts(taxoReader, config, fc);
        FacetResult fr = facets.getTopChildren(1, FieldName.THREAD_ID);
        int totalThreads = fr.childCount;
        
        Sort sort = createSort(sortType);
        TopDocs topDocs = searcher.search(query, filter, Integer.MAX_VALUE, sort, doDocScores, doMaxScore);
        int totalPosts = topDocs.totalHits;
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        
        int pageCount = (int) Math.ceil(totalThreads * 1.0 / pageSize);
        if (page > pageCount) {
            page = 1;
        }
        int start = pageSize * (page - 1);
        Map<String, Object> pageInfo = new HashMap<>();
        pageInfo.put("page", page);
        pageInfo.put("totalThreads", totalThreads);
        pageInfo.put("totalPosts", totalPosts);
        pageInfo.put("pageCount", pageCount);
        ret.put("pageInfo", pageInfo);

        long begin = new Date().getTime();
        Set<Integer> threadIds = new HashSet<>();  // threads to be ignore
        Map<Integer, List<Post>> threadPosts = new HashMap<>();
        for (int i = 0; i < scoreDocs.length; i++) {
            ScoreDoc scoreDoc = scoreDocs[i];
            Document doc = searcher.doc(scoreDoc.doc);
            int threadId = Integer.parseInt(doc.get(FieldName.THREAD_ID));

            if (threadIds.contains(threadId)) {
                continue;
            } else if (threadIds.size() < start) {
                threadIds.add(threadId);
                continue;
            }

            if (threadPosts.containsKey(threadId)) {
                List<Post> posts = threadPosts.get(threadId);
                if (posts != null && posts.size() < maxPostsPerThread) {  // ignore posts if already have 3 posts
                    Post post = PostFactory.create(query, searcher, scoreDoc, doc);
                    posts.add(post);
                    threadPosts.put(post.getThreadId(), posts);
                }
            } else {
                Post post = PostFactory.create(query, searcher, scoreDoc, doc);
                List<Post> posts = new ArrayList<>();
                posts.add(post);
                threadPosts.put(threadId, posts);
            }

            if (threadPosts.size() >= pageSize) {  // stop when have enough threads
                break;
            }
        }
        LOG.log(Level.INFO, "{0} Retrieve top posts done", new Date().getTime() - begin);

        ValueComparator vc = new ValueComparator(threadPosts);
        TreeMap<Integer, List<Post>> sortedThreadPosts = new TreeMap<>(vc);
        sortedThreadPosts.putAll(threadPosts);
        
        List<Post> posts = new ArrayList<>();
        for (List<Post> postList: sortedThreadPosts.values()) {
            Collections.sort(postList, comparator);
            posts.addAll(postList);
        }
        ret.put("posts", posts);
        
        return ret;
    }
    private static Comparator<Post> comparator = new Comparator<Post>() {
        @Override
        public int compare(Post o1, Post o2) {
            int num1 = o1.getStorey();
            int num2 = o2.getStorey();
            if (num1 > num2) {
                return 1;
            } else if (num1 < num2) {
                return -1;
            }
            return 0;
        }
    };
    
    public Map<String, Object> search(
            QueryBuilder builder, int pageSize, int page, int sortType) throws IOException, ParseException {

        Map<String, Object> ret = new HashMap<>();

        int topN = page * pageSize;
        Query query = builder.build();
        Filter filter = null;
        Sort sort = createSort(sortType);
        TopDocs topDocs = searcher.search(query, filter, topN, sort, doDocScores, doMaxScore);
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        int totalHits = topDocs.totalHits;
        int pageCount = (int) Math.ceil(totalHits * 1.0 / pageSize);
        if (page > pageCount) {
            page = 1;
        }
        int start = pageSize * (page - 1);
        int end = pageSize * page;
        end = Math.min(end, scoreDocs.length);

        Map<String, Object> pageInfo = new HashMap<>();
        pageInfo.put("start", start + 1);
        pageInfo.put("end", end);
        pageInfo.put("page", page);
        pageInfo.put("pageSize", pageSize);
        pageInfo.put("totalHits", totalHits);
        pageInfo.put("pageCount", pageCount);
        ret.put("pageInfo", pageInfo);

        List<Post> posts = new ArrayList<>();
        for (int i = start; i < end; i++) {
            ScoreDoc scoreDoc = scoreDocs[i];
            Document doc = searcher.doc(scoreDoc.doc);
            Post post = PostFactory.create(query, searcher, scoreDoc, doc);
            posts.add(post);
        }
        ret.put("posts", posts);

        return ret;
    }

    public Map<String, Object> searchGBT(
            QueryBuilder builder, int pageSize, int page, int sortType) throws IOException, ParseException {
        Map<String, Object> ret = new HashMap<>();

        Query query = builder.build();
        Filter filter = null;
        FacetsCollector fc = new FacetsCollector();
        searcher.search(query, filter, fc);
        Facets facets = new FastTaxonomyFacetCounts(taxoReader, config, fc);
        FacetResult fr = facets.getTopChildren(1, FieldName.THREAD_ID);
        int totalHits = fr.childCount;

        int pageCount = (int) Math.ceil(totalHits * 1.0 / pageSize);
        if (page > pageCount) {
            page = 1;
        }
        int start = pageSize * (page - 1);
        int end = pageSize * page;
        end = Math.min(end, totalHits);
        Map<String, Object> pageInfo = new HashMap<>();
        pageInfo.put("start", start + 1);
        pageInfo.put("end", end);
        pageInfo.put("page", page);
        pageInfo.put("pageSize", pageSize);
        pageInfo.put("totalHits", totalHits);
        pageInfo.put("pageCount", pageCount);
        ret.put("pageInfo", pageInfo);

        Sort sort = createSort(sortType);
        TopDocs topDocs = searcher.search(query, filter, Integer.MAX_VALUE, sort, doDocScores, doMaxScore);
        LOG.log(Level.INFO, "total posts found: " + topDocs.totalHits);
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;

        long begin = new Date().getTime();
        Set<Integer> threadIds = new HashSet<>();  // threads to be ignore
        Map<Integer, List<Post>> threadPosts = new HashMap<>();
        for (int i = 0; i < scoreDocs.length; i++) {
            ScoreDoc scoreDoc = scoreDocs[i];
            Document doc = searcher.doc(scoreDoc.doc);
            int threadId = Integer.parseInt(doc.get(FieldName.THREAD_ID));
            LOG.log(Level.INFO, "threadId: " + threadId);

            if (threadIds.contains(threadId)) {
                continue;
            } else if (threadIds.size() < start) {
                threadIds.add(threadId);
                continue;
            }

            if (threadPosts.containsKey(threadId)) {
                List<Post> posts = threadPosts.get(threadId);
                if (posts != null && posts.size() < maxPostsPerThread) {  // ignore posts if already have 3 posts
                    Post post = PostFactory.create(query, searcher, scoreDoc, doc);
                    posts.add(post);
                    threadPosts.put(post.getThreadId(), posts);
                }
            } else {
                Post post = PostFactory.create(query, searcher, scoreDoc, doc);
                List<Post> posts = new ArrayList<>();
                posts.add(post);
                threadPosts.put(threadId, posts);
            }

            if (threadPosts.size() >= pageSize) {  // stop when have enough threads
                break;
            }
        }
        LOG.log(Level.INFO, "{0} Retrieve top posts done", new Date().getTime() - begin);

        ValueComparator vc = new ValueComparator(threadPosts);
        TreeMap<Integer, List<Post>> sortedThreadPosts = new TreeMap<>(vc);
        sortedThreadPosts.putAll(threadPosts);
        
        List<Post> posts = new ArrayList<>();
        sortedThreadPosts.values().forEach(posts::addAll);
        ret.put("posts", posts);
        return ret;
    }

    private class ValueComparator implements Comparator<Integer> {

        private Map<Integer, List<Post>> threadPosts;

        public ValueComparator(Map<Integer, List<Post>> threadPosts) {
            this.threadPosts = threadPosts;
        }

        @Override
        public int compare(Integer o1, Integer o2) {
            List<Post> posts1 = threadPosts.get(o1);
            List<Post> posts2 = threadPosts.get(o2);
            float score1 = 0;
            for (Post post : posts1) {
                score1 += post.getScore();
            }
            float score2 = 0;
            for (Post post : posts2) {
                score2 += post.getScore();
            }
            if (score1 < score2) {  // in descending order
                return 1;
            }
            return -1;
        }
    }

    public String getContent(int postId) throws IOException {
        Query query = NumericRangeQuery.newIntRange(FieldName.ID, postId, postId, true, true);
        TopDocs topDocs = searcher.search(query, 1);
        if (topDocs.totalHits == 0) {
            return null;
        }

        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        ScoreDoc scoreDoc = scoreDocs[0];
        Document doc = searcher.doc(scoreDoc.doc);
        String content = doc.get(FieldName.CONTENT);
        return content;
    }

    public String getQuoteContent(int postId, int quoteId) throws IOException {
        Query query = NumericRangeQuery.newIntRange(FieldName.ID, postId, postId, true, true);
        TopDocs topDocs = searcher.search(query, 1);
        if (topDocs.totalHits == 0) {
            return null;
        }

        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        ScoreDoc scoreDoc = scoreDocs[0];
        Document doc = searcher.doc(scoreDoc.doc);
        IndexableField[] quoteIds = doc.getFields(FieldName.QUOTE_ID);
        if (quoteIds.length == 0) {
            return null;
        }

        IndexableField[] quoteContents = doc.getFields(FieldName.QUOTE_CONTENT);
        for (int i = 0; i < quoteIds.length; i++) {
            int id = (Integer) quoteIds[i].numericValue();
            if (id == quoteId) {
                return quoteContents[i].stringValue();
            }
        }
        return null;
    }

    public List<Post> getPostsInFrontOfStorey(int threadId, int storey, int num) throws IOException {
        BooleanQuery query = new BooleanQuery();
        DrillDownQuery subQuery = new DrillDownQuery(new FacetsConfig());
        subQuery.add(FieldName.THREAD_ID, Integer.toString(threadId));
        query.add(subQuery, Occur.MUST);
        query.add(NumericRangeQuery.newIntRange(FieldName.STOREY, null, storey, false, false), Occur.MUST);
        Sort sort = new Sort(new SortField(FieldName.STOREY, SortField.Type.INT, true));
        TopDocs topDocs = searcher.search(query, num, sort);
        List<Post> posts = new ArrayList<>();
        if (topDocs.totalHits == 0) {
            return posts;
        }

        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        for (int i = 0; i < scoreDocs.length; i++) {
            ScoreDoc scoreDoc = scoreDocs[i];
            Document doc = searcher.doc(scoreDoc.doc);
            Post post = PostFactory.create(query, searcher, scoreDoc, doc);
            posts.add(post);
        }
        return posts;
    }

    public List<Post> getPostsBackOfStorey(int threadId, int storey, int num) throws IOException {
        BooleanQuery query = new BooleanQuery();
        DrillDownQuery subQuery = new DrillDownQuery(new FacetsConfig());
        subQuery.add(FieldName.THREAD_ID, Integer.toString(threadId));
        query.add(subQuery, Occur.MUST);
        query.add(NumericRangeQuery.newIntRange(FieldName.STOREY, storey, null, false, false), Occur.MUST);
        Sort sort = new Sort(new SortField(FieldName.STOREY, SortField.Type.INT));
        TopDocs topDocs = searcher.search(query, num, sort);
        List<Post> posts = new ArrayList<>();
        if (topDocs.totalHits == 0) {
            return posts;
        }
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        for (int i = 0; i < scoreDocs.length; i++) {
            ScoreDoc scoreDoc = scoreDocs[i];
            Document doc = searcher.doc(scoreDoc.doc);
            Post post = PostFactory.create(query, searcher, scoreDoc, doc);
            posts.add(post);
        }
        return posts;
    }

    private Sort createSort(int sortType) {
        Sort sort;
        SortField topicProp = new SortField(FieldName.TOPIC_PROP, SortField.Type.DOUBLE);
        switch (sortType) {
            case 1:
                SortField postDateDesc = new SortField(FieldName.POST_DATE, SortField.Type.LONG, true);
                sort = new Sort(postDateDesc, SortField.FIELD_SCORE, topicProp);
                break;
            case 3:
                SortField postDateAsc = new SortField(FieldName.POST_DATE, SortField.Type.LONG);
                sort = new Sort(postDateAsc, SortField.FIELD_SCORE, topicProp);
                break;
            default:
                sort = new Sort(SortField.FIELD_SCORE, topicProp);
        }
        return sort;
    }
}
