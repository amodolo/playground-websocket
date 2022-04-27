package org.playground.servlet;

import org.playground.models.User;
import org.playground.models.WindowManager;
import org.playground.models.WindowManagers;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.InetAddress;

@WebServlet("/w/app")
public class Servlet extends HttpServlet {

    private final WindowManagers windowManagers;

    public Servlet() {
        //TODO: CDI will inject this dependency
        windowManagers = WindowManagers.getInstance();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getSession(false) == null) {
            resp.sendRedirect(req.getContextPath() + "/w/login");
        } else {
            User user = (User) req.getSession(false).getAttribute("user");
            WindowManager wm = new WindowManager(user);
            windowManagers.register(wm);

            req.setAttribute("id", user.getId());
            req.setAttribute("name", user.getName());
            req.setAttribute("surname", user.getSurname());
            req.setAttribute("node", InetAddress.getLocalHost().toString());
            req.setAttribute("wmId", wm.getId());
            req.setAttribute("logEnabled", true);
            req.getRequestDispatcher("/main.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            User user = (User) req.getSession(false).getAttribute("user");
            purgeUserWM(user);
            session.invalidate();
        }
        resp.sendRedirect(req.getContextPath() + "/w/login");
    }

    private void purgeUserWM(User user) {
        windowManagers.getUsersWindowManager(user.getId())
                .forEach(windowManagers::unregister);
    }
}
