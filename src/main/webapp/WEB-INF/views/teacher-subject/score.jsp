<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>${pageTitle}</title>
    <link rel="stylesheet" href="<c:url value='/css/admin-layout.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/teacher-subject/score.css'/>">
</head>
<body>
<div class="layout">
    <jsp:include page="/WEB-INF/views/teacher-subject/_sidebar.jsp"/>

    <main class="main teacher-score-page teacher-subject-score-page">
        <c:set var="pageData" value="${scorePageData}"/>
        <c:set var="data" value="${scoreData}"/>
        <c:set var="searchModel" value="${data.search}"/>
        <c:set var="isAnnualView" value="${searchModel.hocKy == '0'}"/>

        <header class="topbar">
            <div class="topbar-left">
                <h1>Quản lý điểm số</h1>
                <p>
                    Lớp bộ môn:
                    <strong>${empty pageData.selectedClassDisplay ? 'Tất cả lớp được phân công' : pageData.selectedClassDisplay}</strong>
                    <span> | Năm học: <strong>${empty data.schoolYear ? '-' : data.schoolYear}</strong></span>
                </p>
            </div>
            <div class="topbar-right">
                <div class="teacher-badge">Giáo viên: <strong>${empty data.teacherId ? '-' : data.teacherId}</strong></div>
                <a class="btn primary" href="<c:url value='/teacher-subject/score/create'/>">+ Thêm điểm số</a>
            </div>
        </header>

        <section class="content">
            <c:if test="${not empty flashMessage}">
                <div class="flash-message alert ${flashType == 'error' ? 'alert-error' : 'alert-success'}">${flashMessage}</div>
            </c:if>
            <c:if test="${not empty warningMessage}">
                <div class="flash-message alert alert-error">${warningMessage}</div>
            </c:if>

            <section class="card overview-card">
                <div class="overview-block">
                    <h2>Môn đang dạy</h2>
                    <div class="chip-wrap">
                        <c:forEach var="subject" items="${data.teachingSubjects}">
                            <span class="subject-chip">${subject.name} (${subject.id})</span>
                        </c:forEach>
                        <c:if test="${empty data.teachingSubjects}">
                            <span class="empty-chip">Chưa có phân công dạy môn trong năm học hiện tại.</span>
                        </c:if>
                    </div>
                </div>

                <div class="overview-block">
                    <h3>Khóa học phụ trách</h3>
                    <div class="chip-wrap">
                        <c:forEach var="course" items="${pageData.courseOptions}">
                            <span class="course-chip">${course.name} (${course.id})</span>
                        </c:forEach>
                        <c:if test="${empty pageData.courseOptions}">
                            <span class="empty-chip">Chưa có khóa học gắn với các lớp bộ môn được phân công.</span>
                        </c:if>
                    </div>
                </div>
            </section>

            <section class="card filter-card">
                <form method="get" action="<c:url value='/teacher-subject/score'/>" class="filters" autocomplete="off">
                    <div class="filter-item search-item">
                        <label for="q">Tìm kiếm</label>
                        <input id="q" type="text" name="q" value="${searchModel.q}" placeholder="Mã HS, tên học sinh hoặc môn học...">
                    </div>

                    <div class="filter-item">
                        <label for="khoa">Khóa học</label>
                        <select id="khoa" name="khoa">
                            <option value="" ${empty searchModel.khoa ? 'selected' : ''}>Tất cả khóa học</option>
                            <c:forEach var="course" items="${pageData.courseOptions}">
                                <option value="${course.id}" ${searchModel.khoa == course.id ? 'selected' : ''}>${course.name} (${course.id})</option>
                            </c:forEach>
                        </select>
                    </div>

                    <div class="filter-item">
                        <label for="classId">Lớp</label>
                        <select id="classId" name="classId">
                            <option value="" ${empty searchModel.classId ? 'selected' : ''}>Tất cả lớp</option>
                            <c:forEach var="classItem" items="${pageData.subjectClassOptions}">
                                <option value="${classItem.id}" ${searchModel.classId == classItem.id ? 'selected' : ''}>
                                    ${classItem.name} (${classItem.id})
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
                            <option value="0" ${searchModel.hocKy == '0' ? 'selected' : ''}>Cả năm</option>
                            <option value="1" ${searchModel.hocKy == '1' ? 'selected' : ''}>Học kỳ I</option>
                            <option value="2" ${searchModel.hocKy == '2' ? 'selected' : ''}>Học kỳ II</option>
                        </select>
                    </div>

                    <div class="filter-actions">
                        <button class="btn primary" type="submit">Lọc dữ liệu</button>
                        <button class="btn btn-outline export-btn export-btn-excel"
                                type="submit"
                                formaction="<c:url value='/teacher-subject/score/export/excel'/>"
                                ${data.totalItems == 0 ? 'disabled' : ''}>
                            Xuất Excel
                        </button>
                        <button class="btn btn-outline export-btn export-btn-pdf"
                                type="submit"
                                formaction="<c:url value='/teacher-subject/score/export/pdf'/>"
                                ${data.totalItems == 0 ? 'disabled' : ''}>
                            Xuất PDF
                        </button>
                        <a class="btn btn-outline" href="<c:url value='/teacher-subject/score'/>">Đặt lại</a>
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
                            <c:choose>
                                <c:when test="${isAnnualView}">
                                    <th>Tổng kết kỳ 1</th>
                                    <th>Tổng kết kỳ 2</th>
                                    <th>Cả năm</th>
                                </c:when>
                                <c:otherwise>
                                    <th>Giữa kỳ</th>
                                    <th>Cuối kỳ</th>
                                    <th>Trung bình</th>
                                </c:otherwise>
                            </c:choose>
                            <th>Học kỳ</th>
                            <c:if test="${isAnnualView}">
                                <th>Năm học</th>
                            </c:if>
                            <th class="th-actions">Thao tác</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="item" items="${data.rows}">
                            <tr>
                                <td>${item.studentId}</td>
                                <td class="student-name">${item.studentName}</td>
                                <td>${item.classDisplay}</td>
                                <td><span class="scope-badge ${item.classScopeBadge}">${item.classScopeDisplay}</span></td>
                                <td>
                                    <div class="subject-line">${item.subjectName}</div>
                                    <small>${item.subjectId}</small>
                                </td>
                                <c:choose>
                                    <c:when test="${isAnnualView}">
                                        <td><span class="score-pill">${item.tongKetHocKy1Display}</span></td>
                                        <td><span class="score-pill">${item.tongKetHocKy2Display}</span></td>
                                        <td><span class="score-pill">${item.tongKetCaNamDisplay}</span></td>
                                    </c:when>
                                    <c:otherwise>
                                        <td>${item.diemGiuaKyDisplay}</td>
                                        <td>${item.diemCuoiKyDisplay}</td>
                                        <td><span class="score-pill">${item.tongKetDisplay}</span></td>
                                    </c:otherwise>
                                </c:choose>
                                <td>${item.hocKyDisplay}</td>
                                <c:if test="${isAnnualView}">
                                    <td>${item.namHoc}</td>
                                </c:if>
                                <td class="actions">
                                    <div class="action-menu">
                                        <c:url var="detailUrl" value="/teacher-subject/score/detail">
                                            <c:param name="studentId" value="${item.studentId}"/>
                                            <c:param name="subjectId" value="${item.subjectId}"/>
                                            <c:param name="namHoc" value="${item.namHoc}"/>
                                            <c:param name="hocKy" value="${item.hocKy}"/>
                                            <c:param name="returnQ" value="${searchModel.q}"/>
                                            <c:param name="returnKhoa" value="${searchModel.khoa}"/>
                                            <c:param name="returnMon" value="${searchModel.mon}"/>
                                            <c:param name="returnHocKy" value="${searchModel.hocKy}"/>
                                            <c:param name="returnClassId" value="${searchModel.classId}"/>
                                            <c:param name="returnPage" value="${data.page}"/>
                                        </c:url>
                                        <button type="button" class="action-toggle" aria-label="Mở menu thao tác" aria-expanded="false" onclick="toggleTeacherSubjectActionMenu(this)">&#8942;</button>
                                        <div class="action-dropdown" role="menu">
                                            <a class="action-item" href="${detailUrl}">Chi tiết điểm</a>
                                            <c:if test="${item.canManage}">
                                                <c:url var="editUrl" value="/teacher-subject/score/edit">
                                                    <c:param name="studentId" value="${item.studentId}"/>
                                                    <c:param name="subjectId" value="${item.subjectId}"/>
                                                    <c:param name="namHoc" value="${item.namHoc}"/>
                                                    <c:param name="hocKy" value="${item.hocKy}"/>
                                                    <c:param name="returnQ" value="${searchModel.q}"/>
                                                    <c:param name="returnKhoa" value="${searchModel.khoa}"/>
                                                    <c:param name="returnMon" value="${searchModel.mon}"/>
                                                    <c:param name="returnHocKy" value="${searchModel.hocKy}"/>
                                                    <c:param name="returnClassId" value="${searchModel.classId}"/>
                                                    <c:param name="returnPage" value="${data.page}"/>
                                                </c:url>
                                                <a class="action-item" href="${editUrl}">Sửa điểm</a>
                                                <form class="score-delete-form" method="post" action="<c:url value='/teacher-subject/score/delete'/>" data-student-name="${item.studentName}" data-subject-name="${item.subjectName}">
                                                    <input type="hidden" name="studentId" value="${item.studentId}">
                                                    <input type="hidden" name="subjectId" value="${item.subjectId}">
                                                    <input type="hidden" name="namHoc" value="${item.namHoc}">
                                                    <input type="hidden" name="returnQ" value="${searchModel.q}">
                                                    <input type="hidden" name="returnKhoa" value="${searchModel.khoa}">
                                                    <input type="hidden" name="returnMon" value="${searchModel.mon}">
                                                    <input type="hidden" name="returnHocKy" value="${searchModel.hocKy}">
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
                                <td colspan="${isAnnualView ? 11 : 10}" class="empty-message">Không có dữ liệu điểm phù hợp với bộ lọc hiện tại.</td>
                            </tr>
                        </c:if>
                        </tbody>
                    </table>
                </div>

                <div class="table-footer">
                    <div class="table-count">Hiển thị ${data.fromRecord}-${data.toRecord} / ${data.totalItems} kết quả</div>
                    <div class="pagination">
                        <c:forEach var="p" items="${data.pageNumbers}">
                            <c:url var="pageUrl" value="/teacher-subject/score">
                                <c:param name="page" value="${p}"/>
                                <c:if test="${not empty searchModel.q}"><c:param name="q" value="${searchModel.q}"/></c:if>
                                <c:if test="${not empty searchModel.khoa}"><c:param name="khoa" value="${searchModel.khoa}"/></c:if>
                                <c:if test="${not empty searchModel.classId}"><c:param name="classId" value="${searchModel.classId}"/></c:if>
                                <c:if test="${not empty searchModel.mon}"><c:param name="mon" value="${searchModel.mon}"/></c:if>
                                <c:if test="${not empty searchModel.hocKy}"><c:param name="hocKy" value="${searchModel.hocKy}"/></c:if>
                            </c:url>
                            <a class="page-btn ${data.page == p ? 'active' : ''}" href="${pageUrl}">${p}</a>
                        </c:forEach>
                    </div>
                </div>
            </section>

            <section class="card activity-card">
                <div class="activity-head">
                    <div>
                        <h2>Lịch sử thao tác</h2>
                        <p>Theo dõi các thao tác nhập, sửa, xóa điểm gần đây của tài khoản giáo viên bộ môn hiện tại.</p>
                    </div>
                </div>

                <div class="history-filter-grid">
                    <div class="activity-search-wrap">
                        <label for="activitySearchInput">Tìm trong lịch sử</label>
                        <input id="activitySearchInput" type="text" placeholder="Tìm theo hành động, nội dung hoặc thời gian...">
                    </div>

                    <div class="filter-item compact-filter">
                        <label for="activityDateFilter">Ngày</label>
                        <input id="activityDateFilter" type="date">
                    </div>

                    <div class="filter-item compact-filter">
                        <label for="activityMonthFilter">Tháng</label>
                        <input id="activityMonthFilter" type="month">
                    </div>

                    <div class="filter-item compact-filter">
                        <label for="activityYearFilter">Năm</label>
                        <input id="activityYearFilter" type="number" min="2000" max="2100" step="1" placeholder="2026">
                    </div>

                    <div class="history-filter-actions">
                        <button type="button" class="btn primary history-apply-btn" id="activityApplyFilterButton">Tìm</button>
                    </div>
                </div>

                <div class="activity-list" id="scoreActivityList">
                    <c:forEach var="log" items="${pageData.activityLogs}">
                        <article class="activity-item activity-${log.actionKind}"
                                 data-actor-name="${log.actorName}"
                                 data-actor-role="${log.actorRole}"
                                 data-action-label="${log.actionLabel}"
                                 data-action-detail="${log.actionDetail}"
                                 data-action-time="${log.actionTime}">
                            <span class="activity-dot" aria-hidden="true"></span>
                            <div class="activity-item-body">
                                <div class="activity-line-top">
                                    <div class="activity-actor">
                                        <span class="activity-role">${log.actorRole}</span>
                                        <strong class="activity-name">${log.actorName}</strong>
                                    </div>
                                    <span class="activity-time">${log.actionTime}</span>
                                </div>
                                <p class="activity-label">${log.actionLabel}</p>
                                <p class="activity-detail">${log.actionDetail}</p>
                            </div>
                        </article>
                    </c:forEach>
                    <c:if test="${empty pageData.activityLogs}">
                        <div class="activity-empty-note">Chưa có lịch sử thao tác điểm để hiển thị.</div>
                    </c:if>
                    <div id="activityEmptyHint" class="activity-empty-note" hidden>Không tìm thấy lịch sử thao tác phù hợp.</div>
                </div>
                <div class="history-pagination" id="activityPagination" hidden></div>
            </section>
        </section>
    </main>
</div>

<div id="teacherSubjectScoreDeleteModal" class="score-delete-modal" hidden>
    <div class="score-delete-backdrop" data-close-teacher-subject-score-delete-modal></div>
    <div class="score-delete-dialog" role="dialog" aria-modal="true" aria-labelledby="teacherSubjectScoreDeleteModalTitle">
        <h3 id="teacherSubjectScoreDeleteModalTitle">Xác nhận xóa điểm</h3>
        <p id="teacherSubjectScoreDeleteModalMessage">Bạn có chắc chắn muốn xóa nhóm điểm này không?</p>
        <div class="score-delete-actions">
            <button type="button" class="btn btn-outline" id="cancelTeacherSubjectScoreDeleteButton">Hủy</button>
            <button type="button" class="btn btn-danger" id="confirmTeacherSubjectScoreDeleteButton">Xóa</button>
        </div>
    </div>
</div>

<script src="<c:url value='/js/teacher-subject/score.js'/>"></script>
</body>
</html>
