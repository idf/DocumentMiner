package km.crawler.multithread;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import km.common.json.JsonWriter;
import km.crawler.entities.Forum;
import km.crawler.entities.Page;
import km.crawler.entities.Post;
import km.crawler.entities.Thread;

public class Coordinator {

	private static final Logger LOGGER = Logger.getLogger(Coordinator.class.getName());
	private Stack<Page> pendingPages;
	private Set<Page> finishedPages;
	private Set<Integer> forumIds;
	private Set<Integer> threadIds;
	private Set<Integer> postIds;
	private String forumPath;
	private String threadPath;
	private String postPath;
	private String urlPath;

	public Coordinator(String datePath, Page seed) {
		this.forumPath = datePath + "/forums.txt";
		this.threadPath = datePath + "/threads.txt";
		this.postPath = datePath + "/posts.txt";
		this.urlPath = datePath + "/urls.txt";

		pendingPages = new Stack<Page>();
		pendingPages.push(seed);
		finishedPages = new HashSet<Page>();
		forumIds = new HashSet<Integer>();
		threadIds = new HashSet<Integer>();
		postIds = new HashSet<Integer>();
	}

	public synchronized void addPage(Page page) {
		if (!exists(page)) {
			pendingPages.push(page);
		}
	}

	private boolean exists(Page page) {
		if (pendingPages.contains(page) || finishedPages.contains(page)) {
			return true;
		}
		return false;
	}

	public synchronized boolean isEmpty() {
		return pendingPages.isEmpty();
	}

	public synchronized Page pop() {
		try {
			Page page = pendingPages.pop();
			return page;
		} catch (EmptyStackException e) {
			return null;
		}
	}

	public synchronized void addForums(List<Forum> forums) throws IOException {
		List<Object> objects = new ArrayList<Object>();
		for (Forum forum : forums) {
			if (forum.getId() == 306) { // ignore forums: 306
				continue;
			}
			if (!(forumIds.contains(forum.getId()))) {
				forumIds.add(forum.getId());
				objects.add(forum);
			}
		}
		JsonWriter.saveList(objects, forumPath, true);
	}

	public synchronized void addThreads(List<Thread> threads) throws IOException {
		List<Object> objects = new ArrayList<Object>();
		for (Thread thread : threads) {
			if (!(threadIds.contains(thread.getId()))) {
				threadIds.add(thread.getId());
				objects.add(thread);
			}
		}
		JsonWriter.saveList(objects, threadPath, true);
	}

	public synchronized void addPosts(List<Post> posts) throws IOException {
		List<Object> objects = new ArrayList<Object>();
		for (Post post : posts) {
			if (!(postIds.contains(post.getId()))) {
				postIds.add(post.getId());
				objects.add(post);
			}
		}
		JsonWriter.saveList(objects, postPath, true);
	}

	public synchronized void addPages(List<Page> pages) {
		for (Page p : pages) {
			if (p.getUrl().indexOf("textbook-garage-306/") == 0) { // ignore forums: 306
				continue;
			}
			if (!exists(p)) {
				pendingPages.push(p);
			}
		}
	}

	public synchronized void finish(Page page) {
		finishedPages.add(page);
		saveUrl(page, urlPath);
	}

	private synchronized void saveUrl(Page page, String filename) {
		File file = new File(filename);
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8));
			bw.write(page.getUrl() + "\n");
			bw.close();
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Failed to save url", e);
		}
	}

	public synchronized String status() {
		return String.format("%d/%d", pendingPages.size(), finishedPages.size());
	}
}
