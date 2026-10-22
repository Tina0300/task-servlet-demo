<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="cn.edu.mju.band.entity.Task" %>
<html>
<head>
    <title>新增/编辑任务</title>
</head>
<body>
<%
    Task task = (Task) request.getAttribute("task");
    String op = (task == null) ? "add" : "update";
    String title = (task == null) ? "" : task.getTitle();
    String priority = (task == null) ? "1" : task.getPriority();
    Byte status = (task == null) ? Byte.valueOf("0") : task.getStatus();
    String dueDate = (task == null || task.getDueDate() == null)
            ? "" : new java.text.SimpleDateFormat("yyyy-MM-dd").format(task.getDueDate());
%>
<h2><%= task == null ? "新增任务" : "编辑任务" %></h2>
<form action="${pageContext.request.contextPath}/taskAdd" method="post">
    <input type="hidden" name="op" value="<%=op%>">
    <% if (task != null) { %>
        <input type="hidden" name="id" value="<%=task.getId()%>">
    <% } %>

    <p>任务名称：<input type="text" name="title" value="<%=title%>"></p>

    <p>优先级别：
        <select name="priority">
            <option value="1" <%= "1".equals(priority) ? "selected" : "" %>>低</option>
            <option value="2" <%= "2".equals(priority) ? "selected" : "" %>>中</option>
            <option value="3" <%= "3".equals(priority) ? "selected" : "" %>>高</option>
        </select>
    </p>

    <p>状态：
        <select name="status">
            <option value="0" <%= status == 0 ? "selected" : "" %>>待办</option>
            <option value="1" <%= status == 1 ? "selected" : "" %>>进行中</option>
            <option value="2" <%= status == 2 ? "selected" : "" %>>已完成</option>
        </select>
    </p>

    <p>截止日期：<input type="date" name="dueDate" value="<%=dueDate%>"></p>

    <p><input type="submit" value="提交"></p>
</form>
<a href="${pageContext.request.contextPath}/task?op=list">返回列表</a>
</body>
</html>
