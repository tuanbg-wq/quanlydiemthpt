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
            <h1>Quáº£n lĂ½ Ä‘iá»ƒm</h1>
            <p>Module quáº£n lĂ½ Ä‘iá»ƒm cho giĂ¡o viĂªn chá»§ nhiá»‡m Ä‘ang Ä‘Æ°á»£c khá»Ÿi táº¡o.</p>
            <div class="teacher-meta-grid">
                <div class="teacher-meta-item">
                    <span class="label">Lá»›p chá»§ nhiá»‡m</span>
                    <span class="value">${empty scope.className ? 'ChÆ°a phĂ¢n cĂ´ng' : scope.className}</span>
                </div>
                <div class="teacher-meta-item">
                    <span class="label">NÄƒm há»c</span>
                    <span class="value">${empty scope.schoolYear ? '-' : scope.schoolYear}</span>
                </div>
                <div class="teacher-meta-item">
                    <span class="label">Tráº¡ng thĂ¡i</span>
                    <span class="value">Äang hoĂ n thiá»‡n</span>
                </div>
            </div>
        </section>
    </main>
</div>
</body>
</html>

