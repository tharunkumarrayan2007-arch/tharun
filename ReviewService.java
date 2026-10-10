package com.onlinmart.onlinmart.service;
import com.onlinmart.onlinmart.dao.ReviewDAO;
import com.onlinmart.onlinmart.model.Review;
import com.zaxxer.hikari.HikariDataSource;
import java.util.List;
public class ReviewService {
    private final ReviewDAO dao;
    public ReviewService(HikariDataSource ds){dao=new ReviewDAO(ds);}
    public List<Review> getByProduct(long id){return dao.findByProduct(id);}
    public Review add(long productId,long userId,int rating,String comment){
        if(rating<1||rating>5) throw new IllegalArgumentException("Rating 1-5");
        Review r=new Review();r.setProductId(productId);r.setUserId(userId);r.setRating(rating);r.setComment(comment);
        return dao.create(r);
    }
}
