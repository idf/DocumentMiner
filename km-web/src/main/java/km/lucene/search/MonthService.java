package km.lucene.search;

import km.lucene.services.TitleService;

public class MonthService implements TitleService {

    private static final String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    @Override
    public String getTitle(int id) {
        if(id<0 || id>12) {
            return "";
        }
        return monthNames[id-1];
    }
}
