package com.onlinmart.onlinmart.controller;
import com.google.gson.*;
import com.onlinmart.onlinmart.model.User;
import com.onlinmart.onlinmart.service.ReviewService;
import com.onlinmart.onlinmart.util.JsonUtil;
import com.zaxxer.hikari.HikariDataSource;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
@WebServlet("/api/reviews/*")
public class ReviewServlet extends HttpServlet {
    private ReviewService service;
    public void init(){service=new ReviewService((HikariDataSource)getServletContext().getAttribute("dataSource"));}
    protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws IOException{
        try{long id=Long.parseLong(req.getPathInfo().substring(1));
            JsonUtil.writeSuccess(resp,service.getByProduct(id));
        }catch(Exception e){JsonUtil.writeError(resp,400,"BAD_REQUEST","Invalid product id");}
    }
    protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws IOException{
        User u=user(req); if(u==null){JsonUtil.writeError(resp,401,"UNAUTHORIZED","Login to review");return;}
        try{JsonObject j=JsonParser.parseString(read(req)).getAsJsonObject();
            JsonUtil.writeSuccess(resp,service.add(j.get("productId").getAsLong(),u.getId(),j.get("rating").getAsInt(),
                j.has("comment")?j.get("comment").getAsString():""));
        }catch(IllegalArgumentException e){JsonUtil.writeError(resp,400,"VALIDATION_ERROR",e.getMessage());}
        catch(Exception e){JsonUtil.writeError(resp,500,"SERVER_ERROR",e.getMessage());}
    }
    private User user(HttpServletRequest req){HttpSession s=req.getSession(false);return s!=null?(User)s.getAttribute("user"):null;}
    private String read(HttpServletRequest req)throws IOException{StringBuilder sb=new StringBuilder();try(BufferedReader r=req.getReader()){String l;while((l=r.readLine())!=null)sb.append(l);}return sb.toString();}
}
