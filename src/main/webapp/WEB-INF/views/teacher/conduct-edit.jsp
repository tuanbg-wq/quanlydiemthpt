<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>${pageTitle}</title>
  <link rel="stylesheet" href="<c:url value='/css/admin-layout.css'/>">
  <link rel="stylesheet" href="<c:url value='/css/teacher/conduct/conduct-list.css'/>">
</head>
<body>
<div class="layout">
  <jsp:include page="/WEB-INF/views/teacher/_sidebar.jsp"/>

  <main class="main conduct-edit-page">
    <section class="form-card">
      <h2>Sửa quyết định</h2>
      <p>Học sinh: <strong>${detail.tenHocSinh}</strong> (${detail.idHocSinh}) • ${detail.tenLop}</p>

      <form method="post" action="<c:url value='/teacher/conduct/${detail.eventId}/edit'/>">
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
          <input id="ngayBanHanh" type="text" name="ngayBanHanh" value="${form.ngayBanHanh}"
                 placeholder="dd/mm/yyyy" inputmode="numeric" maxlength="10" autocomplete="off">
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
          <a class="btn btn-outline" href="<c:url value='/teacher/conduct'/>">Quay lại</a>
          <button class="btn btn-primary" type="submit">Lưu cập nhật</button>
        </div>
      </form>
    </section>
  </main>
</div>
<script>
  (function () {
    const editForm = document.querySelector('main.conduct-edit-page form');
    const ngayBanHanhInput = document.getElementById('ngayBanHanh');
    if (!editForm || !ngayBanHanhInput) return;

    function pad2(value) { return String(value).padStart(2, '0'); }

    function toIsoDate(rawValue) {
      const value = (rawValue || '').trim();
      if (!value) return '';
      const isoMatch = value.match(/^(\d{4})-(\d{2})-(\d{2})$/);
      const vnMatch = value.match(/^(\d{1,2})\/(\d{1,2})\/(\d{4})$/);
      let year; let month; let day;
      if (isoMatch) { year = Number(isoMatch[1]); month = Number(isoMatch[2]); day = Number(isoMatch[3]); }
      else if (vnMatch) { day = Number(vnMatch[1]); month = Number(vnMatch[2]); year = Number(vnMatch[3]); }
      else return null;
      const candidate = new Date(year, month - 1, day);
      const valid = candidate.getFullYear() === year && candidate.getMonth() === month - 1 && candidate.getDate() === day;
      return valid ? year + '-' + pad2(month) + '-' + pad2(day) : null;
    }

    editForm.addEventListener('submit', function (event) {
      const normalized = toIsoDate(ngayBanHanhInput.value);
      if (normalized === null) {
        event.preventDefault();
        alert('Ngày ban hành không hợp lệ. Vui lòng nhập theo dd/mm/yyyy.');
        ngayBanHanhInput.focus();
        return;
      }
      ngayBanHanhInput.value = normalized;
    });
  })();
</script>
</body>
</html>
