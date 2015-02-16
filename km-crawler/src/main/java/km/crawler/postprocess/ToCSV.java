package km.crawler.postprocess;

import km.common.Config;
import km.common.json.JsonReader;
import km.crawler.entities.Post;

import java.io.*;

public class ToCSV {

	public static void main(String[] args) throws IOException {
		String postFilename = Config.settings.getPostsPath();
		String outputFilename = Config.settings.getMalletSettings().getPostsPath();

		JsonReader<Post> jr = new JsonReader<>(postFilename, Post.class);
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(outputFilename))));
		Post post;
		int i = 1;
		while ((post = jr.next()) != null) {
			String content = CSVFactory.create(post);
			bw.write(content);
			System.out.println(i++);
		}
		jr.close();
		bw.close();
	}
}
