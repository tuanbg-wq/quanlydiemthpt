<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>Thêm học sinh</title>
  <link rel="stylesheet" href="<c:url value='/css/admin-layout.css'/>">
  <link rel="stylesheet" href="<c:url value='/css/student-create.css'/>">
</head>

<body>
<div class="layout">
  <jsp:include page="/WEB-INF/views/admin/_sidebar.jsp"/>

  <main class="main student-create-page">
    <header class="topbar">
      <div class="topbar-left">
        <h1>Thêm học sinh</h1>
        <p>Nhập thông tin học sinh. Nếu lớp/khóa học chưa có, hệ thống sẽ tự tạo.</p>
      </div>
    </header>

    <section class="content">

      <c:if test="${not empty error}">
        <div class="alert alert-error">
          ${error}
        </div>
      </c:if>

      <div class="card">
        <form method="post"
              action="<c:url value='/admin/student/create'/>"
              enctype="multipart/form-data">

          <div class="form-grid">
            <div class="form-group">
              <label>Mã học sinh *</label>
              <input type="text" name="idHocSinh" placeholder="VD: HS001" value="${student.idHocSinh}" required>
            </div>

            <div class="form-group">
              <label>Họ tên *</label>
              <input type="text" name="hoTen" value="${student.hoTen}" required>
            </div>

            <div class="form-group">
              <label>Ngày sinh *</label>
              <input type="date" name="ngaySinh" value="${student.ngaySinh}" required>
            </div>

            <div class="form-group">
              <label>Giới tính</label>
              <select name="gioiTinh">
                <option value="">-- Chọn --</option>
                <option value="Nam" ${student.gioiTinh == 'Nam' ? 'selected' : ''}>Nam</option>
                <option value="Nu" ${student.gioiTinh == 'Nu' ? 'selected' : ''}>Nữ</option>
              </select>
            </div>

            <div class="form-group">
              <label>Nơi sinh</label>
              <input type="text" name="noiSinh" value="${student.noiSinh}">
            </div>

            <div class="form-group">
              <label>Dân tộc</label>
              <input type="text" name="danToc" value="${student.danToc}">
            </div>

            <div class="form-group">
              <label>Số điện thoại</label>
              <input type="text" name="soDienThoai" value="${student.soDienThoai}">
            </div>

            <div class="form-group">
              <label>Email</label>
              <input type="text" name="email" value="${student.email}">
            </div>

            <div class="form-group form-group-full">
              <label>Địa chỉ</label>
              <textarea name="diaChi" rows="3">${student.diaChi}</textarea>
            </div>

            <div class="form-group">
              <label>Họ tên cha</label>
              <input type="text" name="hoTenCha" value="${student.hoTenCha}">
            </div>

            <div class="form-group">
              <label>SĐT cha</label>
              <input type="text" name="sdtCha" value="${student.sdtCha}">
            </div>

            <div class="form-group">
              <label>Họ tên mẹ</label>
              <input type="text" name="hoTenMe" value="${student.hoTenMe}">
            </div>

            <div class="form-group">
              <label>SĐT mẹ</label>
              <input type="text" name="sdtMe" value="${student.sdtMe}">
            </div>

            <div class="form-group">
              <label>Ngày nhập học *</label>
              <input type="date" name="ngayNhapHoc" value="${student.ngayNhapHoc}" required>
              <small class="field-help">
                Dùng để tạo <b>ngày bắt đầu khóa học</b> nếu khóa chưa có.
              </small>
            </div>

            <div class="form-group">
              <label>Trạng thái</label>
              <select name="trangThai">
                <option value="dang_hoc" ${empty student.trangThai || student.trangThai == 'dang_hoc' ? 'selected' : ''}>Đang học</option>
                <option value="da_tot_nghiep" ${student.trangThai == 'da_tot_nghiep' ? 'selected' : ''}>Đã tốt nghiệp</option>
                <option value="bo_hoc" ${student.trangThai == 'bo_hoc' ? 'selected' : ''}>Bỏ học</option>
                <option value="chuyen_truong" ${student.trangThai == 'chuyen_truong' ? 'selected' : ''}>Chuyển trường</option>
              </select>
            </div>

            <div class="form-group">
              <label>Tên Khóa (id_khoa) *</label>
              <input type="text" name="courseId" placeholder="VD: K2026" required>
              <small class="field-help">
                Nếu khóa chưa có, hệ thống sẽ tự tạo (ngày bắt đầu = ngày nhập học, ngày kết thúc = NULL).
              </small>
            </div>

            <div class="form-group">
              <label>Khóa học</label>
              <input type="text" name="tenKhoa" placeholder="VD: Khóa 2026">
              <small class="field-help">
                Bỏ trống thì hệ thống tự đặt: "Khóa &lt;id_khoa&gt;".
              </small>
            </div>

            <div class="form-group">
              <label>ID lớp *</label>
              <input type="text" name="idLop" placeholder="VD: 10A1" required>
              <small class="field-help">
                Tên lớp sẽ tự lấy theo ID lớp.
              </small>
            </div>

            <div class="form-group">
              <label>Khối (10/11/12) *</label>
              <select name="khoi" required>
                <option value="">-- Chọn --</option>
                <option value="10">10</option>
                <option value="11">11</option>
                <option value="12">12</option>
              </select>
            </div>

            <div class="form-group form-group-full">
              <label>Ảnh học sinh</label>
              <input type="file" name="avatar" accept="image/png,image/jpeg,image/webp">
              <small class="field-help">
                Hỗ trợ PNG/JPG/JPEG/WEBP.
              </small>
            </div>
          </div>

          <div class="form-actions">
            <button class="btn primary" type="submit">Lưu</button>
            <a class="btn" href="<c:url value='/admin/student'/>">Quay lại danh sách</a>
          </div>

        </form>
      </div>
    </section>
  </main>
</div>
</body>
</html>
