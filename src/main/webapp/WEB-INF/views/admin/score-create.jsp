<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>${pageTitle}</title>

  <link rel="stylesheet" href="<c:url value='/css/admin-layout.css'/>">
  <link rel="stylesheet" href="<c:url value='/css/score-list.css'/>">
</head>
<body>
<div class="layout">
  <jsp:include page="/WEB-INF/views/admin/_sidebar.jsp"/>
  <c:set var="isEditMode" value="${formMode == 'edit'}"/>
  <c:set var="annualMode" value="${filter.hocKy == '0'}"/>
  <c:set var="singleSemesterMode" value="${filter.hocKy == '1' || filter.hocKy == '2'}"/>
  <c:url var="scoreFilterUrl" value="${isEditMode ? '/admin/score/edit' : '/admin/score/create'}"/>
  <c:url var="scoreSubmitUrl" value="${isEditMode ? '/admin/score/edit' : '/admin/score/create'}"/>

  <main class="main score-create-page">
    <header class="score-header score-create-header">
      <div class="header-left">
        <h1>Nhập điểm số THPT</h1>
        <p>Lưu điểm theo từng học kỳ hoặc cả năm, tự động áp dụng số cột điểm thường xuyên theo môn.</p>
      </div>
    </header>

    <section class="content">
      <c:if test="${not empty flashMessage}">
        <div class="alert ${flashType == 'error' ? 'alert-error' : (flashType == 'info' ? 'alert-info' : 'alert-success')}">
          ${flashMessage}
        </div>
      </c:if>
      <c:if test="${not empty createData and not empty createData.consistencyError}">
        <div class="alert alert-error">
          ${createData.consistencyError}
        </div>
      </c:if>
      <c:if test="${not empty createData and not empty createData.filterValidationMessage}">
        <div class="alert alert-error">
          ${createData.filterValidationMessage}
        </div>
      </c:if>
      <c:if test="${not empty createData and not empty createData.existingScoreNotice}">
        <div class="alert alert-info">
          ${createData.existingScoreNotice}
        </div>
      </c:if>

      <section class="card filter-card score-create-filter-card">
        <form method="get"
              action="${scoreFilterUrl}"
              class="filters score-create-filters ${annualMode ? 'annual-mode' : ''}"
              autocomplete="off">
          <input type="hidden" name="applyFilter" value="${empty filter.applyFilter ? '0' : filter.applyFilter}" id="applyFilterFlag">
          <input type="hidden" name="teacherHk1" value="${filter.teacherHk1}" id="filterTeacherHk1">
          <input type="hidden" name="teacherHk2" value="${filter.teacherHk2}" id="filterTeacherHk2">
          <div class="filter-item">
            <label for="namHoc">Năm học</label>
            <input id="namHoc"
                   type="text"
                   name="namHoc"
                   value="${filter.namHoc}"
                   list="schoolYearOptions"
                   placeholder="Ví dụ: 2023-2024">
            <datalist id="schoolYearOptions">
              <c:forEach var="item" items="${createData.schoolYears}">
                <option value="${item.id}">${item.name}</option>
              </c:forEach>
            </datalist>
          </div>

          <div class="filter-item">
            <label for="hocKy">Học kỳ</label>
            <select id="hocKy" name="hocKy">
              <option value="0" ${filter.hocKy == '0' ? 'selected' : ''}>Cả năm</option>
              <option value="1" ${filter.hocKy == '1' ? 'selected' : ''}>Học kỳ I</option>
              <option value="2" ${filter.hocKy == '2' ? 'selected' : ''}>Học kỳ II</option>
            </select>
          </div>

          <div class="filter-item" ${isEditMode ? 'style="display:none;"' : ''}>
            <label for="khoi">Khối</label>
            <select id="khoi" name="khoi" ${isEditMode ? 'disabled' : ''}>
              <option value="">Tất cả</option>
              <c:forEach var="item" items="${createData.grades}">
                <option value="${item.id}" ${filter.khoi == item.id ? 'selected' : ''}>${item.name}</option>
              </c:forEach>
            </select>
          </div>

          <div class="filter-item suggest-field" ${isEditMode ? 'style="display:none;"' : ''}>
            <label for="khoa">Khóa học</label>
            <input id="khoa"
                   type="text"
                   name="khoa"
                   ${isEditMode ? 'disabled' : ''}
                   value="${filter.khoa}"
                   placeholder="Nhập mã/tên khóa..."
                   data-course-input="true"
                   autocomplete="off">
            <div class="suggest-list" data-course-suggest></div>
          </div>

          <div class="filter-item" ${isEditMode ? 'style="display:none;"' : ''}>
            <label for="lop">Lớp</label>
            <select id="lop" name="lop" ${isEditMode ? 'disabled' : ''}>
              <option value="">Tất cả</option>
              <c:forEach var="item" items="${createData.classes}">
                <option value="${item.id}" ${filter.lop == item.id ? 'selected' : ''}>${item.name}</option>
              </c:forEach>
            </select>
          </div>

          <div class="filter-item">
            <label for="mon">Môn học</label>
            <select id="mon" name="mon">
              <option value="">Chọn môn</option>
              <c:forEach var="item" items="${createData.subjects}">
                <option value="${item.id}" ${filter.mon == item.id ? 'selected' : ''}>${item.name}</option>
              </c:forEach>
            </select>
          </div>

          <div class="filter-item suggest-field search-item" ${isEditMode ? 'style="display:none;"' : ''}>
            <label for="q">Tìm học sinh</label>
            <c:set var="studentInputValue" value="${filter.q}"/>
            <c:if test="${not empty createData.selectedStudent}">
              <c:set var="studentInputValue" value="${createData.selectedStudent.name} (${createData.selectedStudent.id}) - ${createData.selectedStudent.className}"/>
            </c:if>
            <input id="q"
                   type="text"
                   name="q"
                   ${isEditMode ? 'disabled' : ''}
                   value="${studentInputValue}"
                   placeholder="Nhập tên hoặc mã học sinh..."
                   data-student-input="true"
                   autocomplete="off">
            <input type="hidden" name="studentId" value="${filter.studentId}" data-student-id="true">
            <div class="suggest-list" data-student-suggest></div>
          </div>

          <div class="filter-actions">
            <button class="btn filter-btn btn-orange" type="submit" data-filter-submit="true">Lọc</button>
            <button class="btn btn-orange btn-outline" type="button" data-open-rule-modal="true">Xem quy định</button>
          </div>
        </form>
      </section>

      <div class="score-create-body">
        <section class="card score-input-card">
          <c:choose>
            <c:when test="${createData.readyForInput}">
              <div class="student-banner">
                <h2>${createData.selectedStudent.name}</h2>
                <p>Mã HS: ${createData.selectedStudent.id} • Lớp: ${createData.selectedStudent.className} • Môn: ${createData.selectedSubjectName}</p>
              </div>

              <form method="post"
                    action="${scoreSubmitUrl}"
                    class="score-create-form"
                    data-score-create-form
                    data-target-semester="${filter.hocKy}"
                    data-tx-count="${createData.frequentColumns}">

                <input type="hidden" name="namHoc" value="${filter.namHoc}">
                <input type="hidden" name="hocKy" value="${filter.hocKy}">
                <input type="hidden" name="khoi" value="${filter.khoi}">
                <input type="hidden" name="khoa" value="${filter.khoa}">
                <input type="hidden" name="lop" value="${filter.lop}">
                <input type="hidden" name="mon" value="${filter.mon}">
                <input type="hidden" name="q" value="${filter.q}">
                <input type="hidden" name="studentId" value="${filter.studentId}">

                <div class="rule-inline">
                  <strong>${createData.requiredTxMessage}</strong>
                  <span>${createData.formulaText}</span>
                </div>

                <div class="semester-grid ${createData.showSemester1 && createData.showSemester2 ? '' : 'single'}">
                  <c:if test="${createData.showSemester1}">
                    <article class="semester-card" data-semester="1">
                      <header>
                        <h3>Học kỳ I</h3>
                      </header>

                      <div class="teacher-field suggest-field">
                        <label for="hk1Teacher">Giáo viên chấm học kỳ I</label>
                        <input id="hk1Teacher"
                               type="text"
                               name="hk1Teacher"
                               value="${filter.teacherHk1}"
                               placeholder="Nhập tên/mã giáo viên..."
                               data-teacher-input="1"
                               autocomplete="off">
                        <div class="suggest-list" data-teacher-suggest="1"></div>
                      </div>

                      <div class="input-group">
                        <p class="group-title">Đánh giá thường xuyên (HS1)</p>
                        <div class="tx-grid">
                          <c:forEach var="tx" items="${createData.hk1Input.frequentScores}" varStatus="status">
                            <label>
                              Cột ${status.index + 1}
                              <input type="number"
                                     name="hk1Tx"
                                     value="${tx}"
                                     min="0"
                                     max="10"
                                     step="0.01"
                                     required>
                            </label>
                          </c:forEach>
                        </div>
                      </div>

                      <div class="input-pair">
                        <label>
                          Giữa kỳ (HS2)
                          <input type="number"
                                 name="hk1Midterm"
                                 value="${createData.hk1Input.midterm}"
                                 min="0"
                                 max="10"
                                 step="0.01"
                                 required>
                        </label>
                        <label>
                          Cuối kỳ (HS3)
                          <input type="number"
                                 name="hk1Final"
                                 value="${createData.hk1Input.finalScore}"
                                 min="0"
                                 max="10"
                                 step="0.01"
                                 required>
                        </label>
                      </div>
                      <c:if test="${annualMode}">
                        <div class="semester-result">
                          <span>ĐTB học kỳ I</span>
                          <strong data-semester-average="1">${createData.hk1Input.averageDisplay}</strong>
                        </div>
                      </c:if>
                    </article>
                  </c:if>

                  <c:if test="${createData.showSemester2}">
                    <article class="semester-card" data-semester="2">
                      <header>
                        <h3>Học kỳ II</h3>
                      </header>

                      <div class="teacher-field suggest-field">
                        <label for="hk2Teacher">Giáo viên chấm học kỳ II</label>
                        <input id="hk2Teacher"
                               type="text"
                               name="hk2Teacher"
                               value="${filter.teacherHk2}"
                               placeholder="Nhập tên/mã giáo viên..."
                               data-teacher-input="2"
                               autocomplete="off">
                        <div class="suggest-list" data-teacher-suggest="2"></div>
                      </div>

                      <div class="input-group">
                        <p class="group-title">Đánh giá thường xuyên (HS1)</p>
                        <div class="tx-grid">
                          <c:forEach var="tx" items="${createData.hk2Input.frequentScores}" varStatus="status">
                            <label>
                              Cột ${status.index + 1}
                              <input type="number"
                                     name="hk2Tx"
                                     value="${tx}"
                                     min="0"
                                     max="10"
                                     step="0.01"
                                     required>
                            </label>
                          </c:forEach>
                        </div>
                      </div>

                      <div class="input-pair">
                        <label>
                          Giữa kỳ (HS2)
                          <input type="number"
                                 name="hk2Midterm"
                                 value="${createData.hk2Input.midterm}"
                                 min="0"
                                 max="10"
                                 step="0.01"
                                 required>
                        </label>
                        <label>
                          Cuối kỳ (HS3)
                          <input type="number"
                                 name="hk2Final"
                                 value="${createData.hk2Input.finalScore}"
                                 min="0"
                                 max="10"
                                 step="0.01"
                                 required>
                        </label>
                      </div>

                      <c:if test="${annualMode}">
                        <div class="semester-result">
                          <span>ĐTB học kỳ II</span>
                          <strong data-semester-average="2">${createData.hk2Input.averageDisplay}</strong>
                        </div>
                      </c:if>
                    </article>
                  </c:if>
                </div>

                <c:if test="${createData.showSemester1 && createData.showSemester2}">
                  <div class="year-result-card">
                    <p>Kết quả cả năm</p>
                    <div class="year-result-value">
                      <span>ĐTB môn cả năm</span>
                      <strong data-year-average>${createData.yearAverageDisplay}</strong>
                    </div>
                    <small>ĐTBmcn = (ĐTBhkI + 2 × ĐTBhkII) / 3</small>
                  </div>
                </c:if>

                <c:if test="${singleSemesterMode}">
                  <div class="semester-summary-card">
                    <p>Kết quả học kỳ ${filter.hocKy == '1' ? 'I' : 'II'}</p>
                    <div class="semester-summary-value">
                      <span>ĐTB học kỳ</span>
                      <strong data-semester-summary>
                        ${filter.hocKy == '1' ? createData.hk1Input.averageDisplay : createData.hk2Input.averageDisplay}
                      </strong>
                    </div>
                  </div>
                </c:if>

                <div class="conduct-grid">
                  <c:choose>
                    <c:when test="${filter.hocKy == '1'}">
                      <label>
                        Hạnh kiểm học kỳ I
                        <select name="hk1Conduct">
                          <c:forEach var="item" items="${createData.conductOptions}">
                            <option value="${item.id}" ${createData.hk1Conduct.value == item.id ? 'selected' : ''}>${item.name}</option>
                          </c:forEach>
                        </select>
                      </label>
                    </c:when>
                    <c:when test="${filter.hocKy == '2'}">
                      <label>
                        Hạnh kiểm học kỳ II
                        <select name="hk2Conduct">
                          <c:forEach var="item" items="${createData.conductOptions}">
                            <option value="${item.id}" ${createData.hk2Conduct.value == item.id ? 'selected' : ''}>${item.name}</option>
                          </c:forEach>
                        </select>
                      </label>
                    </c:when>
                    <c:otherwise>
                      <label>
                        Hạnh kiểm cả năm
                        <select name="yearConduct">
                          <c:forEach var="item" items="${createData.conductOptions}">
                            <option value="${item.id}" ${createData.yearConduct.value == item.id ? 'selected' : ''}>${item.name}</option>
                          </c:forEach>
                        </select>
                      </label>
                    </c:otherwise>
                  </c:choose>
                </div>

                <div class="form-actions">
                  <a class="btn btn-orange btn-outline" href="<c:url value='/admin/score'/>">Quay lại danh sách</a>
                  <button class="btn btn-orange" type="submit">Lưu kết quả</button>
                </div>
              </form>
            </c:when>
            <c:otherwise>
              <div class="empty-state">
                <h3>Chưa đủ dữ liệu để nhập điểm</h3>
                <p>Vui lòng chọn đầy đủ năm học, lớp, môn và học sinh rồi bấm <strong>Lọc</strong>.</p>
              </div>
              <div class="form-actions form-actions-only-back">
                <a class="btn btn-orange btn-outline" href="<c:url value='/admin/score'/>">Quay lại danh sách</a>
              </div>
            </c:otherwise>
          </c:choose>
        </section>
      </div>
    </section>
  </main>
