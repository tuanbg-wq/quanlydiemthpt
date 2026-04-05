<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>${pageTitle}</title>
    <link rel="stylesheet" href="<c:url value='/css/admin-layout.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/teacher/score/score-list.css'/>">
</head>
<body>
<div class="layout">
    <jsp:include page="/WEB-INF/views/teacher/_sidebar.jsp"/>

    <main class="main teacher-score-page">
        <c:set var="data" value="${scoreData}"/>
        <c:set var="searchModel" value="${data.search}"/>

        <header class="topbar">
            <div class="topbar-left">
                <h1>Quản lý điểm số</h1>
                <p>
                    Lớp chủ nhiệm:
                    <strong>${empty scope.className ? 'Chưa phân công' : scope.className}</strong>
                    <span> | Năm học: <strong>${empty data.schoolYear ? '-' : data.schoolYear}</strong></span>
                </p>
            </div>
            <div class="topbar-right">
                <div class="teacher-badge">Giáo viên: <strong>${empty data.teacherId ? '-' : data.teacherId}</strong></div>
                <a class="btn primary" href="<c:url value='/teacher/score/create'/>">+ Thêm điểm số</a>
            </div>
        </header>

        <section class="content">
            <c:if test="${not empty flashMessage}">
                <div class="flash-message alert ${flashType == 'error' ? 'alert-error' : 'alert-success'}">${flashMessage}</div>
            </c:if>
            <c:if test="${not empty warningMessage}">
                <div class="flash-message alert alert-error">${warningMessage}</div>
            </c:if>

            <section class="card taught-subject-card">
                <h2>Môn đang dạy</h2>
                <div class="subject-chip-wrap">
                    <c:forEach var="subject" items="${data.teachingSubjects}">
                        <span class="subject-chip">${subject.name} (${subject.id})</span>
                    </c:forEach>
                    <c:if test="${empty data.teachingSubjects}">
                        <span class="empty-chip">Chưa có phân công dạy môn trong năm học hiện tại.</span>
                    </c:if>
                </div>
            </section>

            <section class="card filter-card">
                <form method="get" action="<c:url value='/teacher/score'/>" class="filters" autocomplete="off">
                    <div class="filter-item search-item">
                        <label for="q">Tìm kiếm</label>
                        <input id="q" type="text" name="q" value="${searchModel.q}" placeholder="Mã HS, tên học sinh hoặc môn học...">
                    </div>

                    <div class="filter-item">
                        <label for="classScope">Nhóm lớp</label>
                        <select id="classScope" name="classScope">
                            <option value="" ${empty searchModel.classScope ? 'selected' : ''}>Tất cả nhóm</option>
                            <option value="HOMEROOM" ${searchModel.classScope == 'HOMEROOM' ? 'selected' : ''}>Lớp chủ nhiệm</option>
                            <option value="SUBJECT" ${searchModel.classScope == 'SUBJECT' ? 'selected' : ''}>Lớp bộ môn</option>
                        </select>
                    </div>

                    <div class="filter-item">
                        <label for="classId">Lớp</label>
                        <select id="classId" name="classId">
                            <option value="" ${empty searchModel.classId ? 'selected' : ''}>Tất cả lớp</option>
                            <c:forEach var="classItem" items="${data.classOptions}">
                                <option value="${classItem.id}" ${searchModel.classId == classItem.id ? 'selected' : ''}>
                                    ${classItem.name} (${classItem.id}) - ${classItem.homeroom ? 'Lớp chủ nhiệm' : 'Lớp bộ môn'}
                                </option>
                            </c:forEach>
                        </select>
                    </div>

                    <div class="filter-item">
                        <label for="mon">Môn học</label>
                        <select id="mon" name="mon">
                            <option value="" ${empty searchModel.mon ? 'selected' : ''}>Tất cả môn</option>
                            <c:forEach var="subject" items="${data.subjectOptions}">
                                <option value="${subject.id}" ${searchModel.mon == subject.id ? 'selected' : ''}>${subject.name}</option>
                            </c:forEach>
                        </select>
                    </div>

                    <div class="filter-item">
                        <label for="hocKy">Học kỳ</label>
                        <select id="hocKy" name="hocKy">
                            <option value="" ${empty searchModel.hocKy ? 'selected' : ''}>Tất cả học kỳ</option>
                            <option value="1" ${searchModel.hocKy == '1' ? 'selected' : ''}>Học kỳ I</option>
                            <option value="2" ${searchModel.hocKy == '2' ? 'selected' : ''}>Học kỳ II</option>
                        </select>
                    </div>

                    <div class="filter-actions">
                        <button class="btn primary" type="submit">Lọc dữ liệu</button>
                        <button class="btn btn-outline export-btn export-btn-excel"
                                type="submit"
                                formaction="<c:url value='/teacher/score/export/excel'/>"
                                ${data.totalItems == 0 ? 'disabled' : ''}
                                title="${data.totalItems == 0 ? 'Cần có dữ liệu để xuất Excel' : 'Xuất Excel theo bộ lọc hiện tại'}">
                            Xuất Excel
                        </button>
                        <button class="btn btn-outline export-btn export-btn-pdf"
                                type="submit"
                                formaction="<c:url value='/teacher/score/export/pdf'/>"
                                ${data.totalItems == 0 ? 'disabled' : ''}
                                title="${data.totalItems == 0 ? 'Cần có dữ liệu để xuất PDF' : 'Xuất PDF theo bộ lọc hiện tại'}">
                            Xuất PDF
                        </button>
                        <a class="btn btn-outline" href="<c:url value='/teacher/score'/>">Đặt lại</a>
                    </div>
                </form>
            </section>

            <section class="card table-card">
                <h2>Danh sách điểm số</h2>
                <div class="table-wrap">
                    <table class="table">
                        <thead>
                        <tr>
                            <th>Mã HS</th>
                            <th>Họ và tên</th>
                            <th>Lớp</th>
                            <th>Loại lớp</th>
                            <th>Môn học</th>
                            <th>Giữa kỳ</th>
                            <th>Cuối kỳ</th>
                            <th>Trung bình</th>
                            <th>Học kỳ</th>
                            <th class="th-actions">Thao tác</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="item" items="${data.rows}">
                            <tr>
                                <td>${item.studentId}</td>
                                <td class="student-name">${item.studentName}</td>
                                <td>${item.classDisplay}</td>
                                <td>
                                    <span class="scope-badge ${item.classScopeBadge}">${item.classScopeDisplay}</span>
                                </td>
                                <td>
                                    <div class="subject-line">${item.subjectName}</div>
                                    <small>${item.subjectId}</small>
                                </td>
                                <td>${item.diemGiuaKyDisplay}</td>
                                <td>${item.diemCuoiKyDisplay}</td>
                                <td><span class="score-pill">${item.tongKetDisplay}</span></td>
                                <td>${item.hocKyDisplay}</td>
                                <td class="actions">
                                    <div class="action-menu">
                                        <c:url var="detailUrl" value="/teacher/score/detail">
                                            <c:param name="studentId" value="${item.studentId}"/>
                                            <c:param name="subjectId" value="${item.subjectId}"/>
                                            <c:param name="namHoc" value="${item.namHoc}"/>
                                            <c:param name="hocKy" value="${item.hocKy}"/>
                                            <c:param name="returnQ" value="${searchModel.q}"/>
                                            <c:param name="returnMon" value="${searchModel.mon}"/>
                                            <c:param name="returnHocKy" value="${searchModel.hocKy}"/>
                                            <c:param name="returnClassScope" value="${searchModel.classScope}"/>
                                            <c:param name="returnClassId" value="${searchModel.classId}"/>
                                            <c:param name="returnPage" value="${data.page}"/>
                                        </c:url>
                                        <button type="button"
                                                class="action-toggle"
                                                aria-label="Mở menu thao tác"
                                                aria-expanded="false"
                                                onclick="toggleTeacherScoreActionMenu(this)">
                                            &#8942;
                                        </button>
                                        <div class="action-dropdown" role="menu">
                                            <a class="action-item" href="${detailUrl}">Chi tiết điểm</a>

                                            <c:if test="${item.canManage}">
                                                <c:url var="editUrl" value="/teacher/score/edit">
                                                    <c:param name="studentId" value="${item.studentId}"/>
                                                    <c:param name="subjectId" value="${item.subjectId}"/>
                                                    <c:param name="namHoc" value="${item.namHoc}"/>
                                                    <c:param name="hocKy" value="${item.hocKy}"/>
                                                    <c:param name="returnQ" value="${searchModel.q}"/>
                                                    <c:param name="returnMon" value="${searchModel.mon}"/>
                                                    <c:param name="returnHocKy" value="${searchModel.hocKy}"/>
                                                    <c:param name="returnClassScope" value="${searchModel.classScope}"/>
                                                    <c:param name="returnClassId" value="${searchModel.classId}"/>
                                                    <c:param name="returnPage" value="${data.page}"/>
                                                </c:url>
                                                <a class="action-item" href="${editUrl}">Sửa điểm</a>

                                                <form class="score-delete-form"
                                                      method="post"
                                                      action="<c:url value='/teacher/score/delete'/>"
                                                      data-student-name="${item.studentName}"
                                                      data-subject-name="${item.subjectName}">
                                                    <input type="hidden" name="studentId" value="${item.studentId}">
                                                    <input type="hidden" name="subjectId" value="${item.subjectId}">
                                                    <input type="hidden" name="namHoc" value="${item.namHoc}">
                                                    <input type="hidden" name="returnQ" value="${searchModel.q}">
                                                    <input type="hidden" name="returnMon" value="${searchModel.mon}">
                                                    <input type="hidden" name="returnHocKy" value="${searchModel.hocKy}">
                                                    <input type="hidden" name="returnClassScope" value="${searchModel.classScope}">
                                                    <input type="hidden" name="returnClassId" value="${searchModel.classId}">
                                                    <input type="hidden" name="returnPage" value="${data.page}">
                                                    <button class="action-item danger" type="submit">Xóa nhóm điểm</button>
                                                </form>
                                            </c:if>
                                        </div>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>

                        <c:if test="${empty data.rows}">
                            <tr>
                                <td colspan="10" class="empty-message">Không có dữ liệu điểm phù hợp với bộ lọc hiện tại.</td>
                            </tr>
                        </c:if>
                        </tbody>
                    </table>
                </div>

                <div class="table-footer">
                    <div class="table-count">Hiển thị ${data.fromRecord}-${data.toRecord} / ${data.totalItems} kết quả</div>
                    <div class="pagination">
                        <c:forEach var="p" items="${data.pageNumbers}">
                            <c:url var="pageUrl" value="/teacher/score">
                                <c:param name="page" value="${p}"/>
                                <c:if test="${not empty searchModel.q}">
                                    <c:param name="q" value="${searchModel.q}"/>
                                </c:if>
                                <c:if test="${not empty searchModel.classScope}">
                                    <c:param name="classScope" value="${searchModel.classScope}"/>
                                </c:if>
                                <c:if test="${not empty searchModel.classId}">
                                    <c:param name="classId" value="${searchModel.classId}"/>
                                </c:if>
                                <c:if test="${not empty searchModel.mon}">
                                    <c:param name="mon" value="${searchModel.mon}"/>
                                </c:if>
                                <c:if test="${not empty searchModel.hocKy}">
                                    <c:param name="hocKy" value="${searchModel.hocKy}"/>
                                </c:if>
                            </c:url>
                            <a class="page-btn ${data.page == p ? 'active' : ''}" href="${pageUrl}">${p}</a>
                        </c:forEach>
                    </div>
                </div>
            </section>
        </section>
    </main>
