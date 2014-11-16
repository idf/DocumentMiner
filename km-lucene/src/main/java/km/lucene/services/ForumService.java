package km.lucene.services;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import km.common.json.JsonReader;
import km.lucene.entities.Forum;

public class ForumService implements TitleService {

	private static final Logger LOG = Logger.getLogger(ForumService.class.getName());
	private static Map<Integer, Forum> forums;

	public static void init(String filename) {
		loadForums(filename);
	}

	@Override
	public String getTitle(int forumId) {
		Forum forum = forums.get(forumId);
		if (forum != null) {
			return forum.getTitle();
		}

		return "";
	}

	private static void loadForums(String filename) {
        forums = new HashMap<>();
        try {
	        List<Forum> forumList = JsonReader.getList(filename, Forum.class);
	        for (Forum forum: forumList) {
	            forums.put(forum.getId(), forum);
	        }
        } catch (IOException e) {
        	LOG.log(Level.SEVERE, "Fail to load forum data.", e);
        }
    }
}
