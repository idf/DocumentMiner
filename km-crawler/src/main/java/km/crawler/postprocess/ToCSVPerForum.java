package km.crawler.postprocess;

import km.common.Settings;
import km.common.json.JsonReader;
import km.crawler.entities.Forum;
import km.crawler.entities.Post;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ToCSVPerForum {

	public static void main(String[] args) throws IOException {
		String forumFilename = Settings.FORUMS_PATH;
        String postFilename = Settings.POSTS_PATH;
		String outputFolder = Settings.MalletSettings.POSTS_FOLDER;

		List<Forum> forums = JsonReader.getList(forumFilename, Forum.class);

		Map<Integer, BufferedWriter> bws = new HashMap<>();
		for (Forum forum : forums) {
			int forumId = forum.getId();
			String filename = outputFolder + "posts_" + forumId + ".csv";
			bws.put(forum.getId(), new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(filename)))));
		}

		JsonReader<Post> jr = new JsonReader<>(postFilename, Post.class);
		Post post;
		int i = 1;
		while ((post = jr.next()) != null) {
			String content = CSVFactory.create(post);
			bws.get(post.getForumId()).write(content);
			System.out.println(i++);
		}
		jr.close();

		for (BufferedWriter jw : bws.values()) {
			jw.close();
		}
	}
}
