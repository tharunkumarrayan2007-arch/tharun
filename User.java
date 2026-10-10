package com.onlinmart.onlinmart.model;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
public class User {
    private long id;
    private String name, email, passwordHash, role, description, category, imageUrl, status, comment, productName, productImage, sellerName, userName, buyerName;
    private long sellerId, buyerId, userId, productId, orderId;
    private BigDecimal price, totalAmount, unitPrice, productPrice;
    private int stockQty, quantity, rating;
    private LocalDateTime createdAt;
    private List<OrderItem> items;
    public long getId(){return id;} public void setId(long v){id=v;}
    public String getName(){return name;} public void setName(String v){name=v;}
    public String getEmail(){return email;} public void setEmail(String v){email=v;}
    public String getPasswordHash(){return passwordHash;} public void setPasswordHash(String v){passwordHash=v;}
    public String getRole(){return role;} public void setRole(String v){role=v;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;}
    public long getSellerId(){return sellerId;} public void setSellerId(long v){sellerId=v;}
    public String getDescription(){return description;} public void setDescription(String v){description=v;}
    public BigDecimal getPrice(){return price;} public void setPrice(BigDecimal v){price=v;}
    public int getStockQty(){return stockQty;} public void setStockQty(int v){stockQty=v;}
    public String getCategory(){return category;} public void setCategory(String v){category=v;}
    public String getImageUrl(){return imageUrl;} public void setImageUrl(String v){imageUrl=v;}
    public String getSellerName(){return sellerName;} public void setSellerName(String v){sellerName=v;}
    public long getBuyerId(){return buyerId;} public void setBuyerId(long v){buyerId=v;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;}
    public BigDecimal getTotalAmount(){return totalAmount;} public void setTotalAmount(BigDecimal v){totalAmount=v;}
    public String getBuyerName(){return buyerName;} public void setBuyerName(String v){buyerName=v;}
    public List getItems(){return items;} public void setItems(List v){items=v;}
    public long getOrderId(){return orderId;} public void setOrderId(long v){orderId=v;}
    public long getProductId(){return productId;} public void setProductId(long v){productId=v;}
    public int getQuantity(){return quantity;} public void setQuantity(int v){quantity=v;}
    public BigDecimal getUnitPrice(){return unitPrice;} public void setUnitPrice(BigDecimal v){unitPrice=v;}
    public String getProductName(){return productName;} public void setProductName(String v){productName=v;}
    public String getProductImage(){return productImage;} public void setProductImage(String v){productImage=v;}
    public long getUserId(){return userId;} public void setUserId(long v){userId=v;}
    public BigDecimal getProductPrice(){return productPrice;} public void setProductPrice(BigDecimal v){productPrice=v;}
    public BigDecimal getLineTotal(){ if(productPrice==null) return BigDecimal.ZERO; return productPrice.multiply(BigDecimal.valueOf(quantity)); }
    public int getRating(){return rating;} public void setRating(int v){rating=v;}
    public String getComment(){return comment;} public void setComment(String v){comment=v;}
    public String getUserName(){return userName;} public void setUserName(String v){userName=v;}
}
