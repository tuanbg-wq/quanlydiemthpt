<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>${pageTitle}</title>
    <link rel="stylesheet" href="<c:url value='/css/admin-layout.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/teacher-home.css'/>">
</head>
<body>
<div class="layout">
    <jsp:include page="/WEB-INF/views/teacher/_sidebar.jsp"/>

    <main class="main teacher-page">
        <section class="teacher-home-card">
            <h1>Trang chủ giáo viên chủ nhiệm</h1>
            <p>Đây là trang tổng quan tượng trưng cho luồng đăng nhập giáo viên chủ nhiệm.</p>

            <div class="teacher-meta-grid">
                <div class="teacher-meta-item">
                    <span class="label">Giáo viên</span>
                    <span class="value">${empty scope.teacherName ? 'Chưa xác định' : scope.teacherName}</span>
                </div>
                <div class="teacher-meta-item">
                    <span class="label">Mã giáo viên</span>
                    <span class="value">${empty scope.teacherId ? '-' : scope.teacherId}</span>
                </div>
                <div class="teacher-meta-item">
                    <span class="label">Lớp chủ nhiệm</span>
                    <span class="value">${empty scope.className ? 'Chưa phân công' : scope.className}</span>
                </div>
            </div>

            <div class="teacher-placeholder">
                Tiếp theo sẽ mở rộng dashboard chi tiết cho GVCN: sĩ số, điểm trung bình lớp, hạnh kiểm và theo dõi học sinh.
            </div>

            <div class="teacher-actions">
                <a class="btn primary" href="<c:url value='/teacher/student'/>">Quản lý học sinh</a>
                <a class="btn" href="<c:url value='/teacher/score'/>">Quản lý điểm</a>
            </div>
        </section>
    </main>
</div>
</body>
</html>
