package km.mallet.postprocess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TopicWithTermWeightParser {

    private static final Logger LOG = Logger.getLogger(TopicWithTermWeightParser.class.getName());

    public static List<TopicWithTermWeight> parse(File file) {
        List<TopicWithTermWeight> topics = new ArrayList<>();
        Map<String, Double> terms = new HashMap<>();
        String line;
        int prevId = 0;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            while ((line = br.readLine()) != null) {
                String[] values = line.split("\\s");
                int id = Integer.parseInt(values[0]);
                if (id != prevId) {
                    DoubleComparator dc = new DoubleComparator(terms);
                    TreeMap<String, Double> sortedTerms = new TreeMap<>(dc);
                    sortedTerms.putAll(terms);
                    topics.add(new TopicWithTermWeight(prevId, sortedTerms));

                    prevId = id;
                    terms.clear();
                }
                String term = values[1];
                double weight = Double.parseDouble(values[2]);
                terms.put(term, weight);
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Fail to parse " + file.getPath(), e);
        }

        DoubleComparator dc = new DoubleComparator(terms);
        TreeMap<String, Double> sortedTerms = new TreeMap<>(dc);
        sortedTerms.putAll(terms);
        topics.add(new TopicWithTermWeight(prevId, sortedTerms));

        return topics;
    }

    private static class DoubleComparator implements Comparator<String> {

        Map<String, Double> map;

        public DoubleComparator(Map<String, Double> map) {
            this.map = map;
        }

        @Override
        public int compare(String key1, String key2) {
            if (map.get(key1) >= map.get(key2)) {
                return -1;
            } else {
                return 1;
            }
        }
    }
}
