package com.onlinmart.onlinmart.controller;

import com.onlinmart.onlinmart.dao.UserDAO;
import com.onlinmart.onlinmart.model.User;
import com.onlinmart.onlinmart.service.OrderService;
import com.onlinmart.onlinmart.service.ProductService;
import com.onlinmart.onlinmart.util.JsonUtil;
import com.zaxxer.hikari.HikariDataSource;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.*;

@WebServlet("/api/admin/*")
public class AdminServlet extends HttpServlet {
    private UserDAO userDAO;
    private OrderService orderService;
    private ProductService productService;

    public void init() {
        HikariDataSource ds = (HikariDataSource) getServletContext().getAttribute("dataSource");
        userDAO = new UserDAO(ds);
        orderService = new OrderService(ds);
        productService = new ProductService(ds);
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User admin = requireAdmin(req, resp);
        if (admin == null) return;

        String path = req.getPathInfo();
        if (path == null) path = "/";

        try {
            if ("/users".equals(path)) {
                List<User> users = userDAO.findAll();
                List<Map<String, Object>> safe = new ArrayList<>();
                for (User u : users) {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", u.getId());
                    m.put("name", u.getName());
                    m.put("email", u.getEmail());
                    m.put("role", u.getRole());
                    m.put("createdAt", u.getCreatedAt() != null ? u.getCreatedAt().toString() : null);
                    safe.add(m);
                }
                JsonUtil.writeSuccess(resp, safe);
            } else if ("/orders".equals(path)) {
                JsonUtil.writeSuccess(resp, orderService.getAllOrdersAdmin());
            } else if ("/products".equals(path)) {
                JsonUtil.writeSuccess(resp, productService.searchProducts(null, null));
            } else if ("/stats".equals(path)) {
                List<User> users = userDAO.findAll();
                long buyers = users.stream().filter(u -> "BUYER".equals(u.getRole())).count();
                long sellers = users.stream().filter(u -> "SELLER".equals(u.getRole())).count();
                List<Map<String, Object>> orders = orderService.getAllOrdersAdmin();
                Map<String, Object> stats = new LinkedHashMap<>();
                stats.put("totalUsers", users.size());
                stats.put("buyers", buyers);
                stats.put("sellers", sellers);
                stats.put("totalOrders", orders.size());
                stats.put("totalProducts", productService.searchProducts(null, null).size());
                JsonUtil.writeSuccess(resp, stats);
            } else {
                JsonUtil.writeError(resp, 404, "NOT_FOUND", "Unknown admin endpoint");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.writeError(resp, 500, "SERVER_ERROR", e.getMessage());
        }
    }

    private User requireAdmin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession s = req.getSession(false);
        User u = s != null ? (User) s.getAttribute("user") : null;
        if (u == null) {
            JsonUtil.writeError(resp, 401, "UNAUTHORIZED", "Login required");
            return null;
        }
        if (!"ADMIN".equals(u.getRole())) {
            JsonUtil.writeError(resp, 403, "FORBIDDEN", "Admin only");
            return null;
        }
        return u;
    }
}
