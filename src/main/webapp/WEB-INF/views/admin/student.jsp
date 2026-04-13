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
    <link rel="stylesheet" href="<c:url value='/css/admin/student/student-list.css'/>">
</head>

<body>
<div class="layout">
    <jsp:include page="/WEB-INF/views/admin/_sidebar.jsp"/>

    <main class="main student-list-page">
        <header class="topbar">
            <div class="topbar-left">
                <h1>Quản lý học sinh</h1>
                <p>Quản lý thông tin học sinh trong hệ thống</p>
            </div>
            <div class="topbar-right">
                <a class="btn primary" href="<c:url value='/admin/student/create'/>">+ Thêm học sinh</a>
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

            <div class="card">
                <form class="filters" method="get" action="<c:url value='/admin/student'/>">
                    <c:if test="${showAllHistory}">
                        <input type="hidden" name="historyMode" value="all">
                    </c:if>
                    <div class="search-row">
                        <input type="text"
                               name="q"
                               placeholder="Tìm theo mã HS, tên, email..."
                               value="${search.q}">
                        <button class="btn" type="submit">Tìm kiếm</button>
                    </div>

                    <div class="filter-row">
                        <select name="courseId">
                            <option value="">-- Chọn khóa học --</option>
                            <c:forEach var="c" items="${courses}">
                                <option value="${c.idKhoa}" ${search.courseId == c.idKhoa ? 'selected' : ''}>
                                    ${c.idKhoa}
                                </option>
                            </c:forEach>
                        </select>

                        <select name="khoi">
                            <option value="">-- Chọn khối --</option>
                            <c:forEach var="g" items="${grades}">
                                <option value="${g}" ${search.khoi == g.toString() ? 'selected' : ''}>Khối ${g}</option>
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

                        <select name="hanhKiem">
                            <option value="">-- Hạnh kiểm --</option>
                            <option value="tot" ${search.hanhKiem == 'tot' ? 'selected' : ''}>Tốt</option>
                            <option value="kha" ${search.hanhKiem == 'kha' ? 'selected' : ''}>Khá</option>
                            <option value="trung_binh" ${search.hanhKiem == 'trung_binh' || search.hanhKiem == 'tb' ? 'selected' : ''}>Trung bình</option>
                            <option value="yeu" ${search.hanhKiem == 'yeu' ? 'selected' : ''}>Yếu</option>
                            <option value="chua_co" ${search.hanhKiem == 'chua_co' ? 'selected' : ''}>Chưa có</option>
                        </select>

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
                                    <td class="history-cell">
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
                                            <a class="dropdown-item" href="<c:url value='/admin/student/${s.idHocSinh}/edit'/>">Sửa</a>
                                            <a class="dropdown-item" href="<c:url value='/admin/student/${s.idHocSinh}/info'/>">Thông tin học sinh</a>
                                            <form method="post"
                                                  action="<c:url value='/admin/student/${s.idHocSinh}/delete'/>"
                                                  data-student-id="${s.idHocSinh}"
                                                  data-student-name="${fn:escapeXml(s.hoTen)}"
                                                  onsubmit="return confirmDeleteStudent(this);">
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

                <c:if test="${totalStudents > 0}">
                    <div class="pagination-wrap">
                        <div class="pagination-summary">
                            Hiển thị
                            ${((currentPage - 1) * pageSize) + 1}
                            -
                            ${((currentPage - 1) * pageSize) + fn:length(students)}
                            / ${totalStudents} học sinh
                        </div>

                        <div class="pagination">
                            <c:if test="${hasPrevPage}">
                                <c:url var="prevPageUrl" value="/admin/student">
                                    <c:param name="page" value="${currentPage - 1}"/>
                                    <c:if test="${not empty search.q}">
                                        <c:param name="q" value="${search.q}"/>
                                    </c:if>
                                    <c:if test="${not empty search.courseId}">
                                        <c:param name="courseId" value="${search.courseId}"/>
                                    </c:if>
                                    <c:if test="${not empty search.khoi}">
                                        <c:param name="khoi" value="${search.khoi}"/>
                                    </c:if>
                                    <c:if test="${not empty search.classId}">
                                        <c:param name="classId" value="${search.classId}"/>
                                    </c:if>
                                    <c:if test="${not empty search.historyType}">
                                        <c:param name="historyType" value="${search.historyType}"/>
                                    </c:if>
                                    <c:if test="${not empty search.hanhKiem}">
                                        <c:param name="hanhKiem" value="${search.hanhKiem}"/>
                                    </c:if>
                                    <c:if test="${showAllHistory}">
                                        <c:param name="historyMode" value="all"/>
                                    </c:if>
                                </c:url>
                                <a class="page-btn" href="${prevPageUrl}">Trước</a>
                            </c:if>

                            <c:forEach var="p" begin="1" end="${totalPages}">
                                <c:url var="pageUrl" value="/admin/student">
                                    <c:param name="page" value="${p}"/>
                                    <c:if test="${not empty search.q}">
                                        <c:param name="q" value="${search.q}"/>
                                    </c:if>
                                    <c:if test="${not empty search.courseId}">
                                        <c:param name="courseId" value="${search.courseId}"/>
                                    </c:if>
                                    <c:if test="${not empty search.khoi}">
                                        <c:param name="khoi" value="${search.khoi}"/>
                                    </c:if>
                                    <c:if test="${not empty search.classId}">
                                        <c:param name="classId" value="${search.classId}"/>
                                    </c:if>
                                    <c:if test="${not empty search.historyType}">
                                        <c:param name="historyType" value="${search.historyType}"/>
                                    </c:if>
                                    <c:if test="${not empty search.hanhKiem}">
                                        <c:param name="hanhKiem" value="${search.hanhKiem}"/>
                                    </c:if>
                                    <c:if test="${showAllHistory}">
                                        <c:param name="historyMode" value="all"/>
                                    </c:if>
                                </c:url>
                                <a class="page-btn ${p == currentPage ? 'active' : ''}" href="${pageUrl}">${p}</a>
                            </c:forEach>

                            <c:if test="${hasNextPage}">
                                <c:url var="nextPageUrl" value="/admin/student">
                                    <c:param name="page" value="${currentPage + 1}"/>
                                    <c:if test="${not empty search.q}">
                                        <c:param name="q" value="${search.q}"/>
                                    </c:if>
                                    <c:if test="${not empty search.courseId}">
                                        <c:param name="courseId" value="${search.courseId}"/>
                                    </c:if>
                                    <c:if test="${not empty search.khoi}">
                                        <c:param name="khoi" value="${search.khoi}"/>
                                    </c:if>
                                    <c:if test="${not empty search.classId}">
                                        <c:param name="classId" value="${search.classId}"/>
                                    </c:if>
                                    <c:if test="${not empty search.historyType}">
                                        <c:param name="historyType" value="${search.historyType}"/>
                                    </c:if>
                                    <c:if test="${not empty search.hanhKiem}">
                                        <c:param name="hanhKiem" value="${search.hanhKiem}"/>
                                    </c:if>
                                    <c:if test="${showAllHistory}">
                                        <c:param name="historyMode" value="all"/>
                                    </c:if>
                                </c:url>
                                <a class="page-btn" href="${nextPageUrl}">Sau</a>
                            </c:if>
                        </div>
                    </div>
                </c:if>
            </div>

            <div class="card history-log-card">
                <h2>Lịch sử thao tác học sinh (5 gần nhất)</h2>

                <div class="history-log-list">
                    <c:forEach var="log" items="${studentHistoryLogs}">
                        <article class="history-log-item">
                            <div class="history-log-head">
                                <div class="history-log-title">
                                    ${log.hanhDongHienThi} - Người thao tác: ${log.nguoiThaoTacHienThi}
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
                        </article>
                    </c:forEach>

                    <c:if test="${empty studentHistoryLogs}">
                        <div class="empty-message">Không có lịch sử thao tác khớp bộ lọc hiện tại.</div>
                    </c:if>

                    <c:if test="${hasMoreHistory}">
                        <div class="history-log-list-footer">
                            <c:url var="moreHistoryUrl" value="/admin/student">
                                <c:param name="historyMode" value="all"/>
                                <c:if test="${not empty search.q}">
                                    <c:param name="q" value="${search.q}"/>
                                </c:if>
                                <c:if test="${not empty search.courseId}">
                                    <c:param name="courseId" value="${search.courseId}"/>
                                </c:if>
                                <c:if test="${not empty search.khoi}">
                                    <c:param name="khoi" value="${search.khoi}"/>
                                </c:if>
                                <c:if test="${not empty search.classId}">
                                    <c:param name="classId" value="${search.classId}"/>
                                </c:if>
                                <c:if test="${not empty search.historyType}">
                                    <c:param name="historyType" value="${search.historyType}"/>
                                </c:if>
                                <c:if test="${not empty search.hanhKiem}">
                                    <c:param name="hanhKiem" value="${search.hanhKiem}"/>
                                </c:if>
                            </c:url>
                            <a class="history-log-link" href="${moreHistoryUrl}">Xem thêm lịch sử thao tác</a>
                        </div>
                    </c:if>

                    <c:if test="${showAllHistory and not empty studentHistoryLogs}">
                        <div class="history-log-list-footer">
                            <c:url var="recentHistoryUrl" value="/admin/student">
                                <c:if test="${not empty search.q}">
                                    <c:param name="q" value="${search.q}"/>
                                </c:if>
                                <c:if test="${not empty search.courseId}">
                                    <c:param name="courseId" value="${search.courseId}"/>
                                </c:if>
                                <c:if test="${not empty search.khoi}">
                                    <c:param name="khoi" value="${search.khoi}"/>
                                </c:if>
                                <c:if test="${not empty search.classId}">
                                    <c:param name="classId" value="${search.classId}"/>
                                </c:if>
                                <c:if test="${not empty search.historyType}">
                                    <c:param name="historyType" value="${search.historyType}"/>
                                </c:if>
                                <c:if test="${not empty search.hanhKiem}">
                                    <c:param name="hanhKiem" value="${search.hanhKiem}"/>
                                </c:if>
                            </c:url>
                            <a class="history-log-link" href="${recentHistoryUrl}">Thu gọn về 5 gần nhất</a>
                        </div>
                    </c:if>
                </div>
            </div>
        </section>
    </main>
