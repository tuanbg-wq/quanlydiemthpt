<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>${pageTitle}</title>
  <link rel="stylesheet" href="<c:url value='/css/admin-layout.css'/>">
  <link rel="stylesheet" href="<c:url value='/css/admin/conduct-list.css'/>">
</head>
<body>
<div class="layout">
  <jsp:include page="/WEB-INF/views/admin/_sidebar.jsp"/>

  <main class="main conduct-edit-page">
    <section class="form-card">
      <h2>Sửa quyết định</h2>
      <p>Học sinh: <strong>${detail.tenHocSinh}</strong> (${detail.idHocSinh}) • ${detail.tenLop}</p>

      <form method="post" action="<c:url value='/admin/conduct/${detail.eventId}/edit'/>">
        <div class="form-row">
          <label for="loai">Loại</label>
          <select id="loai" name="loai">
            <option value="KHEN_THUONG" ${form.loai == 'KHEN_THUONG' ? 'selected' : ''}>Khen thưởng</option>
            <option value="KY_LUAT" ${form.loai == 'KY_LUAT' ? 'selected' : ''}>Kỷ luật</option>
          </select>
        </div>
        <div class="form-row">
          <label for="loaiChiTiet">Loại chi tiết</label>
          <input id="loaiChiTiet" type="text" name="loaiChiTiet" value="${form.loaiChiTiet}">
        </div>
        <div class="form-row">
          <label for="soQuyetDinh">Số quyết định</label>
          <input id="soQuyetDinh" type="text" name="soQuyetDinh" value="${form.soQuyetDinh}">
        </div>
        <div class="form-row">
          <label for="ngayBanHanh">Ngày ban hành</label>
          <input id="ngayBanHanh" type="date" name="ngayBanHanh" value="${form.ngayBanHanh}">
        </div>
        <div class="form-row">
          <label for="noiDung">Nội dung chi tiết</label>
          <textarea id="noiDung" name="noiDung">${form.noiDung}</textarea>
        </div>
        <div class="form-row">
          <label for="ghiChu">Ghi chú</label>
          <textarea id="ghiChu" name="ghiChu">${form.ghiChu}</textarea>
        </div>
        <div class="form-row">
          <label for="namHoc">Năm học</label>
          <input id="namHoc" type="text" name="namHoc" value="${form.namHoc}">
        </div>
        <div class="form-row">
          <label for="hocKy">Học kỳ</label>
          <select id="hocKy" name="hocKy">
            <option value="0" ${form.hocKy == 0 ? 'selected' : ''}>Cả năm</option>
            <option value="1" ${form.hocKy == 1 ? 'selected' : ''}>Học kỳ I</option>
            <option value="2" ${form.hocKy == 2 ? 'selected' : ''}>Học kỳ II</option>
          </select>
        </div>

        <div class="form-actions-bottom">
          <a class="btn btn-outline" href="<c:url value='/admin/conduct'/>">Quay lại</a>
          <button class="btn btn-primary" type="submit">Lưu cập nhật</button>
        </div>
      </form>
    </section>
  </main>
</div>
</body>
</html>

