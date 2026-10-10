package com.onlinmart.onlinmart.service;
import com.onlinmart.onlinmart.dao.CartDAO;
import com.onlinmart.onlinmart.model.CartItem;
import com.zaxxer.hikari.HikariDataSource;
import java.math.BigDecimal;
import java.util.List;
public class CartService {
    private final CartDAO dao;
    public CartService(HikariDataSource ds){dao=new CartDAO(ds);}
    public List<CartItem> getCart(long uid){return dao.findByUser(uid);}
    public void addToCart(long uid,long pid,int q){dao.addOrUpdate(uid,pid,q<=0?1:q);}
    public void removeItem(long id,long uid){dao.remove(id,uid);}
    public void clearCart(long uid){dao.clear(uid);}
    public BigDecimal getTotal(long uid){
        BigDecimal t=BigDecimal.ZERO;
        for(CartItem i:dao.findByUser(uid)) t=t.add(i.getLineTotal());
        return t;
    }
}
