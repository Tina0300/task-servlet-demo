<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>登录</title>
</head>
<body>
<h2>任务管理系统登录</h2>
<% if (request.getAttribute("msg") != null) { %>
    <p style="color:red"><%= request.getAttribute("msg") %></p>
<% } %>
<form action="${pageContext.request.contextPath}/login" method="post">
    <p>用户名：<input type="text" name="loginName"></p>
    <p>密码：<input type="password" name="loginPwd"></p>
    <p><input type="submit" value="登录"></p>
</form>
</body>
</html>
