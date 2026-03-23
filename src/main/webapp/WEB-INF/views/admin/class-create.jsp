<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>${pageTitle}</title>

  <link rel="stylesheet" href="<c:url value='/css/admin-layout.css'/>">
  <link rel="stylesheet" href="<c:url value='/css/class-create.css'/>">
</head>
<body>
<div class="layout">
  <jsp:include page="/WEB-INF/views/admin/_sidebar.jsp"/>

  <main class="main class-create-page">
    <header class="create-header">
      <div class="header-left">
        <h1>Thêm lớp học mới</h1>
        <p>Nhập thông tin lớp học. Hệ thống hỗ trợ gợi ý mã lớp theo Khóa + A1 (ví dụ: K06A1).</p>
      </div>
    </header>

    <section class="content">
      <c:if test="${not empty error}">
        <div class="alert alert-error">${error}</div>
      </c:if>

      <section class="card form-card">
        <form method="post"
              action="<c:url value='/admin/class/create'/>"
              class="class-create-form"
              autocomplete="off"
              data-class-create-form>
          <div class="form-grid">
            <div class="field">
              <label for="tenLop">Tên lớp học <span>*</span></label>
              <input id="tenLop"
                     type="text"
                     name="tenLop"
                     value="${classForm.tenLop}"
                     placeholder="VD: 10A1 hoặc A1"
                     data-class-name-input
                     required>
            </div>

            <div class="field">
              <label for="khoi">Khối <span>*</span></label>
              <select id="khoi" name="khoi" required>
                <option value="">Chọn khối</option>
                <c:forEach var="grade" items="${gradeOptions}">
                  <option value="${grade}" ${classForm.khoi == grade.toString() ? 'selected' : ''}>Khối ${grade}</option>
                </c:forEach>
              </select>
            </div>

            <div class="field">
              <label for="idKhoa">Khóa học <span>*</span></label>
              <select id="idKhoa" name="idKhoa" data-course-input required>
                <option value="">Chọn khóa học</option>
                <c:forEach var="course" items="${courseOptions}">
                  <option value="${course.id}" ${classForm.idKhoa == course.id ? 'selected' : ''}>${course.id}( ${course.name})</option>
                </c:forEach>
              </select>
            </div>

            <div class="field class-code-field">
              <label for="maLop">Mã lớp <span>*</span></label>
              <input id="maLop"
                     type="text"
                     name="maLop"
                     value="${classForm.maLop}"
                     placeholder="VD: K06A1"
                     data-class-code-input
                     required>
              <small class="field-hint">Gợi ý mã lớp theo khóa/lớp, bạn có thể chỉnh sửa trực tiếp.</small>
              <div class="class-code-suggestion-list" data-class-code-suggestions hidden></div>
            </div>

            <div class="field">
              <label for="namHoc">Năm học <span>*</span></label>
              <input id="namHoc"
                     type="text"
                     name="namHoc"
                     value="${classForm.namHoc}"
                     placeholder="2025-2026"
                     required>
            </div>
          </div>

          <div class="field teacher-field">
            <label for="gvcnDisplay">Phân công giáo viên chủ nhiệm <span>*</span></label>
            <input id="gvcnDisplay"
                   type="text"
                   name="gvcnDisplay"
                   value="${classForm.gvcnDisplay}"
                   placeholder="Tìm theo tên hoặc mã giáo viên..."
                   data-teacher-input
                   required>
            <input type="hidden" id="idGvcn" name="idGvcn" value="${classForm.idGvcn}" data-teacher-id>
            <div class="teacher-suggestion-list" data-teacher-suggestions hidden></div>
          </div>

          <div class="field">
            <label for="ghiChu">Ghi chú thêm</label>
            <textarea id="ghiChu"
                      name="ghiChu"
                      rows="4"
                      placeholder="Nhập ghi chú cho lớp học (nếu có)...">${classForm.ghiChu}</textarea>
          </div>

          <div class="form-actions">
            <a class="btn" href="<c:url value='/admin/class'/>">Quay lại</a>
            <button class="btn primary" type="submit">Lưu thông tin</button>
          </div>
        </form>
      </section>
    </section>
  </main>
</div>

