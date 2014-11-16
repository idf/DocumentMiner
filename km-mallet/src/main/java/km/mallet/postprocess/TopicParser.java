package km.mallet.postprocess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TopicParser {

    private static final Logger LOG = Logger.getLogger(TopicParser.class.getName());

    public static List<Topic> parse(File file) {
        List<Topic> topics = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split("\\s");
                int id = Integer.parseInt(values[0]);
                String[] terms = Arrays.copyOfRange(values, 2, values.length);
                Topic topic = new Topic(id, terms);
                topics.add(topic);
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Fail to parse " + file.getPath(), e);
        }
        return topics;
    }
}
