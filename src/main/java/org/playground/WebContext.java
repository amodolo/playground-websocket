package org.playground;

import org.playground.endpoint.WebChannelDispatcher;
import org.playground.services.MonitorService;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class WebContext implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        WebChannelDispatcher.start();
        MonitorService.start();

        String cookiePath = sce.getServletContext().getContextPath() + "/w";
        sce.getServletContext().getSessionCookieConfig().setPath(cookiePath);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        WebChannelDispatcher.stop();
        MonitorService.stop();
    }
}
