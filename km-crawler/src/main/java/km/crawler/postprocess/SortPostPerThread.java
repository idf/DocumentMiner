package km.crawler.postprocess;

import km.common.Config;
import km.common.json.JsonReader;
import km.common.json.JsonWriter;
import km.crawler.entities.Post;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SortPostPerThread {

	public static void main(String[] args) throws IOException {
		List<Post> posts = JsonReader.getList(Config.settings.getPostsPath(), Post.class);
		System.out.println("read done");
		
		Collections.sort(posts, comparator);
		System.out.println("sort done");
		
		JsonWriter.saveList(posts, Config.settings.getSortedPostsPath());
		System.out.println("write done");
	}

	private static Comparator<Post> comparator = new Comparator<Post>() {
		@Override
		public int compare(Post o1, Post o2) {
			if (o1.getThreadId() > o2.getThreadId()) {
				return 1;
			} else if (o1.getThreadId() == o2.getThreadId()) {
				if (o1.getStorey() > o2.getStorey()) {
					return 1;
				} else if (o1.getStorey() < o2.getStorey()) {
					return -1;
				}
				return 0;
			}
			return -1;
		}
	};
}
