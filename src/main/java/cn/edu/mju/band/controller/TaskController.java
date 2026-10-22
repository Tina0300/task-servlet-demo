package cn.edu.mju.band.controller;

import cn.edu.mju.band.buss.impl.TaskBuss;
import cn.edu.mju.band.entity.Task;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

@WebServlet("/task")
public class TaskController extends HttpServlet {
    private TaskBuss taskBuss;

    @Override
    public void init() throws ServletException {
        taskBuss = new TaskBuss();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String op = request.getParameter("op");
        if (op == null || "list".equals(op)) {
            List<Task> taskList = taskBuss.list();
            request.setAttribute("taskList", taskList);
            request.getRequestDispatcher("/list.jsp").forward(request, response);
        } else if ("toEdit".equals(op)) {
            Integer id = Integer.parseInt(request.getParameter("id"));
            Task task = taskBuss.getById(id);
            request.setAttribute("task", task);
            request.getRequestDispatcher("/add.jsp").forward(request, response);
        } else if ("delete".equals(op)) {
            Integer id = Integer.parseInt(request.getParameter("id"));
            taskBuss.delete(id);
            response.sendRedirect(request.getContextPath() + "/task?op=list");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String op = request.getParameter("op");
        if ("add".equals(op)) {
            Task task = new Task();
            task.setTitle(request.getParameter("title"));
            task.setPriority(request.getParameter("priority"));
            task.setStatus(Byte.parseByte(request.getParameter("status")));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            try {
                task.setDueDate(sdf.parse(request.getParameter("dueDate")));
            } catch (Exception e) {
                e.printStackTrace();
            }
            taskBuss.add(task);
            response.sendRedirect(request.getContextPath() + "/task?op=list");
        } else if ("update".equals(op)) {
            Task task = new Task();
            task.setId(Integer.parseInt(request.getParameter("id")));
            task.setTitle(request.getParameter("title"));
            task.setPriority(request.getParameter("priority"));
            task.setStatus(Byte.parseByte(request.getParameter("status")));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            try {
                task.setDueDate(sdf.parse(request.getParameter("dueDate")));
            } catch (Exception e) {
                e.printStackTrace();
            }
            taskBuss.update(task);
            response.sendRedirect(request.getContextPath() + "/task?op=list");
        }
    }
}
