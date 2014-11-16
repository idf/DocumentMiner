package km.lucene.search;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import km.lucene.constants.FieldName;
import km.lucene.entities.Facet;
import km.lucene.entities.FacetItem;
import km.lucene.entities.FacetWithKeyword;
import km.lucene.services.ForumService;
import km.lucene.services.ThreadService;
import km.lucene.services.TitleService;
import km.lucene.services.TopicService;
import org.apache.lucene.facet.FacetResult;
import org.apache.lucene.facet.Facets;
import org.apache.lucene.facet.FacetsCollector;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.taxonomy.FastTaxonomyFacetCounts;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyReader;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.FSDirectory;

public class FacetService {

    private static final Logger LOG = Logger.getLogger(FacetService.class.getName());
    private static final FacetsConfig config = new FacetsConfig();
    private static final TitleService forumSvc = new ForumService();
    private static final TitleService threadSvc = new ThreadService();
    private static final TitleService topicSvc = new TopicService();
    private static final TitleService monthSvc = new MonthService();
    private IndexSearcher searcher;
    private TaxonomyReader taxoReader;

    public FacetService(String indexPath, String taxoPath) {
        try {
            IndexReader indexReader = DirectoryReader.open(FSDirectory.open(new File(indexPath)));
            searcher = new IndexSearcher(indexReader);
            taxoReader = new DirectoryTaxonomyReader(FSDirectory.open(new File(taxoPath)));
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Fail to create searcher.", e);
        }
    }

    public List<Facet> getAll(QueryBuilder builder) throws IOException, ParseException {
        Query query = builder.build();
        Filter filter = null;
        FacetsCollector fc = new FacetsCollector();
        searcher.search(query, filter, fc);
        Facets facets = new FastTaxonomyFacetCounts(taxoReader, config, fc);
        List<Facet> facetList = new ArrayList<>();
        facetList.add(FacetFactory.create(facets.getTopChildren(Integer.MAX_VALUE, FieldName.POST_MONTH), true));
        facetList.add(FacetFactory.create(facets.getTopChildren(Integer.MAX_VALUE, FieldName.POST_YEAR), true));
        facetList.add(FacetFactory.create(facets.getTopChildren(5, FieldName.TOPIC_ID), topicSvc));
        facetList.add(FacetFactory.create(facets.getTopChildren(5, FieldName.FORUM_ID), forumSvc));
        facetList.add(FacetFactory.create(facets.getTopChildren(5, FieldName.THREAD_ID), threadSvc));
        facetList.add(FacetFactory.create(facets.getTopChildren(5, FieldName.POSTER)));
        return facetList;
    }

    public Map<String, List<FacetWithKeyword>> getAll(String queryStr, String filterStr) throws IOException, ParseException {
        Map<String, List<FacetWithKeyword>> fwkMap = new LinkedHashMap<>();
        Map<String, BooleanQuery> queries = QueryParser.parse(queryStr, filterStr);
        if (queries.isEmpty()) {
            List<Facet> facets = getFacets(new MatchAllDocsQuery());
            for (Facet facet : facets) {
                FacetWithKeyword fwk = new FacetWithKeyword("all", facet);
                List<FacetWithKeyword> fwks = new ArrayList<>();
                fwks.add(fwk);
                fwkMap.put(facet.getDim(), fwks);
            }
        } else {
            for (Map.Entry<String, BooleanQuery> entry : queries.entrySet()) {
                String keyword = entry.getKey();
                BooleanQuery query = entry.getValue();
                List<Facet> facets = getFacets(query);
                for (Facet facet : facets) {
                    FacetWithKeyword fwk = new FacetWithKeyword(keyword, facet);
                    if (fwkMap.containsKey(facet.getDim())) {
                        List<FacetWithKeyword> fwks = fwkMap.get(facet.getDim());
                        fwks.add(fwk);
                        fwkMap.put(facet.getDim(), fwks);
                    } else {
                        List<FacetWithKeyword> fwks = new ArrayList<>();
                        fwks.add(fwk);
                        fwkMap.put(facet.getDim(), fwks);
                    }
                }
            }
            normalize(fwkMap);
        }
        return fwkMap;
    }

    private List<Facet> getFacets(Query query) throws IOException {
        List<Facet> facetList = new ArrayList<>();
        Filter filter = null;
        FacetsCollector fc = new FacetsCollector();
        searcher.search(query, filter, fc);
        Facets facets = new FastTaxonomyFacetCounts(taxoReader, config, fc);
        // post month
        FacetResult fr = facets.getTopChildren(12, FieldName.POST_MONTH);
        if (fr == null) {
            facetList.add(new Facet(FieldName.POST_MONTH, null, 0));
        } else {
            facetList.add(FacetFactory.create(fr, monthSvc, true));
        }
        // post year
        fr = facets.getTopChildren(20, FieldName.POST_YEAR);
        if (fr == null) {
            facetList.add(new Facet(FieldName.POST_YEAR, null, 0));
        } else {
            facetList.add(FacetFactory.create(fr, true));
        }
        // topic id
        fr = facets.getTopChildren(10, FieldName.TOPIC_ID);
        if (fr == null) {
            facetList.add(new Facet(FieldName.TOPIC_ID, null, 0));
        } else {
            facetList.add(FacetFactory.create(fr, topicSvc, true));
        }
        // forum id
        fr = facets.getTopChildren(10, FieldName.FORUM_ID);
        if (fr == null) {
            facetList.add(new Facet(FieldName.FORUM_ID, null, 0));
        } else {
            facetList.add(FacetFactory.create(fr, forumSvc, true));
        }
        // thread id
        fr = facets.getTopChildren(10, FieldName.THREAD_ID);
        if (fr == null) {
            facetList.add(new Facet(FieldName.THREAD_ID, null, 0));
        } else {
            facetList.add(FacetFactory.create(fr, threadSvc, true));
        }
        // poster
        fr = facets.getTopChildren(10, FieldName.POSTER);
        if (fr == null) {
            facetList.add(new Facet(FieldName.THREAD_ID, null, 0));
        } else {
            facetList.add(FacetFactory.create(fr, true));
        }
        return facetList;
    }

