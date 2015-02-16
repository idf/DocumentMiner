package km.mallet.topic;

import km.common.Config;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class TopicsEvaluation {

	public static void main(String[] args) throws IOException {
		String filename = Config.settings.getMalletSettings().getTopicsPath(); // "E:/project/kd/data/mallet/50_topics.txt";
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
