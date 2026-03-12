<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

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

    <jsp:include page="/WEB-INF/views/admin/_sidebar.jsp"/>

    <main class="main student-edit-page">

        <header class="topbar">
            <div class="topbar-left">
                <h1>Cập nhật thông tin và lớp học sinh</h1>
                <p>Sửa thông tin học sinh, sửa lớp hiện tại hoặc chuyển sang lớp mới</p>
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
                      action="<c:url value='/admin/student/${student.idHocSinh}/edit'/>"
                      enctype="multipart/form-data">

                    <div class="form-grid">

                        <div class="form-group">
                            <label>Mã học sinh</label>
                            <input type="text" name="idHocSinh" value="${student.idHocSinh}" required/>
                            <small>Có thể đổi mã học sinh. Nếu mã mới đã tồn tại, hệ thống sẽ báo lỗi.</small>
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
                                <option value="Nam" ${student.gioiTinh == 'Nam' ? 'selected' : ''}>Nam</option>
                                <option value="Nữ" ${student.gioiTinh == 'Nữ' ? 'selected' : ''}>Nữ</option>
                                <option value="Nu" ${student.gioiTinh == 'Nu' ? 'selected' : ''}>Nữ</option>
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
                            <label>Khóa học (id_khoa)</label>
                            <input type="text"
                                   name="courseId"
                                   value="${student.lop != null && student.lop.khoaHoc != null ? student.lop.khoaHoc.idKhoa : ''}"
                                   required/>
                        </div>

                        <div class="form-group">
                            <label>Tên khóa</label>
                            <input type="text"
                                   name="tenKhoa"
                                   value="${student.lop != null && student.lop.khoaHoc != null ? student.lop.khoaHoc.tenKhoa : ''}"/>
                        </div>

                        <div class="form-group">
                            <label>Khối</label>
                            <select name="khoi" required>
                                <option value="">-- Chọn --</option>
                                <option value="10" ${student.lop != null && student.lop.khoi == 10 ? 'selected' : ''}>10</option>
                                <option value="11" ${student.lop != null && student.lop.khoi == 11 ? 'selected' : ''}>11</option>
                                <option value="12" ${student.lop != null && student.lop.khoi == 12 ? 'selected' : ''}>12</option>
                            </select>
                        </div>

                        <div class="form-group">
                            <label>Lớp hiện tại (sửa dữ liệu, không lưu lịch sử)</label>
                            <select name="currentClassId" required>
                                <c:forEach var="cl" items="${classes}">
                                    <option value="${cl.idLop}"
                                            ${student.lop != null && student.lop.idLop == cl.idLop ? 'selected' : ''}>
                                            ${cl.tenLop}
                                            <c:if test="${cl.khoaHoc != null}">
                                                - ${cl.khoaHoc.idKhoa}
                                            </c:if>
                                            <c:if test="${cl.khoi != null}">
                                                - Khối ${cl.khoi}
                                            </c:if>
                                        </option>
                                </c:forEach>
                            </select>
                            <small>Dùng khi lúc thêm học sinh bị nhập nhầm lớp.</small>
                        </div>

                        <div class="form-group">
                            <label>Chuyển sang lớp (có lưu lịch sử)</label>
                            <select name="transferClassId">
                                <option value="">-- Không chuyển lớp --</option>
                                <c:forEach var="cl" items="${classes}">
                                    <option value="${cl.idLop}">
                                        ${cl.tenLop}
                                        <c:if test="${cl.khoaHoc != null}">
                                            - ${cl.khoaHoc.idKhoa}
                                        </c:if>
                                        <c:if test="${cl.khoi != null}">
                                            - Khối ${cl.khoi}
                                        </c:if>
                                    </option>
                                </c:forEach>
                            </select>
                            <small>Chỉ chọn ô này khi học sinh thực sự chuyển lớp.</small>
                        </div>

                        <div class="form-group full-width">
                            <label>Ảnh học sinh</label>

                            <c:if test="${not empty student.anh}">
                                <div class="avatar-preview">
                                    <img src="<c:url value='/uploads/${student.anh}'/>"
                                         alt="avatar"/>
                                </div>
                            </c:if>

                            <input type="file" name="avatar" accept="image/png,image/jpeg,image/webp"/>
                            <small>Hỗ trợ PNG/JPG/JPEG/WEBP.</small>
                        </div>

                    </div>

                    <div class="form-actions">
                        <button type="submit" class="btn primary">Lưu thay đổi</button>
                        <a href="<c:url value='/admin/student'/>" class="btn">Quay lại</a>
                    </div>

                </form>
            </div>
        </section>
    </main>
</div>

</body>
</html>
