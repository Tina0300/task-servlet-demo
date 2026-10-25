package cn.edu.mju.band.filters;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * 登录鉴权过滤器（web.xml 中显式注册，拦截 /task、/task/*，放行 /login、/login.jsp）
 */
public class AuthFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String path = req.getRequestURI().substring(req.getContextPath().length());

        // 登录页与登录提交接口直接放行
        if ("/login".equals(path) || "/login.jsp".equals(path)) {
            chain.doFilter(request, response);
            return;
        }

        // 其余业务请求必须登录
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("loginUser") != null) {
            chain.doFilter(request, response);
        } else {
            resp.sendRedirect(req.getContextPath() + "/login");
        }
    }
}
