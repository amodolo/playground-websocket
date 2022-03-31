package org.playground.servlet;

import org.playground.models.User;
import org.playground.services.UserService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.InetAddress;

@WebServlet("/w/login")
public class LoginServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        if (UserService.validate(username, password)) {
            User user = UserService.get(username);
            HttpSession session = req.getSession(true);
            session.setAttribute("user", user);
            resp.sendRedirect(req.getContextPath() + "/w/app");
        } else {
            resp.sendRedirect(req.getContextPath() + "/w/login");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getSession(false) != null) {
            resp.sendRedirect(req.getContextPath() + "/w/app");
        } else {
            req.setAttribute("node", InetAddress.getLocalHost().toString());
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
        }
    }
}
