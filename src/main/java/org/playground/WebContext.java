package org.playground;

import org.playground.endpoint.NotificationService;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class WebContext implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        NotificationService.start();

        String cookiePath = sce.getServletContext().getContextPath() + "/w";
        sce.getServletContext().getSessionCookieConfig().setPath(cookiePath);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        NotificationService.stop();
    }
}