</div>
<div id="teacherScoreDeleteModal" class="score-delete-modal" hidden>
    <div class="score-delete-backdrop" data-close-teacher-score-delete-modal></div>
    <div class="score-delete-dialog" role="dialog" aria-modal="true" aria-labelledby="teacherScoreDeleteModalTitle">
        <h3 id="teacherScoreDeleteModalTitle">Xác nhận xóa điểm</h3>
        <p id="teacherScoreDeleteModalMessage">Bạn có chắc chắn muốn xóa nhóm điểm này không?</p>
        <div class="score-delete-actions">
            <button type="button" class="btn btn-outline" id="cancelTeacherScoreDeleteButton">Hủy</button>
            <button type="button" class="btn btn-danger" id="confirmTeacherScoreDeleteButton">Xóa</button>
        </div>
    </div>
</div>
<script>
    (function () {
        function closeAllMenus() {
            document.querySelectorAll('.action-dropdown').forEach(function (menu) {
                menu.classList.remove('show');
                menu.classList.remove('open-up');
            });
            document.querySelectorAll('.action-menu.is-open').forEach(function (menu) {
                menu.classList.remove('is-open');
            });
            document.querySelectorAll('.table tbody tr.menu-open').forEach(function (row) {
                row.classList.remove('menu-open');
            });
            document.querySelectorAll('.action-toggle[aria-expanded="true"]').forEach(function (button) {
                button.setAttribute('aria-expanded', 'false');
            });
        }

        function positionMenu(button, menu) {
            const spacing = 8;
            menu.classList.add('show');
            menu.classList.remove('open-up');
            menu.style.left = '0px';
            menu.style.top = '0px';

            const buttonRect = button.getBoundingClientRect();
            const menuRect = menu.getBoundingClientRect();

            let left = buttonRect.right - menuRect.width;
            if (left < spacing) {
                left = spacing;
            }
            if (left + menuRect.width > window.innerWidth - spacing) {
                left = window.innerWidth - menuRect.width - spacing;
            }

            let top = buttonRect.bottom + spacing;
            const canOpenUp = buttonRect.top - menuRect.height - spacing >= spacing;
            const willOverflowDown = top + menuRect.height > window.innerHeight - spacing;

            if (willOverflowDown && canOpenUp) {
                top = buttonRect.top - menuRect.height - spacing;
                menu.classList.add('open-up');
            } else if (willOverflowDown) {
                top = Math.max(spacing, window.innerHeight - menuRect.height - spacing);
            }

            menu.style.left = left + 'px';
            menu.style.top = top + 'px';
        }

        window.toggleTeacherScoreActionMenu = function (button) {
            const currentMenu = button.nextElementSibling;
            const currentRow = button.closest('tr');
            const shouldShow = !currentMenu.classList.contains('show');

            closeAllMenus();
            if (!shouldShow) {
                return;
            }

            positionMenu(button, currentMenu);
            button.setAttribute('aria-expanded', 'true');
            if (currentRow) {
                currentRow.classList.add('menu-open');
            }
            const currentWrap = button.closest('.action-menu');
            if (currentWrap) {
                currentWrap.classList.add('is-open');
            }
        };

        document.addEventListener('click', function (event) {
            if (!event.target.closest('.action-menu')) {
                closeAllMenus();
            }
        });
        window.addEventListener('resize', closeAllMenus);
        document.addEventListener('scroll', closeAllMenus, true);

        const deleteModal = document.getElementById('teacherScoreDeleteModal');
        const deleteModalMessage = document.getElementById('teacherScoreDeleteModalMessage');
        const cancelDeleteButton = document.getElementById('cancelTeacherScoreDeleteButton');
        const confirmDeleteButton = document.getElementById('confirmTeacherScoreDeleteButton');
        let pendingDeleteForm = null;

        function openDeleteModal(studentName, subjectName) {
            const who = studentName ? ' của học sinh "' + studentName + '"' : '';
            const subject = subjectName ? ' môn "' + subjectName + '"' : '';
            deleteModalMessage.textContent = 'Bạn có chắc chắn muốn xóa nhóm điểm' + who + subject + ' không?';
            deleteModal.hidden = false;
            document.body.classList.add('modal-open');
            confirmDeleteButton.focus();
            closeAllMenus();
        }

        function closeDeleteModal() {
            deleteModal.hidden = true;
            document.body.classList.remove('modal-open');
            pendingDeleteForm = null;
        }

        document.querySelectorAll('.score-delete-form').forEach(function (form) {
            form.addEventListener('submit', function (event) {
                if (form.dataset.confirmed === 'true') {
                    form.dataset.confirmed = 'false';
                    return;
                }
                event.preventDefault();
                pendingDeleteForm = form;
                openDeleteModal(form.dataset.studentName || '', form.dataset.subjectName || '');
            });
        });

        if (confirmDeleteButton) {
            confirmDeleteButton.addEventListener('click', function () {
                if (!pendingDeleteForm) {
                    closeDeleteModal();
                    return;
                }
                pendingDeleteForm.dataset.confirmed = 'true';
                pendingDeleteForm.submit();
            });
        }
        if (cancelDeleteButton) {
            cancelDeleteButton.addEventListener('click', closeDeleteModal);
        }
        if (deleteModal) {
            deleteModal.querySelectorAll('[data-close-teacher-score-delete-modal]').forEach(function (button) {
                button.addEventListener('click', closeDeleteModal);
            });
        }

        document.addEventListener('keydown', function (event) {
            if (event.key === 'Escape') {
                if (deleteModal && !deleteModal.hidden) {
                    closeDeleteModal();
                    return;
                }
                closeAllMenus();
            }
        });
    })();
</script>
</body>
</html>
