<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>${pageTitle}</title>
    <link rel="stylesheet" href="<c:url value='/css/admin-layout.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/student-info.css'/>">
</head>
<body>

<div class="layout">
    <jsp:include page="/WEB-INF/views/admin/_sidebar.jsp"/>

    <main class="main student-info-page">
        <header class="topbar">
            <div class="topbar-left">
                <h1>Thông tin học sinh</h1>
                <p>Xem toàn bộ hồ sơ cá nhân, lớp học và lịch sử chỉnh sửa.</p>
            </div>
        </header>

        <section class="content">
            <div class="card profile-card">
                <c:choose>
                    <c:when test="${not empty student.anh}">
                        <img class="profile-avatar" src="<c:url value='/uploads/${student.anh}'/>" alt="avatar"/>
                    </c:when>
                    <c:otherwise>
                        <div class="avatar-fallback">HS</div>
                    </c:otherwise>
                </c:choose>

                <div class="profile-meta">
                    <h2>${student.hoTen}</h2>
                    <div class="meta-row">
                        <span class="pill">Mã HS: ${student.idHocSinh}</span>
                        <span class="pill">Trạng thái: ${student.trangThai}</span>
                        <span class="pill">Lớp: ${student.lop != null ? student.lop.tenLop : '(trống)'}</span>
                        <span class="pill">Khối: ${student.lop != null ? student.lop.khoi : '(trống)'}</span>
                        <span class="pill">Khóa: ${student.lop != null && student.lop.khoaHoc != null ? student.lop.khoaHoc.idKhoa : '(trống)'}</span>
                    </div>
                </div>
            </div>

            <div class="info-grid">
                <div class="card info-card">
                    <h3>Thông tin cá nhân</h3>
                    <dl>
                        <dt>Họ tên</dt><dd>${student.hoTen}</dd>
                        <dt>Ngày sinh</dt><dd>${student.ngaySinh}</dd>
                        <dt>Giới tính</dt><dd>${student.gioiTinh}</dd>
                        <dt>Nơi sinh</dt><dd>${student.noiSinh}</dd>
                        <dt>Dân tộc</dt><dd>${student.danToc}</dd>
                        <dt>Ngày nhập học</dt><dd>${student.ngayNhapHoc}</dd>
                        <dt>Ngày tạo</dt><dd>${student.ngayTao}</dd>
                    </dl>
                </div>

                <div class="card info-card">
                    <h3>Liên hệ</h3>
                    <dl>
                        <dt>Số điện thoại</dt><dd>${student.soDienThoai}</dd>
                        <dt>Email</dt><dd>${student.email}</dd>
                        <dt>Địa chỉ</dt><dd>${student.diaChi}</dd>
                    </dl>
                </div>

                <div class="card info-card">
                    <h3>Thông tin phụ huynh</h3>
                    <dl>
                        <dt>Họ tên cha</dt><dd>${student.hoTenCha}</dd>
                        <dt>SĐT cha</dt><dd>${student.sdtCha}</dd>
                        <dt>Họ tên mẹ</dt><dd>${student.hoTenMe}</dd>
                        <dt>SĐT mẹ</dt><dd>${student.sdtMe}</dd>
                    </dl>
                </div>

                <div class="card info-card">
                    <h3>Thông tin lớp và khóa</h3>
                    <dl>
                        <dt>ID lớp</dt><dd>${student.lop != null ? student.lop.idLop : ''}</dd>
                        <dt>Tên lớp</dt><dd>${student.lop != null ? student.lop.tenLop : ''}</dd>
                        <dt>Khối</dt><dd>${student.lop != null ? student.lop.khoi : ''}</dd>
                        <dt>Mã khóa</dt><dd>${student.lop != null && student.lop.khoaHoc != null ? student.lop.khoaHoc.idKhoa : ''}</dd>
                        <dt>Tên khóa</dt><dd>${student.lop != null && student.lop.khoaHoc != null ? student.lop.khoaHoc.tenKhoa : ''}</dd>
                        <dt>Năm học</dt><dd>${student.lop != null ? student.lop.namHoc : ''}</dd>
                    </dl>
                </div>
            </div>

            <div class="card info-card">
                <h3>Lịch sử lớp học</h3>
                <div class="timeline">
                    <c:forEach var="h" items="${classHistories}">
                        <div class="timeline-item">
                            <div class="timeline-head">
                                <span>${h.loaiChuyen}</span>
                                <span>${h.ngayChuyen}</span>
                            </div>
                            <div class="timeline-note">
                                <c:if test="${not empty h.lopCu || not empty h.lopMoi}">
                                    <div>Lớp: ${h.lopCu} -> ${h.lopMoi}</div>
                                </c:if>
                                <c:if test="${not empty h.truongCu || not empty h.truongMoi}">
                                    <div>Trường: ${h.truongCu} -> ${h.truongMoi}</div>
                                </c:if>
                                <c:if test="${not empty h.ghiChu}">
                                    <div>Ghi chú: ${h.ghiChu}</div>
                                </c:if>
                            </div>
                        </div>
                    </c:forEach>

                    <c:if test="${empty classHistories}">
                        <div class="empty-note">Chưa có lịch sử chuyển lớp/chuyển trường.</div>
                    </c:if>
                </div>
            </div>

            <div class="card info-card">
                <h3>Lịch sử chỉnh sửa hồ sơ</h3>
                <div class="timeline">
                    <c:forEach var="log" items="${editLogs}">
                        <div class="timeline-item">
                            <div class="timeline-head">
                                <span>${log.hanhDong} - ${log.user != null ? log.user.tenDangNhap : 'N/A'}</span>
                                <span>${log.thoiGian}</span>
                            </div>
                            <div class="timeline-note log-note">${log.noiDung}</div>
                        </div>
                    </c:forEach>

                    <c:if test="${empty editLogs}">
                        <div class="empty-note">Chưa có lịch sử chỉnh sửa hồ sơ.</div>
                    </c:if>
                </div>
            </div>

            <div class="page-actions">
                <a class="btn primary" href="<c:url value='/admin/student/${student.idHocSinh}/edit'/>">Chỉnh sửa</a>
                <a class="btn" href="<c:url value='/admin/student'/>">Quay lại danh sách</a>
            </div>
        </section>
    </main>
</div>

</body>
</html>
