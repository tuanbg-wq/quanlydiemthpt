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

  <main class="main score-detail-page">
    <section class="detail-card">
      <h1>Chi tiết điểm</h1>
      <p class="detail-subtitle">
        Học sinh <strong>${summary.studentName}</strong> (${summary.studentId})
        • Lớp ${summary.className}
        • Môn ${summary.subjectName}
        • Năm học ${summary.namHoc}
      </p>

      <div class="detail-table-wrap">
        <table class="table">
          <thead>
          <tr>
            <th>ID điểm</th>
            <th>Học kỳ</th>
            <th>Loại điểm</th>
            <th>Điểm</th>
            <th>Ngày nhập</th>
            <th>Ghi chú</th>
          </tr>
          </thead>
          <tbody>
          <c:forEach var="entry" items="${entries}">
            <tr>
              <td>${entry.scoreId}</td>
              <td>${entry.hocKyDisplay}</td>
              <td>${entry.scoreTypeName}</td>
              <td><span class="total-badge">${entry.scoreValueDisplay}</span></td>
              <td>${entry.ngayNhap}</td>
              <td>${entry.ghiChu}</td>
            </tr>
          </c:forEach>
          <c:if test="${empty entries}">
            <tr>
              <td colspan="6" class="empty-message">Không có bản ghi điểm trong nhóm dữ liệu này.</td>
            </tr>
          </c:if>
          </tbody>
        </table>
      </div>

      <div class="detail-actions">
        <c:url var="editUrl" value="/admin/score/edit">
          <c:param name="studentId" value="${summary.studentId}"/>
          <c:param name="subjectId" value="${summary.subjectId}"/>
          <c:param name="namHoc" value="${summary.namHoc}"/>
        </c:url>
        <a class="btn" href="<c:url value='/admin/score'/>">Quay lại danh sách</a>
        <a class="btn primary" href="${editUrl}">Chỉnh sửa điểm</a>
      </div>
    </section>
  </main>
</div>
</body>
</html>
