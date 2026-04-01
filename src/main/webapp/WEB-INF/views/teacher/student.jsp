<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>${pageTitle}</title>
    <link rel="stylesheet" href="<c:url value='/css/admin-layout.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/student-list.css'/>">
</head>
<body>
<div class="layout">
    <jsp:include page="/WEB-INF/views/teacher/_sidebar.jsp"/>

    <main class="main student-list-page">
        <header class="topbar">
            <div class="topbar-left">
                <h1>Quản lý học sinh chủ nhiệm</h1>
                <p>
                    Lớp:
                    <strong>${empty scope.className ? 'Chưa phân công' : scope.className}</strong>
                </p>
            </div>
            <div class="topbar-right">
                <a class="btn primary" href="<c:url value='/teacher/student/create'/>">+ Thêm học sinh</a>
            </div>
        </header>

        <section class="content">
            <c:if test="${param.created == 'true'}">
                <div class="flash-message alert alert-success">Thêm học sinh thành công.</div>
            </c:if>
            <c:if test="${param.updated == 'true'}">
                <div class="flash-message alert alert-info">Cập nhật học sinh thành công.</div>
            </c:if>
            <c:if test="${param.deleted == 'true'}">
                <div class="flash-message alert alert-error">Xóa học sinh thành công.</div>
            </c:if>
            <c:if test="${not empty flashMessage}">
                <div class="flash-message alert ${flashType == 'success' ? 'alert-success' : 'alert-error'}">${flashMessage}</div>
            </c:if>
            <c:if test="${not empty warningMessage}">
                <div class="flash-message alert alert-error">${warningMessage}</div>
            </c:if>

            <div class="card">
                <form class="filters" method="get" action="<c:url value='/teacher/student'/>">
                    <div class="search-row">
                        <input type="text"
                               name="q"
                               placeholder="Tìm theo mã HS, tên, email..."
                               value="${search.q}">
                        <button class="btn" type="submit">Tìm kiếm</button>
                    </div>

                    <div class="filter-row">
                        <select name="historyType">
                            <option value="">-- Lịch sử chuyển --</option>
                            <option value="CHUYEN_LOP" ${search.historyType == 'CHUYEN_LOP' ? 'selected' : ''}>Chuyển lớp</option>
                            <option value="CHUYEN_TRUONG" ${search.historyType == 'CHUYEN_TRUONG' ? 'selected' : ''}>Chuyển trường</option>
                        </select>
                        <button class="btn" type="submit">Lọc</button>
                    </div>
                </form>
            </div>

            <div class="card">
                <h2>Danh sách học sinh</h2>

                <div class="table-wrap">
                    <table class="table">
                        <thead>
                        <tr>
                            <th>Ảnh</th>
                            <th>Mã HS</th>
                            <th>Họ và tên</th>
                            <th>Mã lớp - Tên lớp</th>
                            <th>Hạnh kiểm</th>
                            <th>Địa chỉ</th>
                            <th>Email</th>
                            <th>Ngày nhập học</th>
                            <c:if test="${showHistoryColumn}">
                                <th>Lịch sử chuyển</th>
                            </c:if>
                            <th>Hành động</th>
                        </tr>
                        </thead>

                        <tbody>
                        <c:forEach var="s" items="${students}">
                            <tr>
                                <td>
                                    <c:choose>
                                        <c:when test="${not empty s.anh}">
                                            <c:choose>
                                                <c:when test="${fn:startsWith(s.anh, '/uploads/')}">
                                                    <img class="avatar-img" src="<c:url value='${s.anh}'/>" alt="avatar"/>
                                                </c:when>
                                                <c:otherwise>
                                                    <img class="avatar-img" src="<c:url value='/uploads/${s.anh}'/>" alt="avatar"/>
                                                </c:otherwise>
                                            </c:choose>
                                        </c:when>
                                        <c:otherwise>
                                            <div class="avatar">HS</div>
                                        </c:otherwise>
                                    </c:choose>
                                </td>

                                <td><span class="pill">${s.idHocSinh}</span></td>
                                <td>${s.hoTen}</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${not empty s.lop}">
                                            ${s.lop.maVaTenLop}
                                        </c:when>
                                        <c:otherwise>-</c:otherwise>
                                    </c:choose>
                                </td>
                                <td>${empty s.hanhKiemTongHienThi ? 'Chưa có' : s.hanhKiemTongHienThi}</td>
                                <td>${s.diaChi}</td>
                                <td>${s.email}</td>
                                <td>${s.ngayNhapHocHienThi}</td>

                                <c:if test="${showHistoryColumn}">
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty s.historyTypeDisplay}">
                                                <div class="history-title">${s.historyTypeDisplay}</div>
                                                <div class="history-detail">${s.historyDetail}</div>
                                            </c:when>
                                            <c:otherwise>-</c:otherwise>
                                        </c:choose>
                                    </td>
                                </c:if>

                                <td class="actions">
                                    <div class="action-menu">
                                        <button type="button" class="action-toggle" onclick="toggleActionMenu(this)">⋮</button>
                                        <div class="action-dropdown">
                                            <a class="dropdown-item" href="<c:url value='/teacher/student/${s.idHocSinh}/edit'/>">Sửa</a>
                                            <a class="dropdown-item" href="<c:url value='/teacher/student/${s.idHocSinh}/info'/>">Thông tin học sinh</a>
                                            <form method="post"
                                                  action="<c:url value='/teacher/student/${s.idHocSinh}/delete'/>"
                                                  onsubmit="return confirm('Xóa học sinh này?')">
                                                <button type="submit" class="dropdown-item danger-item">Xóa</button>
                                            </form>
                                        </div>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>

                        <c:if test="${empty students}">
                            <tr>
                                <td colspan="${showHistoryColumn ? 10 : 9}" class="empty-message">Không có học sinh nào.</td>
                            </tr>
                        </c:if>
                        </tbody>
                    </table>
                </div>
            </div>

            <div class="card history-log-card">
                <h2>Lịch sử thao tác học sinh (5 gần nhất)</h2>

                <div class="history-log-list">
                    <c:forEach var="log" items="${studentHistoryLogs}">
                        <article class="history-log-item">
                            <div class="history-log-head">
                                <div class="history-log-title">
                                    ${log.hanhDongHienThi} - ${log.user != null ? log.user.tenDangNhap : 'N/A'}
                                </div>
                                <div class="history-log-time">${log.thoiGianHienThi}</div>
                            </div>
                            <div class="history-log-student">
                                Học sinh:
                                <c:choose>
                                    <c:when test="${not empty studentDisplayById[log.idBanGhi]}">
                                        ${studentDisplayById[log.idBanGhi]}
                                    </c:when>
                                    <c:otherwise>${empty log.idBanGhi ? '(không rõ)' : log.idBanGhi}</c:otherwise>
                                </c:choose>
                            </div>
                            <div class="history-log-content">
                                <c:out value="${log.noiDung}"/>
                            </div>
                            <div class="history-log-footer">
                                <c:if test="${not empty log.idBanGhi}">
                                    <a class="history-log-link" href="<c:url value='/teacher/student/${log.idBanGhi}/info'/>">Xem chi tiết</a>
                                </c:if>
                            </div>
                        </article>
                    </c:forEach>

                    <c:if test="${empty studentHistoryLogs}">
                        <div class="empty-message">Không có lịch sử thao tác khớp bộ lọc hiện tại.</div>
                    </c:if>
                </div>
            </div>
        </section>
    </main>
