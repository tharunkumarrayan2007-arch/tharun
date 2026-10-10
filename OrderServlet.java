package com.onlinmart.onlinmart.controller;

import com.google.gson.*;
import com.onlinmart.onlinmart.model.User;
import com.onlinmart.onlinmart.service.OrderService;
import com.onlinmart.onlinmart.util.JsonUtil;
import com.zaxxer.hikari.HikariDataSource;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.util.Map;

@WebServlet("/api/orders/*")
public class OrderServlet extends HttpServlet {
    private OrderService service;

    public void init() {
        service = new OrderService((HikariDataSource) getServletContext().getAttribute("dataSource"));
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User u = user(req);
        if (u == null) {
            JsonUtil.writeError(resp, 401, "UNAUTHORIZED", "Login first");
            return;
        }
        if (!"BUYER".equals(u.getRole()) && !"ADMIN".equals(u.getRole())) {
            JsonUtil.writeError(resp, 403, "FORBIDDEN", "Only buyers can place orders");
            return;
        }
        try {
            String body = read(req);
            String paymentMethod = "COD";
            String upiId = null;
            boolean quoteOnly = false;
            if (body != null && !body.isBlank()) {
                JsonObject j = JsonParser.parseString(body).getAsJsonObject();
                if (j.has("paymentMethod")) paymentMethod = j.get("paymentMethod").getAsString();
                if (j.has("upiId")) upiId = j.get("upiId").getAsString();
                if (j.has("quote") && j.get("quote").getAsBoolean()) quoteOnly = true;
            }
            if ("ONLINE".equalsIgnoreCase(paymentMethod) || "UPI".equalsIgnoreCase(paymentMethod)) {
                paymentMethod = "UPI";
            } else {
                paymentMethod = "COD";
            }

            if (quoteOnly) {
                Map<String, Object> quote = service.calculateCharges(u.getId(), paymentMethod);
                JsonUtil.writeSuccess(resp, quote);
                return;
            }

            if ("UPI".equals(paymentMethod)) {
                if (upiId == null || upiId.isBlank() || !upiId.contains("@")) {
                    JsonUtil.writeError(resp, 400, "VALIDATION_ERROR", "Valid UPI ID required (e.g. name@upi)");
                    return;
                }
            }

            Map<String, Object> result = service.placeOrder(u.getId(), paymentMethod);
            result.put("buyerName", u.getName());
            result.put("buyerEmail", u.getEmail());
            if (upiId != null) result.put("upiId", upiId);
            JsonUtil.writeSuccess(resp, result);
        } catch (IllegalArgumentException e) {
            JsonUtil.writeError(resp, 400, "VALIDATION_ERROR", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.writeError(resp, 500, "SERVER_ERROR", e.getMessage());
        }
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User u = user(req);
        if (u == null) {
            JsonUtil.writeError(resp, 401, "UNAUTHORIZED", "Login first");
            return;
        }
        JsonUtil.writeSuccess(resp, service.getOrders(u.getId()));
    }

    private User user(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        return s != null ? (User) s.getAttribute("user") : null;
    }

    private String read(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader r = req.getReader()) {
            String l;
            while ((l = r.readLine()) != null) sb.append(l);
        }
        return sb.toString();
    }
}
