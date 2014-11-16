package km.lucene.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import km.lucene.entities.Facet;
import km.lucene.entities.FacetItem;
import km.lucene.services.TitleService;
import org.apache.lucene.facet.FacetResult;
import org.apache.lucene.facet.LabelAndValue;

public class FacetFactory {

    public static Facet create(FacetResult fr, TitleService ts) {
        Facet f = new Facet(fr.dim, fr.path, fr.childCount);
        List<FacetItem> items = new ArrayList<>();
        for (int i = 0; i < fr.labelValues.length; i++) {
            LabelAndValue lv = fr.labelValues[i];
            String key = lv.label;
            String name = ts.getTitle(Integer.parseInt(key));
            FacetItem fi = new FacetItem(key, name, lv.value.intValue());
            items.add(fi);
        }
        f.addAll(items);
        return f;
    }

    public static Facet create(FacetResult fr, TitleService ts, boolean sortByKey) {
        Facet f = new Facet(fr.dim, fr.path, fr.childCount);
        List<FacetItem> items = new ArrayList<>();
        for (int i = 0; i < fr.labelValues.length; i++) {
            LabelAndValue lv = fr.labelValues[i];
            String key = lv.label;
            String name = ts.getTitle(Integer.parseInt(key));
            FacetItem fi = new FacetItem(key, name, lv.value.intValue());
            items.add(fi);
        }
        if (sortByKey) {
            Collections.sort(items, comparator);
        }
        f.addAll(items);
        return f;
    }

    public static Facet create(FacetResult fr) {
        Facet f = new Facet(fr.dim, fr.path, fr.childCount);
        List<FacetItem> items = new ArrayList<>();
        for (int i = 0; i < fr.labelValues.length; i++) {
            LabelAndValue lv = fr.labelValues[i];
            FacetItem fi = new FacetItem(lv.label, lv.label, lv.value.intValue());
            items.add(fi);
        }
        f.addAll(items);
        return f;
    }

    public static Facet create(FacetResult fr, boolean sortByKey) {
        Facet f = new Facet(fr.dim, fr.path, fr.childCount);
        List<FacetItem> items = new ArrayList<>();
        for (int i = 0; i < fr.labelValues.length; i++) {
            LabelAndValue lv = fr.labelValues[i];
            FacetItem fi = new FacetItem(lv.label, lv.label, lv.value.intValue());
            items.add(fi);
        }
        if (sortByKey) {
            Collections.sort(items, comparator);
        }
        f.addAll(items);
        return f;
    }
    private static Comparator<FacetItem> comparator = new Comparator<FacetItem>() {
        @Override
        public int compare(FacetItem o1, FacetItem o2) {
            try {
                int num1 = Integer.parseInt(o1.getKey());
                int num2 = Integer.parseInt(o2.getKey());
                if (num1 == num2) {
                    return 0;
                }
                if (num1 < num2) {
                    return -1;
                }
                return 1;
            } catch (NumberFormatException e) {
                return o1.getKey().compareToIgnoreCase(o2.getKey());
            }
        }
    };
}
