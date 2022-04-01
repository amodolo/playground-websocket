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
import java.util.List;
import java.util.stream.Collectors;

@WebServlet("/w/app")
public class Servlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getSession(false) == null) {
            resp.sendRedirect(req.getContextPath() + "/w/login");
        } else {
            User user = (User) req.getSession(false).getAttribute("user");
            WindowManager wm = new WindowManager(user);
            WindowManagers.register(wm);

            req.setAttribute("id", user.getId());
            req.setAttribute("name", user.getName());
            req.setAttribute("surname", user.getSurname());
            req.setAttribute("node", InetAddress.getLocalHost().toString());
            req.setAttribute("wmId", wm.getId());
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
        List<WindowManager> wms = WindowManagers.getAll()
                .stream()
                .filter(wm -> wm.getUser().equals(user))
                .collect(Collectors.toList());

        for (WindowManager wm : wms) WindowManagers.unregister(wm);
    }
}
