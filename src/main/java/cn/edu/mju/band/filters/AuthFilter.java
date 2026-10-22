package cn.edu.mju.band.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebFilter({"/task", "/taskAdd", "/taskEdit", "/taskDelete"})
public class AuthFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        // 登录请求放行
        String path = req.getRequestURI().substring(req.getContextPath().length());
        if (path.equals("/login") || path.equals("/login.jsp")) {
            chain.doFilter(request, response);
            return;
        }

        // 其他任务路径必须登录
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("loginUser") != null) {
            chain.doFilter(request, response);
        } else {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
        }
    }
}
