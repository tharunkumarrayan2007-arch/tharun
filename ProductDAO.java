package com.onlinmart.onlinmart.dao;
import com.onlinmart.onlinmart.model.Product;
import com.zaxxer.hikari.HikariDataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
public class ProductDAO {
    private final HikariDataSource ds;
    public ProductDAO(HikariDataSource ds) { this.ds = ds; }
    public List<Product> search(String keyword, String category) {
        StringBuilder sql = new StringBuilder(
            "SELECT p.*, u.name AS seller_name FROM products p JOIN users u ON p.seller_id=u.id WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (LOWER(p.name) LIKE ? OR LOWER(p.description) LIKE ?)");
            String like = "%" + keyword.toLowerCase() + "%";
            params.add(like); params.add(like);
        }
        if (category != null && !category.isBlank()) {
            sql.append(" AND p.category=?"); params.add(category);
        }
        sql.append(" ORDER BY p.created_at DESC");
        List<Product> list = new ArrayList<>();
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i+1, params.get(i));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }
    public Optional<Product> findById(long id) {
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(
            "SELECT p.*, u.name AS seller_name FROM products p JOIN users u ON p.seller_id=u.id WHERE p.id=?")) {
            ps.setLong(1, id); ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(map(rs));
        } catch (Exception e) { e.printStackTrace(); }
        return Optional.empty();
    }
    public Product create(Product p) {
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(
            "INSERT INTO products(seller_id,name,description,price,stock_qty,category,image_url) VALUES(?,?,?,?,?,?,?)",
            Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, p.getSellerId()); ps.setString(2, p.getName()); ps.setString(3, p.getDescription());
            ps.setBigDecimal(4, p.getPrice()); ps.setInt(5, p.getStockQty());
            ps.setString(6, p.getCategory()); ps.setString(7, p.getImageUrl());
            ps.executeUpdate();
            ResultSet k = ps.getGeneratedKeys(); if (k.next()) p.setId(k.getLong(1));
            return p;
        } catch (Exception e) { throw new RuntimeException(e); }
    }
    public boolean update(Product p) {
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(
            "UPDATE products SET name=?,description=?,price=?,stock_qty=?,category=?,image_url=? WHERE id=? AND seller_id=?")) {
            ps.setString(1, p.getName()); ps.setString(2, p.getDescription()); ps.setBigDecimal(3, p.getPrice());
            ps.setInt(4, p.getStockQty()); ps.setString(5, p.getCategory()); ps.setString(6, p.getImageUrl());
            ps.setLong(7, p.getId()); ps.setLong(8, p.getSellerId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }
    public boolean deleteById(long id) {
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(
            "DELETE FROM products WHERE id=?")) {
            ps.setLong(1, id); return ps.executeUpdate() > 0;
        } catch (Exception e) { return false; }
    }
    public boolean delete(long id, long sellerId) {
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(
            "DELETE FROM products WHERE id=? AND seller_id=?")) {
            ps.setLong(1, id); ps.setLong(2, sellerId); return ps.executeUpdate() > 0;
        } catch (Exception e) { return false; }
    }
    private Product map(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getLong("id")); p.setSellerId(rs.getLong("seller_id")); p.setName(rs.getString("name"));
        p.setDescription(rs.getString("description")); p.setPrice(rs.getBigDecimal("price"));
        p.setStockQty(rs.getInt("stock_qty")); p.setCategory(rs.getString("category"));
        p.setImageUrl(rs.getString("image_url"));
        try { p.setSellerName(rs.getString("seller_name")); } catch (Exception ignored) {}
        return p;
    }
}
