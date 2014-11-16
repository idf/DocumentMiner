package km.crawler.parsers;

import java.nio.charset.StandardCharsets;
import java.util.List;

import km.crawler.entities.Page;
import km.crawler.enums.PageType;
import km.crawler.entities.Thread;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ThreadParser {

    public static void parse(Page page, String html, List<Page> pages, List<Thread> threads) {
        String pageUrl = page.getUrl();
        int start = pageUrl.lastIndexOf("-") + 1;
        int end = pageUrl.lastIndexOf("/");
        String forumIdStr = pageUrl.substring(start, end);
        int forumId = Integer.parseInt(forumIdStr);

        Document doc = Jsoup.parse(html, StandardCharsets.UTF_8.name());

        if (!page.isSubPage()) {
            int pageCount = PaginationParser.parse(html);
            if (pageCount > 1) {
                for (int i = 2; i <= pageCount; i++) {
                    String url = pageUrl + "index" + i + ".html";
                    pages.add(new Page(PageType.FORUMPAGE, url, true));
                }
            }
        }

        Elements tds = doc.select("td.alt1[id^=td_threadtitle]");
        for (Element td : tds) {
            String tdId = td.attr("id");
            int id = Integer.parseInt(tdId.substring(tdId.lastIndexOf("_") + 1));
            Element link = td.select("a[id^=thread_title_]").first();
            String title = link.text();
            String url = link.attr("href").substring(1);

            Page childPage = new Page(PageType.THREADPAGE, url);
            pages.add(childPage);

            Thread thread = new Thread(id, forumId, title);
            threads.add(thread);
        }
    }
}
