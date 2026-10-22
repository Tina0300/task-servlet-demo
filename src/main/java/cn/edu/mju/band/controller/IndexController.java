package cn.edu.mju.band.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * 首页控制器：访问 /index 时根据登录状态跳转
 */
@WebServlet("/index")
public class IndexController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        // 已登录 → 直接进任务列表
        if (session != null && session.getAttribute("loginUser") != null) {
            response.sendRedirect(request.getContextPath() + "/task?op=list");
        } else {
            // 未登录 → 去登录页
            response.sendRedirect(request.getContextPath() + "/login.jsp");
        }
    }
}
