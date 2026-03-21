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
  <link rel="stylesheet" href="<c:url value='/css/account.css'/>">
</head>
<body>
<div class="layout">
  <jsp:include page="/WEB-INF/views/admin/_sidebar.jsp"/>

  <main class="main account-info-page">
    <section class="account-header">
      <div>
        <h1>Thông tin tài khoản</h1>
        <p>Xem nhanh thông tin tài khoản và giáo viên liên kết.</p>
      </div>
      <a class="btn" href="<c:url value='/admin/account'/>">Quay lại danh sách</a>
    </section>

    <section class="content">
      <section class="card account-info-card">
        <h2>Thông tin chung</h2>
        <div class="info-grid">
          <div class="info-item">
            <span class="info-label">ID tài khoản</span>
            <span class="info-value">${accountInfo.idTaiKhoan}</span>
          </div>
          <div class="info-item">
            <span class="info-label">Tên đăng nhập</span>
            <span class="info-value">${accountInfo.tenDangNhap}</span>
          </div>
          <div class="info-item">
            <span class="info-label">Email</span>
            <span class="info-value">${accountInfo.email}</span>
          </div>
          <div class="info-item">
            <span class="info-label">Mật khẩu hiện tại (đã mã hóa)</span>
            <span class="info-value info-value-password">${accountInfo.matKhauHienTai}</span>
          </div>
          <div class="info-item">
            <span class="info-label">Vai trò</span>
            <span class="info-value">${accountInfo.vaiTro}</span>
          </div>
          <div class="info-item">
            <span class="info-label">Trạng thái</span>
            <span class="info-value">${accountInfo.trangThai == 'khoa' ? 'Đã khóa' : 'Hoạt động'}</span>
          </div>
        </div>
      </section>

      <section class="card account-info-card">
        <h2>Thông tin giáo viên</h2>
        <c:choose>
          <c:when test="${not empty accountInfo.teacherProfile}">
            <div class="info-grid">
              <div class="info-item">
                <span class="info-label">Mã giáo viên</span>
                <span class="info-value">${accountInfo.teacherProfile.idGiaoVien}</span>
              </div>
              <div class="info-item">
                <span class="info-label">Họ và tên</span>
                <span class="info-value">${accountInfo.teacherProfile.hoTen}</span>
              </div>
              <div class="info-item">
                <span class="info-label">Giới tính</span>
                <span class="info-value">${accountInfo.teacherProfile.gioiTinh}</span>
              </div>
              <div class="info-item">
                <span class="info-label">Ngày sinh</span>
                <span class="info-value">${accountInfo.teacherProfile.ngaySinh}</span>
              </div>
              <div class="info-item">
                <span class="info-label">Môn dạy</span>
                <span class="info-value">${accountInfo.teacherProfile.monDay}</span>
              </div>
              <div class="info-item">
                <span class="info-label">Số điện thoại</span>
                <span class="info-value">${accountInfo.teacherProfile.soDienThoai}</span>
              </div>
            </div>
          </c:when>
          <c:otherwise>
            <p class="empty-note">Tài khoản này chưa liên kết giáo viên.</p>
          </c:otherwise>
        </c:choose>
      </section>
    </section>
  </main>
</div>
</body>
</html>
