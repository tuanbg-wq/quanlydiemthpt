<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>${pageTitle}</title>
  <link rel="stylesheet" href="<c:url value='/css/admin-layout.css'/>">
  <link rel="stylesheet" href="<c:url value='/css/conduct-list.css'/>">
</head>
<body>
<div class="layout">
  <jsp:include page="/WEB-INF/views/admin/_sidebar.jsp"/>

  <main class="main conduct-info-page">
    <section class="info-card">
      <h2>Thông tin quyết định</h2>
      <div class="info-grid">
        <div class="info-item"><span>Mã học sinh</span><strong>${detail.idHocSinh}</strong></div>
        <div class="info-item"><span>Họ tên</span><strong>${detail.tenHocSinh}</strong></div>
        <div class="info-item"><span>Lớp</span><strong>${detail.tenLop}</strong></div>
        <div class="info-item"><span>Loại</span><strong>${detail.loaiDisplay}</strong></div>
        <div class="info-item"><span>Loại chi tiết</span><strong>${empty detail.loaiChiTiet ? '-' : detail.loaiChiTiet}</strong></div>
        <div class="info-item"><span>Số quyết định</span><strong>${empty detail.soQuyetDinh ? '-' : detail.soQuyetDinh}</strong></div>
        <div class="info-item"><span>Ngày ban hành</span><strong>${detail.ngayBanHanh}</strong></div>
        <div class="info-item"><span>Năm học / Học kỳ</span><strong>${detail.namHoc} • ${detail.hocKyDisplay}</strong></div>
        <div class="info-item full"><span>Nội dung chi tiết</span><p>${detail.noiDungChiTiet}</p></div>
        <div class="info-item full"><span>Ghi chú</span><p>${empty detail.ghiChu ? '-' : detail.ghiChu}</p></div>
      </div>

      <div class="form-actions-bottom">
        <a class="btn btn-outline" href="<c:url value='/admin/conduct'/>">Quay lại danh sách</a>
        <a class="btn btn-primary" href="<c:url value='/admin/conduct/${detail.eventId}/edit'/>">Chỉnh sửa</a>
      </div>
    </section>
  </main>
</div>
</body>
</html>
