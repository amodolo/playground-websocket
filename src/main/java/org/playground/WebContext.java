package org.playground;

import org.playground.pipe.dispatcher.redis.RedisSubscriberServiceManager;
import org.playground.services.MonitorService;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class WebContext implements ServletContextListener {

    private RedisSubscriberServiceManager serviceManager;

    public WebContext() {
        //TODO: CDI will inject this dependency
        serviceManager = RedisSubscriberServiceManager.getInstance();
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        serviceManager.start();
        MonitorService.start();

        String cookiePath = sce.getServletContext().getContextPath() + "/w";
        sce.getServletContext().getSessionCookieConfig().setPath(cookiePath);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        MonitorService.stop();
        serviceManager.stop();
    }
}
