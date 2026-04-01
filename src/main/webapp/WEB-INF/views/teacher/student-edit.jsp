<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>${pageTitle}</title>
    <link rel="stylesheet" href="<c:url value='/css/admin-layout.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/student-edit.css'/>">
</head>
<body>
<div class="layout">
    <jsp:include page="/WEB-INF/views/teacher/_sidebar.jsp"/>

    <main class="main student-edit-page">
        <header class="topbar">
            <div class="topbar-left">
                <h1>Cập nhật thông tin học sinh</h1>
                <p>Giáo viên chủ nhiệm được chỉnh sửa hồ sơ và chuyển lớp/chuyển trường theo phân quyền.</p>
            </div>
        </header>

        <section class="content">
            <c:if test="${not empty error}">
                <div class="alert alert-error">${error}</div>
            </c:if>

            <div class="card">
                <form method="post"
                      action="<c:url value='/teacher/student/${student.idHocSinh}/edit'/>"
                      enctype="multipart/form-data">

                    <div class="form-grid">
                        <div class="form-group">
                            <label>Mã học sinh</label>
                            <input type="text" name="idHocSinh" value="${student.idHocSinh}" required/>
                        </div>
                        <div class="form-group">
                            <label>Họ tên *</label>
                            <input type="text" name="hoTen" value="${student.hoTen}" required/>
                        </div>
                        <div class="form-group">
                            <label>Ngày sinh *</label>
                            <input type="date" name="ngaySinh" value="${student.ngaySinh}" required/>
                        </div>
                        <div class="form-group">
                            <label>Giới tính</label>
                            <select name="gioiTinh">
                                <option value="">-- Chọn --</option>
                                <option value="Nam" ${student.gioiTinhHienThi == 'Nam' ? 'selected' : ''}>Nam</option>
                                <option value="Nữ" ${student.gioiTinhHienThi == 'Nữ' ? 'selected' : ''}>Nữ</option>
                            </select>
                        </div>

                        <div class="form-group">
                            <label>Nơi sinh</label>
                            <input type="text" name="noiSinh" value="${student.noiSinh}"/>
                        </div>
                        <div class="form-group">
                            <label>Dân tộc</label>
                            <input type="text" name="danToc" value="${student.danToc}"/>
                        </div>
                        <div class="form-group">
                            <label>Số điện thoại</label>
                            <input type="text" name="soDienThoai" value="${student.soDienThoai}"/>
                        </div>
                        <div class="form-group">
                            <label>Email</label>
                            <input type="email" name="email" value="${student.email}"/>
                        </div>

                        <div class="form-group full-width">
                            <label>Địa chỉ</label>
                            <textarea name="diaChi" rows="3">${student.diaChi}</textarea>
                        </div>

                        <div class="form-group">
                            <label>Họ tên cha</label>
                            <input type="text" name="hoTenCha" value="${student.hoTenCha}"/>
                        </div>
                        <div class="form-group">
                            <label>SĐT cha</label>
                            <input type="text" name="sdtCha" value="${student.sdtCha}"/>
                        </div>
                        <div class="form-group">
                            <label>Họ tên mẹ</label>
                            <input type="text" name="hoTenMe" value="${student.hoTenMe}"/>
                        </div>
                        <div class="form-group">
                            <label>SĐT mẹ</label>
                            <input type="text" name="sdtMe" value="${student.sdtMe}"/>
                        </div>

                        <div class="form-group">
                            <label>Ngày nhập học</label>
                            <input type="date" name="ngayNhapHoc" value="${student.ngayNhapHoc}"/>
                        </div>
                        <div class="form-group">
                            <label>Trạng thái</label>
                            <select name="trangThai">
                                <option value="dang_hoc" ${student.trangThai == 'dang_hoc' ? 'selected' : ''}>Đang học</option>
                                <option value="da_tot_nghiep" ${student.trangThai == 'da_tot_nghiep' ? 'selected' : ''}>Đã tốt nghiệp</option>
                                <option value="bo_hoc" ${student.trangThai == 'bo_hoc' ? 'selected' : ''}>Bỏ học</option>
                                <option value="chuyen_truong" ${student.trangThai == 'chuyen_truong' ? 'selected' : ''}>Chuyển trường</option>
                            </select>
                        </div>

                        <div class="form-group">
                            <label>Hạnh kiểm học kỳ I</label>
                            <select name="hanhKiemHocKy1">
                                <option value="" ${empty student.hanhKiemHocKy1 ? 'selected' : ''}>(Chưa có)</option>
                                <option value="Tốt" ${student.hanhKiemHocKy1HienThi == 'Tốt' ? 'selected' : ''}>Tốt</option>
                                <option value="Khá" ${student.hanhKiemHocKy1HienThi == 'Khá' ? 'selected' : ''}>Khá</option>
                                <option value="Trung bình" ${student.hanhKiemHocKy1HienThi == 'Trung bình' ? 'selected' : ''}>Trung bình</option>
                                <option value="Yếu" ${student.hanhKiemHocKy1HienThi == 'Yếu' ? 'selected' : ''}>Yếu</option>
                                <option value="Kém" ${student.hanhKiemHocKy1HienThi == 'Kém' ? 'selected' : ''}>Kém</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label>Hạnh kiểm học kỳ II</label>
                            <select name="hanhKiemHocKy2">
                                <option value="" ${empty student.hanhKiemHocKy2 ? 'selected' : ''}>(Chưa có)</option>
                                <option value="Tốt" ${student.hanhKiemHocKy2HienThi == 'Tốt' ? 'selected' : ''}>Tốt</option>
                                <option value="Khá" ${student.hanhKiemHocKy2HienThi == 'Khá' ? 'selected' : ''}>Khá</option>
                                <option value="Trung bình" ${student.hanhKiemHocKy2HienThi == 'Trung bình' ? 'selected' : ''}>Trung bình</option>
                                <option value="Yếu" ${student.hanhKiemHocKy2HienThi == 'Yếu' ? 'selected' : ''}>Yếu</option>
                                <option value="Kém" ${student.hanhKiemHocKy2HienThi == 'Kém' ? 'selected' : ''}>Kém</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label>Hạnh kiểm cả năm</label>
                            <select name="hanhKiemCaNam">
                                <option value="" ${empty student.hanhKiemCaNam ? 'selected' : ''}>(Chưa có)</option>
                                <option value="Tốt" ${student.hanhKiemCaNamHienThi == 'Tốt' ? 'selected' : ''}>Tốt</option>
                                <option value="Khá" ${student.hanhKiemCaNamHienThi == 'Khá' ? 'selected' : ''}>Khá</option>
                                <option value="Trung bình" ${student.hanhKiemCaNamHienThi == 'Trung bình' ? 'selected' : ''}>Trung bình</option>
                                <option value="Yếu" ${student.hanhKiemCaNamHienThi == 'Yếu' ? 'selected' : ''}>Yếu</option>
                                <option value="Kém" ${student.hanhKiemCaNamHienThi == 'Kém' ? 'selected' : ''}>Kém</option>
                            </select>
                        </div>

                        <div class="form-group">
                            <label>Lớp hiện tại</label>
                            <input type="text" value="${student.lop != null ? student.lop.maVaTenLop : '(trống)'}" readonly>
                        </div>

                        <div class="form-group">
                            <label>Chuyển sang lớp khác (lưu lịch sử)</label>
                            <select name="transferClassId">
                                <option value="">-- Không chuyển lớp --</option>
                                <c:forEach var="cl" items="${classes}">
                                    <option value="${cl.idLop}">
                                        ${cl.maVaTenLop}
                                        <c:if test="${cl.khoaHoc != null}">
                                            - ${cl.khoaHoc.idKhoa}
                                        </c:if>
                                        <c:if test="${cl.khoi != null}">
                                            - Khối ${cl.khoi}
                                        </c:if>
                                    </option>
                                </c:forEach>
                            </select>
                        </div>

                        <div class="form-group full-width">
                            <label>Ảnh học sinh</label>
                            <c:if test="${not empty student.anh}">
                                <div class="avatar-preview">
                                    <c:choose>
                                        <c:when test="${fn:startsWith(student.anh, '/uploads/')}">
                                            <img src="<c:url value='${student.anh}'/>" alt="avatar"/>
                                        </c:when>
                                        <c:otherwise>
                                            <img src="<c:url value='/uploads/${student.anh}'/>" alt="avatar"/>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </c:if>
                            <input type="file" name="avatar" accept="image/png,image/jpeg,image/webp"/>
                        </div>
                    </div>

                    <div class="form-actions">
                        <button type="submit" class="btn primary">Lưu thay đổi</button>
                        <a href="<c:url value='/teacher/student'/>" class="btn">Quay lại</a>
                    </div>
                </form>
            </div>
        </section>
    </main>
</div>
</body>
</html>
