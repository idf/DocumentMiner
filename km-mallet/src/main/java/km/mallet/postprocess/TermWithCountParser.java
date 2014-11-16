package km.mallet.postprocess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TermWithCountParser {
	private static final Logger LOG = Logger.getLogger(TermWithCountParser.class.getName());

	public static List<TermWithCount> parse(File file) {
		List<TermWithCount> terms = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] values = line.split("\\s");
				int id = Integer.parseInt(values[0]);
				String term = values[1];
				int count = 0;
				for (int i = 2; i < values.length; i++) {
					String valuePair = values[i];
					String[] pairs = valuePair.split(":");
					count += Integer.parseInt(pairs[1]);
				}
				TermWithCount twc = new TermWithCount(id, term, count);
				terms.add(twc);
			}
		} catch (IOException e) {
			LOG.log(Level.SEVERE, "Fail to parse " + file.getPath(), e);
		}
		
		Collections.sort(terms, new Comparator<TermWithCount>() {
			@Override
			public int compare(TermWithCount o1, TermWithCount o2) {
				if (o1.getCount() > o2.getCount()) {
					return -1;
				} else if (o1.getCount() < o2.getCount()) {
					return 1;
				}
				return 0;
			}
		});
		return terms;
	}
}
