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

  <main class="main conduct-create-page">
    <header class="conduct-header">
      <div class="header-left">
        <h1>Thêm khen thưởng</h1>
        <p>Chọn học sinh theo khối, khóa, lớp rồi nhập quyết định.</p>
      </div>
    </header>

    <section class="content">
      <c:if test="${not empty flashMessage}">
        <div class="alert ${flashType == 'error' ? 'alert-error' : (flashType == 'info' ? 'alert-info' : 'alert-success')}">
          ${flashMessage}
        </div>
      </c:if>

      <section class="form-card">
        <form class="filters" method="get" action="<c:url value='/admin/conduct/reward/create'/>" autocomplete="off">
          <div class="filter-item search-item">
            <label for="q">Tìm kiếm học sinh</label>
            <input id="q" type="text" name="q" value="${filter.q}" placeholder="Tên hoặc mã học sinh">
          </div>
          <div class="filter-item">
            <label for="khoi">Khối</label>
            <select id="khoi" name="khoi">
              <option value="">Tất cả</option>
              <c:forEach var="grade" items="${pageData.grades}">
                <option value="${grade}" ${filter.khoi == grade ? 'selected' : ''}>Khối ${grade}</option>
              </c:forEach>
            </select>
          </div>
          <div class="filter-item">
            <label for="khoa">Khóa</label>
            <select id="khoa" name="khoa">
              <option value="">Tất cả</option>
              <c:forEach var="item" items="${pageData.courseOptions}">
                <option value="${item.id}" ${filter.khoa == item.id ? 'selected' : ''}>${item.name}</option>
              </c:forEach>
            </select>
          </div>
          <div class="filter-item">
            <label for="lop">Lớp</label>
            <select id="lop" name="lop">
              <option value="">Tất cả</option>
              <c:forEach var="item" items="${pageData.classOptions}">
                <option value="${item.id}" ${filter.lop == item.id ? 'selected' : ''}>${item.name}</option>
              </c:forEach>
            </select>
          </div>
          <div class="filter-actions">
            <button class="btn filter-btn action-btn-search" type="submit">Tìm học sinh</button>
          </div>
        </form>
      </section>

      <section class="form-card">
        <form method="post" action="<c:url value='/admin/conduct/reward/create'/>">
          <input type="hidden" name="q" value="${filter.q}">
          <input type="hidden" name="khoi" value="${filter.khoi}">
          <input type="hidden" name="khoa" value="${filter.khoa}">
          <input type="hidden" name="lop" value="${filter.lop}">

          <div class="create-grid">
            <div>
              <h3>Danh sách học sinh</h3>
              <div class="student-candidates">
                <c:forEach var="student" items="${pageData.studentCandidates}">
                  <label class="candidate-item">
                    <input type="radio" name="studentId" value="${student.idHocSinh}"
                           ${pageData.selectedStudent != null && pageData.selectedStudent.idHocSinh == student.idHocSinh ? 'checked' : ''}>
                    <div class="candidate-meta">
                      <strong>${student.hoTen}</strong>
                      <span>${student.idHocSinh} • ${student.tenLop}</span>
                      <span>Khối ${student.khoi} • ${student.khoaHoc}</span>
                    </div>
                  </label>
                </c:forEach>
                <c:if test="${empty pageData.studentCandidates}">
                  <div class="candidate-item">
                    <div class="candidate-meta"><span>Không có học sinh theo bộ lọc hiện tại.</span></div>
                  </div>
                </c:if>
              </div>
            </div>

            <div>
              <div class="form-row">
                <label for="loaiChiTiet">Loại khen thưởng</label>
                <select id="loaiChiTiet" name="loaiChiTiet">
                  <option value="Học tập">Học tập</option>
                  <option value="Phong trào">Phong trào</option>
                  <option value="Đạo đức">Đạo đức</option>
                  <option value="Khác">Khác</option>
                </select>
              </div>
              <div class="form-row">
                <label for="ngayBanHanh">Ngày ban hành</label>
                <input id="ngayBanHanh" type="date" name="ngayBanHanh">
              </div>
              <div class="form-row">
                <label for="soQuyetDinh">Số quyết định</label>
                <input id="soQuyetDinh" type="text" name="soQuyetDinh" placeholder="Ví dụ: 123/QD-KT">
              </div>
              <div class="form-row">
                <label for="noiDung">Nội dung khen thưởng</label>
                <textarea id="noiDung" name="noiDung" placeholder="Nhập chi tiết thành tích, hình thức khen thưởng..."></textarea>
              </div>
              <div class="form-row">
                <label for="ghiChu">Ghi chú bổ sung</label>
                <textarea id="ghiChu" name="ghiChu" placeholder="Thông tin nội bộ khác..."></textarea>
              </div>
              <div class="form-row">
                <label for="namHoc">Năm học</label>
                <input id="namHoc" type="text" name="namHoc" placeholder="Ví dụ: 2025-2026">
              </div>
              <div class="form-row">
                <label for="hocKy">Học kỳ áp dụng</label>
                <select id="hocKy" name="hocKy">
                  <option value="0">Cả năm</option>
                  <option value="1">Học kỳ I</option>
                  <option value="2">Học kỳ II</option>
                </select>
              </div>
            </div>
          </div>

          <div class="form-actions-bottom">
            <a class="btn btn-outline" href="<c:url value='/admin/conduct'/>">Quay lại</a>
            <button class="btn btn-primary" type="submit">Lưu quyết định</button>
          </div>
        </form>
      </section>
    </section>
  </main>
</div>
</body>
</html>
