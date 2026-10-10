package com.onlinmart.onlinmart.controller;
import com.google.gson.*;
import com.onlinmart.onlinmart.model.User;
import com.onlinmart.onlinmart.service.CartService;
import com.onlinmart.onlinmart.util.JsonUtil;
import com.zaxxer.hikari.HikariDataSource;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
@WebServlet("/api/cart/*")
public class CartServlet extends HttpServlet {
    private CartService service;
    public void init(){service=new CartService((HikariDataSource)getServletContext().getAttribute("dataSource"));}
    protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws IOException{
        User u=user(req); if(u==null){JsonUtil.writeError(resp,401,"UNAUTHORIZED","Login first");return;}
        Map<String,Object> d=new HashMap<>(); d.put("items",service.getCart(u.getId())); d.put("total",service.getTotal(u.getId()));
        JsonUtil.writeSuccess(resp,d);
    }
    protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws IOException{
        User u=user(req); if(u==null){JsonUtil.writeError(resp,401,"UNAUTHORIZED","Login first");return;}
        try{JsonObject j=JsonParser.parseString(read(req)).getAsJsonObject();
            service.addToCart(u.getId(),j.get("productId").getAsLong(),j.has("quantity")?j.get("quantity").getAsInt():1);
            JsonUtil.writeSuccess(resp,"Added");
        }catch(Exception e){JsonUtil.writeError(resp,400,"BAD_REQUEST",e.getMessage());}
    }
    protected void doDelete(HttpServletRequest req,HttpServletResponse resp)throws IOException{
        User u=user(req); if(u==null){JsonUtil.writeError(resp,401,"UNAUTHORIZED","Login first");return;}
        String path=req.getPathInfo();
        if(path!=null&&path.length()>1) service.removeItem(Long.parseLong(path.substring(1)),u.getId());
        else service.clearCart(u.getId());
        JsonUtil.writeSuccess(resp,"OK");
    }
    private User user(HttpServletRequest req){HttpSession s=req.getSession(false);return s!=null?(User)s.getAttribute("user"):null;}
    private String read(HttpServletRequest req)throws IOException{StringBuilder sb=new StringBuilder();try(BufferedReader r=req.getReader()){String l;while((l=r.readLine())!=null)sb.append(l);}return sb.toString();}
}
