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

    <main class="main teacher-score-edit-page">
        <c:set var="d" value="${editData}"/>
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
            <c:if test="${not empty returnPage}">
                <c:param name="page" value="${returnPage}"/>
            </c:if>
        </c:url>

        <header class="topbar">
            <div class="topbar-left">
                <h1>Nhập/Sửa điểm môn học</h1>
                <p>Chỉ cho phép lưu điểm ở môn/lớp bạn được phân công giảng dạy.</p>
            </div>
            <div class="topbar-right">
                <a class="btn" href="${backUrl}">Quay lại danh sách</a>
            </div>
        </header>

        <section class="content">
            <c:if test="${not empty flashMessage}">
                <div class="flash-message alert ${flashType == 'error' ? 'alert-error' : 'alert-success'}">${flashMessage}</div>
            </c:if>

            <c:choose>
                <c:when test="${not empty d and d.readyForInput}">
                    <section class="card info-card">
                        <h2>${d.selectedStudent.name}</h2>
                        <div class="meta-grid">
                            <div><span>Mã học sinh</span><strong>${d.selectedStudent.id}</strong></div>
                            <div><span>Lớp</span><strong>${d.selectedStudent.className}</strong></div>
                            <div><span>Môn học</span><strong>${d.selectedSubjectName}</strong></div>
                            <div><span>Năm học</span><strong>${d.filter.namHoc}</strong></div>
                            <div><span>Học kỳ</span><strong>${d.filter.hocKy == '0' ? 'Cả năm' : (d.filter.hocKy == '1' ? 'Học kỳ I' : 'Học kỳ II')}</strong></div>
                            <div><span>Số cột TX</span><strong>${d.frequentColumns}</strong></div>
                        </div>
                    </section>

                    <section class="card form-card">
                        <form method="post" action="<c:url value='/teacher/score/edit'/>" class="score-form" data-score-edit-form data-tx-count="${d.frequentColumns}" data-target-semester="${d.filter.hocKy}">
                            <input type="hidden" name="namHoc" value="${d.filter.namHoc}">
                            <input type="hidden" name="hocKy" value="${d.filter.hocKy}">
                            <input type="hidden" name="mon" value="${d.filter.mon}">
                            <input type="hidden" name="studentId" value="${d.filter.studentId}">
                            <input type="hidden" name="hk1Teacher" value="${d.filter.teacherHk1}">
                            <input type="hidden" name="hk2Teacher" value="${d.filter.teacherHk2}">

                            <input type="hidden" name="returnQ" value="${returnQ}">
                            <input type="hidden" name="returnMon" value="${returnMon}">
                            <input type="hidden" name="returnHocKy" value="${returnHocKy}">
                            <input type="hidden" name="returnClassScope" value="${returnClassScope}">
                            <input type="hidden" name="returnPage" value="${returnPage}">

                            <div class="rule-line">${d.requiredTxMessage} | ${d.formulaText}</div>

                            <div class="semester-grid ${d.showSemester1 and d.showSemester2 ? '' : 'single'}">
                                <c:if test="${d.showSemester1}">
                                    <article class="semester-card" data-semester="1">
                                        <h3>Học kỳ I</h3>
                                        <div class="tx-grid">
                                            <c:forEach var="tx" items="${d.hk1Input.frequentScores}" varStatus="status">
                                                <label>
                                                    TX${status.index + 1}
                                                    <input type="number" name="hk1Tx" value="${tx}" min="0" max="10" step="0.01" required>
                                                </label>
                                            </c:forEach>
                                        </div>
                                        <div class="pair-grid">
                                            <label>
                                                Giữa kỳ
                                                <input type="number" name="hk1Midterm" value="${d.hk1Input.midterm}" min="0" max="10" step="0.01" required>
                                            </label>
                                            <label>
                                                Cuối kỳ
                                                <input type="number" name="hk1Final" value="${d.hk1Input.finalScore}" min="0" max="10" step="0.01" required>
                                            </label>
                                        </div>
                                        <div class="avg-line">ĐTB HKI: <strong data-semester-average="1">${d.hk1Input.averageDisplay}</strong></div>
                                    </article>
                                </c:if>

                                <c:if test="${d.showSemester2}">
                                    <article class="semester-card" data-semester="2">
                                        <h3>Học kỳ II</h3>
                                        <div class="tx-grid">
                                            <c:forEach var="tx" items="${d.hk2Input.frequentScores}" varStatus="status">
                                                <label>
                                                    TX${status.index + 1}
                                                    <input type="number" name="hk2Tx" value="${tx}" min="0" max="10" step="0.01" required>
                                                </label>
                                            </c:forEach>
                                        </div>
                                        <div class="pair-grid">
                                            <label>
                                                Giữa kỳ
                                                <input type="number" name="hk2Midterm" value="${d.hk2Input.midterm}" min="0" max="10" step="0.01" required>
                                            </label>
                                            <label>
                                                Cuối kỳ
                                                <input type="number" name="hk2Final" value="${d.hk2Input.finalScore}" min="0" max="10" step="0.01" required>
                                            </label>
                                        </div>
                                        <div class="avg-line">ĐTB HKII: <strong data-semester-average="2">${d.hk2Input.averageDisplay}</strong></div>
                                    </article>
                                </c:if>
                            </div>

                            <c:if test="${d.showSemester1 and d.showSemester2}">
                                <div class="year-average">ĐTB cả năm: <strong data-year-average>${d.yearAverageDisplay}</strong></div>
                            </c:if>

                            <div class="form-actions">
                                <a class="btn btn-outline" href="${backUrl}">Quay lại</a>
                                <button class="btn primary" type="submit">Lưu điểm</button>
                            </div>
                        </form>
                    </section>
                </c:when>
                <c:otherwise>
                    <section class="card empty-card">
                        <h3>Không đủ dữ liệu để chỉnh sửa điểm</h3>
                        <p>Vui lòng quay lại danh sách và chọn lại nhóm điểm hợp lệ.</p>
                        <a class="btn btn-outline" href="${backUrl}">Quay lại danh sách</a>
                    </section>
                </c:otherwise>
            </c:choose>
        </section>
    </main>
