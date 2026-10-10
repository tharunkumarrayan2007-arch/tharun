package com.onlinmart.onlinmart.service;

import com.onlinmart.onlinmart.dao.ProductDAO;
import com.onlinmart.onlinmart.model.Product;
import com.zaxxer.hikari.HikariDataSource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class ProductService {
    private final ProductDAO dao;

    public ProductService(HikariDataSource ds) {
        this.dao = new ProductDAO(ds);
    }

    public List<Product> searchProducts(String k, String c) {
        return dao.search(k, c);
    }

    public Optional<Product> getProductById(long id) {
        return dao.findById(id);
    }

    public Product createProduct(long sellerId, String name, String desc, BigDecimal price, int stock, String cat, String img) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name required");
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Invalid price");
        Product p = new Product();
        p.setSellerId(sellerId);
        p.setName(name.trim());
        p.setDescription(desc);
        p.setPrice(price);
        p.setStockQty(stock);
        p.setCategory(cat);
        p.setImageUrl(img);
        return dao.create(p);
    }

    public boolean updateProduct(Product p) {
        return dao.update(p);
    }

    public boolean deleteProduct(long id, long sellerId) {
        return dao.delete(id, sellerId);
    }

    public boolean deleteProductAsAdmin(long id) {
        return dao.deleteById(id);
    }
}
