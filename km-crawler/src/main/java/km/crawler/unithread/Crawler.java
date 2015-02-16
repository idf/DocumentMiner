package km.crawler.unithread;

import km.common.Config;
import km.common.json.JsonWriter;
import km.common.util.HttpUtil;
import km.crawler.entities.Forum;
import km.crawler.entities.Page;
import km.crawler.entities.Post;
import km.crawler.entities.Thread;
import km.crawler.enums.PageType;
import km.crawler.parsers.ForumParser;
import km.crawler.parsers.PostParser;
import km.crawler.parsers.ThreadParser;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Crawler {
	private static final String URL_ROOT = "http://forums.hardwarezone.com.sg/";
	private static final Page HOMEPAGE = new Page(PageType.THREADPAGE, "degree-programs-courses-70/need-help-taking-levels-private-candidate-4729850.html", true);
	private static final Logger LOGGER = Logger.getLogger(Crawler.class.getName());

	public static void main(String[] args) throws IOException {

		// testing parameters
		args = new String[2];
		args[0] = Config.settings.getDataFolder();
		args[1] = Config.settings.getRawHtmlFolder();

		if (args.length < 2) {
			System.out.println("Please specify data path to store the output files, and html path to store html pages.");
			System.exit(1);
		}

		String datePath = args[0];
		String forumPath = datePath + "/forums.txt";
		String threadPath = datePath + "/threads.txt";
		String postPath = datePath + "/posts.txt";
		String urlPath = datePath + "/urls.txt";
		String htmlPath = args[1];

		Stack<Page> pendingPages = new Stack<>();
		pendingPages.push(HOMEPAGE);
		Set<Page> finishedPages = new HashSet<>();

		Set<Integer> forumIds = new HashSet<>();
		Set<Integer> threadIds = new HashSet<>();
		Set<Integer> postIds = new HashSet<>();

		while (!pendingPages.isEmpty()) {
			Page page = pendingPages.pop();

			// change to loadHTML for reparsing
			String html = HttpUtil.getHTML(URL_ROOT + page.getUrl(), htmlPath);
			if (html.isEmpty()) {
				pendingPages.add(page);
				continue;
			}

			List<Page> pages = new ArrayList<>();
			List<Object> objects = new ArrayList<>();
			if (page.getType() == PageType.HOMEPAGE) {
				List<Forum> forums = new ArrayList<>();
				ForumParser.parse(html, pages, forums);
				for (Forum forum : forums) {
					if (!(forumIds.contains(forum.getId()))) {
						forumIds.add(forum.getId());
						objects.add(forum);
					}
				}
				JsonWriter.saveList(objects, forumPath, true);

			} else if (page.getType() == PageType.FORUMPAGE) {
				List<Thread> threads = new ArrayList<>();
				ThreadParser.parse(page, html, pages, threads);
				for (Thread thread : threads) {
					if (!(threadIds.contains(thread.getId()))) {
						threadIds.add(thread.getId());
						objects.add(thread);
					}
				}
				JsonWriter.saveList(objects, threadPath, true);

			} else if (page.getType() == PageType.THREADPAGE) {
				List<Post> posts = new ArrayList<>();
				PostParser.parse(page, html, pages, posts);
				for (Post post : posts) {
					if (!(postIds.contains(post.getId()))) {
						postIds.add(post.getId());
						objects.add(post);
					}
				}
				JsonWriter.saveList(objects, postPath, true);
			}

			for (Page p : pages) {
				if (!(pendingPages.contains(p) || finishedPages.contains(p))) {
					pendingPages.add(p);
				}
			}

			finishedPages.add(page);
			saveUrl(page, urlPath);
			System.out.println(String.format(" finish (%d/%d) %s", pendingPages.size(), finishedPages.size(), page));
		}
	}

	private static void saveUrl(Page page, String filename) {
		File file = new File(filename);
		try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.ISO_8859_1))) {
			bw.write(page.getUrl() + "\n");
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Failed to save url", e);
		}
	}
}
