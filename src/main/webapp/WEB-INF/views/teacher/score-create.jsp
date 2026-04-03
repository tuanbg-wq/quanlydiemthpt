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

    <main class="main teacher-score-create-page">
        <c:set var="d" value="${createData}"/>
        <c:set var="scopeData" value="${createScope}"/>

        <header class="topbar">
            <div class="topbar-left">
                <h1>Thêm điểm số lớp bộ môn</h1>
                <p>Chỉ được thêm/sửa/xóa điểm cho các lớp bộ môn bạn đang giảng dạy. Lớp chủ nhiệm chỉ xem.</p>
            </div>
            <div class="topbar-right">
                <a class="btn btn-outline" href="<c:url value='/teacher/score'/>">Quay lại danh sách</a>
            </div>
        </header>

        <section class="content">
            <c:if test="${not empty flashMessage}">
                <div class="flash-message alert ${flashType == 'error' ? 'alert-error' : 'alert-success'}">${flashMessage}</div>
            </c:if>
            <c:if test="${not empty warningMessage}">
                <div class="flash-message alert alert-error">${warningMessage}</div>
            </c:if>

            <section class="card filter-card">
                <form method="get" action="<c:url value='/teacher/score/create'/>" class="create-filters" autocomplete="off">
                    <input type="hidden" name="applyFilter" value="1">

                    <div class="filter-item">
                        <label for="namHoc">Năm học</label>
                        <input id="namHoc" type="text" name="namHoc" value="${filter.namHoc}" readonly>
                    </div>

                    <div class="filter-item">
                        <label for="hocKy">Học kỳ</label>
                        <select id="hocKy" name="hocKy">
                            <option value="0" ${filter.hocKy == '0' ? 'selected' : ''}>Cả năm</option>
                            <option value="1" ${filter.hocKy == '1' ? 'selected' : ''}>Học kỳ I</option>
                            <option value="2" ${filter.hocKy == '2' ? 'selected' : ''}>Học kỳ II</option>
                        </select>
                    </div>

                    <div class="filter-item">
                        <label for="lop">Lớp bộ môn</label>
                        <select id="lop" name="lop" data-class-select>
                            <option value="">Chọn lớp</option>
                            <c:forEach var="classItem" items="${scopeData.classOptions}">
                                <option value="${classItem.id}" ${filter.lop == classItem.id ? 'selected' : ''}>${classItem.name} (${classItem.id})</option>
                            </c:forEach>
                        </select>
                    </div>

                    <div class="filter-item">
                        <label for="mon">Môn học</label>
                        <select id="mon" name="mon" data-subject-select>
                            <option value="">Chọn môn</option>
                            <c:forEach var="subject" items="${scopeData.subjectOptions}">
                                <option value="${subject.id}" ${filter.mon == subject.id ? 'selected' : ''}>${subject.name}</option>
                            </c:forEach>
                        </select>
                    </div>

                    <div class="filter-item suggest-field search-item filter-wide">
                        <label for="q">Học sinh</label>
                        <c:set var="studentInputValue" value="${filter.q}"/>
                        <c:if test="${not empty d and not empty d.selectedStudent}">
                            <c:set var="studentInputValue" value="${d.selectedStudent.name} (${d.selectedStudent.id}) - ${d.selectedStudent.className}"/>
                        </c:if>
                        <input id="q"
                               type="text"
                               name="q"
                               value="${studentInputValue}"
                               placeholder="Nhập mã hoặc tên học sinh..."
                               data-student-input>
                        <input type="hidden" name="studentId" value="${filter.studentId}" data-student-id>
                        <div class="suggest-list" data-student-suggest></div>
                    </div>

                    <div class="filter-actions">
                        <button class="btn primary" type="submit">Lọc học sinh</button>
                    </div>
                </form>
            </section>

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
                        <form method="post" action="<c:url value='/teacher/score/create'/>" class="score-form" data-score-edit-form data-tx-count="${d.frequentColumns}" data-target-semester="${d.filter.hocKy}">
                            <input type="hidden" name="namHoc" value="${d.filter.namHoc}">
                            <input type="hidden" name="hocKy" value="${d.filter.hocKy}">
                            <input type="hidden" name="lop" value="${d.filter.lop}">
                            <input type="hidden" name="mon" value="${d.filter.mon}">
                            <input type="hidden" name="q" value="${d.selectedStudent.id}">
                            <input type="hidden" name="studentId" value="${d.filter.studentId}">

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
                                <a class="btn btn-outline" href="<c:url value='/teacher/score'/>">Quay lại</a>
                                <button class="btn primary" type="submit">Lưu điểm</button>
                            </div>
                        </form>
                    </section>
                </c:when>
                <c:otherwise>
                    <section class="card empty-card">
                        <h3>Chưa đủ dữ liệu để nhập điểm</h3>
                        <p>Hãy chọn lớp bộ môn, môn học và học sinh trước khi lưu điểm.</p>
                    </section>
                </c:otherwise>
            </c:choose>
        </section>
    </main>
