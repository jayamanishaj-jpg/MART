package com.elitemart.controller;

import com.elitemart.util.DBConnection;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        String sql = "SELECT user_id, name, role FROM users WHERE email = ? AND password = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, email);
            statement.setString(2, password);

            ResultSet result = statement.executeQuery();

            if (result.next()) {

                int userId = result.getInt("user_id");
                String name = result.getString("name");
                String role = result.getString("role");

                HttpSession session = request.getSession();
                session.setAttribute("userId", userId);
                session.setAttribute("userName", name);
                session.setAttribute("role", role);

                if ("ADMIN".equals(role)) {
                    response.sendRedirect("admin.html");
                } else if ("SELLER".equals(role)) {
                    response.sendRedirect("seller.html");
                } else {
                    response.sendRedirect("home.html");
                }

            } else {
                out.println("<h2>Invalid email or password!</h2>");
            }

        } catch (Exception e) {
            out.println("<h2>Login Error!</h2>");
            e.printStackTrace();
        }
    }
}