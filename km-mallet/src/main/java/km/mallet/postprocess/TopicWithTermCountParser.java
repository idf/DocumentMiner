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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TopicWithTermCountParser {

    private static final Logger LOG = Logger.getLogger(TopicWithTermCountParser.class.getName());

    public static List<TopicWithTermCount> parse(File file) {
        Map<Integer, Integer> counts = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split("\\s");
                for (int i = 2; i < values.length; i++) {
                    String valuePair = values[i];
                    String[] pairs = valuePair.split(":");
                    int id = Integer.parseInt(pairs[0]);
                    int count = Integer.parseInt(pairs[1]);
                    if (counts.containsKey(id)) {
                        count += counts.get(id);
                    }
                    counts.put(id, count);
                }
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Fail to parse " + file.getPath(), e);
        }
        
        List<TopicWithTermCount> topicsWithTermCount = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry: counts.entrySet()) {
        	int id = entry.getKey();
        	int count = entry.getValue();
        	TopicWithTermCount topicWithTermCount = new TopicWithTermCount(id, count);
        	topicsWithTermCount.add(topicWithTermCount);
        }
        
        Collections.sort(topicsWithTermCount, comparator);
        return topicsWithTermCount;
    }
    
    private static Comparator<TopicWithTermCount> comparator = new Comparator<TopicWithTermCount>() {
        @Override
        public int compare(TopicWithTermCount t1, TopicWithTermCount t2) {
            return t1.compareTo(t2);
        }
    };
}
