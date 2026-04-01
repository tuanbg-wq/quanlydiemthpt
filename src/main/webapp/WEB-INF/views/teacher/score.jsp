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
            <h1>Quản lý điểm</h1>
            <p>Module quản lý điểm cho giáo viên chủ nhiệm đang được khởi tạo.</p>
            <div class="teacher-meta-grid">
                <div class="teacher-meta-item">
                    <span class="label">Lớp chủ nhiệm</span>
                    <span class="value">${empty scope.className ? 'Chưa phân công' : scope.className}</span>
                </div>
                <div class="teacher-meta-item">
                    <span class="label">Năm học</span>
                    <span class="value">${empty scope.schoolYear ? '-' : scope.schoolYear}</span>
                </div>
                <div class="teacher-meta-item">
                    <span class="label">Trạng thái</span>
                    <span class="value">Đang hoàn thiện</span>
                </div>
            </div>
        </section>
    </main>
</div>
</body>
</html>
