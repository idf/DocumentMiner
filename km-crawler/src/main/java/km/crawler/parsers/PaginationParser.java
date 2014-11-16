package km.crawler.parsers;

import java.nio.charset.StandardCharsets;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class PaginationParser {

    public static int parse(String html) {
        Document doc = Jsoup.parse(html, StandardCharsets.UTF_8.name());
        Element pagination = doc.select("div.pagination").first();
        if (pagination != null) {
            Element pageSpan = pagination.select("span.desc").first();
            int numOfPages = Integer.parseInt(pageSpan.text().split(" ")[3]);
            return numOfPages;
        }
        return 1;
    }
}
