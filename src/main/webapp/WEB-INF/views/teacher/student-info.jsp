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
    <link rel="stylesheet" href="<c:url value='/css/teacher/student/student-info.css'/>">
</head>
<body>
<div class="layout">
    <jsp:include page="/WEB-INF/views/teacher/_sidebar.jsp"/>

    <main class="main student-info-page">
        <header class="topbar">
            <div class="topbar-left">
                <h1>Thông tin học sinh</h1>
                <p>Xem hồ sơ, lịch sử chuyển lớp/chuyển trường và lịch sử thao tác.</p>
            </div>
        </header>

        <section class="content">
            <div class="card profile-card">
                <c:choose>
                    <c:when test="${not empty student.anh}">
                        <c:choose>
                            <c:when test="${fn:startsWith(student.anh, '/uploads/')}">
                                <img class="profile-avatar" src="<c:url value='${student.anh}'/>" alt="avatar"/>
                            </c:when>
                            <c:otherwise>
                                <img class="profile-avatar" src="<c:url value='/uploads/${student.anh}'/>" alt="avatar"/>
                            </c:otherwise>
                        </c:choose>
                    </c:when>
                    <c:otherwise>
                        <div class="avatar-fallback">HS</div>
                    </c:otherwise>
                </c:choose>

                <div class="profile-meta">
                    <h2>${student.hoTen}</h2>
                    <div class="meta-row">
                        <span class="pill">Mã HS: ${student.idHocSinh}</span>
                        <span class="pill">Trạng thái: ${empty student.trangThaiHienThi ? '(trống)' : student.trangThaiHienThi}</span>
                        <span class="pill">Lớp: ${student.lop != null ? student.lop.maVaTenLop : '(trống)'}</span>
                        <span class="pill">Khối: ${student.lop != null ? student.lop.khoi : '(trống)'}</span>
                        <span class="pill">Khóa: ${empty student.khoaHienThi ? '(trống)' : student.khoaHienThi}</span>
                    </div>
                </div>
            </div>

            <div class="info-grid">
                <div class="card info-card">
                    <h3>Thông tin cá nhân</h3>
                    <dl>
                        <dt>Họ tên</dt><dd>${student.hoTen}</dd>
                        <dt>Ngày sinh</dt><dd>${empty student.ngaySinhHienThi ? '(trống)' : student.ngaySinhHienThi}</dd>
                        <dt>Giới tính</dt><dd>${empty student.gioiTinhHienThi ? '(trống)' : student.gioiTinhHienThi}</dd>
                        <dt>Nơi sinh</dt><dd>${empty student.noiSinh ? '(trống)' : student.noiSinh}</dd>
                        <dt>Dân tộc</dt><dd>${empty student.danToc ? '(trống)' : student.danToc}</dd>
                        <dt>Ngày nhập học</dt><dd>${empty student.ngayNhapHocHienThi ? '(trống)' : student.ngayNhapHocHienThi}</dd>
                        <dt>Khóa</dt><dd>${empty student.khoaHienThi ? '(trống)' : student.khoaHienThi}</dd>
                    </dl>
                </div>

                <div class="card info-card">
                    <h3>Liên hệ</h3>
                    <dl>
                        <dt>Số điện thoại</dt><dd>${empty student.soDienThoai ? '(trống)' : student.soDienThoai}</dd>
                        <dt>Email</dt><dd>${empty student.email ? '(trống)' : student.email}</dd>
                        <dt>Địa chỉ</dt><dd>${empty student.diaChi ? '(trống)' : student.diaChi}</dd>
                    </dl>
                </div>

                <div class="card info-card">
                    <h3>Hạnh kiểm</h3>
                    <dl>
                        <dt>Học kỳ I</dt><dd>${empty student.hanhKiemHocKy1HienThi ? '(trống)' : student.hanhKiemHocKy1HienThi}</dd>
                        <dt>Học kỳ II</dt><dd>${empty student.hanhKiemHocKy2HienThi ? '(trống)' : student.hanhKiemHocKy2HienThi}</dd>
                        <dt>Cả năm</dt><dd>${empty student.hanhKiemCaNamHienThi ? '(trống)' : student.hanhKiemCaNamHienThi}</dd>
                    </dl>
                </div>

                <div class="card info-card">
                    <h3>Thông tin phụ huynh</h3>
                    <dl>
                        <dt>Họ tên cha</dt><dd>${empty student.hoTenCha ? '(trống)' : student.hoTenCha}</dd>
                        <dt>SĐT cha</dt><dd>${empty student.sdtCha ? '(trống)' : student.sdtCha}</dd>
                        <dt>Họ tên mẹ</dt><dd>${empty student.hoTenMe ? '(trống)' : student.hoTenMe}</dd>
                        <dt>SĐT mẹ</dt><dd>${empty student.sdtMe ? '(trống)' : student.sdtMe}</dd>
                    </dl>
                </div>
            </div>

            <div class="card info-card">
                <h3>Lịch sử lớp học</h3>
                <div class="timeline">
                    <c:forEach var="h" items="${classHistories}">
                        <div class="timeline-item">
                            <div class="timeline-head">
                                <span>${h.loaiChuyenHienThi}</span>
                                <span>${h.ngayChuyenHienThi}</span>
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
                <h3>Lịch sử thao tác hồ sơ</h3>
                <div class="timeline">
                    <c:forEach var="log" items="${editLogs}">
                        <div class="timeline-item">
                            <div class="timeline-head">
                                <span>${log.hanhDongHienThi} - Người thao tác: ${log.nguoiThaoTacHienThi}</span>
                                <span>${log.thoiGianHienThi}</span>
                            </div>
                            <div class="timeline-note log-note">${log.noiDung}</div>
                        </div>
                    </c:forEach>

                    <c:if test="${empty editLogs}">
                        <div class="empty-note">Chưa có lịch sử thao tác.</div>
                    </c:if>
                </div>
            </div>

            <div class="page-actions">
                <a class="btn primary" href="<c:url value='/teacher/student/${student.idHocSinh}/edit'/>">Chỉnh sửa</a>
                <a class="btn" href="<c:url value='/teacher/student'/>">Quay lại danh sách</a>
            </div>
        </section>
    </main>
</div>
</body>
</html>
