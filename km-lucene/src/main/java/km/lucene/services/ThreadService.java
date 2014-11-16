package km.lucene.services;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import km.common.json.JsonReader;
import km.lucene.entities.Thread;

public class ThreadService implements TitleService {

    private static final Logger LOG = Logger.getLogger(ThreadService.class.getName());
    private static Map<Integer, Thread> threads;

    public static void init(String filename) {
        loadThreads(filename);
    }

    @Override
    public String getTitle(int threadId) {
        Thread thread = threads.get(threadId);
        if (thread != null) {
            return thread.getTitle();
        }

        return "";
    }

    private static void loadThreads(String filename) {
        threads = new HashMap<>();
        try {
	        List<Thread> threadList = JsonReader.getList(filename, Thread.class);
	        for (Thread thread: threadList) {
	        	threads.put(thread.getId(), thread);
	        }
        } catch (IOException e) {
        	LOG.log(Level.SEVERE, "Fail to load thread data.", e);
        }
    }
}
