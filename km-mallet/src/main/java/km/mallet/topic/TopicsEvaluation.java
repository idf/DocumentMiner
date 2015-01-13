package km.mallet.topic;

import km.common.Settings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class TopicsEvaluation {

	public static void main(String[] args) throws IOException {
		String filename = Settings.MalletSettings.TOPICS_PATH; // "E:/project/kd/data/mallet/50_topics.txt";
		int docs = 0;
		float props = 0;
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filename)), StandardCharsets.UTF_8))) {
			String line = br.readLine(); // ignore the first line
			while ((line = br.readLine()) != null) {
				String[] values = line.split("\\s");
				if (values.length == 4) {
					docs++;
					props += Float.parseFloat(values[3]);
				}
			}
		}
		props /= docs;
		System.out.println(docs);
		System.out.println(props);
	}
}
