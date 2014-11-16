package km.crawler.multithread;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import km.common.util.HttpUtil;
import km.crawler.entities.Forum;
import km.crawler.entities.Post;
import km.crawler.entities.Thread;
import km.crawler.entities.Page;
import km.crawler.enums.PageType;
import km.crawler.parsers.ForumParser;
import km.crawler.parsers.PostParser;
import km.crawler.parsers.ThreadParser;

public class CrawlerTask implements WorkerTask {

	private static final Logger LOG = Logger.getLogger(CrawlerTask.class.getName());
	private int iniTimeout = 1000;
	private int maxTimeout = 4000;
	private String urlRoot;
	private Coordinator coordinator;
	private String threadName;
	private String htmlPath;

	public CrawlerTask(Coordinator coordinator, String urlRoot, String htmlPath) {
		this.coordinator = coordinator;
		this.urlRoot = urlRoot;
		this.htmlPath = htmlPath;
	}

	@Override
	public void process() {
		this.threadName = java.lang.Thread.currentThread().getName();
		int timeout = iniTimeout;
		while (true) {
			if (coordinator.isEmpty()) {
				if (timeout > maxTimeout) {
					System.out.println(String.format("%s stops due to idle", threadName));
					break;
				}

				System.out.println(String.format("%s idle, wait for %d millis", threadName, timeout));
				sleep(timeout);
				timeout *= 2;
				continue;
			}

			crawl();

			if (timeout != iniTimeout) {
				timeout = iniTimeout;
			}
		}
	}

	private void crawl() {
		Page page = coordinator.pop();
		if (page == null) {
			System.out.println(String.format("%s failed to get a page", threadName));
			return;
		}

		/*
		 * getHTML for downloading
		 * loadHTML for reparsing
		 */
		String html = HttpUtil.loadHTML(urlRoot + page.getUrl(), htmlPath);
		if (html.isEmpty()) {
			LOG.log(Level.SEVERE, "Fail to retrieve html from " + page.getUrl());
			sleep(iniTimeout);
			coordinator.addPage(page);
			return;
		}

		List<Page> pages = new ArrayList<>();
		if (page.getType() == PageType.HOMEPAGE) {
			List<Forum> forums = new ArrayList<>();
			ForumParser.parse(html, pages, forums);
			try {
				coordinator.addForums(forums);
			} catch (IOException e) {
				LOG.log(Level.SEVERE, "Fail to add forums.", e);
				sleep(iniTimeout);
				coordinator.addPage(page);
				return;
			}

		} else if (page.getType() == PageType.FORUMPAGE) {
			List<Thread> threads = new ArrayList<>();
			ThreadParser.parse(page, html, pages, threads);
			try {
				coordinator.addThreads(threads);
			} catch (IOException e) {
				LOG.log(Level.SEVERE, "Fail to add threads.", e);
				sleep(iniTimeout);
				coordinator.addPage(page);
				return;
			}

		} else if (page.getType() == PageType.THREADPAGE) {
			List<Post> posts = new ArrayList<>();
			PostParser.parse(page, html, pages, posts);
			try {
				coordinator.addPosts(posts);
			} catch (IOException e) {
				LOG.log(Level.SEVERE, "Fail to add posts.", e);
				sleep(iniTimeout);
				coordinator.addPage(page);
				return;
			}
		}

		coordinator.addPages(pages);
		coordinator.finish(page);
	}

	private void sleep(int millis) {
		try {
			java.lang.Thread.sleep(millis);
		} catch (InterruptedException e) {
			LOG.log(Level.SEVERE, "Failed to sleep thread", e);
		}
	}
}
