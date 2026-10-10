package com.onlinmart.onlinmart.filter;
import com.onlinmart.onlinmart.model.User;
import com.onlinmart.onlinmart.util.JsonUtil;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.*;
import java.io.IOException;
@WebFilter("/api/*")
public class AuthFilter implements Filter {
    public void doFilter(ServletRequest request,ServletResponse response,FilterChain chain)throws IOException,ServletException{
        HttpServletRequest req=(HttpServletRequest)request;
        HttpServletResponse resp=(HttpServletResponse)response;
        String path=req.getRequestURI().substring(req.getContextPath().length());
        String method=req.getMethod();
        if(path.startsWith("/api/auth/") ||
           (path.startsWith("/api/products") && "GET".equals(method)) ||
           (path.startsWith("/api/reviews") && "GET".equals(method)) ||
           path.equals("/api/chat")){
            chain.doFilter(request,response); return;
        }
        HttpSession s=req.getSession(false);
        User u=s!=null?(User)s.getAttribute("user"):null;
        if(u==null){JsonUtil.writeError(resp,401,"UNAUTHORIZED","Please login first");return;}
        chain.doFilter(request,response);
    }
}
