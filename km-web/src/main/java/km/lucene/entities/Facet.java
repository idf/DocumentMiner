package km.lucene.entities;

import java.util.ArrayList;
import java.util.List;

public class Facet {
    private String dim;
    private String[] path;
    private int childCount;
    private List<FacetItem> items;

    public Facet(String dim, String[] path, int childCount) {
        this.dim = dim;
        this.path = path;
        this.childCount = childCount;
        this.items = new ArrayList<>();
    }
    
    public void add(FacetItem item) {
        this.items.add(item);
    }
    
    public void addAll(List<FacetItem> items) {
        this.items.addAll(items);
    }

    public String getDim() {
        return dim;
    }

    public String[] getPath() {
        return path;
    }

    public int getChildCount() {
        return childCount;
    }

    public List<FacetItem> getItems() {
        return items;
    }
}