</div>

<script>
    function confirmDeleteStudent(form) {
        if (!form) return false;
        const studentId = (form.getAttribute('data-student-id') || '').trim();
        const studentName = (form.getAttribute('data-student-name') || '').trim();
        let display = 'học sinh này';
        if (studentName && studentId) display = studentName + ' (' + studentId + ')';
        else if (studentName) display = studentName;
        else if (studentId) display = 'mã HS ' + studentId;
        return confirm('Bạn có chắc muốn xóa ' + display + ' không?\nHành động này không thể hoàn tác.');
    }

    function closeActionMenus() {
        document.querySelectorAll('.action-dropdown.show').forEach(menu => {
            menu.classList.remove('show', 'open-up');
        });
        document.querySelectorAll('.table tbody tr.menu-open').forEach(row => {
            row.classList.remove('menu-open');
        });
    }

    function toggleActionMenu(button) {
        const menu = button ? button.nextElementSibling : null;
        const row = button ? button.closest('tr') : null;
        if (!menu) return;

        const wasOpen = menu.classList.contains('show');
        closeActionMenus();
        if (wasOpen) return;

        const btnRect = button.getBoundingClientRect();
        const spaceBelow = window.innerHeight - btnRect.bottom;
        const menuHeight = 120;
        if (spaceBelow < menuHeight) {
            menu.classList.add('open-up');
        }

        menu.classList.add('show');
        if (row) row.classList.add('menu-open');
    }

    document.addEventListener('click', function (e) {
        if (!e.target.closest('.action-menu')) {
            closeActionMenus();
        }
    });

    document.addEventListener('scroll', closeActionMenus, true);
    window.addEventListener('resize', closeActionMenus);
</script>

</body>
</html>


