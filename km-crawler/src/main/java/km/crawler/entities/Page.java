package km.crawler.entities;

import km.crawler.enums.PageType;

public class Page {

    private PageType type;
    private String url;
    private boolean subPage;

    public Page(PageType type, String url, boolean subPage) {
        this.type = type;
        this.url = url;
        this.subPage = subPage;
    }

    public Page(PageType type, String url) {
        this(type, url, false);
    }

    public PageType getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

    public boolean isSubPage() {
        return subPage;
    }

    @Override
    public String toString() {
        return String.format("%s - %s", type, url);
    }

    @Override
    public int hashCode() {
        return url.hashCode();
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof Page)) {
            return false;
        }

        Page p = (Page) obj;
        if (url.equals(p.url)) {
            return true;
        }

        return false;
    }
}
