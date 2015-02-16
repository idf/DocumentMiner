package km.crawler.postprocess;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import km.common.Config;
import km.crawler.entities.Post;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class ToText {

	public static void main(String[] args) throws JsonSyntaxException, IOException {
		String sourceFilename = Config.settings.getPostsPath();
        String targetFilename = Config.settings.getContentsPath();
        toSingleFile(sourceFilename, targetFilename);
	}

	private static void toSingleFile(String sourceFilename, String targetFilename) throws JsonSyntaxException, IOException {
        File sourceFile = new File(sourceFilename);
        File targetFile = new File(targetFilename);
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(sourceFile), StandardCharsets.UTF_8));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetFile), StandardCharsets.UTF_8));
        Gson gson = new Gson();
        String line;

        int i = 1;
        while ((line = br.readLine()) != null) {
            Post post = gson.fromJson(line, Post.class);

            String content = post.getContent();
            content = content.trim();
            content = filterInvalidCharacter(content);
            content = content.replaceAll("[ ]*\\.([ ]*\\.)*", ".");
            bw.write(post.getId() + ",");
            bw.write(content);
            bw.write("\n");
            System.out.println(String.format("%d", i++));
        }
        br.close();
        bw.close();
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