</div>

<script>
    function toggleActionMenu(button) {
        const currentMenu = button.nextElementSibling;
        const tableWrap = button.closest('.table-wrap');
        const currentRow = button.closest('tr');
        const buttonRect = button.getBoundingClientRect();

        document.querySelectorAll('.action-dropdown').forEach(menu => {
            if (menu !== currentMenu) {
                menu.classList.remove('show');
                menu.classList.remove('open-up');
            }
        });
        document.querySelectorAll('.table tbody tr.menu-open').forEach(row => {
            row.classList.remove('menu-open');
        });

        currentMenu.classList.toggle('show');
        if (currentMenu.classList.contains('show')) {
            if (currentRow) {
                currentRow.classList.add('menu-open');
            }
            currentMenu.classList.remove('open-up');
            if (tableWrap) {
                const wrapRect = tableWrap.getBoundingClientRect();
                const menuRect = currentMenu.getBoundingClientRect();
                const spaceBelow = wrapRect.bottom - buttonRect.bottom;
                const spaceAbove = buttonRect.top - wrapRect.top;
                if (menuRect.height + 10 > spaceBelow && spaceAbove > spaceBelow) {
                    currentMenu.classList.add('open-up');
                }
            }
        } else if (currentRow) {
            currentRow.classList.remove('menu-open');
        }
    }

    document.addEventListener('click', function (event) {
        if (!event.target.closest('.action-menu')) {
            document.querySelectorAll('.action-dropdown').forEach(menu => {
                menu.classList.remove('show');
                menu.classList.remove('open-up');
            });
            document.querySelectorAll('.table tbody tr.menu-open').forEach(row => {
                row.classList.remove('menu-open');
            });
        }
    });
</script>
</body>
</html>
