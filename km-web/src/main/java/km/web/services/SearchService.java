package km.web.services;

import km.common.Settings;
import km.lucene.constants.FieldName;
import km.lucene.entities.Facet;
import km.lucene.entities.Post;
import km.lucene.search.FacetService;
import km.lucene.search.PostService;
import km.lucene.search.QueryBuilder;
import org.apache.lucene.queryparser.classic.ParseException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/")
public class SearchService {

    @GET
    @Path("/posts")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> posts(
            @QueryParam("queryType") String queryType,
            @QueryParam("keyword") String keyword,
            @QueryParam("dateFrom") String dateFrom,
            @QueryParam("dateTo") String dateTo,
            @QueryParam("pageSize") int pageSize,
            @QueryParam("page") int page,
            @QueryParam("sortType") int sortType,
            @QueryParam("forumId") String forumIdStr,
            @QueryParam("threadId") String threadIdStr,
            @QueryParam("postYear") String postYearStr,
            @QueryParam("postMonth") String postMonthStr,
            @QueryParam("poster") String posterStr,
            @QueryParam("topicId") String topicIdStr) throws IOException, ParseException {

        long start = new Date().getTime();

        String indexPath = Settings.INDEX_PATH; // "E:/project/kd/data/index";
        PostService ps = new PostService(indexPath);
        QueryBuilder builder = new QueryBuilder();
        builder.addBasicFields(queryType, keyword, dateFrom, dateTo);
        builder.addFilterFields(forumIdStr, threadIdStr, postYearStr, postMonthStr, posterStr, topicIdStr);
        Map<String, Object> ret = ps.search(builder, pageSize, page, sortType);

        ret.put("elapsed", new Date().getTime() - start);

        return ret;
    }

    @GET
    @Path("/postsGBT")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> postsGBT(
            @QueryParam("queryType") String queryType,
            @QueryParam("keyword") String keyword,
            @QueryParam("dateFrom") String dateFrom,
            @QueryParam("dateTo") String dateTo,
            @QueryParam("pageSize") int pageSize,
            @QueryParam("page") int page,
            @QueryParam("sortType") int sortType,
            @QueryParam("forumId") String forumIdStr,
            @QueryParam("threadId") String threadIdStr,
            @QueryParam("postYear") String postYearStr,
            @QueryParam("postMonth") String postMonthStr,
            @QueryParam("poster") String posterStr,
            @QueryParam("topicId") String topicIdStr) throws IOException, ParseException {

        long start = new Date().getTime();

        String indexPath = Settings.INDEX_PATH; // "E:/project/kd/data/index";
        String taxoPath = Settings.TAXOINDEX_PATH; // "E:/project/kd/data/taxoindex";
        PostService ps = new PostService(indexPath, taxoPath);
        QueryBuilder builder = new QueryBuilder();
        builder.addBasicFields(queryType, keyword, dateFrom, dateTo);
        builder.addFilterFields(forumIdStr, threadIdStr, postYearStr, postMonthStr, posterStr, topicIdStr);
        Map<String, Object> ret = ps.searchGBT(builder, pageSize, page, sortType);

        ret.put("elapsed", new Date().getTime() - start);

        return ret;
    }

    @GET
    @Path("/content")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> content(
            @QueryParam("postId") int postId) throws IOException {

        String indexPath = Settings.INDEX_PATH; // "E:/project/kd/data/index";
        PostService ps = new PostService(indexPath);
        String content = ps.getContent(postId);
        Map<String, String> ret = new HashMap<>();
        ret.put("content", content);
        return ret;
    }

    @GET
    @Path("/quoteContent")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> quoteContent(
            @QueryParam("postId") int postId,
            @QueryParam("quoteId") int quoteId) throws IOException {

        String indexPath = Settings.INDEX_PATH; // "E:/project/kd/data/index";
        PostService ps = new PostService(indexPath);
        String quoteContent = ps.getQuoteContent(postId, quoteId);
        Map<String, String> ret = new HashMap<>();
        ret.put("quoteContent", quoteContent);
        return ret;
    }

