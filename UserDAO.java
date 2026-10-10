package com.onlinmart.onlinmart.dao;

import com.onlinmart.onlinmart.model.User;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.*;

public class UserDAO {
    private final HikariDataSource ds;

    public UserDAO(HikariDataSource ds) {
        this.ds = ds;
    }

    public Optional<User> findByEmail(String email) {
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT id,name,email,password_hash,role,created_at FROM users WHERE email=?")) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(map(rs));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT id,name,email,password_hash,role,created_at FROM users ORDER BY id")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean emailExists(String email) {
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT 1 FROM users WHERE email=?")) {
            ps.setString(1, email);
            return ps.executeQuery().next();
        } catch (Exception e) {
            return false;
        }
    }

    public User create(User u) {
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO users(name,email,password_hash,role) VALUES(?,?,?,?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getName());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getPasswordHash());
            ps.setString(4, u.getRole());
            ps.executeUpdate();
            ResultSet k = ps.getGeneratedKeys();
            if (k.next()) u.setId(k.getLong(1));
            return u;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getLong("id"));
        u.setName(rs.getString("name"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setRole(rs.getString("role"));
        Timestamp t = rs.getTimestamp("created_at");
        if (t != null) u.setCreatedAt(t.toLocalDateTime());
        return u;
    }
}
