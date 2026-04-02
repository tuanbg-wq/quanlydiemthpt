<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>${pageTitle}</title>
    <link rel="stylesheet" href="<c:url value='/css/admin-layout.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/admin/teacher-info.css'/>">
</head>
<body>

<div class="layout">
    <jsp:include page="/WEB-INF/views/admin/_sidebar.jsp"/>

    <main class="main teacher-info-page">
        <header class="topbar">
            <div class="topbar-left">
                <h1>ThĂ´ng tin giĂ¡o viĂªn</h1>
                <p>Xem há»“ sÆ¡ Ä‘áº§y Ä‘á»§ vĂ  lá»‹ch sá»­ cĂ´ng tĂ¡c táº¡i trÆ°á»ng.</p>
            </div>
            <div class="topbar-right">
                <a class="btn" href="<c:url value='/admin/teacher/${teacherInfo.idGiaoVien}/info/export/excel'/>">
                    Xuáº¥t Excel
                </a>
                <a class="btn" href="<c:url value='/admin/teacher/${teacherInfo.idGiaoVien}/info/export/pdf'/>">
                    Xuáº¥t PDF
                </a>
            </div>
        </header>

        <section class="content">
            <div class="card profile-summary">
                <c:choose>
                    <c:when test="${not empty teacherInfo.avatar}">
                        <img class="profile-avatar" src="<c:url value='${teacherInfo.avatar}'/>" alt="áº¢nh giĂ¡o viĂªn"/>
                    </c:when>
                    <c:otherwise>
                        <div class="avatar-fallback">GV</div>
                    </c:otherwise>
                </c:choose>
                <div class="profile-main">
                    <h2>${teacherInfo.hoTen}</h2>
                    <div class="profile-pills">
                        <span class="info-pill">MĂ£ GV: ${teacherInfo.idGiaoVien}</span>
                        <span class="info-pill">Tráº¡ng thĂ¡i: ${teacherInfo.trangThai}</span>
                        <span class="info-pill">Vai trĂ² hiá»‡n táº¡i: ${teacherInfo.currentRole}</span>
                        <span class="info-pill">NÄƒm há»c vai trĂ²: ${teacherInfo.currentRoleSchoolYear}</span>
                    </div>
                </div>
            </div>

            <div class="info-grid">
                <div class="card info-card">
                    <h3>ThĂ´ng tin cĂ¡ nhĂ¢n</h3>
                    <dl>
                        <dt>MĂ£ giĂ¡o viĂªn</dt><dd>${teacherInfo.idGiaoVien}</dd>
                        <dt>Há» vĂ  tĂªn</dt><dd>${teacherInfo.hoTen}</dd>
                        <dt>NgĂ y sinh</dt><dd>${teacherInfo.ngaySinh}</dd>
                        <dt>Giá»›i tĂ­nh</dt><dd>${teacherInfo.gioiTinh}</dd>
                        <dt>Sá»‘ Ä‘iá»‡n thoáº¡i</dt><dd>${teacherInfo.soDienThoai}</dd>
                        <dt>Email</dt><dd>${teacherInfo.email}</dd>
                        <dt>Äá»‹a chá»‰</dt><dd>${teacherInfo.diaChi}</dd>
                        <dt>Tráº¡ng thĂ¡i</dt><dd>${teacherInfo.trangThai}</dd>
                    </dl>
                </div>

                <div class="card info-card">
                    <h3>ThĂ´ng tin chuyĂªn mĂ´n</h3>
                    <dl>
                        <dt>ChuyĂªn mĂ´n</dt><dd>${teacherInfo.chuyenMon}</dd>
                        <dt>TrĂ¬nh Ä‘á»™ há»c váº¥n</dt><dd>${teacherInfo.trinhDo}</dd>
                        <dt>NgĂ y báº¯t Ä‘áº§u cĂ´ng tĂ¡c</dt><dd>${teacherInfo.ngayVaoLam}</dd>
                        <dt>Vai trĂ² hiá»‡n táº¡i</dt><dd>${teacherInfo.currentRole}</dd>
                        <dt>NÄƒm há»c Ă¡p dá»¥ng vai trĂ²</dt><dd>${teacherInfo.currentRoleSchoolYear}</dd>
                        <dt>Lá»›p bá»™ mĂ´n phá»¥ trĂ¡ch</dt><dd>${teacherInfo.currentSubjectClasses}</dd>
                        <dt>Ghi chĂº</dt><dd>${teacherInfo.ghiChu}</dd>
                    </dl>
                </div>
            </div>

            <div class="card history-card">
                <h3>Lá»‹ch sá»­ cĂ´ng tĂ¡c táº¡i trÆ°á»ng</h3>
                <div class="history-table-wrap">
                    <table class="history-table">
                        <thead>
                        <tr>
                            <th>Khoáº£ng thá»i gian</th>
                            <th>Vai trĂ²</th>
                            <th>Lá»›p Chá»§ nhiá»‡m</th>
                            <th>Lá»›p Bá»™ mĂ´n phá»¥ trĂ¡ch</th>
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
                                <td class="empty-note" colspan="4">ChÆ°a cĂ³ lá»‹ch sá»­ cĂ´ng tĂ¡c táº¡i trÆ°á»ng.</td>
                            </tr>
                        </c:if>
                        </tbody>
                    </table>
                </div>
            </div>

            <div class="page-actions">
                <a class="btn primary" href="<c:url value='/admin/teacher/${teacherInfo.idGiaoVien}/edit'/>">Chá»‰nh sá»­a</a>
                <a class="btn" href="<c:url value='/admin/teacher'/>">Quay láº¡i danh sĂ¡ch</a>
            </div>
        </section>
    </main>
</div>

</body>
</html>