    @GET
    @Path("/postsInFrontOfStorey")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Post> postsInFrontOfStorey(
            @QueryParam("threadId") int threadId,
            @QueryParam("storey") int storey) throws IOException {

        String indexPath = Settings.INDEX_PATH; // "E:/project/kd/data/index";
        PostService ps = new PostService(indexPath);
        List<Post> posts = ps.getPostsInFrontOfStorey(threadId, storey, 3);
        return posts;
    }

    @GET
    @Path("/postsBackOfStorey")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Post> postsBackOfStorey(
            @QueryParam("threadId") int threadId,
            @QueryParam("storey") int storey) throws IOException {
        
        String indexPath = Settings.INDEX_PATH; // "E:/project/kd/data/index";
        PostService ps = new PostService(indexPath);
        List<Post> posts = ps.getPostsBackOfStorey(threadId, storey, 3);
        return posts;
    }

    @GET
    @Path("/facets")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Facet> facets(
            @QueryParam("queryType") String queryType,
            @QueryParam("keyword") String keyword,
            @QueryParam("dateFrom") String dateFrom,
            @QueryParam("dateTo") String dateTo,
            @QueryParam("forumId") String forumIdStr,
            @QueryParam("threadId") String threadIdStr,
            @QueryParam("postYear") String postYearStr,
            @QueryParam("postMonth") String postMonthStr,
            @QueryParam("poster") String posterStr,
            @QueryParam("topicId") String topicIdStr) throws IOException, ParseException {

        String indexPath = Settings.INDEX_PATH; // "E:/project/kd/data/index";
        String taxoPath = Settings.TAXOINDEX_PATH; // "E:/project/kd/data/taxoindex";
        FacetService fs = new FacetService(indexPath, taxoPath);
        QueryBuilder builder = new QueryBuilder();
        builder.addBasicFields(queryType, keyword, dateFrom, dateTo);
        builder.addFilterFields(forumIdStr, threadIdStr, postYearStr, postMonthStr, posterStr, topicIdStr);
        List<Facet> facets = fs.getAll(builder);
        return facets;
    }

    @GET
    @Path("/facet")
    @Produces(MediaType.APPLICATION_JSON)
    public Facet facet(
            @QueryParam("queryType") String queryType,
            @QueryParam("keyword") String keyword,
            @QueryParam("dateFrom") String dateFrom,
            @QueryParam("dateTo") String dateTo,
            @QueryParam("forumId") String forumIdStr,
            @QueryParam("threadId") String threadIdStr,
            @QueryParam("postYear") String postYearStr,
            @QueryParam("postMonth") String postMonthStr,
            @QueryParam("poster") String posterStr,
            @QueryParam("topicId") String topicIdStr,
            @QueryParam("dim") String dim) throws IOException, ParseException {

        String indexPath = Settings.INDEX_PATH; // "E:/project/kd/data/index";
        String taxoPath = Settings.TAXOINDEX_PATH; // "E:/project/kd/data/taxoindex";
        FacetService fs = new FacetService(indexPath, taxoPath);
        QueryBuilder builder = new QueryBuilder();
        builder.addBasicFields(queryType, keyword, dateFrom, dateTo);
        switch (dim) {
            case FieldName.FORUM_ID:
                forumIdStr = "";
                break;
            case FieldName.THREAD_ID:
                threadIdStr = "";
                break;
            case FieldName.POSTER:
                posterStr = "";
                break;
            case FieldName.TOPIC_ID:
                topicIdStr = "";
                break;
        }
        builder.addFilterFields(forumIdStr, threadIdStr, postYearStr, postMonthStr, posterStr, topicIdStr);
        Facet f = fs.getByDim(builder, dim);
        return f;
    }
}
