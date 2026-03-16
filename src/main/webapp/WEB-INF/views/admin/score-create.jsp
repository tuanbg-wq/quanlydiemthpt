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

  <main class="main score-create-page">
    <header class="score-header score-create-header">
      <div class="header-left">
        <h1>Nhập điểm số THPT</h1>
        <p>Lưu điểm theo từng học kỳ hoặc cả năm, tự động áp dụng số cột điểm thường xuyên theo môn.</p>
      </div>
      <div class="header-right">
        <a class="btn" href="<c:url value='/admin/score'/>">Quay lại danh sách</a>
      </div>
    </header>

    <section class="content">
      <c:if test="${not empty flashMessage}">
        <div class="alert ${flashType == 'error' ? 'alert-error' : (flashType == 'info' ? 'alert-info' : 'alert-success')}">
          ${flashMessage}
        </div>
      </c:if>

      <section class="card filter-card score-create-filter-card">
        <form method="get" action="<c:url value='/admin/score/create'/>" class="filters score-create-filters" autocomplete="off">
          <div class="filter-item">
            <label for="namHoc">Năm học</label>
            <select id="namHoc" name="namHoc">
              <c:forEach var="item" items="${createData.schoolYears}">
                <option value="${item.id}" ${filter.namHoc == item.id ? 'selected' : ''}>${item.name}</option>
              </c:forEach>
            </select>
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
            <label for="khoi">Khối</label>
            <select id="khoi" name="khoi">
              <option value="">Tất cả</option>
              <c:forEach var="item" items="${createData.grades}">
                <option value="${item.id}" ${filter.khoi == item.id ? 'selected' : ''}>${item.name}</option>
              </c:forEach>
            </select>
          </div>

          <div class="filter-item">
            <label for="khoa">Khóa học</label>
            <select id="khoa" name="khoa">
              <option value="">Tất cả</option>
              <c:forEach var="item" items="${createData.courses}">
                <option value="${item.id}" ${filter.khoa == item.id ? 'selected' : ''}>${item.id} (${item.name})</option>
              </c:forEach>
            </select>
          </div>

          <div class="filter-item">
            <label for="lop">Lớp</label>
            <select id="lop" name="lop">
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

          <div class="filter-item search-item">
            <label for="q">Tìm học sinh</label>
            <input id="q" type="text" name="q" value="${filter.q}" placeholder="Nhập tên hoặc mã học sinh...">
          </div>

          <div class="filter-item">
            <label for="studentId">Học sinh</label>
            <select id="studentId" name="studentId">
              <option value="">Chọn học sinh</option>
              <c:forEach var="item" items="${createData.students}">
                <option value="${item.id}" ${filter.studentId == item.id ? 'selected' : ''}>${item.name} (${item.id}) - ${item.className}</option>
              </c:forEach>
            </select>
          </div>

          <div class="filter-actions">
            <button class="btn filter-btn" type="submit">Tải dữ liệu</button>
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
                    action="<c:url value='/admin/score/create'/>"
                    class="score-create-form"
                    data-score-create-form
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

                      <div class="semester-result">
                        <span>ĐTB học kỳ I</span>
                        <strong data-semester-average="1">${createData.hk1Input.averageDisplay}</strong>
                      </div>
                    </article>
                  </c:if>

                  <c:if test="${createData.showSemester2}">
                    <article class="semester-card" data-semester="2">
                      <header>
                        <h3>Học kỳ II</h3>
                      </header>

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

                      <div class="semester-result">
                        <span>ĐTB học kỳ II</span>
                        <strong data-semester-average="2">${createData.hk2Input.averageDisplay}</strong>
                      </div>
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

                <div class="form-actions">
                  <button class="btn primary" type="submit">Lưu kết quả</button>
                </div>
              </form>
            </c:when>
            <c:otherwise>
              <div class="empty-state">
                <h3>Chưa đủ dữ liệu để nhập điểm</h3>
                <p>Vui lòng chọn đầy đủ năm học, lớp, môn và học sinh rồi bấm <strong>Tải dữ liệu</strong>.</p>
              </div>
            </c:otherwise>
          </c:choose>
        </section>

        <aside class="card rule-card">
          <h3>Quy định số cột điểm thường xuyên</h3>
          <ul>
            <c:forEach var="rule" items="${createData.frequentRuleItems}">
              <li>
                <span>${rule.subjectName}</span>
                <strong>${rule.frequentColumns}</strong>
              </li>
            </c:forEach>
          </ul>
        </aside>
      </div>
    </section>
  </main>
</div>

<script>
  (function () {
    const form = document.querySelector('[data-score-create-form]');
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