</div>

<script>
    (function () {
        const filterForm = document.querySelector('.create-filters');
        const classSelect = document.querySelector('[data-class-select]');
        const subjectSelect = document.querySelector('[data-subject-select]');
        const studentInput = document.querySelector('[data-student-input]');
        const studentIdInput = document.querySelector('[data-student-id]');
        const suggestBox = document.querySelector('[data-student-suggest]');

        function closeSuggestBox() {
            if (!suggestBox) {
                return;
            }
            suggestBox.innerHTML = '';
            suggestBox.classList.remove('open');
        }

        function renderSuggestItems(items) {
            closeSuggestBox();
            if (!suggestBox || !items || items.length === 0) {
                return;
            }

            const fragment = document.createDocumentFragment();
            items.forEach(function (item) {
                const button = document.createElement('button');
                button.type = 'button';
                button.className = 'suggest-item';
                button.textContent = item.name + ' (' + item.id + ') - ' + item.className;
                button.addEventListener('mousedown', function (event) {
                    event.preventDefault();
                    studentInput.value = item.name + ' (' + item.id + ') - ' + item.className;
                    studentIdInput.value = item.id;
                    closeSuggestBox();
                });
                fragment.appendChild(button);
            });

            suggestBox.appendChild(fragment);
            suggestBox.classList.add('open');
        }

        function debounce(fn, wait) {
            let timeoutId = null;
            return function () {
                const context = this;
                const args = arguments;
                clearTimeout(timeoutId);
                timeoutId = setTimeout(function () {
                    fn.apply(context, args);
                }, wait);
            };
        }

        function loadStudents() {
            if (!studentInput || !classSelect || !suggestBox) {
                return;
            }
            const classId = (classSelect.value || '').trim();
            if (!classId) {
                closeSuggestBox();
                return;
            }
            const keyword = (studentInput.value || '').trim();
            const url = '<c:url value="/teacher/score/suggest/students"/>'
                + '?classId=' + encodeURIComponent(classId)
                + '&q=' + encodeURIComponent(keyword);

            fetch(url, { headers: { 'Accept': 'application/json' } })
                .then(function (response) {
                    return response.ok ? response.json() : [];
                })
                .then(renderSuggestItems)
                .catch(closeSuggestBox);
        }

        const debouncedLoadStudents = debounce(loadStudents, 200);

        if (studentInput && studentIdInput) {
            studentInput.addEventListener('input', function () {
                studentIdInput.value = '';
                debouncedLoadStudents();
            });
            studentInput.addEventListener('focus', debouncedLoadStudents);
            studentInput.addEventListener('blur', function () {
                setTimeout(closeSuggestBox, 120);
            });
        }

        if (classSelect) {
            classSelect.addEventListener('change', function () {
                if (studentInput) {
                    studentInput.value = '';
                }
                if (studentIdInput) {
                    studentIdInput.value = '';
                }
                if (subjectSelect) {
                    subjectSelect.value = '';
                }
                if (filterForm) {
                    filterForm.submit();
                }
            });
        }

        if (subjectSelect) {
            subjectSelect.addEventListener('change', function () {
                if (studentInput) {
                    studentInput.value = '';
                }
                if (studentIdInput) {
                    studentIdInput.value = '';
                }
            });
        }

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
