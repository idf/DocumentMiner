package km.web.services;

import io.deepreader.java.commons.util.Sorter;
import io.deepreader.java.commons.util.Timestamper;
import km.common.Settings;
import km.lucene.applets.collocations.TermCollocationExtractor;
import km.lucene.constants.FieldName;
import km.lucene.entities.Facet;
import km.lucene.entities.FacetWithKeyword;
import km.lucene.search.FacetService;
import km.lucene.search.PostService;
import org.apache.lucene.index.collocations.CollocationScorer;
import org.apache.lucene.queryparser.classic.ParseException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Path("/v2")
public class WebServiceV2 {
    TermCollocationExtractor tce;
    public WebServiceV2() throws Exception {
        this.tce = new TermCollocationExtractor("", Settings.POSTINDEX_PATH, "", Settings.DriverSettings.ROOT_FOLDER+"rakeIndex-post-clustered-9152.ser");
    }

    @GET
    @Path("/posts")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> getPosts(
            @QueryParam("query") String queryStr,
            @QueryParam("filter") String filterStr,
            @QueryParam("page") int page,
            @QueryParam("sort") int sortType) throws IOException, ParseException {
        long start = new Date().getTime();
        String indexPath = Settings.INDEX_PATH; // "E:/project/kd/data/index";
        String taxoPath = Settings.TAXOINDEX_PATH; // "E:/project/kd/data/taxoindex";
        PostService ps = new PostService(indexPath, taxoPath);
        Map<String, Object> ret = ps.getPosts(queryStr, filterStr, page, sortType);
        ret.put("elapsed", new Date().getTime() - start);
        return ret;
    }

    @GET
    @Path("/collocations")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> getCollocations(
            @QueryParam("query") String queryStr
    ) throws Exception {
        Timestamper timer = new Timestamper();
        timer.start();
        Map<String, Object> ret = new HashMap<>();
        TreeMap<String, CollocationScorer> sortedPhraseBScores = this.tce.search(queryStr);
        sortedPhraseBScores = Sorter.topEntries(sortedPhraseBScores, 10,
                (e1, e2) -> Float.compare(e1.getValue().getScore(), e2.getValue().getScore()));
        List<CollocationScorer> rankedLst = sortedPhraseBScores.entrySet().stream().map(Map.Entry<String, CollocationScorer>::getValue).collect(Collectors.toList());
        ret.put("results", rankedLst);
        ret.put("elapsed", timer.end());
        return ret;
    }
    
    @GET
    @Path("/facet")
    @Produces(MediaType.APPLICATION_JSON)
    public Facet getFacet(
            @QueryParam("query") String queryStr,
            @QueryParam("filter") String filterStr,
            @QueryParam("dim") String dim) throws IOException, ParseException {
        String indexPath = Settings.INDEX_PATH; // "E:/project/kd/data/index";
        String taxoPath = Settings.TAXOINDEX_PATH; // "E:/project/kd/data/taxoindex";
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
        String indexPath = Settings.INDEX_PATH; // "E:/project/kd/data/index";
        String taxoPath = Settings.TAXOINDEX_PATH; // "E:/project/kd/data/taxoindex";
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
        String indexPath = Settings.INDEX_PATH; // "E:/project/kd/data/index";
        String taxoPath = Settings.TAXOINDEX_PATH; // "E:/project/kd/data/taxoindex";
        FacetService fs = new FacetService(indexPath, taxoPath);
        Map<String, List<FacetWithKeyword>> facets = fs.getAll(queryStr, filterStr);
        return facets;
    }
}
