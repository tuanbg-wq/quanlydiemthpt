<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>${pageTitle}</title>

  <link rel="stylesheet" href="<c:url value='/css/admin-layout.css'/>">
  <link rel="stylesheet" href="<c:url value='/css/admin/score-list.css'/>">
</head>
<body>
<div class="layout">
  <jsp:include page="/WEB-INF/views/admin/_sidebar.jsp"/>

  <main class="main score-edit-page">
    <section class="detail-card">
      <h1>Chá»‰nh sá»­a Ä‘iá»ƒm</h1>
      <p class="detail-subtitle">
        Há»c sinh <strong>${summary.studentName}</strong> (${summary.studentId})
        â€¢ Lá»›p ${summary.className}
        â€¢ MĂ´n ${summary.subjectName}
        â€¢ NÄƒm há»c ${summary.namHoc}
      </p>

      <form method="post" action="<c:url value='/admin/score/edit'/>">
        <input type="hidden" name="studentId" value="${summary.studentId}">
        <input type="hidden" name="subjectId" value="${summary.subjectId}">
        <input type="hidden" name="namHoc" value="${summary.namHoc}">

        <div class="detail-table-wrap">
          <table class="table">
            <thead>
            <tr>
              <th>ID Ä‘iá»ƒm</th>
              <th>Há»c ká»³</th>
              <th>Loáº¡i Ä‘iá»ƒm</th>
              <th>Äiá»ƒm</th>
              <th>Ghi chĂº</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="entry" items="${entries}">
              <tr>
                <td>
                  ${entry.scoreId}
                  <input type="hidden" name="scoreId" value="${entry.scoreId}">
                </td>
                <td>${entry.hocKyDisplay}</td>
                <td>${entry.scoreTypeName}</td>
                <td>
                  <input class="score-input"
                         type="number"
                         name="scoreValue"
                         min="0"
                         max="10"
                         step="0.01"
                         value="${entry.scoreValueDisplay}"
                         required>
                </td>
                <td>
                  <input class="note-input" type="text" name="scoreNote" value="${entry.ghiChu}" placeholder="Ghi chĂº...">
                </td>
              </tr>
            </c:forEach>
            <c:if test="${empty entries}">
              <tr>
                <td colspan="5" class="empty-message">KhĂ´ng cĂ³ báº£n ghi Ä‘iá»ƒm Ä‘á»ƒ chá»‰nh sá»­a.</td>
              </tr>
            </c:if>
            </tbody>
          </table>
        </div>

        <div class="detail-actions">
          <c:url var="detailUrl" value="/admin/score/detail">
            <c:param name="studentId" value="${summary.studentId}"/>
            <c:param name="subjectId" value="${summary.subjectId}"/>
            <c:param name="namHoc" value="${summary.namHoc}"/>
          </c:url>
          <a class="btn" href="${detailUrl}">Há»§y</a>
          <button class="btn primary" type="submit">LÆ°u thay Ä‘á»•i</button>
        </div>
      </form>
    </section>
  </main>
</div>
</body>
</html>

