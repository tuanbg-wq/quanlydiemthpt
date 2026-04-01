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
                <option value="Nữ" ${student.gioiTinh == 'Nữ' || student.gioiTinh == 'Nu' ? 'selected' : ''}>Nữ</option>
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
              <label>Hạnh kiểm học kỳ I</label>
              <select name="hanhKiemHocKy1">
                <option value="" ${empty student.hanhKiemHocKy1 ? 'selected' : ''}>(Chưa có)</option>
                <option value="Tốt" ${student.hanhKiemHocKy1 == 'Tốt' || student.hanhKiemHocKy1 == 'Tot' ? 'selected' : ''}>Tốt</option>
                <option value="Khá" ${student.hanhKiemHocKy1 == 'Khá' || student.hanhKiemHocKy1 == 'Kha' ? 'selected' : ''}>Khá</option>
                <option value="Trung bình" ${student.hanhKiemHocKy1 == 'Trung bình' || student.hanhKiemHocKy1 == 'Trung_binh' || student.hanhKiemHocKy1 == 'TB' ? 'selected' : ''}>Trung bình</option>
                <option value="Yếu" ${student.hanhKiemHocKy1 == 'Yếu' || student.hanhKiemHocKy1 == 'Yeu' ? 'selected' : ''}>Yếu</option>
                <option value="Kém" ${student.hanhKiemHocKy1 == 'Kém' || student.hanhKiemHocKy1 == 'Kem' ? 'selected' : ''}>Kém</option>
              </select>
            </div>

            <div class="form-group">
              <label>Hạnh kiểm học kỳ II</label>
              <select name="hanhKiemHocKy2">
                <option value="" ${empty student.hanhKiemHocKy2 ? 'selected' : ''}>(Chưa có)</option>
                <option value="Tốt" ${student.hanhKiemHocKy2 == 'Tốt' || student.hanhKiemHocKy2 == 'Tot' ? 'selected' : ''}>Tốt</option>
                <option value="Khá" ${student.hanhKiemHocKy2 == 'Khá' || student.hanhKiemHocKy2 == 'Kha' ? 'selected' : ''}>Khá</option>
                <option value="Trung bình" ${student.hanhKiemHocKy2 == 'Trung bình' || student.hanhKiemHocKy2 == 'Trung_binh' || student.hanhKiemHocKy2 == 'TB' ? 'selected' : ''}>Trung bình</option>
                <option value="Yếu" ${student.hanhKiemHocKy2 == 'Yếu' || student.hanhKiemHocKy2 == 'Yeu' ? 'selected' : ''}>Yếu</option>
                <option value="Kém" ${student.hanhKiemHocKy2 == 'Kém' || student.hanhKiemHocKy2 == 'Kem' ? 'selected' : ''}>Kém</option>
              </select>
            </div>

            <div class="form-group">
              <label>Hạnh kiểm cả năm</label>
              <select name="hanhKiemCaNam">
                <option value="" ${empty student.hanhKiemCaNam ? 'selected' : ''}>(Chưa có)</option>
                <option value="Tốt" ${student.hanhKiemCaNam == 'Tốt' || student.hanhKiemCaNam == 'Tot' ? 'selected' : ''}>Tốt</option>
                <option value="Khá" ${student.hanhKiemCaNam == 'Khá' || student.hanhKiemCaNam == 'Kha' ? 'selected' : ''}>Khá</option>
                <option value="Trung bình" ${student.hanhKiemCaNam == 'Trung bình' || student.hanhKiemCaNam == 'Trung_binh' || student.hanhKiemCaNam == 'TB' ? 'selected' : ''}>Trung bình</option>
                <option value="Yếu" ${student.hanhKiemCaNam == 'Yếu' || student.hanhKiemCaNam == 'Yeu' ? 'selected' : ''}>Yếu</option>
                <option value="Kém" ${student.hanhKiemCaNam == 'Kém' || student.hanhKiemCaNam == 'Kem' ? 'selected' : ''}>Kém</option>
              </select>
            </div>

            <div class="form-group">
              <label>Mã khóa (id_khoa) *</label>
              <input type="text" name="courseId" placeholder="VD: K2026" required>
              <small class="field-help">
                Nếu khóa chưa có, hệ thống sẽ tự tạo (ngày bắt đầu = ngày nhập học, ngày kết thúc = NULL).
              </small>
            </div>

            <div class="form-group">
              <label>Tên khóa học</label>
              <input type="text" name="tenKhoa" placeholder="VD: Khóa 2026">
              <small class="field-help">
                Bỏ trống thì hệ thống tự đặt: "Khóa &lt;id_khoa&gt;".
              </small>
            </div>

            <div class="form-group">
              <label>Mã lớp *</label>
              <input type="text" name="idLop" placeholder="VD: K06A1" value="${param.idLop}" required>
              <small class="field-help">
                Nhập theo mẫu Mã khóa + A1 (ví dụ: K06A1).
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
