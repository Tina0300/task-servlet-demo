package cn.edu.mju.band.servlet;

import cn.edu.mju.band.dal.ITaskDal;
import cn.edu.mju.band.dal.impl.TaskDal;
import cn.edu.mju.band.entity.Task;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.text.SimpleDateFormat;

@WebServlet("/taskAdd")
public class TaskAddServlet extends HttpServlet {
    private ITaskDal taskDal;

    @Override
    public void init() throws ServletException {
        taskDal = new TaskDal();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String op = request.getParameter("op");

        Task task = new Task();
        task.setTitle(request.getParameter("title"));
        task.setPriority(request.getParameter("priority"));
        task.setStatus(Byte.parseByte(request.getParameter("status")));

        // 截止日期可能为空，为空就不设置，避免 NPE
        String dueDateStr = request.getParameter("dueDate");
        if (dueDateStr != null && !dueDateStr.trim().isEmpty()) {
            try {
                task.setDueDate(new SimpleDateFormat("yyyy-MM-dd").parse(dueDateStr));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        int rows;
        if ("update".equals(op)) {
            // 编辑：带 id，更新
            task.setId(Integer.parseInt(request.getParameter("id")));
            rows = taskDal.update(task);
        } else {
            // 新增
            rows = taskDal.add(task);
        }
        System.out.println("=== taskAdd op=" + op + " rows=" + rows);

        // PRG：POST 后重定向，刷新不重复提交
        response.sendRedirect(request.getContextPath() + "/task?op=list");
    }
}
