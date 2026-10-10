package com.onlinmart.onlinmart.dao;
import com.onlinmart.onlinmart.model.Review;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.*;
import java.util.*;
public class ReviewDAO {
    private final HikariDataSource ds;
    public ReviewDAO(HikariDataSource ds){this.ds=ds;}
    public List<Review> findByProduct(long productId){
        List<Review> list=new ArrayList<>();
        try(Connection c=ds.getConnection();PreparedStatement ps=c.prepareStatement(
            "SELECT r.*,u.name AS user_name FROM reviews r JOIN users u ON r.user_id=u.id WHERE r.product_id=? ORDER BY r.created_at DESC")){
            ps.setLong(1,productId);ResultSet rs=ps.executeQuery();
            while(rs.next()){Review r=new Review();r.setId(rs.getLong("id"));r.setProductId(rs.getLong("product_id"));
                r.setUserId(rs.getLong("user_id"));r.setRating(rs.getInt("rating"));r.setComment(rs.getString("comment"));
                r.setUserName(rs.getString("user_name"));
                Timestamp t=rs.getTimestamp("created_at");if(t!=null)r.setCreatedAt(t.toLocalDateTime());list.add(r);}
        }catch(Exception e){e.printStackTrace();}
        return list;
    }
    public Review create(Review r){
        try(Connection c=ds.getConnection();PreparedStatement ps=c.prepareStatement(
            "INSERT INTO reviews(product_id,user_id,rating,comment) VALUES(?,?,?,?)",Statement.RETURN_GENERATED_KEYS)){
            ps.setLong(1,r.getProductId());ps.setLong(2,r.getUserId());ps.setInt(3,r.getRating());ps.setString(4,r.getComment());
            ps.executeUpdate();ResultSet k=ps.getGeneratedKeys();if(k.next())r.setId(k.getLong(1));return r;
        }catch(Exception e){throw new RuntimeException(e);}
    }
}