</div>

<div class="score-rule-modal" data-rule-modal hidden>
  <div class="score-rule-backdrop" data-close-rule-modal></div>
  <div class="score-rule-dialog" role="dialog" aria-modal="true" aria-label="Quy định cột điểm">
    <button type="button" class="score-rule-close" data-close-rule-modal aria-label="Đóng">×</button>
    <h3>Quy định số cột điểm và công thức tính</h3>
    <p class="rule-formula">${createData.formulaText}</p>
    <p class="rule-formula">ĐTBmcn = (ĐTBmhkI + 2 × ĐTBmhkII) / 3</p>
    <p class="rule-note">Điểm miệng được tính trong nhóm điểm đánh giá thường xuyên.</p>

    <div class="rule-table-wrap">
      <table class="rule-table">
        <thead>
        <tr>
          <th>Môn / hoạt động</th>
          <th>Số cột thường xuyên</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="rule" items="${createData.frequentRuleItems}">
          <tr>
            <td>${rule.subjectName}</td>
            <td>${rule.frequentColumns}</td>
          </tr>
        </c:forEach>
        </tbody>
      </table>
    </div>
  </div>
</div>

<script>
  (function () {
    const form = document.querySelector('[data-score-create-form]');
    if (form) {
      const txCount = parseInt(form.dataset.txCount || '0', 10);
      const semesterOneCard = form.querySelector('[data-semester="1"]');
      const semesterTwoCard = form.querySelector('[data-semester="2"]');
      const semesterOneAverage = form.querySelector('[data-semester-average="1"]');
      const semesterTwoAverage = form.querySelector('[data-semester-average="2"]');
      const yearAverageElement = form.querySelector('[data-year-average]');
      const semesterSummaryElement = form.querySelector('[data-semester-summary]');
      const targetSemester = form.dataset.targetSemester || '0';

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

        let totalTx = 0;
        const txInputs = card.querySelectorAll('.tx-grid input');
        if (txInputs.length < txCount) {
          return null;
        }

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
            const yearAverage = (avg1 + 2 * avg2) / 3;
            yearAverageElement.textContent = formatDisplay(yearAverage);
          }
        }

        if (semesterSummaryElement) {
          const summaryValue = targetSemester === '1'
            ? avg1
            : (targetSemester === '2' ? avg2 : null);
          semesterSummaryElement.textContent = formatDisplay(summaryValue);
        }
      }

      form.addEventListener('input', function (event) {
        if (event.target.matches('input[type="number"]')) {
          refreshAverages();
        }
      });

      refreshAverages();
    }

    const ruleModal = document.querySelector('[data-rule-modal]');
    const openRuleButton = document.querySelector('[data-open-rule-modal]');
    const filterForm = document.querySelector('.score-create-filters');
    const semesterFilterSelect = document.querySelector('#hocKy');
    const applyFilterFlag = document.querySelector('#applyFilterFlag');
    const filterSubmitButton = document.querySelector('[data-filter-submit]');

    if (filterSubmitButton && applyFilterFlag) {
      filterSubmitButton.addEventListener('click', function () {
        applyFilterFlag.value = '1';
      });
    }

    if (filterForm && applyFilterFlag) {
      filterForm.addEventListener('submit', function () {
        if (filterForm.dataset.autoSubmit !== '1') {
          applyFilterFlag.value = '1';
          return;
        }
        filterForm.dataset.autoSubmit = '0';
      });
    }

    if (filterForm && semesterFilterSelect && applyFilterFlag) {
      semesterFilterSelect.addEventListener('change', function () {
        applyFilterFlag.value = '0';
        filterForm.dataset.autoSubmit = '1';
        filterForm.submit();
      });
    }

    if (ruleModal && openRuleButton) {
      function openRuleModal() {
        ruleModal.hidden = false;
        document.body.classList.add('modal-open');
      }

      function closeRuleModal() {
        ruleModal.hidden = true;
        document.body.classList.remove('modal-open');
      }

      openRuleButton.addEventListener('click', openRuleModal);
      ruleModal.addEventListener('click', function (event) {
        if (event.target.hasAttribute('data-close-rule-modal')) {
          closeRuleModal();
        }
      });
      document.addEventListener('keydown', function (event) {
        if (event.key === 'Escape' && !ruleModal.hidden) {
          closeRuleModal();
        }
      });
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

    function closeSuggestBox(box) {
      if (!box) {
        return;
      }
      box.innerHTML = '';
      box.classList.remove('open');
    }

    function renderSuggestItems(box, items, onSelect) {
      closeSuggestBox(box);
      if (!items || items.length === 0) {
        return;
      }

      const fragment = document.createDocumentFragment();
      items.forEach(function (item) {
        const button = document.createElement('button');
        button.type = 'button';
        button.className = 'suggest-item';
        button.textContent = item.label;
        button.addEventListener('mousedown', function (event) {
          event.preventDefault();
          onSelect(item);
          closeSuggestBox(box);
        });
        fragment.appendChild(button);
      });

      box.appendChild(fragment);
      box.classList.add('open');
    }

    const courseInput = document.querySelector('[data-course-input]');
    const courseSuggestBox = document.querySelector('[data-course-suggest]');
    if (courseInput && courseSuggestBox) {
      const loadCourseSuggestions = debounce(function () {
        const keyword = (courseInput.value || '').trim();
        fetch('<c:url value="/admin/score/suggest/courses"/>?q=' + encodeURIComponent(keyword), {
          headers: { 'Accept': 'application/json' }
        })
          .then(function (response) { return response.ok ? response.json() : []; })
          .then(function (rows) {
            const items = (rows || []).map(function (row) {
              return {
                id: row.id,
                label: row.id + ' (' + row.name + ')'
              };
            });
            renderSuggestItems(courseSuggestBox, items, function (selected) {
              courseInput.value = selected.id || '';
            });
          })
          .catch(function () {
            closeSuggestBox(courseSuggestBox);
          });
      }, 200);

      courseInput.addEventListener('input', loadCourseSuggestions);
      courseInput.addEventListener('focus', loadCourseSuggestions);
      courseInput.addEventListener('blur', function () {
        setTimeout(function () {
          closeSuggestBox(courseSuggestBox);
        }, 120);
      });
    }

    const studentInput = document.querySelector('[data-student-input]');
    const studentIdInput = document.querySelector('[data-student-id]');
    const studentSuggestBox = document.querySelector('[data-student-suggest]');
    const classInput = document.querySelector('#lop');
    const gradeInput = document.querySelector('#khoi');
    const subjectInput = document.querySelector('#mon');
    const schoolYearInput = document.querySelector('#namHoc');

    function setSelectValue(selectElement, value, label) {
      if (!selectElement || !value) {
        return;
      }
      const normalized = String(value).toLowerCase();
      let targetOption = null;
      for (const option of selectElement.options) {
        if (String(option.value || '').toLowerCase() === normalized) {
          targetOption = option;
          break;
        }
      }
      if (!targetOption) {
        targetOption = document.createElement('option');
        targetOption.value = value;
        targetOption.textContent = label || value;
        selectElement.appendChild(targetOption);
      }
      selectElement.value = targetOption.value;
    }

    if (studentInput && studentIdInput && studentSuggestBox) {
      const loadStudentSuggestions = debounce(function () {
        const keyword = (studentInput.value || '').trim();
        const classId = classInput ? (classInput.value || '').trim() : '';
        const url = '<c:url value="/admin/score/suggest/students"/>'
          + '?q=' + encodeURIComponent(keyword)
          + '&classId=' + encodeURIComponent(classId);

        fetch(url, { headers: { 'Accept': 'application/json' } })
          .then(function (response) { return response.ok ? response.json() : []; })
          .then(function (rows) {
            const items = (rows || []).map(function (row) {
              return {
                id: row.id,
                label: row.name + ' (' + row.id + ') - ' + row.className,
                className: row.className,
                classId: row.classId,
                grade: row.grade,
                courseId: row.courseId
              };
            });
            renderSuggestItems(studentSuggestBox, items, function (selected) {
              studentInput.value = selected.label;
              studentIdInput.value = selected.id || '';
              setSelectValue(classInput, selected.classId, selected.className || selected.classId);
              if (selected.grade) {
                setSelectValue(gradeInput, selected.grade, 'Khối ' + selected.grade);
              }
              if (courseInput && selected.courseId) {
                courseInput.value = selected.courseId;
              }
            });
          })
          .catch(function () {
            closeSuggestBox(studentSuggestBox);
          });
      }, 200);

      studentInput.addEventListener('input', function () {
        studentIdInput.value = '';
        loadStudentSuggestions();
      });
      studentInput.addEventListener('focus', loadStudentSuggestions);
      studentInput.addEventListener('blur', function () {
        setTimeout(function () {
          closeSuggestBox(studentSuggestBox);
        }, 120);
      });

      if (classInput) {
        classInput.addEventListener('change', function () {
          studentIdInput.value = '';
          studentInput.value = '';
        });
      }
    }

    function bindTeacherSuggest(inputElement, suggestBox, semesterValue) {
      if (!inputElement || !suggestBox) {
        return;
      }

      function syncTeacherToFilter() {
        const hidden = document.querySelector('#filterTeacherHk' + semesterValue);
        if (hidden) {
          hidden.value = inputElement.value || '';
        }
      }

      const loadTeacherSuggestions = debounce(function () {
        const keyword = (inputElement.value || '').trim();
        const subjectId = subjectInput ? (subjectInput.value || '').trim() : '';
        const classId = classInput ? (classInput.value || '').trim() : '';
        const namHoc = schoolYearInput ? (schoolYearInput.value || '').trim() : '';
        const url = '<c:url value="/admin/score/suggest/teachers"/>'
          + '?q=' + encodeURIComponent(keyword)
          + '&subjectId=' + encodeURIComponent(subjectId)
          + '&classId=' + encodeURIComponent(classId)
          + '&namHoc=' + encodeURIComponent(namHoc)
          + '&hocKy=' + encodeURIComponent(semesterValue);

        fetch(url, { headers: { 'Accept': 'application/json' } })
          .then(function (response) { return response.ok ? response.json() : []; })
          .then(function (rows) {
            const items = (rows || []).map(function (row) {
              return {
                id: row.id,
                label: row.name + ' (' + row.id + ')'
              };
            });
            renderSuggestItems(suggestBox, items, function (selected) {
              inputElement.value = selected.label || selected.id || '';
              syncTeacherToFilter();
            });
          })
          .catch(function () {
            closeSuggestBox(suggestBox);
          });
      }, 220);

      inputElement.addEventListener('input', function () {
        syncTeacherToFilter();
        loadTeacherSuggestions();
      });
      inputElement.addEventListener('focus', loadTeacherSuggestions);
      inputElement.addEventListener('blur', function () {
        syncTeacherToFilter();
        setTimeout(function () {
          closeSuggestBox(suggestBox);
        }, 120);
      });
    }

    const teacherInputHk1 = document.querySelector('[data-teacher-input="1"]');
    const teacherSuggestHk1 = document.querySelector('[data-teacher-suggest="1"]');
    bindTeacherSuggest(teacherInputHk1, teacherSuggestHk1, '1');

    const teacherInputHk2 = document.querySelector('[data-teacher-input="2"]');
    const teacherSuggestHk2 = document.querySelector('[data-teacher-suggest="2"]');
    bindTeacherSuggest(teacherInputHk2, teacherSuggestHk2, '2');
  })();
</script>
</body>
</html>
