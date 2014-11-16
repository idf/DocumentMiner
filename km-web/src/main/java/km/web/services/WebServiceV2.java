package km.web.services;

import km.common.Setting;
import km.lucene.constants.FieldName;
import km.lucene.entities.Facet;
import km.lucene.entities.FacetWithKeyword;
import km.lucene.search.FacetService;
import km.lucene.search.PostService;
import org.apache.lucene.queryparser.classic.ParseException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Path("/v2")
public class WebServiceV2 {

    @GET
    @Path("/posts")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> getPosts(
            @QueryParam("query") String queryStr,
            @QueryParam("filter") String filterStr,
            @QueryParam("page") int page,
            @QueryParam("sort") int sortType) throws IOException, ParseException {
        long start = new Date().getTime();
        String indexPath = Setting.INDEX_PATH; // "E:/project/kd/data/index";
        String taxoPath = Setting.TAXOINDEX_PATH; // "E:/project/kd/data/taxoindex";
        PostService ps = new PostService(indexPath, taxoPath);
        Map<String, Object> ret = ps.getPosts(queryStr, filterStr, page, sortType);
        ret.put("elapsed", new Date().getTime() - start);
        return ret;
    }
    
    @GET
    @Path("/facet")
    @Produces(MediaType.APPLICATION_JSON)
    public Facet getFacet(
            @QueryParam("query") String queryStr,
            @QueryParam("filter") String filterStr,
            @QueryParam("dim") String dim) throws IOException, ParseException {
        String indexPath = Setting.INDEX_PATH; // "E:/project/kd/data/index";
        String taxoPath = Setting.TAXOINDEX_PATH; // "E:/project/kd/data/taxoindex";
        FacetService fs = new FacetService(indexPath, taxoPath);
        int maxItems = 50;
        Facet facet = fs.getByDim(queryStr, filterStr, dim, maxItems);
        return facet;
    }
    
    @GET
    @Path("/facetTopItems")
    @Produces(MediaType.APPLICATION_JSON)
    public Facet getFacetTopItems(
            @QueryParam("query") String queryStr,
            @QueryParam("filter") String filterStr,
            @QueryParam("dim") String dim) throws IOException, ParseException {
        String indexPath = Setting.INDEX_PATH; // "E:/project/kd/data/index";
        String taxoPath = Setting.TAXOINDEX_PATH; // "E:/project/kd/data/taxoindex";
        FacetService fs = new FacetService(indexPath, taxoPath);
        int maxItems = 5;
        if (dim.equals(FieldName.POST_MONTH) || dim.equals(FieldName.POST_YEAR)) {
            maxItems = 0;
        }
        Facet facet = fs.getByDim(queryStr, filterStr, dim, maxItems);
        return facet;
    }
    
    @GET
    @Path("/facets")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, List<FacetWithKeyword>> getFacets(
            @QueryParam("query") String queryStr,
            @QueryParam("filter") String filterStr) throws IOException, ParseException {
        String indexPath = Setting.INDEX_PATH; // "E:/project/kd/data/index";
        String taxoPath = Setting.TAXOINDEX_PATH; // "E:/project/kd/data/taxoindex";
        FacetService fs = new FacetService(indexPath, taxoPath);
        Map<String, List<FacetWithKeyword>> facets = fs.getAll(queryStr, filterStr);
        return facets;
    }
}
