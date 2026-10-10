package com.onlinmart.onlinmart.controller;

import com.google.gson.*;
import com.onlinmart.onlinmart.model.User;
import com.onlinmart.onlinmart.service.AuthService;
import com.onlinmart.onlinmart.util.JsonUtil;
import com.zaxxer.hikari.HikariDataSource;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;

@WebServlet("/api/auth/*")
public class AuthServlet extends HttpServlet {
    private AuthService authService;

    public void init() {
        HikariDataSource ds = (HikariDataSource) getServletContext().getAttribute("dataSource");
        authService = new AuthService(ds);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
        try {
            JsonObject json = JsonParser.parseString(read(req)).getAsJsonObject();
            if ("/register".equals(path)) {
                String role = json.has("role") ? json.get("role").getAsString() : "BUYER";
                User u = authService.register(get(json, "name"), get(json, "email"), get(json, "password"), role);
                HttpSession s = req.getSession(true);
                s.setAttribute("user", u);
                s.setMaxInactiveInterval(3600);
                writeUser(resp, u);
            } else if ("/login".equals(path)) {
                User u = authService.login(get(json, "email"), get(json, "password"));
                HttpSession old = req.getSession(false);
                if (old != null) old.invalidate();
                HttpSession s = req.getSession(true);
                s.setAttribute("user", u);
                s.setMaxInactiveInterval(3600);
                writeUser(resp, u);
            } else {
                JsonUtil.writeError(resp, 404, "NOT_FOUND", "Unknown");
            }
        } catch (IllegalArgumentException e) {
            JsonUtil.writeError(resp, 400, "VALIDATION_ERROR", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.writeError(resp, 500, "SERVER_ERROR", e.getMessage());
        }
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
        if ("/me".equals(path)) {
            HttpSession s = req.getSession(false);
            User u = s != null ? (User) s.getAttribute("user") : null;
            if (u == null) {
                JsonUtil.writeError(resp, 401, "UNAUTHORIZED", "Not logged in");
            } else {
                writeUser(resp, u);
            }
        } else if ("/logout".equals(path)) {
            HttpSession s = req.getSession(false);
            if (s != null) s.invalidate();
            JsonUtil.writeSuccess(resp, "Logged out");
        } else {
            JsonUtil.writeError(resp, 404, "NOT_FOUND", "Unknown");
        }
    }

    private void writeUser(HttpServletResponse resp, User u) throws IOException {
        JsonObject d = new JsonObject();
        d.addProperty("id", u.getId());
        d.addProperty("name", u.getName());
        d.addProperty("email", u.getEmail());
        d.addProperty("role", u.getRole());
        JsonUtil.writeSuccess(resp, d);
    }

    private String read(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader r = req.getReader()) {
            String l;
            while ((l = r.readLine()) != null) sb.append(l);
        }
        return sb.toString();
    }

    private String get(JsonObject j, String k) {
        return j.has(k) && !j.get(k).isJsonNull() ? j.get(k).getAsString() : null;
    }
}
