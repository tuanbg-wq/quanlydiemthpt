<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>${pageTitle}</title>

  <link rel="stylesheet" href="<c:url value='/css/admin-layout.css'/>">
  <link rel="stylesheet" href="<c:url value='/css/score-list.css'/>">
</head>
<body>
<div class="layout">
  <jsp:include page="/WEB-INF/views/admin/_sidebar.jsp"/>

  <main class="main score-create-page">
    <section class="create-card">
      <h1>Thêm điểm số</h1>
      <p>Trang nhập điểm chi tiết đang được hoàn thiện theo luồng dữ liệu hiện tại.</p>
      <div class="create-actions">
        <a class="btn" href="<c:url value='/admin/score'/>">Quay lại danh sách</a>
      </div>
    </section>
  </main>
</div>
</body>
</html>
