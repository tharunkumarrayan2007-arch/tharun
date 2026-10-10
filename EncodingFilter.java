package com.onlinmart.onlinmart.filter;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import java.io.IOException;
@WebFilter("/*")
public class EncodingFilter implements Filter {
    public void doFilter(ServletRequest req,ServletResponse resp,FilterChain chain)throws IOException,ServletException{
        req.setCharacterEncoding("UTF-8"); resp.setCharacterEncoding("UTF-8"); chain.doFilter(req,resp);
    }
}
