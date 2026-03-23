<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>${pageTitle}</title>

  <link rel="stylesheet" href="<c:url value='/css/admin-layout.css'/>">
  <link rel="stylesheet" href="<c:url value='/css/class-edit.css'/>">
</head>
<body>
<div class="layout">
  <jsp:include page="/WEB-INF/views/admin/_sidebar.jsp"/>

  <main class="main class-create-page">
    <header class="create-header">
      <div class="header-left">
        <h1>Chỉnh sửa lớp học</h1>
        <p>Cập nhật thông tin lớp học. Mã lớp cố định để đảm bảo định danh duy nhất.</p>
      </div>
    </header>

    <section class="content">
      <c:if test="${not empty error}">
        <div class="alert alert-error">${error}</div>
      </c:if>

      <section class="card form-card">
        <form method="post"
              action="<c:url value='/admin/class/${classId}/edit'/>"
              class="class-create-form"
              autocomplete="off"
              data-class-edit-form
              data-class-id="${classId}">
          <div class="form-grid">
            <div class="field">
              <label for="maLop">Mã lớp</label>
              <input id="maLop"
                     type="text"
                     name="maLop"
                     value="${classForm.maLop}"
                     readonly>
            </div>

            <div class="field">
              <label for="tenLop">Tên lớp học <span>*</span></label>
              <input id="tenLop"
                     type="text"
                     name="tenLop"
                     value="${classForm.tenLop}"
                     placeholder="VD: 10A1 hoặc A1"
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
              <select id="idKhoa" name="idKhoa" required>
                <option value="">Chọn khóa học</option>
                <c:forEach var="course" items="${courseOptions}">
                  <option value="${course.id}" ${classForm.idKhoa == course.id ? 'selected' : ''}>${course.id}( ${course.name})</option>
                </c:forEach>
              </select>
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
    const form = document.querySelector('[data-class-edit-form]');
    if (!form) {
      return;
    }

    const classId = (form.dataset.classId || '').trim();
    const teacherInput = form.querySelector('[data-teacher-input]');
    const teacherIdInput = form.querySelector('[data-teacher-id]');
    const suggestionList = form.querySelector('[data-teacher-suggestions]');
    let debounceTimer = null;

    function hideSuggestions() {
      suggestionList.hidden = true;
      suggestionList.innerHTML = '';
    }

    function renderSuggestions(items) {
      suggestionList.innerHTML = '';
      if (!items || items.length === 0) {
        hideSuggestions();
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
          hideSuggestions();
        });
        suggestionList.appendChild(button);
      });

      suggestionList.hidden = false;
    }

    async function fetchTeacherSuggestions(keyword) {
      const q = (keyword || '').trim();
      if (!q) {
        hideSuggestions();
        return;
      }

      const params = new URLSearchParams();
      params.set('q', q);
      if (classId) {
        params.set('classId', classId);
      }

      try {
        const response = await fetch('<c:url value="/admin/class/suggest/homeroom-teachers"/>' + '?' + params.toString(), {
          headers: { 'Accept': 'application/json' }
        });
        if (!response.ok) {
          hideSuggestions();
          return;
        }
        const data = await response.json();
        renderSuggestions(Array.isArray(data) ? data : []);
      } catch (error) {
        hideSuggestions();
      }
    }

    teacherInput.addEventListener('input', function () {
      teacherIdInput.value = '';
      clearTimeout(debounceTimer);
      debounceTimer = setTimeout(function () {
        fetchTeacherSuggestions(teacherInput.value);
      }, 180);
    });

    document.addEventListener('click', function (event) {
      if (!event.target.closest('.teacher-field')) {
        hideSuggestions();
      }
    });
  })();
</script>
</body>
</html>
