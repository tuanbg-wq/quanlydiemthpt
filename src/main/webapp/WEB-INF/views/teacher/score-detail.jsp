<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>${pageTitle}</title>
    <link rel="stylesheet" href="<c:url value='/css/admin-layout.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/teacher/score/score-edit.css'/>">
</head>
<body>
<div class="layout">
    <jsp:include page="/WEB-INF/views/teacher/_sidebar.jsp"/>

    <main class="main teacher-score-detail-page">
        <c:set var="d" value="${detailData}"/>
        <c:set var="viewSemester" value="${empty selectedHocKy ? '0' : selectedHocKy}"/>

        <c:url var="backUrl" value="/teacher/score">
            <c:if test="${not empty returnQ}">
                <c:param name="q" value="${returnQ}"/>
            </c:if>
            <c:if test="${not empty returnMon}">
                <c:param name="mon" value="${returnMon}"/>
            </c:if>
            <c:if test="${not empty returnHocKy}">
                <c:param name="hocKy" value="${returnHocKy}"/>
            </c:if>
            <c:if test="${not empty returnClassScope}">
                <c:param name="classScope" value="${returnClassScope}"/>
            </c:if>
            <c:if test="${not empty returnClassId}">
                <c:param name="classId" value="${returnClassId}"/>
            </c:if>
            <c:if test="${not empty returnPage}">
                <c:param name="page" value="${returnPage}"/>
            </c:if>
        </c:url>

        <c:url var="tabHk1Url" value="/teacher/score/detail">
            <c:param name="studentId" value="${d.filter.studentId}"/>
            <c:param name="subjectId" value="${d.filter.mon}"/>
            <c:param name="namHoc" value="${d.filter.namHoc}"/>
            <c:param name="hocKy" value="1"/>
            <c:param name="returnQ" value="${returnQ}"/>
            <c:param name="returnMon" value="${returnMon}"/>
            <c:param name="returnHocKy" value="${returnHocKy}"/>
            <c:param name="returnClassScope" value="${returnClassScope}"/>
            <c:param name="returnClassId" value="${returnClassId}"/>
            <c:param name="returnPage" value="${returnPage}"/>
        </c:url>

        <c:url var="tabHk2Url" value="/teacher/score/detail">
            <c:param name="studentId" value="${d.filter.studentId}"/>
            <c:param name="subjectId" value="${d.filter.mon}"/>
            <c:param name="namHoc" value="${d.filter.namHoc}"/>
            <c:param name="hocKy" value="2"/>
            <c:param name="returnQ" value="${returnQ}"/>
            <c:param name="returnMon" value="${returnMon}"/>
            <c:param name="returnHocKy" value="${returnHocKy}"/>
            <c:param name="returnClassScope" value="${returnClassScope}"/>
            <c:param name="returnClassId" value="${returnClassId}"/>
            <c:param name="returnPage" value="${returnPage}"/>
        </c:url>

        <c:url var="tabYearUrl" value="/teacher/score/detail">
            <c:param name="studentId" value="${d.filter.studentId}"/>
            <c:param name="subjectId" value="${d.filter.mon}"/>
            <c:param name="namHoc" value="${d.filter.namHoc}"/>
            <c:param name="hocKy" value="0"/>
            <c:param name="returnQ" value="${returnQ}"/>
            <c:param name="returnMon" value="${returnMon}"/>
            <c:param name="returnHocKy" value="${returnHocKy}"/>
            <c:param name="returnClassScope" value="${returnClassScope}"/>
            <c:param name="returnClassId" value="${returnClassId}"/>
            <c:param name="returnPage" value="${returnPage}"/>
        </c:url>

        <header class="topbar">
            <div class="topbar-left">
                <h1>Chi tiết điểm số</h1>
                <p>Xem điểm theo học kỳ hoặc cả năm. Trang này chỉ hiển thị, không chỉnh sửa.</p>
            </div>
            <div class="topbar-right">
                <a class="btn btn-outline" href="${backUrl}">Quay lại danh sách</a>
            </div>
        </header>

        <section class="content">
            <c:choose>
                <c:when test="${not empty d and d.readyForInput}">
                    <section class="card info-card">
                        <h2>${d.selectedStudent.name}</h2>
                        <div class="meta-grid">
                            <div><span>Mã học sinh</span><strong>${d.selectedStudent.id}</strong></div>
                            <div><span>Lớp</span><strong>${d.selectedStudent.className}</strong></div>
                            <div><span>Môn học</span><strong>${d.selectedSubjectName}</strong></div>
                            <div><span>Năm học</span><strong>${d.filter.namHoc}</strong></div>
                            <div><span>Số cột TX</span><strong>${d.frequentColumns}</strong></div>
                            <div><span>Công thức</span><strong>ĐTBmcn = (ĐTBhkI + 2 x ĐTBhkII) / 3</strong></div>
                        </div>
                    </section>

                    <section class="card score-detail-tabs">
                        <a class="detail-tab ${viewSemester == '1' ? 'active' : ''}" href="${tabHk1Url}">Học kỳ I</a>
                        <a class="detail-tab ${viewSemester == '2' ? 'active' : ''}" href="${tabHk2Url}">Học kỳ II</a>
                        <a class="detail-tab ${viewSemester == '0' ? 'active' : ''}" href="${tabYearUrl}">Cả năm</a>
                    </section>

                    <section class="card form-card">
                        <div class="rule-line">${d.requiredTxMessage} | ${d.formulaText}</div>

                        <div class="detail-semester-grid ${viewSemester == '0' ? '' : 'single'}">
                            <c:if test="${viewSemester == '0' or viewSemester == '1'}">
                                <article class="semester-card">
                                    <h3>Học kỳ I</h3>
                                    <div class="detail-score-table">
                                        <table>
                                            <thead>
                                            <tr>
                                                <c:forEach begin="1" end="${d.frequentColumns}" var="txIndex">
                                                    <th>TX${txIndex}</th>
                                                </c:forEach>
                                                <th>Giữa kỳ</th>
                                                <th>Cuối kỳ</th>
                                                <th>ĐTB HKI</th>
                                            </tr>
                                            </thead>
                                            <tbody>
                                            <tr>
                                                <c:forEach begin="1" end="${d.frequentColumns}" var="txIndex">
                                                    <td>${empty d.hk1Input.frequentScores[txIndex - 1] ? '-' : d.hk1Input.frequentScores[txIndex - 1]}</td>
                                                </c:forEach>
                                                <td>${empty d.hk1Input.midterm ? '-' : d.hk1Input.midterm}</td>
                                                <td>${empty d.hk1Input.finalScore ? '-' : d.hk1Input.finalScore}</td>
                                                <td><span class="score-pill">${d.hk1Input.averageDisplay}</span></td>
                                            </tr>
                                            </tbody>
                                        </table>
                                    </div>
                                </article>
                            </c:if>

                            <c:if test="${viewSemester == '0' or viewSemester == '2'}">
                                <article class="semester-card">
                                    <h3>Học kỳ II</h3>
                                    <div class="detail-score-table">
                                        <table>
                                            <thead>
                                            <tr>
                                                <c:forEach begin="1" end="${d.frequentColumns}" var="txIndex">
                                                    <th>TX${txIndex}</th>
                                                </c:forEach>
                                                <th>Giữa kỳ</th>
                                                <th>Cuối kỳ</th>
                                                <th>ĐTB HKII</th>
                                            </tr>
                                            </thead>
                                            <tbody>
                                            <tr>
                                                <c:forEach begin="1" end="${d.frequentColumns}" var="txIndex">
                                                    <td>${empty d.hk2Input.frequentScores[txIndex - 1] ? '-' : d.hk2Input.frequentScores[txIndex - 1]}</td>
                                                </c:forEach>
                                                <td>${empty d.hk2Input.midterm ? '-' : d.hk2Input.midterm}</td>
                                                <td>${empty d.hk2Input.finalScore ? '-' : d.hk2Input.finalScore}</td>
                                                <td><span class="score-pill">${d.hk2Input.averageDisplay}</span></td>
                                            </tr>
                                            </tbody>
                                        </table>
                                    </div>
                                </article>
                            </c:if>
                        </div>

                        <c:if test="${viewSemester == '0'}">
                            <div class="year-average">ĐTB cả năm: <strong>${d.yearAverageDisplay}</strong></div>
                        </c:if>
                    </section>
                </c:when>
                <c:otherwise>
                    <section class="card empty-card">
                        <h3>Không đủ dữ liệu để hiển thị chi tiết điểm</h3>
                        <p>Vui lòng quay lại danh sách và chọn nhóm điểm hợp lệ.</p>
                        <a class="btn btn-outline" href="${backUrl}">Quay lại danh sách</a>
                    </section>
                </c:otherwise>
            </c:choose>
        </section>
    </main>
</div>
</body>
</html>
