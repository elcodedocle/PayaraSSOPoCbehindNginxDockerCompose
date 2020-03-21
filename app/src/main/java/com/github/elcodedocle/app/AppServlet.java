package com.github.elcodedocle.app;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.Principal;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AppServlet extends HttpServlet {

    static final Logger logger = Logger.getLogger(AppServlet.class.getName());

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response){
        try {
            processRequest(request, response);
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response){
        try {
            processRequest(request, response);
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // shamelessly stolen from https://blog.payara.fish/using-jaspic-to-secure-a-web-application-in-payara-server (with some pertinent changes)
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter())
        {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet AppServlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet AppServlet at " + request.getContextPath() + "</h1>");

            Principal userPrincipal = request.getUserPrincipal();
            boolean adminUser = request.isUserInRole("admin");
            String userName;

            if (userPrincipal != null)
            {
                userName = userPrincipal.getName();
            }
            else
            {
                userName = "Unknown User";
            }

            out.println("You are currently authenticated as: " + userName + "<br>");

            if (adminUser)
            {
                out.println("<br>As you're admin you can view this.<br>");
            }
            else
            {
                out.println("<br>Sorry, you're not admin. Nothing to see here.<br>");
            }

            out.println("</body>");
            out.println("</html>");
        }
    }
}
