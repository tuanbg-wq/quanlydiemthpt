<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>${pageTitle}</title>
    <link rel="stylesheet" href="<c:url value='/css/admin-layout.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/teacher/teacher-home.css'/>">
</head>
<body>
<div class="layout">
    <jsp:include page="/WEB-INF/views/teacher/_sidebar.jsp"/>

    <main class="main teacher-page">
        <section class="teacher-home-card">
            <h1>ThĂ´ng tin cĂ¡ nhĂ¢n</h1>
            <p>Trang há»“ sÆ¡ cĂ¡ nhĂ¢n cá»§a giĂ¡o viĂªn chá»§ nhiá»‡m.</p>

            <div class="teacher-meta-grid">
                <div class="teacher-meta-item">
                    <span class="label">Há» tĂªn</span>
                    <span class="value">${empty scope.teacherName ? 'ChÆ°a xĂ¡c Ä‘á»‹nh' : scope.teacherName}</span>
                </div>
                <div class="teacher-meta-item">
                    <span class="label">MĂ£ giĂ¡o viĂªn</span>
                    <span class="value">${empty scope.teacherId ? '-' : scope.teacherId}</span>
                </div>
                <div class="teacher-meta-item">
                    <span class="label">Lá»›p chá»§ nhiá»‡m</span>
                    <span class="value">${empty scope.className ? 'ChÆ°a phĂ¢n cĂ´ng' : scope.className}</span>
                </div>
            </div>
        </section>
    </main>
</div>
</body>
</html>