<script>
  (function () {
    const form = document.querySelector('[data-class-create-form]');
    if (!form) {
      return;
    }

    const teacherInput = form.querySelector('[data-teacher-input]');
    const teacherIdInput = form.querySelector('[data-teacher-id]');
    const teacherSuggestionList = form.querySelector('[data-teacher-suggestions]');

    const classNameInput = form.querySelector('[data-class-name-input]');
    const classCodeInput = form.querySelector('[data-class-code-input]');
    const classCodeSuggestionList = form.querySelector('[data-class-code-suggestions]');
    const courseInput = form.querySelector('[data-course-input]');
    const gradeInput = form.querySelector('#khoi');

    let teacherDebounceTimer = null;
    let classCodeDebounceTimer = null;
    let classCodeRequestCounter = 0;
    let lastAutoClassCode = '';

    function normalizeCourseId(value) {
      return (value || '').trim().toUpperCase().replace(/[^A-Z0-9]/g, '');
    }

    function normalizeClassToken(value) {
      return (value || '').trim().toUpperCase().replace(/[^A-Z0-9]/g, '');
    }

    function classSuffixFromNameToken(token) {
      const normalized = normalizeClassToken(token);
      const withGrade = normalized.match(/^(10|11|12)([A-Z]\d{1,2})$/);
      if (withGrade) {
        return { grade: withGrade[1], suffix: withGrade[2] };
      }
      const withoutGrade = normalized.match(/^([A-Z]\d{1,2})$/);
      if (withoutGrade) {
        return { grade: null, suffix: withoutGrade[1] };
      }
      return null;
    }

    function classSuffixFromClassCode(classCode, courseId) {
      const normalizedCode = normalizeClassToken(classCode);
      const normalizedCourse = normalizeCourseId(courseId);
      if (!normalizedCode || !normalizedCourse || !normalizedCode.startsWith(normalizedCourse)) {
        return null;
      }
      const suffix = normalizedCode.substring(normalizedCourse.length);
      if (!/^[A-Z]\d{1,2}$/.test(suffix)) {
        return null;
      }
      return suffix;
    }

    function hideTeacherSuggestions() {
      teacherSuggestionList.hidden = true;
      teacherSuggestionList.innerHTML = '';
    }

    function hideClassCodeSuggestions() {
      classCodeSuggestionList.hidden = true;
      classCodeSuggestionList.innerHTML = '';
    }

    function renderTeacherSuggestions(items) {
      teacherSuggestionList.innerHTML = '';
      if (!items || items.length === 0) {
        hideTeacherSuggestions();
        return;
      }

      items.forEach(item => {
        const button = document.createElement('button');
        button.type = 'button';
        button.className = 'teacher-suggestion-item';
        button.textContent = item.label || item.display || item.value;
        button.addEventListener('click', function () {
          teacherInput.value = item.display || item.label || item.value;
          teacherIdInput.value = item.value || '';
          hideTeacherSuggestions();
        });
        teacherSuggestionList.appendChild(button);
      });

      teacherSuggestionList.hidden = false;
    }

    function applySelectedClassCode(code) {
      const selectedCode = normalizeClassToken(code);
      if (!selectedCode) {
        return;
      }

      classCodeInput.value = selectedCode;
      const suffix = classSuffixFromClassCode(selectedCode, courseInput.value);
      if (suffix && gradeInput && gradeInput.value) {
        classNameInput.value = gradeInput.value + suffix;
      }
      hideClassCodeSuggestions();
    }

    function renderClassCodeSuggestions(items) {
      classCodeSuggestionList.innerHTML = '';
      if (!items || items.length === 0) {
        hideClassCodeSuggestions();
        return;
      }

      items.forEach(item => {
        const button = document.createElement('button');
        button.type = 'button';
        button.className = 'class-code-suggestion-item';
        button.textContent = item.label || item.display || item.value;
        button.addEventListener('click', function () {
          applySelectedClassCode(item.value || '');
        });
        classCodeSuggestionList.appendChild(button);
      });

      classCodeSuggestionList.hidden = false;
    }

    function buildAutoClassCode() {
      const courseId = normalizeCourseId(courseInput.value);
      const classTokenInfo = classSuffixFromNameToken(classNameInput.value);
      if (!courseId || !classTokenInfo || !classTokenInfo.suffix) {
        return '';
      }

      if (classTokenInfo.grade && gradeInput && gradeInput.value && classTokenInfo.grade !== gradeInput.value) {
        return '';
      }

      return courseId + classTokenInfo.suffix;
    }

    function maybeApplyAutoClassCode() {
      const autoCode = buildAutoClassCode();
      if (!autoCode) {
        if (classCodeInput.value === lastAutoClassCode) {
          classCodeInput.value = '';
        }
        lastAutoClassCode = '';
        return;
      }

      const currentValue = normalizeClassToken(classCodeInput.value);
      if (!currentValue || currentValue === normalizeClassToken(lastAutoClassCode)) {
        classCodeInput.value = autoCode;
      }
      lastAutoClassCode = autoCode;
    }

    async function fetchTeacherSuggestions(keyword) {
      const q = (keyword || '').trim();
      if (!q) {
        hideTeacherSuggestions();
        return;
      }

      try {
        const response = await fetch('<c:url value="/admin/class/suggest/homeroom-teachers"/>' + '?q=' + encodeURIComponent(q), {
          headers: { 'Accept': 'application/json' }
        });
        if (!response.ok) {
          hideTeacherSuggestions();
          return;
        }
        const data = await response.json();
        renderTeacherSuggestions(Array.isArray(data) ? data : []);
      } catch (error) {
        hideTeacherSuggestions();
      }
    }

    async function refreshClassCodeSuggestions() {
      const autoCode = buildAutoClassCode();
      const query = (classCodeInput.value || autoCode || classNameInput.value || '').trim();
      const courseId = normalizeCourseId(courseInput.value);
      const grade = (gradeInput && gradeInput.value ? gradeInput.value : '').trim();

      if (!query && !autoCode) {
        hideClassCodeSuggestions();
        return;
      }

      const requestId = ++classCodeRequestCounter;
      const params = new URLSearchParams();
      if (query) {
        params.set('q', query);
      }
      if (courseId) {
        params.set('courseId', courseId);
      }
      if (grade) {
        params.set('grade', grade);
      }

      let suggestions = [];
      if (autoCode) {
        suggestions.push({
          value: autoCode,
          label: 'Đề xuất: ' + autoCode,
          display: autoCode
        });
      }

      try {
        const response = await fetch('<c:url value="/admin/class/suggest/class-codes"/>' + '?' + params.toString(), {
          headers: { 'Accept': 'application/json' }
        });
        if (response.ok) {
          const data = await response.json();
          if (requestId !== classCodeRequestCounter) {
            return;
          }
          if (Array.isArray(data)) {
            data.forEach(item => {
              const value = normalizeClassToken(item && item.value ? item.value : '');
              if (!value) {
                return;
              }
              const exists = suggestions.some(existing => normalizeClassToken(existing.value) === value);
              if (!exists) {
                suggestions.push(item);
              }
            });
          }
        }
      } catch (error) {
        // Keep local suggestion only
      }

      renderClassCodeSuggestions(suggestions);
    }

    function scheduleClassCodeSuggestions() {
      clearTimeout(classCodeDebounceTimer);
      classCodeDebounceTimer = setTimeout(function () {
        refreshClassCodeSuggestions();
      }, 180);
    }

    teacherInput.addEventListener('input', function () {
      teacherIdInput.value = '';
      clearTimeout(teacherDebounceTimer);
      teacherDebounceTimer = setTimeout(function () {
        fetchTeacherSuggestions(teacherInput.value);
      }, 180);
    });

    classCodeInput.addEventListener('input', function () {
      scheduleClassCodeSuggestions();
    });

    classCodeInput.addEventListener('focus', function () {
      scheduleClassCodeSuggestions();
    });

    if (classNameInput) {
      classNameInput.addEventListener('input', function () {
        maybeApplyAutoClassCode();
        scheduleClassCodeSuggestions();
      });
      classNameInput.addEventListener('change', function () {
        maybeApplyAutoClassCode();
        scheduleClassCodeSuggestions();
      });
      classNameInput.addEventListener('focus', scheduleClassCodeSuggestions);
    }

    if (courseInput) {
      courseInput.addEventListener('change', function () {
        maybeApplyAutoClassCode();
        scheduleClassCodeSuggestions();
      });
      courseInput.addEventListener('input', function () {
        maybeApplyAutoClassCode();
        scheduleClassCodeSuggestions();
      });
      courseInput.addEventListener('focus', scheduleClassCodeSuggestions);
    }

    if (gradeInput) {
      gradeInput.addEventListener('change', function () {
        maybeApplyAutoClassCode();
        scheduleClassCodeSuggestions();
      });
    }

    document.addEventListener('click', function (event) {
      if (!event.target.closest('.teacher-field')) {
        hideTeacherSuggestions();
      }
      if (!event.target.closest('.class-code-field')) {
        hideClassCodeSuggestions();
      }
    });

    maybeApplyAutoClassCode();
  })();
</script>
</body>
</html>
