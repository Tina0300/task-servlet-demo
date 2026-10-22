<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="cn.edu.mju.band.entity.Task" %>
<%@ page import="java.util.List" %>
<html>
<head>
    <title>任务列表</title>
    <style>
        body {
            font-family: "Microsoft YaHei", "PingFang SC", Arial, sans-serif;
            background: #f5f6fa;
            margin: 0;
            padding: 40px 20px;
        }
        .container {
            width: 90%;
            max-width: 1200px;
            margin: 0 auto;
            background: #fff;
            border-radius: 12px;
            padding: 40px 50px;
            box-shadow: 0 4px 20px rgba(0,0,0,0.08);
        }
        h2 {
            text-align: center;
            color: #333;
            margin-bottom: 25px;
        }
        .add-link {
            display: inline-block;
            margin-bottom: 20px;
            background: #4a7dff;
            color: #fff !important;
            text-decoration: none;
            padding: 10px 24px;
            border-radius: 6px;
            font-size: 15px;
        }
        .add-link:hover {
            background: #3a66db;
        }
        table {
            width: 100%;
            border-collapse: collapse;
            font-size: 15px;
        }
        th, td {
            padding: 14px 18px;
            text-align: center;
            border: 1px solid #e3e6ef;
        }
        th {
            background: #f0f3fb;
            color: #333;
            font-weight: bold;
        }
        tr:nth-child(even) {
            background: #fafbfe;
        }
        tr:hover {
            background: #f0f4ff;
        }
        .task-title {
            text-align: left;
        }
        a {
            color: #4a7dff;
            text-decoration: none;
            margin: 0 6px;
        }
        a:hover {
            text-decoration: underline;
        }
        .empty {
            text-align: center;
            color: #999;
            padding: 40px;
        }
    </style>
</head>
<body>
<div class="container">
    <h2>任务列表</h2>
    <a class="add-link" href="${pageContext.request.contextPath}/add.jsp">＋ 新增任务</a>
    <table>
        <tr>
            <th>ID</th>
            <th>任务名称</th>
            <th>优先级别</th>
            <th>状态</th>
            <th>截止日期</th>
            <th>创建时间</th>
            <th>操作</th>
        </tr>
        <%
            List<Task> taskList = (List<Task>) request.getAttribute("taskList");
            if (taskList != null && !taskList.isEmpty()) {
                for (Task item : taskList) {
                    // 优先级数字转文字：1低 2中 3高
                    String p = item.getPriority();
                    String prio = "低";
                    if (p != null) {
                        if ("2".equals(p.trim())) prio = "中";
                        else if ("3".equals(p.trim())) prio = "高";
                    }
                    // 创建时间格式化为 yyyy-MM-dd
                    String createDate = (item.getCreateDate() == null) ? ""
                            : new java.text.SimpleDateFormat("yyyy-MM-dd").format(item.getCreateDate());
        %>
        <tr>
            <td><%=item.getId()%></td>
            <td class="task-title"><%=item.getTitle()%></td>
            <td><%=prio%></td>
            <td><%=item.getStatus()%></td>
            <td><%=item.getDueDate()%></td>
            <td><%=createDate%></td>
            <td>
                <a href="${pageContext.request.contextPath}/task?op=toEdit&id=<%=item.getId()%>">编辑</a>
                <a href="${pageContext.request.contextPath}/task?op=delete&id=<%=item.getId()%>" onclick="return confirm('确认删除？')">删除</a>
            </td>
        </tr>
        <%
                }
            } else {
        %>
        <tr><td colspan="7" class="empty">暂无任务数据</td></tr>
        <%
            }
        %>
    </table>
</div>
</body>
</html>
