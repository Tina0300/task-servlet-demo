package cn.edu.mju.band.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 环节五：把原先的 AuthFilter（Servlet 过滤器）改为 Spring MVC 拦截器。
 * 拦截器在 DispatcherServlet 之内执行，能拿到 HandlerMethod，也能复用 Spring 的异常处理链；
 * 过滤器在 Servlet 容器层执行，粒度更粗、无法区分控制器方法。
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    /** 登录成功后写入的会话标记，与 LoginController 保持一致 */
    public static final String SESSION_FLAG = "flag";
    public static final String SESSION_USER_ID = "userId";
    public static final String SESSION_LOGIN_USER = "loginUser";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        HttpSession session = request.getSession(false);
        boolean logged = session != null && Boolean.TRUE.equals(session.getAttribute(SESSION_FLAG));
        if (logged) {
            // 返回 true 才会继续执行 Controller
            return true;
        }
        // 未登录：直接重定向到登录页，并返回 false 截断请求
        response.sendRedirect(request.getContextPath() + "/login");
        return false;
    }
}
