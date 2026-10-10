package com.onlinmart.onlinmart.dao;
import com.onlinmart.onlinmart.model.CartItem;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.*;
import java.util.*;
public class CartDAO {
    private final HikariDataSource ds;
    public CartDAO(HikariDataSource ds){this.ds=ds;}
    public List<CartItem> findByUser(long userId){
        List<CartItem> list=new ArrayList<>();
        try(Connection c=ds.getConnection();PreparedStatement ps=c.prepareStatement(
            "SELECT c.*,p.name,p.image_url,p.price,p.stock_qty FROM cart_items c JOIN products p ON c.product_id=p.id WHERE c.user_id=?")){
            ps.setLong(1,userId);ResultSet rs=ps.executeQuery();
            while(rs.next()){CartItem i=new CartItem();i.setId(rs.getLong("id"));i.setUserId(rs.getLong("user_id"));
                i.setProductId(rs.getLong("product_id"));i.setQuantity(rs.getInt("quantity"));
                i.setProductName(rs.getString("name"));i.setProductImage(rs.getString("image_url"));
                i.setProductPrice(rs.getBigDecimal("price"));i.setStockQty(rs.getInt("stock_qty"));list.add(i);}
        }catch(Exception e){e.printStackTrace();}
        return list;
    }
    public void addOrUpdate(long userId,long productId,int qty){
        try(Connection c=ds.getConnection()){
            PreparedStatement check=c.prepareStatement("SELECT id,quantity FROM cart_items WHERE user_id=? AND product_id=?");
            check.setLong(1,userId);check.setLong(2,productId);ResultSet rs=check.executeQuery();
            if(rs.next()){PreparedStatement u=c.prepareStatement("UPDATE cart_items SET quantity=? WHERE id=?");
                u.setInt(1,rs.getInt("quantity")+qty);u.setLong(2,rs.getLong("id"));u.executeUpdate();}
            else{PreparedStatement i=c.prepareStatement("INSERT INTO cart_items(user_id,product_id,quantity) VALUES(?,?,?)");
                i.setLong(1,userId);i.setLong(2,productId);i.setInt(3,qty);i.executeUpdate();}
        }catch(Exception e){throw new RuntimeException(e);}
    }
    public void remove(long id,long userId){
        try(Connection c=ds.getConnection();PreparedStatement ps=c.prepareStatement("DELETE FROM cart_items WHERE id=? AND user_id=?")){
            ps.setLong(1,id);ps.setLong(2,userId);ps.executeUpdate();
        }catch(Exception e){e.printStackTrace();}
    }
    public void clear(long userId){
        try(Connection c=ds.getConnection();PreparedStatement ps=c.prepareStatement("DELETE FROM cart_items WHERE user_id=?")){
            ps.setLong(1,userId);ps.executeUpdate();
        }catch(Exception e){e.printStackTrace();}
    }
}
