package km.crawler.parsers;

import java.nio.charset.StandardCharsets;
import java.util.List;

import km.crawler.entities.Forum;
import km.crawler.entities.Page;
import km.crawler.enums.PageType;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ForumParser {

    public static void parse(String html, List<Page> pages, List<Forum> forums) {
        Document doc = Jsoup.parse(html, StandardCharsets.UTF_8.name());
        Elements forumElements = doc.select("td.alt1Active");
        for (Element forumElement : forumElements) {
            int id = Integer.parseInt(forumElement.attr("id").substring(1));
            Element link = forumElement.select("a[href]").first();
            String title = link.text();
            String url = link.attr("href").substring(1);
            String desc = forumElement.select("div.smallfont").first().text();

            Page page = new Page(PageType.FORUMPAGE, url);
            pages.add(page);

            Forum forum = new Forum(id, title, desc);
            forums.add(forum);
        }
    }
}
