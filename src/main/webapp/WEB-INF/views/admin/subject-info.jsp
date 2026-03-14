<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>${pageTitle}</title>
  <link rel="stylesheet" href="<c:url value='/css/admin-layout.css'/>">
  <link rel="stylesheet" href="<c:url value='/css/subject-info.css'/>">
</head>
<body>
<div class="layout">
  <jsp:include page="/WEB-INF/views/admin/_sidebar.jsp"/>

  <main class="main subject-info-page">
    <header class="topbar">
      <div class="topbar-left">
        <h1>Thông tin môn học</h1>
        <p>Xem đầy đủ thông tin môn học và danh sách giáo viên phụ trách.</p>
      </div>
    </header>

    <section class="content">
      <div class="card subject-summary">
        <h2>${subjectInfo.tenMonHoc}</h2>
        <div class="summary-pills">
          <span class="info-pill">Mã môn: ${subjectInfo.idMonHoc}</span>
          <span class="info-pill">Năm học: ${subjectInfo.namHoc}</span>
          <span class="info-pill">Học kỳ: ${subjectInfo.hocKy}</span>
          <span class="info-pill">Khối áp dụng: ${subjectInfo.khoiApDung}</span>
        </div>
      </div>

      <div class="info-grid">
        <div class="card info-card">
          <h3>Thông tin chung</h3>
          <dl>
            <dt>Mã môn học</dt><dd>${subjectInfo.idMonHoc}</dd>
            <dt>Tên môn học</dt><dd>${subjectInfo.tenMonHoc}</dd>
            <dt>Khóa học</dt><dd>${subjectInfo.khoaHoc}</dd>
            <dt>Năm học áp dụng</dt><dd>${subjectInfo.namHoc}</dd>
            <dt>Học kỳ áp dụng</dt><dd>${subjectInfo.hocKy}</dd>
            <dt>Khối lớp áp dụng</dt><dd>${subjectInfo.khoiApDung}</dd>
            <dt>Tổ bộ môn</dt><dd>${subjectInfo.toBoMon}</dd>
            <dt>Giáo viên phụ trách chính</dt><dd>${subjectInfo.giaoVienPhuTrachChinh}</dd>
            <dt>Ngày tạo</dt><dd>${subjectInfo.ngayTao}</dd>
            <dt>Mô tả</dt><dd>${subjectInfo.moTa}</dd>
          </dl>
        </div>
      </div>

      <div class="card teacher-list-card">
        <h3>Danh sách giáo viên phụ trách môn học</h3>
        <div class="teacher-table-wrap">
          <table class="teacher-table">
            <thead>
            <tr>
              <th>Mã giáo viên</th>
              <th>Họ và tên</th>
              <th>Email</th>
              <th>Số điện thoại</th>
              <th>Trạng thái</th>
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
                <td class="empty-note" colspan="5">Chưa có giáo viên phụ trách môn học này.</td>
              </tr>
            </c:if>
            </tbody>
          </table>
        </div>
      </div>

      <div class="page-actions">
        <a class="btn primary" href="<c:url value='/admin/subject/${subjectInfo.idMonHoc}/edit'/>">Chỉnh sửa</a>
        <a class="btn" href="<c:url value='/admin/subject'/>">Quay lại danh sách</a>
      </div>
    </section>
  </main>
</div>

</body>
</html>
