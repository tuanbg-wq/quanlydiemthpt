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
            </div>
        </header>

        <section class="content">
            <c:if test="${not empty flashMessage}">
                <div class="flash-message alert ${flashType == 'error' ? 'alert-error' : 'alert-success'}">${flashMessage}</div>
            </c:if>
            <c:if test="${not empty warningMessage}">
                <div class="flash-message alert alert-error">${warningMessage}</div>
            </c:if>

            <section class="stats-grid">
                <article class="stats-card">
                    <span class="stats-label">Tổng nhóm điểm hiển thị</span>
                    <strong class="stats-value">${data.stats.totalRows}</strong>
                </article>
                <article class="stats-card">
                    <span class="stats-label">Điểm TB lớp chủ nhiệm</span>
                    <strong class="stats-value">${data.stats.homeroomAverageDisplay}</strong>
                </article>
                <article class="stats-card">
                    <span class="stats-label">Nhóm điểm lớp chủ nhiệm</span>
                    <strong class="stats-value">${data.stats.homeroomRows}</strong>
                </article>
                <article class="stats-card">
                    <span class="stats-label">Nhóm điểm lớp bộ môn</span>
                    <strong class="stats-value">${data.stats.subjectRows}</strong>
                </article>
                <article class="stats-card">
                    <span class="stats-label">Nhóm điểm được nhập/sửa</span>
                    <strong class="stats-value">${data.stats.editableRows}</strong>
                </article>
            </section>

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
                        <label for="classScope">Phạm vi lớp</label>
                        <select id="classScope" name="classScope">
                            <option value="" ${empty searchModel.classScope ? 'selected' : ''}>Tất cả phạm vi</option>
                            <option value="HOMEROOM" ${searchModel.classScope == 'HOMEROOM' ? 'selected' : ''}>Lớp chủ nhiệm</option>
                            <option value="SUBJECT" ${searchModel.classScope == 'SUBJECT' ? 'selected' : ''}>Lớp bộ môn</option>
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
                        <button class="btn primary" type="submit">Áp dụng lọc</button>
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
                            <th>Phạm vi lớp</th>
                            <th>Môn học</th>
                            <th>Giữa kỳ</th>
                            <th>Cuối kỳ</th>
                            <th>Trung bình</th>
                            <th>Học kỳ</th>
                            <th>Thao tác</th>
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
                                <td>
                                    <c:choose>
                                        <c:when test="${item.canEdit}">
                                            <c:url var="editUrl" value="/teacher/score/edit">
                                                <c:param name="studentId" value="${item.studentId}"/>
                                                <c:param name="subjectId" value="${item.subjectId}"/>
                                                <c:param name="namHoc" value="${item.namHoc}"/>
                                                <c:param name="hocKy" value="${item.hocKy}"/>
                                                <c:param name="returnQ" value="${searchModel.q}"/>
                                                <c:param name="returnMon" value="${searchModel.mon}"/>
                                                <c:param name="returnHocKy" value="${searchModel.hocKy}"/>
                                                <c:param name="returnClassScope" value="${searchModel.classScope}"/>
                                                <c:param name="returnPage" value="${data.page}"/>
                                            </c:url>
                                            <a class="btn btn-row" href="${editUrl}">Nhập/Sửa điểm</a>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="readonly-text">Chỉ xem</span>
                                        </c:otherwise>
                                    </c:choose>
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
</body>
</html>
