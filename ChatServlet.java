package com.onlinmart.onlinmart.controller;

import com.google.gson.*;
import com.onlinmart.onlinmart.model.Product;
import com.onlinmart.onlinmart.service.ProductService;
import com.onlinmart.onlinmart.util.JsonUtil;
import com.zaxxer.hikari.HikariDataSource;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@WebServlet("/api/chat")
public class ChatServlet extends HttpServlet {
    private ProductService productService;

    public void init() {
        productService = new ProductService((HikariDataSource) getServletContext().getAttribute("dataSource"));
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            JsonObject j = JsonParser.parseString(read(req)).getAsJsonObject();
            String msg = j.has("message") ? j.get("message").getAsString().toLowerCase().trim() : "";
            Map<String, String> d = new HashMap<>();
            d.put("reply", answer(msg));
            d.put("assistant", "TM Assistant");
            JsonUtil.writeSuccess(resp, d);
        } catch (Exception e) {
            Map<String, String> d = new HashMap<>();
            d.put("reply", "TM Assistant: Something went wrong. Please try again.");
            d.put("assistant", "TM Assistant");
            JsonUtil.writeSuccess(resp, d);
        }
    }

    private String answer(String msg) {
        List<Product> products = productService.searchProducts(null, null);
        String prefix = "";

        if (msg.isEmpty())
            return "TM Assistant here. Ask about products, prices, COD/UPI charges, cart, or seller help.";

        if (containsAny(msg, "hello", "hi", "hey", "vanakkam", "namaste", "who are you", "your name"))
            return "I'm **TM Assistant** (Tharun Mart Support). I help with products, prices, delivery charges, COD/UPI, cart and orders.";

        if (containsAny(msg, "thank", "thanks", "nandri"))
            return "You're welcome! — TM Assistant";

        if (containsAny(msg, "charge", "delivery", "service charge", "shipping", "extra fee"))
            return "Charges: Subtotal + Service charge (COD 2% / UPI 1.5%, min ₹10) + Delivery (COD ₹49 / UPI ₹29). Free delivery on orders ≥ ₹999.";

        if (containsAny(msg, "cod", "cash on delivery"))
            return "COD: Pay when delivered. Service 2% (min ₹10) + delivery ₹49 (free if cart ≥ ₹999). Choose COD at checkout.";

        if (containsAny(msg, "upi", "online payment", "pay online"))
            return "UPI: Enter UPI ID (name@upi) + PIN at checkout. Service 1.5% (min ₹10) + delivery ₹29 (free if ≥ ₹999). You get a full bill after payment.";

        if (containsAny(msg, "how many", "total product", "product count"))
            return products.isEmpty() ? "No products listed yet." : "We have " + products.size() + " products in the catalog.";

        if (containsAny(msg, "list product", "all product", "show product", "what products", "catalog")) {
            if (products.isEmpty()) return "Catalog is empty. Sellers can add products from Seller Dashboard.";
            return "Catalog: " + products.stream()
                    .map(p -> p.getName() + " ₹" + p.getPrice() + " [" + nz(p.getCategory()) + "]")
                    .collect(Collectors.joining("; "));
        }

        if (containsAny(msg, "electronic", "gadget", "headphone", "watch", "earbud", "airdope")) {
            List<Product> el = products.stream().filter(p ->
                    (p.getCategory() != null && p.getCategory().toLowerCase().contains("electronic"))
                            || (p.getName() != null && (p.getName().toLowerCase().contains("headphone")
                            || p.getName().toLowerCase().contains("watch")
                            || p.getName().toLowerCase().contains("earbud")
                            || p.getName().toLowerCase().contains("airdope")))
            ).collect(Collectors.toList());
            if (el.isEmpty()) return "No electronics found right now.";
            return "Electronics: " + el.stream()
                    .map(p -> p.getName() + " ₹" + p.getPrice() + " (stock " + p.getStockQty() + ")")
                    .collect(Collectors.joining("; "));
        }

        if (containsAny(msg, "fashion", "cloth", "shirt", "shoe", "t-shirt", "tshirt")) {
            List<Product> f = filterCat(products, "fashion");
            if (f.isEmpty()) return "No fashion items right now.";
            return "Fashion: " + f.stream().map(p -> p.getName() + " ₹" + p.getPrice()).collect(Collectors.joining("; "));
        }

        if (containsAny(msg, "cheap", "lowest", "budget", "low price")) {
            return products.stream().min(Comparator.comparing(Product::getPrice))
                    .map(p -> "Cheapest: " + p.getName() + " at ₹" + p.getPrice())
                    .orElse("No products available.");
        }

        if (containsAny(msg, "expensive", "highest", "premium", "costly")) {
            return products.stream().max(Comparator.comparing(Product::getPrice))
                    .map(p -> "Most expensive: " + p.getName() + " at ₹" + p.getPrice())
                    .orElse("No products available.");
        }

        for (Product p : products) {
            if (p.getName() != null && msg.contains(p.getName().toLowerCase())) {
                return p.getName() + " — ₹" + p.getPrice() + " | Stock: " + p.getStockQty()
                        + " | " + nz(p.getCategory())
                        + (p.getDescription() != null ? " | " + p.getDescription() : "");
            }
        }
        for (Product p : products) {
            if (p.getName() == null) continue;
            for (String w : p.getName().toLowerCase().split("\\s+")) {
                if (w.length() > 3 && msg.contains(w)) {
                    return "Found " + p.getName() + " for ₹" + p.getPrice() + " (stock " + p.getStockQty() + "). Open product page to buy.";
                }
            }
        }

        if (containsAny(msg, "price", "cost", "rate", "how much")) {
            if (products.isEmpty()) return "No products to show prices.";
            return "Prices: " + products.stream().limit(6)
                    .map(p -> p.getName() + " ₹" + p.getPrice()).collect(Collectors.joining("; "));
        }

        if (containsAny(msg, "cart", "add to cart"))
            return "Login as Buyer → Shop → Add to Cart → Cart → Checkout. Only buyers can use cart.";

        if (containsAny(msg, "checkout", "order", "buy", "purchase", "bill"))
            return "Checkout shows subtotal + service charge + delivery, then grand total. COD or UPI. Full bill after order.";

        if (containsAny(msg, "seller", "sell", "dashboard"))
            return "Register as Seller → Seller Dashboard → add/edit/delete your products.";

        if (containsAny(msg, "admin"))
            return "Admin: admin@tharunmart.com / password123 → Admin Panel to monitor users, orders and products.";

        if (containsAny(msg, "review", "rating", "star"))
            return "Buyers can rate products 1–5 stars on the product page. Sellers can only view reviews.";

        if (containsAny(msg, "help", "what can", "options", "menu", "ps assistant"))
            return "TM Assistant can help with: product list, electronics, fashion, cheapest, prices, cart, COD/UPI charges, delivery, seller, admin.";

        if (products.isEmpty())
            return "Catalog is empty. Register as Seller to add products.";

        return "TM Assistant: Try \"list products\", \"electronics\", \"delivery charge\", \"COD\", \"UPI\", or a product name.";
    }

    private String nz(String s) { return s != null ? s : "General"; }

    private boolean containsAny(String msg, String... keys) {
        for (String k : keys) if (msg.contains(k)) return true;
        return false;
    }

    private List<Product> filterCat(List<Product> products, String catPart) {
        return products.stream()
                .filter(p -> p.getCategory() != null && p.getCategory().toLowerCase().contains(catPart))
                .collect(Collectors.toList());
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