</div>

<script>
    (function () {
        const form = document.querySelector('[data-score-edit-form]');
        if (!form) {
            return;
        }

        const txCount = parseInt(form.dataset.txCount || '0', 10);
        const semesterOneCard = form.querySelector('[data-semester="1"]');
        const semesterTwoCard = form.querySelector('[data-semester="2"]');
        const semesterOneAverage = form.querySelector('[data-semester-average="1"]');
        const semesterTwoAverage = form.querySelector('[data-semester-average="2"]');
        const yearAverageElement = form.querySelector('[data-year-average]');

        function parseScore(input) {
            if (!input) {
                return null;
            }
            const raw = (input.value || '').trim().replace(',', '.');
            if (!raw) {
                return null;
            }
            const value = Number(raw);
            if (!Number.isFinite(value) || value < 0 || value > 10) {
                return null;
            }
            return value;
        }

        function formatDisplay(value) {
            if (!Number.isFinite(value)) {
                return '--';
            }
            const rounded = Math.round(value * 10) / 10;
            return rounded.toString().replace('.0', '');
        }

        function calculateSemester(card) {
            if (!card) {
                return null;
            }

            const txInputs = card.querySelectorAll('.tx-grid input');
            if (txInputs.length < txCount) {
                return null;
            }

            let totalTx = 0;
            for (const input of txInputs) {
                const score = parseScore(input);
                if (score == null) {
                    return null;
                }
                totalTx += score;
            }

            const midterm = parseScore(card.querySelector('input[name$="Midterm"]'));
            const finalScore = parseScore(card.querySelector('input[name$="Final"]'));
            if (midterm == null || finalScore == null) {
                return null;
            }

            return (totalTx + 2 * midterm + 3 * finalScore) / (txCount + 5);
        }

        function refreshAverages() {
            const avg1 = calculateSemester(semesterOneCard);
            const avg2 = calculateSemester(semesterTwoCard);

            if (semesterOneAverage) {
                semesterOneAverage.textContent = formatDisplay(avg1);
            }
            if (semesterTwoAverage) {
                semesterTwoAverage.textContent = formatDisplay(avg2);
            }
            if (yearAverageElement) {
                if (avg1 == null || avg2 == null) {
                    yearAverageElement.textContent = '--';
                } else {
                    yearAverageElement.textContent = formatDisplay((avg1 + 2 * avg2) / 3);
                }
            }
        }

        form.addEventListener('input', function (event) {
            if (event.target.matches('input[type="number"]')) {
                refreshAverages();
            }
        });

        refreshAverages();
    })();
</script>
</body>
</html>
