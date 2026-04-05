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
    <link rel="stylesheet" href="<c:url value='/css/teacher/student/student-list.css'/>">
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
                    <span> | Năm học: <strong>${empty scope.schoolYear ? '-' : scope.schoolYear}</strong></span>
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

                    <div class="filter-row teacher-filter-row">
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
                                        <button type="button" class="action-toggle" onclick="toggleActionMenu(this)">&#8942;</button>
                                        <div class="action-dropdown">
                                            <a class="dropdown-item" href="<c:url value='/teacher/student/${s.idHocSinh}/edit'/>">Sửa</a>
                                            <a class="dropdown-item" href="<c:url value='/teacher/student/${s.idHocSinh}/info'/>">Thông tin học sinh</a>
                                            <form method="post"
                                                  action="<c:url value='/teacher/student/${s.idHocSinh}/delete'/>"
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
            </div>

            <div class="card activity-card">
                <div class="activity-head">
                    <div>
                        <h2>Lịch sử thao tác học sinh</h2>
                        <p>Hiển thị thao tác của Admin và giáo viên chủ nhiệm của lớp hiện tại.</p>
                    </div>
                </div>

                <div class="activity-toolbar">
                    <div class="activity-search-wrap">
                        <label for="studentActivitySearchInput">Tìm trong lịch sử</label>
                        <input id="studentActivitySearchInput" type="text" placeholder="Tìm theo học sinh, người thao tác hoặc nội dung...">
                    </div>
                    <div class="activity-role-wrap">
                        <label for="studentActivityRoleFilter">Vai trò</label>
                        <select id="studentActivityRoleFilter">
                            <option value="">Tất cả</option>
                            <option value="Admin">Admin</option>
                            <option value="GVCN">GVCN</option>
                        </select>
                    </div>
                </div>

                <div class="activity-list" id="studentActivityList">
                    <c:forEach var="log" items="${activityLogs}">
                        <article class="activity-item activity-${log.actionKind}" data-role="${log.actorRole}">
                            <span class="activity-dot" aria-hidden="true"></span>
                            <div class="activity-item-body">
                                <div class="activity-line-top">
                                    <div class="activity-actor">
                                        <span class="activity-role">${log.actorRole}</span>
                                        <strong class="activity-name">${log.actorName}</strong>
                                    </div>
                                    <span class="activity-time">${log.actionTime}</span>
                                </div>
                                <p class="activity-subject">Học sinh: <strong>${log.studentName}</strong><c:if test="${not empty log.studentId}"> (${log.studentId})</c:if> • Lớp: ${log.classDisplay}</p>
                                <p class="activity-detail"><strong>${log.actionLabel}:</strong> ${log.actionDetail}</p>
                            </div>
                        </article>
                    </c:forEach>

                    <c:if test="${empty activityLogs}">
                        <div class="activity-empty-note">Chưa có lịch sử thao tác học sinh trong lớp chủ nhiệm hiện tại.</div>
                    </c:if>
                    <div id="studentActivityEmptyHint" class="activity-empty-note" hidden>Không tìm thấy lịch sử phù hợp.</div>
                </div>
            </div>
        </section>
    </main>
</div>

<script>
    function confirmDeleteStudent(form) {
        if (!form) {
            return false;
        }
        const studentId = (form.getAttribute('data-student-id') || '').trim();
        const studentName = (form.getAttribute('data-student-name') || '').trim();
        let display = 'học sinh này';
        if (studentName && studentId) {
            display = studentName + ' (' + studentId + ')';
        } else if (studentName) {
            display = studentName;
        } else if (studentId) {
            display = 'mã HS ' + studentId;
        }
        return confirm('Bạn có chắc muốn xóa ' + display + ' không?\nHành động này không thể hoàn tác.');
    }

    function closeAllActionMenus() {
        document.querySelectorAll('.action-dropdown').forEach(menu => {
            menu.classList.remove('show');
            menu.classList.remove('open-up');
        });
        document.querySelectorAll('.table tbody tr.menu-open').forEach(row => {
            row.classList.remove('menu-open');
        });
    }

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
            if (row !== currentRow) {
                row.classList.remove('menu-open');
            }
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
            closeAllActionMenus();
        }
    });

    const studentActivitySearchInput = document.getElementById('studentActivitySearchInput');
    const studentActivityRoleFilter = document.getElementById('studentActivityRoleFilter');
    const studentActivityItems = Array.from(document.querySelectorAll('#studentActivityList .activity-item'));
    const studentActivityEmptyHint = document.getElementById('studentActivityEmptyHint');

    function normalizeActivityText(value) {
        return (value || '')
            .toLowerCase()
            .normalize('NFD')
            .replace(/[\u0300-\u036f]/g, '')
            .replace(/đ/g, 'd')
            .trim();
    }

    function filterStudentActivities() {
        if (!studentActivityItems.length) {
            return;
        }

        const keyword = normalizeActivityText(studentActivitySearchInput ? studentActivitySearchInput.value : '');
        const role = normalizeActivityText(studentActivityRoleFilter ? studentActivityRoleFilter.value : '');
        let visibleCount = 0;

        studentActivityItems.forEach(item => {
            const itemRole = normalizeActivityText(item.dataset.role || '');
            const itemText = normalizeActivityText(item.textContent || '');
            const matchesKeyword = !keyword || itemText.includes(keyword);
            const matchesRole = !role || itemRole === role;
            const isVisible = matchesKeyword && matchesRole;
            item.hidden = !isVisible;
            if (isVisible) {
                visibleCount += 1;
            }
        });

        if (studentActivityEmptyHint) {
            studentActivityEmptyHint.hidden = visibleCount > 0;
        }
    }

    if (studentActivitySearchInput) {
        studentActivitySearchInput.addEventListener('input', filterStudentActivities);
    }

    if (studentActivityRoleFilter) {
        studentActivityRoleFilter.addEventListener('change', filterStudentActivities);
    }
</script>
</body>
</html>
