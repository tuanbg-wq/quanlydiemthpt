<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>${pageTitle}</title>
  <link rel="stylesheet" href="<c:url value='/css/admin-layout.css'/>">
</head>
<body>
<div class="layout">
  <jsp:include page="/WEB-INF/views/admin/_sidebar.jsp"/>

  <main class="main">
    <header class="topbar">
      <div class="topbar-left">
        <h1>Quản lý lớp</h1>
        <p>Trang quản lý lớp đã được thêm vào sidebar, có thể phát triển chức năng chi tiết tiếp theo.</p>
      </div>
    </header>

    <section class="content">
      <div class="card">
        <p>Danh sách lớp, phân công giáo viên chủ nhiệm và các thao tác lớp sẽ được triển khai tại đây.</p>
      </div>
    </section>
  </main>
</div>
</body>
</html>