    private void normalize(Map<String, List<FacetWithKeyword>> fwkMap) {
        for (List<FacetWithKeyword> fwks : fwkMap.values()) {
            // get all possible keys, keep in order
            Map<String, FacetItem> keys = new HashMap<>();
            for (FacetWithKeyword fwk : fwks) {
                for (FacetItem item : fwk.getItems()) {
                    keys.put(item.getKey(), new FacetItem(item.getKey(), item.getName(), 0));
                }
            }
            CustomComparator cc = new CustomComparator(null);
            SortedMap<String, FacetItem> sortedKeys = new TreeMap<>(cc);
            sortedKeys.putAll(keys);
            for (FacetWithKeyword fwk : fwks) {
                List<FacetItem> items = fwk.getItems();
                int pos = 0;
                for (Map.Entry<String, FacetItem> entry : sortedKeys.entrySet()) {
                    String key = entry.getKey();
                    FacetItem item = entry.getValue();
                    if (pos >= items.size()) {
                        items.add(item);
                        pos++;
                        continue;
                    }
                    if (key.equals(items.get(pos).getKey())) {
                        pos++;
                        continue;
                    }
                    if (key.compareTo(items.get(pos).getKey()) < 0) {
                        items.add(pos, item);
                        pos++;
                        continue;
                    }
                }
            }
        }
    }

    private class CustomComparator implements Comparator<String> {
        Map<String, FacetItem> keys;
        public CustomComparator(Map<String, FacetItem> keys) {
            this.keys = keys;
        }
        @Override
        public int compare(String key1, String key2) {
            try {
                int num1 = Integer.parseInt(key1);
                int num2 = Integer.parseInt(key2);
                if (num1 == num2) {
                    return 0;
                }
                if (num1 < num2) {
                    return -1;
                }
                return 1;
            } catch (NumberFormatException e) {
                return key1.compareToIgnoreCase(key2);
            }
        }
    }

    public Facet getByDim(QueryBuilder builder, String dim) throws IOException, ParseException {
        Query query = builder.build();
        Filter filter = null;
        FacetsCollector fc = new FacetsCollector();
        searcher.search(query, filter, fc);
        Facets facets = new FastTaxonomyFacetCounts(taxoReader, config, fc);
        FacetResult fr = facets.getTopChildren(Integer.MAX_VALUE, dim);
        if (fr == null) {
            return null;
        }

        Facet f;
        switch (fr.dim) {
            case FieldName.FORUM_ID:
                f = FacetFactory.create(fr, forumSvc);
                break;
            case FieldName.THREAD_ID:
                f = FacetFactory.create(fr, threadSvc);
                break;
            case FieldName.TOPIC_ID:
                f = FacetFactory.create(fr, topicSvc);
                break;
            default:
                f = FacetFactory.create(fr);
                break;
        }

        return f;
    }

    public Facet getByDim(String queryStr, String filterStr, String dim, int maxItems) throws IOException, ParseException {
        Map<String, BooleanQuery> queries = QueryParser.parse(queryStr, filterStr);
        Query query;
        if (queries.isEmpty()) {
            query = new MatchAllDocsQuery();
        } else {
            BooleanQuery sumQuery = new BooleanQuery();
            for (Query subQuery : queries.values()) {
                sumQuery.add(subQuery, Occur.SHOULD);
            }
            query = sumQuery;
        }
        Filter filter = null;
        FacetsCollector fc = new FacetsCollector();
        searcher.search(query, filter, fc);
        Facets facets = new FastTaxonomyFacetCounts(taxoReader, config, fc);
        if (maxItems == 0) {
            maxItems = Integer.MAX_VALUE;
        }
        FacetResult fr = facets.getTopChildren(maxItems, dim);
        if (fr == null) {
            return null;
        }

        Facet f;
        switch (fr.dim) {
            case FieldName.FORUM_ID:
                f = FacetFactory.create(fr, forumSvc);
                break;
            case FieldName.THREAD_ID:
                f = FacetFactory.create(fr, threadSvc);
                break;
            case FieldName.TOPIC_ID:
                f = FacetFactory.create(fr, topicSvc);
                break;
            case FieldName.POST_MONTH:
                f = FacetFactory.create(fr, monthSvc, true);
                break;
            case FieldName.POST_YEAR:
                f = FacetFactory.create(fr, true);
                break;
            default:
                f = FacetFactory.create(fr);
                break;
        }

        return f;
    }
}
