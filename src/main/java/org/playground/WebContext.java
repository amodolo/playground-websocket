package org.playground;

import org.playground.pipe.dispatcher.redis.RedisRemoteMessageBroker;
import org.playground.pipe.dispatcher.redis.RemoteMessageBroker;
import org.playground.services.MonitorService;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class WebContext implements ServletContextListener {

    private RemoteMessageBroker remoteMessageBroker;

    public WebContext() {
        //TODO: CDI will inject this dependency
        remoteMessageBroker = RedisRemoteMessageBroker.getInstance();
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        remoteMessageBroker.start();
        MonitorService.start();

        String cookiePath = sce.getServletContext().getContextPath() + "/w";
        sce.getServletContext().getSessionCookieConfig().setPath(cookiePath);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        MonitorService.stop();
        remoteMessageBroker.stop();
    }
}
