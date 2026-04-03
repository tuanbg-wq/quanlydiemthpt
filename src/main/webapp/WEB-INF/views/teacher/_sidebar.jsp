<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<aside class="sidebar">
    <div class="brand teacher-brand">Giáo Viên Chủ Nhiệm</div>

    <nav class="nav">
        <a class="nav-item ${activePage == 'dashboard' ? 'active' : ''}" href="<c:url value='/teacher/dashboard'/>">
            <svg fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round"
                      d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"/>
            </svg>
            Trang chủ
        </a>

        <a class="nav-item ${activePage == 'student' ? 'active' : ''}" href="<c:url value='/teacher/student'/>">
            <svg fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round"
                      d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z"/>
            </svg>
            Quản lý học sinh
        </a>

        <a class="nav-item ${activePage == 'score' ? 'active' : ''}" href="<c:url value='/teacher/score'/>">
            <svg fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round"
                      d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"/>
            </svg>
            Quản lý điểm
        </a>

        <a class="nav-item ${activePage == 'profile' ? 'active' : ''}" href="<c:url value='/teacher/profile'/>">
            <svg fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round"
                      d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/>
            </svg>
            Thông tin cá nhân
        </a>

        <div class="nav-spacer"></div>

        <c:if test="${not empty scope}">
            <div class="teacher-scope">
                <div class="scope-label">Lớp chủ nhiệm</div>
                <div class="scope-value">${empty scope.className ? 'Chưa phân công' : scope.className}</div>
            </div>
        </c:if>

        <form method="post" action="<c:url value='/logout'/>">
            <button class="nav-item logout-item" type="submit">
                <svg fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                    <path stroke-linecap="round" stroke-linejoin="round"
                          d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"/>
                </svg>
                Đăng xuất
            </button>
        </form>
    </nav>
</aside>
