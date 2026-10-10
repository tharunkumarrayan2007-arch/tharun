package com.onlinmart.onlinmart.service;

import com.onlinmart.onlinmart.dao.*;
import com.onlinmart.onlinmart.model.*;
import com.zaxxer.hikari.HikariDataSource;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class OrderService {
    private final OrderDAO orderDAO;
    private final CartDAO cartDAO;

    // Real e-com style charges
    private static final BigDecimal COD_DELIVERY = new BigDecimal("49.00");
    private static final BigDecimal UPI_DELIVERY = new BigDecimal("29.00");
    private static final BigDecimal FREE_DELIVERY_MIN = new BigDecimal("999.00");
    private static final BigDecimal COD_SERVICE_PCT = new BigDecimal("0.02");   // 2%
    private static final BigDecimal UPI_SERVICE_PCT = new BigDecimal("0.015"); // 1.5%
    private static final BigDecimal MIN_SERVICE = new BigDecimal("10.00");

    public OrderService(HikariDataSource ds) {
        orderDAO = new OrderDAO(ds);
        cartDAO = new CartDAO(ds);
    }

    public Map<String, Object> calculateCharges(long buyerId, String paymentMethod) {
        List<CartItem> cart = cartDAO.findByUser(buyerId);
        if (cart.isEmpty()) throw new IllegalArgumentException("Cart is empty");

        BigDecimal subtotal = BigDecimal.ZERO;
        List<Map<String, Object>> billLines = new ArrayList<>();
        for (CartItem c : cart) {
            subtotal = subtotal.add(c.getLineTotal());
            Map<String, Object> line = new LinkedHashMap<>();
            line.put("product", c.getProductName());
            line.put("qty", c.getQuantity());
            line.put("unitPrice", c.getProductPrice());
            line.put("lineTotal", c.getLineTotal());
            billLines.add(line);
        }
        subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);

        boolean upi = "UPI".equalsIgnoreCase(paymentMethod) || "ONLINE".equalsIgnoreCase(paymentMethod);
        BigDecimal pct = upi ? UPI_SERVICE_PCT : COD_SERVICE_PCT;
        BigDecimal service = subtotal.multiply(pct).setScale(2, RoundingMode.HALF_UP);
        if (service.compareTo(MIN_SERVICE) < 0) service = MIN_SERVICE;

        BigDecimal delivery = BigDecimal.ZERO;
        if (subtotal.compareTo(FREE_DELIVERY_MIN) < 0) {
            delivery = upi ? UPI_DELIVERY : COD_DELIVERY;
        }

        BigDecimal total = subtotal.add(service).add(delivery).setScale(2, RoundingMode.HALF_UP);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("items", billLines);
        result.put("subtotal", subtotal);
        result.put("serviceCharge", service);
        result.put("servicePercent", upi ? "1.5%" : "2%");
        result.put("deliveryCharge", delivery);
        result.put("freeDeliveryMin", FREE_DELIVERY_MIN);
        result.put("deliveryNote", delivery.compareTo(BigDecimal.ZERO) == 0
                ? "Free delivery (order ≥ ₹999)"
                : (upi ? "UPI delivery charge" : "COD delivery charge"));
        result.put("total", total);
        result.put("paymentMethod", upi ? "UPI" : "COD");
        return result;
    }

    public Map<String, Object> placeOrder(long buyerId, String paymentMethod) {
        Map<String, Object> charges = calculateCharges(buyerId, paymentMethod);
        List<CartItem> cart = cartDAO.findByUser(buyerId);
        List<OrderItem> items = new ArrayList<>();
        for (CartItem c : cart) {
            OrderItem oi = new OrderItem();
            oi.setProductId(c.getProductId());
            oi.setQuantity(c.getQuantity());
            oi.setUnitPrice(c.getProductPrice());
            items.add(oi);
        }
        String pm = (String) charges.get("paymentMethod");
        BigDecimal subtotal = (BigDecimal) charges.get("subtotal");
        BigDecimal service = (BigDecimal) charges.get("serviceCharge");
        BigDecimal delivery = (BigDecimal) charges.get("deliveryCharge");
        BigDecimal total = (BigDecimal) charges.get("total");

        long id = orderDAO.create(buyerId, subtotal, service, delivery, total, pm, items);
        cartDAO.clear(buyerId);

        charges.put("orderId", id);
        charges.put("message", "COD".equals(pm)
                ? "Order placed. Pay cash on delivery (includes service + delivery charges)."
                : "UPI payment successful. Order confirmed.");
        return charges;
    }

    public List<Order> getOrders(long buyerId) {
        return orderDAO.findByBuyer(buyerId);
    }

    public List<Map<String, Object>> getAllOrdersAdmin() {
        return orderDAO.findAllDetailed();
    }
}
