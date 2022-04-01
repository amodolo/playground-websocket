package org.playground;

import org.playground.endpoint.PipeDispatcher;
import org.playground.services.MonitorService;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class WebContext implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        PipeDispatcher.start();
        MonitorService.start();

        String cookiePath = sce.getServletContext().getContextPath() + "/w";
        sce.getServletContext().getSessionCookieConfig().setPath(cookiePath);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        PipeDispatcher.stop();
        MonitorService.stop();
    }
}
