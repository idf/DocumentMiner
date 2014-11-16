package km.crawler.parsers;

import km.common.Setting;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class PostParserTest {
	public static void main(String[] args) throws IOException {
		File file = new File(Setting.RAW_HTML_FOLDER, "textbook-garage-306/sim-rmit-2nd-hand-textbooks-marketing-bus-management-2398214.html");
		Document doc = Jsoup.parse(file, StandardCharsets.ISO_8859_1.name());
		Element postContainer = doc.select("div#posts").first();
		Elements posts = postContainer.select("table[id^=post38856198]");
		// System.out.println("posts: " + posts.size());

		for (Element post : posts) {
			Element content = post.select("div[id^=post_message").first();
			String text = content.text();
			System.out.println(text);
			System.out.println(text.length());

			String html = content.html();
			html = getTextFromHTML(html);
			System.out.println(html);
			System.out.println(html.length());

			Elements quotes = content.select("div.quote");
			for (Element quote : quotes) {
				Element quoteContent = quote.select("blockquote").first();
				html = quoteContent.html();
				html = getTextFromHTML(html);
				System.out.println(html);
				System.out.println(html.length());
			}

			break;
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
