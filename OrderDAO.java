package com.onlinmart.onlinmart.dao;

import com.onlinmart.onlinmart.model.*;
import com.zaxxer.hikari.HikariDataSource;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

public class OrderDAO {
    private final HikariDataSource ds;

    public OrderDAO(HikariDataSource ds) {
        this.ds = ds;
    }

    public long create(long buyerId, BigDecimal subtotal, BigDecimal serviceCharge,
                       BigDecimal deliveryCharge, BigDecimal total, String paymentMethod,
                       List<OrderItem> items) {
        Connection c = null;
        try {
            c = ds.getConnection();
            c.setAutoCommit(false);
            PreparedStatement ops = c.prepareStatement(
                    "INSERT INTO orders(buyer_id,status,subtotal,service_charge,delivery_charge,total_amount,payment_method) " +
                            "VALUES(?,'CONFIRMED',?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            ops.setLong(1, buyerId);
            ops.setBigDecimal(2, subtotal);
            ops.setBigDecimal(3, serviceCharge);
            ops.setBigDecimal(4, deliveryCharge);
            ops.setBigDecimal(5, total);
            ops.setString(6, paymentMethod != null ? paymentMethod : "COD");
            ops.executeUpdate();
            ResultSet k = ops.getGeneratedKeys();
            k.next();
            long orderId = k.getLong(1);

            PreparedStatement ips = c.prepareStatement(
                    "INSERT INTO order_items(order_id,product_id,quantity,unit_price) VALUES(?,?,?,?)");
            PreparedStatement sps = c.prepareStatement(
                    "UPDATE products SET stock_qty=stock_qty-? WHERE id=? AND stock_qty>=?");
            for (OrderItem i : items) {
                ips.setLong(1, orderId);
                ips.setLong(2, i.getProductId());
                ips.setInt(3, i.getQuantity());
                ips.setBigDecimal(4, i.getUnitPrice());
                ips.addBatch();
                sps.setInt(1, i.getQuantity());
                sps.setLong(2, i.getProductId());
                sps.setInt(3, i.getQuantity());
                sps.addBatch();
            }
            ips.executeBatch();
            sps.executeBatch();
            c.commit();
            return orderId;
        } catch (Exception e) {
            try { if (c != null) c.rollback(); } catch (Exception ignored) {}
            throw new RuntimeException(e);
        } finally {
            try { if (c != null) { c.setAutoCommit(true); c.close(); } } catch (Exception ignored) {}
        }
    }

    public List<Order> findByBuyer(long buyerId) {
        List<Order> list = new ArrayList<>();
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT * FROM orders WHERE buyer_id=? ORDER BY created_at DESC")) {
            ps.setLong(1, buyerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapOrder(rs));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Map<String, Object>> findAllDetailed() {
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT o.*, u.name AS buyer_name, u.email AS buyer_email FROM orders o " +
                             "JOIN users u ON u.id=o.buyer_id ORDER BY o.created_at DESC")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", rs.getLong("id"));
                m.put("buyerId", rs.getLong("buyer_id"));
                m.put("buyerName", rs.getString("buyer_name"));
                m.put("buyerEmail", rs.getString("buyer_email"));
                m.put("status", rs.getString("status"));
                m.put("subtotal", rs.getBigDecimal("subtotal"));
                m.put("serviceCharge", rs.getBigDecimal("service_charge"));
                m.put("deliveryCharge", rs.getBigDecimal("delivery_charge"));
                m.put("totalAmount", rs.getBigDecimal("total_amount"));
                m.put("paymentMethod", rs.getString("payment_method"));
                Timestamp t = rs.getTimestamp("created_at");
                m.put("createdAt", t != null ? t.toLocalDateTime().toString() : null);
                list.add(m);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private Order mapOrder(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setId(rs.getLong("id"));
        o.setBuyerId(rs.getLong("buyer_id"));
        o.setStatus(rs.getString("status"));
        o.setTotalAmount(rs.getBigDecimal("total_amount"));
        try { o.setName(rs.getString("payment_method")); } catch (Exception ignored) {}
        try {
            o.setPrice(rs.getBigDecimal("subtotal"));
            o.setUnitPrice(rs.getBigDecimal("service_charge"));
            o.setProductPrice(rs.getBigDecimal("delivery_charge"));
        } catch (Exception ignored) {}
        Timestamp t = rs.getTimestamp("created_at");
        if (t != null) o.setCreatedAt(t.toLocalDateTime());
        return o;
    }
}
