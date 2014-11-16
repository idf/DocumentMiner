package km.lucene.entities;

public class FacetWithKeyword extends Facet {
    private String keyword;
    
    public FacetWithKeyword(String keyword, Facet facet) {
        super(facet.getDim(), facet.getPath(), facet.getChildCount());
        this.addAll(facet.getItems());
        this.keyword = keyword;
    }

    public String getKeyword() {
        return keyword;
    }
}
