package km.web.services;

import io.deepreader.java.commons.util.Sorter;
import io.deepreader.java.commons.util.Timestamper;
import km.common.Config;
import km.lucene.applets.collocations.TermCollocationExtractor;
import km.lucene.applets.collocations.TermCollocationHelper;
import km.lucene.constants.FieldName;
import km.lucene.entities.Facet;
import km.lucene.entities.FacetWithKeyword;
import km.lucene.entities.ScoreMap;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/v2")
public class WebServiceV2 {
    static PostService ps  = new PostService(Config.settings.getIndexPath(), Config.settings.getTaxoindexPath());
    static TermCollocationExtractor tce = new TermCollocationExtractor("", Config.settings.getPostindexPath(), "", Config.settings.getDriverSettings().getClusteredIndexPath());

    @GET
    @Path("/posts")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> getPosts(
            @QueryParam("query") String queryStr,
            @QueryParam("filter") String filterStr,
            @QueryParam("page") int page,
            @QueryParam("sort") int sortType) throws IOException, ParseException {
        Timestamper timer = new Timestamper();
        timer.start();
        Map<String, Object> ret = ps.getPosts(queryStr, filterStr, page, sortType);
        ret.put("elapsed", timer.end());
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
        int displayTopK = Config.settings.getDisplayTopK();
        TermCollocationHelper helper = new TermCollocationHelper();
        Map<String, Object> ret = new HashMap<>();
        Map<String, ScoreMap> sorts = tce.search(queryStr);  // terms, phrases, phrases_excluded
        Map<String, List<CollocationScorer>> results = sorts.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                            e-> Sorter.topEntries(e.getValue(), displayTopK, helper.getComparator())
                                    .entrySet().stream()
                                    .map(Map.Entry::getValue)
                                    .collect(Collectors.toList())
                        )
                );

        ret.put("results", results);
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
        String indexPath = Config.settings.getIndexPath(); // "E:/project/kd/data/index";
        String taxoPath = Config.settings.getTaxoindexPath(); // "E:/project/kd/data/taxoindex";
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
        String indexPath = Config.settings.getIndexPath(); // "E:/project/kd/data/index";
        String taxoPath = Config.settings.getTaxoindexPath(); // "E:/project/kd/data/taxoindex";
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
        String indexPath = Config.settings.getIndexPath(); // "E:/project/kd/data/index";
        String taxoPath = Config.settings.getTaxoindexPath(); // "E:/project/kd/data/taxoindex";
        FacetService fs = new FacetService(indexPath, taxoPath);
        Map<String, List<FacetWithKeyword>> facets = fs.getAll(queryStr, filterStr);
        return facets;
    }
}
