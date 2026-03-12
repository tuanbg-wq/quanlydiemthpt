<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head><title>Home</title></head>
<body>
<h2>Xin chào! Bạn đã đăng nhập.</h2>

<p><a href="<%=request.getContextPath()%>/logout">Đăng xuất</a></p>

<p>Test link:</p>
<ul>
  <li><a href="<%=request.getContextPath()%>/admin">/admin</a> (ROLE_Admin)</li>
  <li><a href="<%=request.getContextPath()%>/teacher">/teacher</a> (ROLE_Giao_vien)</li>
</ul>
</body>
</html>