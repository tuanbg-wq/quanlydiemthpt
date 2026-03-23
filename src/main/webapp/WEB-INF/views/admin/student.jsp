<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

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

    <jsp:include page="/WEB-INF/views/admin/_sidebar.jsp"/>

    <main class="main student-list-page">

        <header class="topbar">

            <div class="topbar-left">
                <h1>Quản Lý Học Sinh</h1>
                <p>Quản lý thông tin học sinh trong hệ thống</p>
            </div>

            <div class="topbar-right">
                <a class="btn primary" href="<c:url value='/admin/student/create'/>">
                    + Thêm Học Sinh
                </a>
            </div>

        </header>

        <section class="content">

            <c:if test="${param.created == 'true'}">
                <div class="flash-message alert alert-success">
                    ✅ Thêm học sinh thành công!
                </div>
            </c:if>

            <c:if test="${param.updated == 'true'}">
                <div class="flash-message alert alert-info">
                    ✅ Cập nhật học sinh thành công!
                </div>
            </c:if>

            <c:if test="${param.deleted == 'true'}">
                <div class="flash-message alert alert-error">
                    ✅ Xóa học sinh thành công!
                </div>
            </c:if>

            <div class="card">

                <form class="filters" method="get" action="<c:url value='/admin/student'/>">

                    <div class="search-row">
                        <input type="text"
                               name="q"
                               placeholder="Tìm theo mã HS, tên, email..."
                               value="${search.q}">

                        <button class="btn" type="submit">
                            🔎 Tìm kiếm
                        </button>
                    </div>

                    <div class="filter-row">

                        <select name="courseId">
                            <option value="">-- Chọn khóa học --</option>

                            <c:forEach var="c" items="${courses}">
                                <option value="${c.idKhoa}"
                                        ${search.courseId == c.idKhoa ? 'selected' : ''}>
                                    ${c.idKhoa}
                                </option>
                            </c:forEach>
                        </select>

                        <select name="khoi">
                            <option value="">-- Chọn khối --</option>
                            <c:forEach var="g" items="${grades}">
                                <option value="${g}" ${search.khoi == g.toString() ? 'selected' : ''}>
                                    Khối ${g}
                                </option>
                            </c:forEach>
                        </select>

                        <select name="classId">
                            <option value="">-- Chọn lớp --</option>
                            <c:forEach var="cl" items="${classes}">
                                <option value="${cl.idLop}" ${search.classId == cl.idLop ? 'selected' : ''}>
                                    ${cl.maVaTenLop}
                                </option>
                            </c:forEach>
                        </select>

                        <select name="historyType">

                            <option value="">
                                -- Lịch sử chuyển --
                            </option>

                            <option value="CHUYEN_LOP"
                                ${search.historyType == 'CHUYEN_LOP' ? 'selected' : ''}>
                                Chuyển lớp
                            </option>

                            <option value="CHUYEN_TRUONG"
                                ${search.historyType == 'CHUYEN_TRUONG' ? 'selected' : ''}>
                                Chuyển trường
                            </option>

                        </select>
                        <button class="btn" type="submit">
                            ⚙ Lọc
                        </button>

                    </div>

                </form>

            </div>

            <div class="card">

                <h2>Danh Sách Học Sinh</h2>

                <div class="table-wrap">

                    <table class="table">

                        <thead>
                        <tr>
                            <th>Ảnh</th>
                            <th>Mã HS</th>
                            <th>Họ và Tên</th>
                            <th>Mã lớp - Tên lớp</th>
                            <th>Địa chỉ</th>
                            <th>Email</th>
                            <th>Ngày Nhập Học</th>

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
                                            <img class="avatar-img"
                                                 src="<c:url value='/uploads/${s.anh}'/>"
                                                 alt="avatar"/>
                                        </c:when>
                                        <c:otherwise>
                                            <div class="avatar">
                                                HS
                                            </div>
                                        </c:otherwise>
                                    </c:choose>
                                </td>

                                <td>
                                    <span class="pill">
                                        ${s.idHocSinh}
                                    </span>
                                </td>

                                <td>
                                    ${s.hoTen}
                                </td>

                                <td>
                                    <c:choose>
                                        <c:when test="${not empty s.lop}">
                                            ${s.lop.maVaTenLop}
                                        </c:when>
                                        <c:otherwise>
                                            -
                                        </c:otherwise>
                                    </c:choose>
                                </td>

                                <td>
                                    ${s.diaChi}
                                </td>

                                <td>
                                    ${s.email}
                                </td>

                                <td>
                                    ${s.ngayNhapHoc}
                                </td>

                                <c:if test="${showHistoryColumn}">
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty s.historyTypeDisplay}">
                                                <div class="history-title">
                                                    ${s.historyTypeDisplay}
                                                </div>
                                                <div class="history-detail">
                                                    ${s.historyDetail}
                                                </div>
                                            </c:when>
                                            <c:otherwise>
                                                -
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                </c:if>

                                <td class="actions">
                                    <div class="action-menu">
                                        <button type="button" class="action-toggle" onclick="toggleActionMenu(this)">
                                            ⋮
                                        </button>

                                        <div class="action-dropdown">
                                            <a class="dropdown-item"
                                               href="<c:url value='/admin/student/${s.idHocSinh}/edit'/>">
                                                Sửa
                                            </a>

                                            <a class="dropdown-item"
                                               href="<c:url value='/admin/student/${s.idHocSinh}/info'/>">
                                                Thông tin học sinh
                                            </a>

                                            <form method="post"
                                                  action="<c:url value='/admin/student/${s.idHocSinh}/delete'/>"
                                                  onsubmit="return confirm('Xóa học sinh này?')">
                                                <button type="submit" class="dropdown-item danger-item">
                                                    Xóa
                                                </button>
                                            </form>
                                        </div>
                                    </div>
                                </td>

                            </tr>
                        </c:forEach>

                        <c:if test="${empty students}">
                            <tr>
                                <td colspan="${showHistoryColumn ? 9 : 8}" class="empty-message">
                                    Không có học sinh nào.
                                </td>
                            </tr>
                        </c:if>

                        </tbody>

                    </table>

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
