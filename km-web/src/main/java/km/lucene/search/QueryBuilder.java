package km.lucene.search;

import java.util.Date;
import km.common.util.DateUtil;
import km.lucene.constants.FieldName;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;

public class QueryBuilder {

    private BooleanQuery query;

    public QueryBuilder() {
        query = new BooleanQuery();
    }

    public Query build() {
        if (query.clauses().isEmpty()) {
            return new MatchAllDocsQuery();
        }
        return query;
    }

    public void addBasicFields(String queryType, String keyword, String dateFromStr, String dateToStr) throws ParseException {
        if (!keyword.isEmpty()) {
            switch (queryType) {
                case "content":
                    this.addContent(keyword);
                    break;
                case "title":
                    this.addTitle(keyword);
                    break;
                case "poster":
                    query.add(QueryFactory.createMultiTermQuery(FieldName.POSTER, new String[]{keyword}), Occur.MUST);
                    break;
            }
        }

        Date dateFrom = DateUtil.parse(dateFromStr, "yyyy-MM-dd");
        Date dateTo = DateUtil.parse(dateToStr, "yyyy-MM-dd");
        Long start = null;
        Long end = null;
        if (dateFrom != null) {
            start = dateFrom.getTime();
        }
        if (dateTo != null) {
            end = DateUtil.addDay(dateTo, 1).getTime() - 1;
        }
        query.add(NumericRangeQuery.newLongRange(FieldName.POST_DATE, start, end, true, true), Occur.MUST);
    }

    public void addFilterFields(
            String forumIdStr, String threadIdStr,
            String postYearStr, String postMonthStr, String posterStr, String topicIdStr) {

        if (!forumIdStr.isEmpty()) {
            String[] forumIds = forumIdStr.split(";");
            query.add(QueryFactory.createDrillDownQuery(FieldName.FORUM_ID, forumIds), Occur.MUST);
        }
        if (!threadIdStr.isEmpty()) {
            String[] threadIds = threadIdStr.split(";");
            query.add(QueryFactory.createDrillDownQuery(FieldName.THREAD_ID, threadIds), Occur.MUST);
        }
        if (!postYearStr.isEmpty()) {
            String[] postYears = postYearStr.split(";");
            query.add(QueryFactory.createDrillDownQuery(FieldName.POST_YEAR, postYears), Occur.MUST);
        }
        if (!postMonthStr.isEmpty()) {
            String[] postMonths = postMonthStr.split(";");
            query.add(QueryFactory.createDrillDownQuery(FieldName.POST_MONTH, postMonths), Occur.MUST);
        }
        if (!posterStr.isEmpty()) {
            String[] posters = posterStr.split(";");
            query.add(QueryFactory.createDrillDownQuery(FieldName.POSTER, posters), Occur.MUST);
        }
        if (!topicIdStr.isEmpty()) {
            String[] topicIds = topicIdStr.split(";");
            query.add(QueryFactory.createDrillDownQuery(FieldName.TOPIC_ID, topicIds), Occur.MUST);
        }
    }

    private void addContent(String content) throws ParseException {
        BooleanQuery subQuery = new BooleanQuery();
        subQuery.add(QueryFactory.createTextQuery(FieldName.CONTENT, content), Occur.SHOULD);
        subQuery.add(QueryFactory.createTextQuery(FieldName.QUOTE_CONTENT, content), Occur.SHOULD);
        query.add(subQuery, Occur.MUST);
    }

    private void addTitle(String title) throws ParseException {
        query.add(QueryFactory.createTextQuery(FieldName.THREAD_TITLE, title), Occur.MUST);
        query.add(NumericRangeQuery.newIntRange(FieldName.STOREY, 1, 1, true, true), Occur.MUST);
    }
}
