package km.web.filters;

import km.common.Setting;
import km.lucene.services.ForumService;
import km.lucene.services.ThreadService;
import km.lucene.services.TopicService;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebListener
public class AppContextListener implements ServletContextListener {

    private static final Logger LOGGER = Logger.getLogger(AppContextListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LOGGER.log(Level.INFO, "application started");
        ForumService.init(Setting.FORUMS_PATH);
        ThreadService.init(Setting.THREADS_PATH);
        // TopicService.init(Setting.TopicSetting.TOPICS_PATH);
        TopicService.init(Setting.MalletSetting.TOPICS_PATH);
    }


    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOGGER.log(Level.INFO, "application ended");
    }
}
