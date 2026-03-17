<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>${pageTitle}</title>
    <link rel="stylesheet" href="<c:url value='/css/admin-layout.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/teacher-info.css'/>">
</head>
<body>

<div class="layout">
    <jsp:include page="/WEB-INF/views/admin/_sidebar.jsp"/>

    <main class="main teacher-info-page">
        <header class="topbar">
            <div class="topbar-left">
                <h1>Thông tin giáo viên</h1>
                <p>Xem hồ sơ đầy đủ và lịch sử công tác tại trường.</p>
            </div>
            <div class="topbar-right">
                <a class="btn" href="<c:url value='/admin/teacher/${teacherInfo.idGiaoVien}/info/export/excel'/>">
                    Xuất Excel
                </a>
                <a class="btn" href="<c:url value='/admin/teacher/${teacherInfo.idGiaoVien}/info/export/pdf'/>">
                    Xuất PDF
                </a>
            </div>
        </header>

        <section class="content">
            <div class="card profile-summary">
                <c:choose>
                    <c:when test="${not empty teacherInfo.avatar}">
                        <img class="profile-avatar" src="<c:url value='${teacherInfo.avatar}'/>" alt="Ảnh giáo viên"/>
                    </c:when>
                    <c:otherwise>
                        <div class="avatar-fallback">GV</div>
                    </c:otherwise>
                </c:choose>
                <div class="profile-main">
                    <h2>${teacherInfo.hoTen}</h2>
                    <div class="profile-pills">
                        <span class="info-pill">Mã GV: ${teacherInfo.idGiaoVien}</span>
                        <span class="info-pill">Trạng thái: ${teacherInfo.trangThai}</span>
                        <span class="info-pill">Vai trò hiện tại: ${teacherInfo.currentRole}</span>
                        <span class="info-pill">Năm học vai trò: ${teacherInfo.currentRoleSchoolYear}</span>
                    </div>
                </div>
            </div>

            <div class="info-grid">
                <div class="card info-card">
                    <h3>Thông tin cá nhân</h3>
                    <dl>
                        <dt>Mã giáo viên</dt><dd>${teacherInfo.idGiaoVien}</dd>
                        <dt>Họ và tên</dt><dd>${teacherInfo.hoTen}</dd>
                        <dt>Ngày sinh</dt><dd>${teacherInfo.ngaySinh}</dd>
                        <dt>Giới tính</dt><dd>${teacherInfo.gioiTinh}</dd>
                        <dt>Số điện thoại</dt><dd>${teacherInfo.soDienThoai}</dd>
                        <dt>Email</dt><dd>${teacherInfo.email}</dd>
                        <dt>Địa chỉ</dt><dd>${teacherInfo.diaChi}</dd>
                        <dt>Trạng thái</dt><dd>${teacherInfo.trangThai}</dd>
                    </dl>
                </div>

                <div class="card info-card">
                    <h3>Thông tin chuyên môn</h3>
                    <dl>
                        <dt>Chuyên môn</dt><dd>${teacherInfo.chuyenMon}</dd>
                        <dt>Trình độ học vấn</dt><dd>${teacherInfo.trinhDo}</dd>
                        <dt>Ngày bắt đầu công tác</dt><dd>${teacherInfo.ngayVaoLam}</dd>
                        <dt>Vai trò hiện tại</dt><dd>${teacherInfo.currentRole}</dd>
                        <dt>Năm học áp dụng vai trò</dt><dd>${teacherInfo.currentRoleSchoolYear}</dd>
                        <dt>Lớp bộ môn phụ trách</dt><dd>${teacherInfo.currentSubjectClasses}</dd>
                        <dt>Ghi chú</dt><dd>${teacherInfo.ghiChu}</dd>
                    </dl>
                </div>
            </div>

            <div class="card history-card">
                <h3>Lịch sử công tác tại trường</h3>
                <div class="history-table-wrap">
                    <table class="history-table">
                        <thead>
                        <tr>
                            <th>Khoảng thời gian</th>
                            <th>Vai trò</th>
                            <th>Lớp Chủ nhiệm</th>
                            <th>Lớp Bộ môn phụ trách</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="item" items="${teacherInfo.workHistory}">
                            <tr>
                                <td>${item.schoolYear}</td>
                                <td>${item.roleName}</td>
                                <td>${item.homeroomClasses}</td>
                                <td>${item.subjectClassNames}</td>
                            </tr>
                        </c:forEach>

                        <c:if test="${empty teacherInfo.workHistory}">
                            <tr>
                                <td class="empty-note" colspan="4">Chưa có lịch sử công tác tại trường.</td>
                            </tr>
                        </c:if>
                        </tbody>
                    </table>
                </div>
            </div>

            <div class="page-actions">
                <a class="btn primary" href="<c:url value='/admin/teacher/${teacherInfo.idGiaoVien}/edit'/>">Chỉnh sửa</a>
                <a class="btn" href="<c:url value='/admin/teacher'/>">Quay lại danh sách</a>
            </div>
        </section>
    </main>
</div>

</body>
</html>
