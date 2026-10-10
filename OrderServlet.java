package com.elitemart.controller;

import com.elitemart.util.DBConnection;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet("/placeOrder")
public class OrderServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        int buyerId = (Integer) session.getAttribute("userId");

        String totalValue = request.getParameter("total");
        String cartData = request.getParameter("cart");

        if (totalValue == null || totalValue.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Total amount is required");
            return;
        }

        try (Connection connection = DBConnection.getConnection()) {

            connection.setAutoCommit(false);

            PreparedStatement orderStatement =
                    connection.prepareStatement(
                            "INSERT INTO orders (buyer_id, total_amount, status) VALUES (?, ?, ?)",
                            java.sql.Statement.RETURN_GENERATED_KEYS);

            orderStatement.setInt(1, buyerId);
            orderStatement.setDouble(2, Double.parseDouble(totalValue));
            orderStatement.setString(3, "PLACED");

            orderStatement.executeUpdate();

            ResultSet keys = orderStatement.getGeneratedKeys();

            int orderId = 0;

            if (keys.next()) {
                orderId = keys.getInt(1);
            }

            if (cartData != null && !cartData.trim().isEmpty()) {

                String[] items = cartData.split("\\|");

                PreparedStatement itemStatement =
                        connection.prepareStatement(
                                "INSERT INTO order_items " +
                                "(order_id, product_id, quantity, price) " +
                                "VALUES (?, ?, ?, ?)");

                for (String item : items) {

                    String[] details = item.split(",");

                    if (details.length >= 3) {

                        int productId = Integer.parseInt(details[0]);
                        int quantity = Integer.parseInt(details[1]);
                        double price = Double.parseDouble(details[2]);

                        itemStatement.setInt(1, orderId);
                        itemStatement.setInt(2, productId);
                        itemStatement.setInt(3, quantity);
                        itemStatement.setDouble(4, price);

                        itemStatement.addBatch();
                    }
                }

                itemStatement.executeBatch();
            }

            connection.commit();

            response.setContentType("text/plain");
            response.getWriter().print("Order placed successfully!");

        } catch (Exception e) {

            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Order failed"
            );
        }
    }
}