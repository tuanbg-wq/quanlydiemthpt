<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>${pageTitle}</title>
  <link rel="stylesheet" href="<c:url value='/css/admin-layout.css'/>">
  <link rel="stylesheet" href="<c:url value='/css/admin/subject-info.css'/>">
</head>
<body>
<div class="layout">
  <jsp:include page="/WEB-INF/views/admin/_sidebar.jsp"/>

  <main class="main subject-info-page">
    <header class="topbar">
      <div class="topbar-left">
        <h1>ThĂ´ng tin mĂ´n há»c</h1>
        <p>Xem Ä‘áº§y Ä‘á»§ thĂ´ng tin mĂ´n há»c vĂ  danh sĂ¡ch giĂ¡o viĂªn phá»¥ trĂ¡ch.</p>
      </div>
    </header>

    <section class="content">
      <div class="card subject-summary">
        <h2>${subjectInfo.tenMonHoc}</h2>
        <div class="summary-pills">
          <span class="info-pill">MĂ£ mĂ´n: ${subjectInfo.idMonHoc}</span>
          <span class="info-pill">NÄƒm há»c: ${subjectInfo.namHoc}</span>
          <span class="info-pill">Há»c ká»³: ${subjectInfo.hocKy}</span>
          <span class="info-pill">Khá»‘i Ă¡p dá»¥ng: ${subjectInfo.khoiApDung}</span>
        </div>
      </div>

      <div class="info-grid">
        <div class="card info-card">
          <h3>ThĂ´ng tin chung</h3>
          <dl>
            <dt>MĂ£ mĂ´n há»c</dt><dd>${subjectInfo.idMonHoc}</dd>
            <dt>TĂªn mĂ´n há»c</dt><dd>${subjectInfo.tenMonHoc}</dd>
            <dt>KhĂ³a há»c</dt><dd>${subjectInfo.khoaHoc}</dd>
            <dt>NÄƒm há»c Ă¡p dá»¥ng</dt><dd>${subjectInfo.namHoc}</dd>
            <dt>Há»c ká»³ Ă¡p dá»¥ng</dt><dd>${subjectInfo.hocKy}</dd>
            <dt>Khá»‘i lá»›p Ă¡p dá»¥ng</dt><dd>${subjectInfo.khoiApDung}</dd>
            <dt>Sá»‘ Ä‘iá»ƒm thÆ°á»ng xuyĂªn</dt><dd>${subjectInfo.soDiemThuongXuyen}</dd>
            <dt>Tá»• bá»™ mĂ´n</dt><dd>${subjectInfo.toBoMon}</dd>
            <dt>GiĂ¡o viĂªn phá»¥ trĂ¡ch chĂ­nh</dt><dd>${subjectInfo.giaoVienPhuTrachChinh}</dd>
            <dt>NgĂ y táº¡o</dt><dd>${subjectInfo.ngayTao}</dd>
            <dt>MĂ´ táº£</dt><dd>${subjectInfo.moTa}</dd>
          </dl>
        </div>
      </div>

      <div class="card teacher-list-card">
        <h3>Danh sĂ¡ch giĂ¡o viĂªn phá»¥ trĂ¡ch mĂ´n há»c</h3>
        <div class="teacher-table-wrap">
          <table class="teacher-table">
            <thead>
            <tr>
              <th>MĂ£ giĂ¡o viĂªn</th>
              <th>Há» vĂ  tĂªn</th>
              <th>Email</th>
              <th>Sá»‘ Ä‘iá»‡n thoáº¡i</th>
              <th>Tráº¡ng thĂ¡i</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="teacher" items="${subjectInfo.teacherList}">
              <tr>
                <td>${teacher.idGiaoVien}</td>
                <td>${teacher.hoTen}</td>
                <td>${teacher.email}</td>
                <td>${teacher.soDienThoai}</td>
                <td>${teacher.trangThai}</td>
              </tr>
            </c:forEach>
            <c:if test="${empty subjectInfo.teacherList}">
              <tr>
                <td class="empty-note" colspan="5">ChÆ°a cĂ³ giĂ¡o viĂªn phá»¥ trĂ¡ch mĂ´n há»c nĂ y.</td>
              </tr>
            </c:if>
            </tbody>
          </table>
        </div>
      </div>

      <div class="page-actions">
        <a class="btn primary" href="<c:url value='/admin/subject/${subjectInfo.idMonHoc}/edit'/>">Chá»‰nh sá»­a</a>
        <a class="btn" href="<c:url value='/admin/subject'/>">Quay láº¡i danh sĂ¡ch</a>
      </div>
    </section>
  </main>
</div>

</body>
</html>

