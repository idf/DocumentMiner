package km.crawler.parsers;

import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import km.common.util.DateUtil;
import km.crawler.entities.Page;
import km.crawler.entities.Post;
import km.crawler.entities.Quote;
import km.crawler.enums.PageType;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class PostParser {

    private static final Logger LOGGER = Logger.getLogger(PostParser.class.getName());
    private static String today;
    private static String yesterday;

    public static void parse(Page page, String html, List<Page> pages, List<Post> posts) {
        Calendar c = Calendar.getInstance();
        c.set(2014, 6, 15);  //2014-07-15, the day the data is download
        today = DateUtil.format(c.getTime(), "dd-MM-yyyy");
        c.add(Calendar.DATE, -1);
        yesterday = DateUtil.format(c.getTime(), "dd-MM-yyyy");

        String pageUrl = page.getUrl();
        String forumUrl = pageUrl.substring(0, pageUrl.indexOf("/"));
        String forumIdStr = forumUrl.substring(forumUrl.lastIndexOf("-") + 1);
        int forumId = Integer.parseInt(forumIdStr);

        Document doc = Jsoup.parse(html, StandardCharsets.UTF_8.name());

        if (!page.isSubPage()) {
            int pageCount = PaginationParser.parse(html);
            if (pageCount > 1) {
                String prefix = pageUrl.substring(0, pageUrl.indexOf(".html"));
                for (int i = 2; i <= pageCount; i++) {
                    String url = prefix + "-" + i + ".html";
                    pages.add(new Page(PageType.THREADPAGE, url, true));
                }
            }
        }

        String href = doc.select("a[href^=/sendmessage]").first().attr("href");
        int threadId = Integer.parseInt(href.substring(href.indexOf("t=") + 2));

        Elements tables = doc.select("table[id^=post]");
        for (Element table : tables) {
            int id = Integer.parseInt(table.attr("id").substring(4));
            String postDate = table.select("td.thead").first().text();
            if (postDate.indexOf("Today") != -1) {
                postDate = postDate.replace("Today", today);
            } else if (postDate.indexOf("Yesterday") != -1) {
                postDate = postDate.replace("Yesterday", yesterday);
            }
            Element divMsg = table.select("div[id^=post_message_]").first();
            String content = getTextFromHTML(divMsg.html());
            int storey = Integer.parseInt(table.select("a[id^=postcount]").text());
            String poster = table.select("a.bigusername[href^=/users/]").text();
            String posterLevel = table.select("div[id^=postmenu_] + div").text();

            Element eJoinDate = table.select("div:containsOwn(Join Date:)").first();
            String joinDate;
            if (eJoinDate == null) {
                joinDate = "";
            } else {
                joinDate = eJoinDate.text().substring(11);
            }

            Element eTotalPost = table.select("div:containsOwn(Posts:)").first();
            String totalPostStr = eTotalPost.text().substring(7).replaceAll(",", "");
            int totalPost = 0;
            try {
                totalPost = Integer.parseInt(totalPostStr);
            } catch (NumberFormatException e) {
                LOGGER.log(Level.INFO, "Invalid number " + totalPostStr);
            }
            Post post = new Post(forumId, threadId, id, postDate, content, storey, poster, posterLevel, joinDate, totalPost);

            Elements divQuotes = divMsg.select("div.quote");
            int quoteId = 0;
            for (Element divQuote : divQuotes) {
                Element blockquote = divQuote.select("blockquote").first();
                String quoteContent = getTextFromHTML(blockquote.html());
                String cite = blockquote.attr("cite");
                int refId = 0;
                if (cite != null && !cite.isEmpty()) {
                    refId = Integer.parseInt(cite.substring(cite.indexOf("#post") + 5));
                }
                String quotePoster = "";
                Element elQuotePoster = divQuote.select("span.byline > strong").first();
                if (elQuotePoster != null) {
                    quotePoster = elQuotePoster.text();
                }

                Quote quote = new Quote(quoteId, refId, quotePoster, quoteContent);
                post.addQuuote(quote);
                quoteId++;
            }

            posts.add(post);
        }
    }

    private static String getTextFromHTML(String html) {
        String text = html;
        text = text.replaceAll("\n", "");
        text = text.replaceAll("<div class=\"quote\">.*</div>", "");
        text = text.replaceAll("<br />", ".");
        text = text.replaceAll("<[/]?[^>]*>", "");
        text += ".";
        return text;
    }
}
