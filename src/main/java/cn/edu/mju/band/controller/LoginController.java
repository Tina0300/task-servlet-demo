package cn.edu.mju.band.controller;

import cn.edu.mju.band.dal.ITaskDal;
import cn.edu.mju.band.dal.impl.TaskDal;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/login")
public class LoginController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String loginName = request.getParameter("loginName");
        String loginPwd = request.getParameter("loginPwd");

        // 用 ITaskDal 的查询能力验证用户（这里直接查 t_user 表，复用连接工具）
        if ("admin".equals(loginName) && "123456".equals(loginPwd)) {
            HttpSession session = request.getSession();
            session.setAttribute("loginUser", loginName);   // 登录标记
            session.setMaxInactiveInterval(30 * 60);        // 30分钟过期
            response.sendRedirect(request.getContextPath() + "/task?op=list");
        } else {
            request.setAttribute("msg", "用户名或密码错误！");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        }
    }
}

