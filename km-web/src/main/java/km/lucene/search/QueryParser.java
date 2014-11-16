package km.lucene.search;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import km.lucene.constants.FieldName;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;

public class QueryParser {

    private static final Logger LOG = Logger.getLogger(QueryParser.class.getName());

    public static Map<String, BooleanQuery> parse(String queryStr, String filterStr) throws ParseException {
        Map<String, BooleanQuery> queries = new LinkedHashMap<>();
        String[] queryStrs = queryStr.split("vs");
        for (int i = 0; i < queryStrs.length; i++) {
            String keyword = queryStrs[i];
            BooleanQuery query = new BooleanQuery();
            BooleanQuery subQuery = parseQuery(keyword);
            if (subQuery != null) {
                query.add(subQuery, Occur.MUST);
                queries.put(keyword, query);
            }
        }
        List<Query> filters = parseFilters(filterStr);
        if (!filters.isEmpty() && queries.isEmpty()) {
            queries.put("all", new BooleanQuery());
        }
        for (BooleanQuery query : queries.values()) {
            for (Query filter : filters) {
                query.add(filter, Occur.MUST);
            }
        }

        return queries;
    }

    private static BooleanQuery parseQuery(String queryStr) throws ParseException {
        if (queryStr.isEmpty()) {
            return null;
        }

        BooleanQuery query = new BooleanQuery();

        BooleanQuery subQuery = new BooleanQuery();
        subQuery.add(QueryFactory.createTextQuery(FieldName.THREAD_TITLE, queryStr), Occur.MUST);
        subQuery.add(NumericRangeQuery.newIntRange(FieldName.STOREY, 1, 1, true, true), Occur.MUST);
        query.add(subQuery, Occur.SHOULD);

        query.add(QueryFactory.createTextQuery(FieldName.CONTENT, queryStr), Occur.SHOULD);
        query.add(QueryFactory.createTextQuery(FieldName.QUOTE_CONTENT, queryStr), Occur.SHOULD);
        
        return query;
    }

    private static List<Query> parseFilters(String filterStr) {
        List<Query> queries = new ArrayList<>();
        if (filterStr.isEmpty()) {
            return queries;
        }

        String[] filters = filterStr.split(",");
        for (int i = 0; i < filters.length; i++) {
            String filter = filters[i];
            String[] values = filter.split(":");
            if (values.length < 2) {
                continue;
            }
            String field = values[0];
            String valueStr = values[1];
            String[] terms = valueStr.split(";");
            switch (field) {
                case FieldName.POST_MONTH:
                    queries.add(QueryFactory.createDrillDownQuery(FieldName.POST_MONTH, terms));
                    break;
                case FieldName.POST_YEAR:
                    queries.add(QueryFactory.createDrillDownQuery(FieldName.POST_YEAR, terms));
                    break;
                case FieldName.TOPIC_ID:
                    queries.add(QueryFactory.createDrillDownQuery(FieldName.TOPIC_ID, terms));
                    break;
                case FieldName.FORUM_ID:
                    queries.add(QueryFactory.createDrillDownQuery(FieldName.FORUM_ID, terms));
                    break;
                case FieldName.THREAD_ID:
                    queries.add(QueryFactory.createDrillDownQuery(FieldName.THREAD_ID, terms));
                    break;
                case FieldName.POSTER:
                    queries.add(QueryFactory.createDrillDownQuery(FieldName.POSTER, terms));
                    break;
            }
        }
        return queries;
    }
}
