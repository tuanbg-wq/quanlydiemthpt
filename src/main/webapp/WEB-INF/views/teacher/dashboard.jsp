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
            <h1>Trang chá»§ giĂ¡o viĂªn chá»§ nhiá»‡m</h1>
            <p>ÄĂ¢y lĂ  trang tá»•ng quan tÆ°á»£ng trÆ°ng cho luá»“ng Ä‘Äƒng nháº­p giĂ¡o viĂªn chá»§ nhiá»‡m.</p>

            <div class="teacher-meta-grid">
                <div class="teacher-meta-item">
                    <span class="label">GiĂ¡o viĂªn</span>
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

            <div class="teacher-placeholder">
                Tiáº¿p theo sáº½ má»Ÿ rá»™ng dashboard chi tiáº¿t cho GVCN: sÄ© sá»‘, Ä‘iá»ƒm trung bĂ¬nh lá»›p, háº¡nh kiá»ƒm vĂ  theo dĂµi há»c sinh.
            </div>

            <div class="teacher-actions">
                <a class="btn primary" href="<c:url value='/teacher/student'/>">Quáº£n lĂ½ há»c sinh</a>
                <a class="btn" href="<c:url value='/teacher/score'/>">Quáº£n lĂ½ Ä‘iá»ƒm</a>
            </div>
        </section>
    </main>
</div>
</body>
</html>

