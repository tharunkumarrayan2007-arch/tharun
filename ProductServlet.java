package com.onlinmart.onlinmart.controller;

import com.google.gson.*;
import com.onlinmart.onlinmart.model.*;
import com.onlinmart.onlinmart.service.ProductService;
import com.onlinmart.onlinmart.util.JsonUtil;
import com.zaxxer.hikari.HikariDataSource;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.math.BigDecimal;

@WebServlet("/api/products/*")
public class ProductServlet extends HttpServlet {
    private ProductService service;

    public void init() {
        service = new ProductService((HikariDataSource) getServletContext().getAttribute("dataSource"));
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String path = req.getPathInfo();
            if (path == null || path.equals("/")) {
                JsonUtil.writeSuccess(resp, service.searchProducts(req.getParameter("q"), req.getParameter("category")));
            } else {
                var p = service.getProductById(Long.parseLong(path.substring(1)));
                if (p.isPresent()) JsonUtil.writeSuccess(resp, p.get());
                else JsonUtil.writeError(resp, 404, "NOT_FOUND", "Product not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.writeError(resp, 500, "SERVER_ERROR", e.getMessage() != null ? e.getMessage() : "Error");
        }
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = getUser(req);
        if (user == null || (!"SELLER".equals(user.getRole()) && !"ADMIN".equals(user.getRole()))) {
            JsonUtil.writeError(resp, 403, "FORBIDDEN", "Seller login required");
            return;
        }
        try {
            JsonObject j = JsonParser.parseString(read(req)).getAsJsonObject();
            Product p = service.createProduct(user.getId(), get(j, "name"), get(j, "description"),
                    j.has("price") ? j.get("price").getAsBigDecimal() : null,
                    j.has("stockQty") ? j.get("stockQty").getAsInt() : 0,
                    get(j, "category"), get(j, "imageUrl"));
            JsonUtil.writeSuccess(resp, p);
        } catch (IllegalArgumentException e) {
            JsonUtil.writeError(resp, 400, "VALIDATION_ERROR", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.writeError(resp, 500, "SERVER_ERROR", e.getMessage());
        }
    }

    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = getUser(req);
        if (user == null || (!"SELLER".equals(user.getRole()) && !"ADMIN".equals(user.getRole()))) {
            JsonUtil.writeError(resp, 403, "FORBIDDEN", "Seller required");
            return;
        }
        try {
            JsonObject j = JsonParser.parseString(read(req)).getAsJsonObject();
            Product p = new Product();
            p.setId(j.get("id").getAsLong());
            p.setSellerId(user.getId());
            p.setName(get(j, "name"));
            p.setDescription(get(j, "description"));
            p.setPrice(j.has("price") ? j.get("price").getAsBigDecimal() : BigDecimal.ZERO);
            p.setStockQty(j.has("stockQty") ? j.get("stockQty").getAsInt() : 0);
            p.setCategory(get(j, "category"));
            p.setImageUrl(get(j, "imageUrl"));
            if (service.updateProduct(p)) JsonUtil.writeSuccess(resp, "Updated");
            else JsonUtil.writeError(resp, 404, "NOT_FOUND", "Not found or not owned");
        } catch (Exception e) {
            JsonUtil.writeError(resp, 500, "SERVER_ERROR", e.getMessage());
        }
    }

    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = getUser(req);
        if (user == null || (!"SELLER".equals(user.getRole()) && !"ADMIN".equals(user.getRole()))) {
            JsonUtil.writeError(resp, 403, "FORBIDDEN", "Seller required");
            return;
        }
        try {
            long id = Long.parseLong(req.getPathInfo().substring(1));
            boolean ok;
            if ("ADMIN".equals(user.getRole())) {
                ok = service.deleteProductAsAdmin(id);
            } else {
                ok = service.deleteProduct(id, user.getId());
            }
            if (ok) JsonUtil.writeSuccess(resp, "Deleted");
            else JsonUtil.writeError(resp, 404, "NOT_FOUND", "Not found");
        } catch (Exception e) {
            JsonUtil.writeError(resp, 500, "SERVER_ERROR", e.getMessage());
        }
    }

    private User getUser(HttpServletRequest req) {
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

    private String get(JsonObject j, String k) {
        return j.has(k) && !j.get(k).isJsonNull() ? j.get(k).getAsString() : null;
    }
}
