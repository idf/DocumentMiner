package km.lucene.search;

import km.lucene.analysis.CustomAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.facet.DrillDownQuery;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.util.Version;

public class QueryFactory {

    private static final FacetsConfig config = new FacetsConfig();

    public static Query createTextQuery(String field, String text) throws ParseException {
        Analyzer analyzer = new CustomAnalyzer(Version.LUCENE_48);
        QueryParser parser = new QueryParser(Version.LUCENE_48, field, analyzer);
        Query query = parser.parse(text);
        return query;
    }

    public static Query createMultiIntQuery(String field, int[] ids) {
        BooleanQuery query = new BooleanQuery();
        for (int id : ids) {
            query.add(NumericRangeQuery.newIntRange(field, id, id, true, true), Occur.SHOULD);
        }
        return query;
    }

    public static Query createMultiTermQuery(String field, String[] terms) {
        BooleanQuery query = new BooleanQuery();
        for (String term : terms) {
            query.add(new TermQuery(new Term(field, term)), Occur.SHOULD);
        }
        return query;
    }

    public static Query createDrillDownQuery(String field, String[] paths) {
        DrillDownQuery query = new DrillDownQuery(config);
        for (int i = 0; i < paths.length; i++) {
            query.add(field, paths[i]);
        }
        return query;
    }
}
