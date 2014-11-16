package km.crawler.postprocess;

import java.nio.charset.StandardCharsets;

import km.crawler.entities.Post;
import km.crawler.entities.Quote;

public class CSVFactory {
	public static String create(Post post) {
		String content = post.getContent();
		content = content.trim();
		if (!post.getQuotes().isEmpty()) {
			for (Quote q : post.getQuotes()) {
				content += ". " + q.getContent();
			}
		}

		content = content.replaceAll("&amp;", "&");
		content = content.replaceAll("&quot;", "\"");
		content = filterInvalidCharacter(content);
		
		content = String.format("%d\t%d\t%s\n", post.getId(), post.getThreadId(), content);
		return content;
	}

	private static String filterInvalidCharacter(String in) {
		byte[] bytes = in.getBytes(StandardCharsets.UTF_8);
		for (int i = 0; i < bytes.length; i++) {
			if (bytes[i] < 0) {
				bytes[i] = 32;
			}
		}
		return new String(bytes, StandardCharsets.UTF_8);
	}
}
