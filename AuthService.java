package com.onlinmart.onlinmart.service;
import com.onlinmart.onlinmart.dao.UserDAO;
import com.onlinmart.onlinmart.model.User;
import com.onlinmart.onlinmart.util.PasswordUtil;
import com.zaxxer.hikari.HikariDataSource;
public class AuthService {
    private final UserDAO userDAO;
    public AuthService(HikariDataSource ds) { this.userDAO = new UserDAO(ds); }
    public User register(String name, String email, String password, String role) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name required");
        if (email == null || email.isBlank()) throw new IllegalArgumentException("Email required");
        if (password == null || password.length() < 6) throw new IllegalArgumentException("Password min 6 chars");
        if (!"BUYER".equals(role) && !"SELLER".equals(role)) role = "BUYER";
        if (userDAO.emailExists(email.trim().toLowerCase())) throw new IllegalArgumentException("Email already registered");
        User u = new User();
        u.setName(name.trim()); u.setEmail(email.trim().toLowerCase());
        u.setPasswordHash(PasswordUtil.hash(password)); u.setRole(role);
        return userDAO.create(u);
    }
    public User login(String email, String password) {
        var opt = userDAO.findByEmail(email.trim().toLowerCase());
        if (opt.isEmpty() || !PasswordUtil.check(password, opt.get().getPasswordHash()))
            throw new IllegalArgumentException("Invalid email or password");
        return opt.get();
    }
}
