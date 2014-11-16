package km.lucene.entities;

public class FacetItem {

    private String key;
    private String name;
    private int count;

    public FacetItem(String key, String name, int count) {
        this.key = key;
        this.name = name;
        this.count = count;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public int getCount() {
        return count;
    }
}
