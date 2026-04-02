<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>${pageTitle}</title>

  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
  <link rel="stylesheet" href="<c:url value='/css/admin-layout.css'/>">
  <link rel="stylesheet" href="<c:url value='/css/admin/account.css'/>">
</head>
<body>
<div class="layout">
  <jsp:include page="/WEB-INF/views/admin/_sidebar.jsp"/>

  <main class="main account-info-page">
    <section class="account-header">
      <div>
        <h1>ThĂ´ng tin tĂ i khoáº£n</h1>
        <p>Xem nhanh thĂ´ng tin tĂ i khoáº£n vĂ  giĂ¡o viĂªn liĂªn káº¿t.</p>
      </div>
      <a class="btn" href="<c:url value='/admin/account'/>">Quay láº¡i danh sĂ¡ch</a>
    </section>

    <section class="content">
      <section class="card account-info-card">
        <h2>ThĂ´ng tin chung</h2>
        <div class="info-grid">
          <div class="info-item">
            <span class="info-label">ID tĂ i khoáº£n</span>
            <span class="info-value">${accountInfo.idTaiKhoan}</span>
          </div>
          <div class="info-item">
            <span class="info-label">TĂªn Ä‘Äƒng nháº­p</span>
            <span class="info-value">${accountInfo.tenDangNhap}</span>
          </div>
          <div class="info-item">
            <span class="info-label">Email</span>
            <span class="info-value">${accountInfo.email}</span>
          </div>
          <div class="info-item">
            <span class="info-label">Vai trĂ²</span>
            <span class="info-value">${accountInfo.vaiTro}</span>
          </div>
          <div class="info-item">
            <span class="info-label">Tráº¡ng thĂ¡i</span>
            <span class="info-value">${accountInfo.trangThai == 'khoa' ? 'ÄĂ£ khĂ³a' : 'Hoáº¡t Ä‘á»™ng'}</span>
          </div>
          <div class="info-item">
            <span class="info-label">Máº­t kháº©u hiá»‡n táº¡i</span>
            <span class="info-value info-value-password">${accountInfo.matKhauHienTai}</span>
          </div>
        </div>
      </section>

      <section class="card account-info-card">
        <h2>ThĂ´ng tin giĂ¡o viĂªn</h2>
        <c:choose>
          <c:when test="${not empty accountInfo.teacherProfile}">
            <div class="info-grid">
              <div class="info-item">
                <span class="info-label">MĂ£ giĂ¡o viĂªn</span>
                <span class="info-value">${accountInfo.teacherProfile.idGiaoVien}</span>
              </div>
              <div class="info-item">
                <span class="info-label">Há» vĂ  tĂªn</span>
                <span class="info-value">${accountInfo.teacherProfile.hoTen}</span>
              </div>
              <div class="info-item">
                <span class="info-label">Giá»›i tĂ­nh</span>
                <span class="info-value">${accountInfo.teacherProfile.gioiTinh}</span>
              </div>
              <div class="info-item">
                <span class="info-label">NgĂ y sinh</span>
                <span class="info-value">${accountInfo.teacherProfile.ngaySinh}</span>
              </div>
              <div class="info-item">
                <span class="info-label">MĂ´n dáº¡y</span>
                <span class="info-value">${accountInfo.teacherProfile.monDay}</span>
              </div>
              <div class="info-item">
                <span class="info-label">Sá»‘ Ä‘iá»‡n thoáº¡i</span>
                <span class="info-value">${accountInfo.teacherProfile.soDienThoai}</span>
              </div>
            </div>
          </c:when>
          <c:otherwise>
            <p class="empty-note">TĂ i khoáº£n nĂ y chÆ°a liĂªn káº¿t giĂ¡o viĂªn.</p>
          </c:otherwise>
        </c:choose>
      </section>

      <section class="card account-info-card">
        <h2>Lá»‹ch sá»­ Ä‘á»•i máº­t kháº©u</h2>
        <c:choose>
          <c:when test="${not empty accountInfo.passwordHistory}">
            <div class="password-history-list">
              <c:forEach var="item" items="${accountInfo.passwordHistory}">
                <article class="password-history-item">
                  <p><strong>Thá»i gian:</strong> ${item.thoiGian}</p>
                  <p><strong>NgÆ°á»i thay Ä‘á»•i:</strong> ${item.nguoiThayDoi}</p>
                  <p><strong>HĂ nh Ä‘á»™ng:</strong> ${item.hanhDong}</p>
                  <p><strong>Máº­t kháº©u cÅ©:</strong> ${item.matKhauCu}</p>
                  <p><strong>Máº­t kháº©u hiá»‡n táº¡i Ä‘Ă£ Ä‘á»•i:</strong> ${item.matKhauMoi}</p>
                  <c:if test="${not empty item.ghiChu}">
                    <p><strong>Ghi chĂº:</strong> ${item.ghiChu}</p>
                  </c:if>
                </article>
              </c:forEach>
            </div>
          </c:when>
          <c:otherwise>
            <p class="empty-note">ChÆ°a cĂ³ lá»‹ch sá»­ Ä‘á»•i máº­t kháº©u.</p>
          </c:otherwise>
        </c:choose>
      </section>
    </section>
  </main>
</div>
</body>
</html>

