<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>${pageTitle}</title>
  <link rel="stylesheet" href="<c:url value='/css/admin-layout.css'/>">
  <link rel="stylesheet" href="<c:url value='/css/admin/conduct-list.css'/>">
</head>
<body>
<div class="layout">
  <jsp:include page="/WEB-INF/views/admin/_sidebar.jsp"/>

  <main class="main conduct-create-page">
    <header class="conduct-header">
      <div class="header-left">
        <h1>Thêm khen thưởng</h1>
        <p>Chọn học sinh theo khối, khóa, lớp rồi nhập quyết định.</p>
      </div>
    </header>

    <section class="content">
      <c:if test="${not empty flashMessage}">
        <div class="alert ${flashType == 'error' ? 'alert-error' : (flashType == 'info' ? 'alert-info' : 'alert-success')}">
          ${flashMessage}
        </div>
      </c:if>

      <section class="form-card">
        <form id="rewardFilterForm" class="filters filters-create" method="get" action="<c:url value='/admin/conduct/reward/create'/>" autocomplete="off">
          <div class="filter-item search-item">
            <label for="q">Tìm kiếm học sinh</label>
            <input id="q" type="text" name="q" value="${filter.q}" placeholder="Tên hoặc mã học sinh">
            <input id="filterStudentId" type="hidden" name="studentId"
                   value="${pageData.selectedStudent != null ? pageData.selectedStudent.idHocSinh : filter.studentId}">
            <div id="studentSuggestBox" class="student-suggest-box" hidden></div>
          </div>
          <div class="filter-item">
            <label for="khoi">Khối</label>
            <select id="khoi" name="khoi">
              <option value="">Tất cả</option>
              <c:forEach var="grade" items="${pageData.grades}">
                <option value="${grade}" ${filter.khoi == grade ? 'selected' : ''}>Khối ${grade}</option>
              </c:forEach>
            </select>
          </div>
          <div class="filter-item">
            <label for="khoa">Khóa</label>
            <select id="khoa" name="khoa">
              <option value="">Tất cả</option>
              <c:forEach var="item" items="${pageData.courseOptions}">
                <option value="${item.id}" ${filter.khoa == item.id ? 'selected' : ''}>${item.name}</option>
              </c:forEach>
            </select>
          </div>
          <div class="filter-item">
            <label for="lop">Lớp</label>
            <select id="lop" name="lop">
              <option value="">Tất cả</option>
              <c:forEach var="item" items="${pageData.classOptions}">
                <option value="${item.id}" ${filter.lop == item.id ? 'selected' : ''}>${item.name}</option>
              </c:forEach>
            </select>
          </div>
          <div class="filter-actions">
            <button class="btn filter-btn action-btn-search" type="submit">Tìm học sinh</button>
          </div>
        </form>
      </section>

      <section class="form-card">
        <form id="rewardCreateForm" method="post" action="<c:url value='/admin/conduct/reward/create'/>">
          <input id="postQ" type="hidden" name="q" value="${filter.q}">
          <input id="postKhoi" type="hidden" name="khoi" value="${filter.khoi}">
          <input id="postKhoa" type="hidden" name="khoa" value="${filter.khoa}">
          <input id="postLop" type="hidden" name="lop" value="${filter.lop}">
          <input id="formStudentId" type="hidden" name="studentId"
                 value="${pageData.selectedStudent != null ? pageData.selectedStudent.idHocSinh : filter.studentId}">

          <div class="create-grid">
            <div>
              <h3>Thông tin học sinh</h3>
              <div id="selectedStudentCard"
                   class="selected-student-card ${pageData.selectedStudent == null ? 'is-empty' : ''}"
                   data-student-id="${pageData.selectedStudent != null ? pageData.selectedStudent.idHocSinh : ''}"
                   data-student-name="${pageData.selectedStudent != null ? pageData.selectedStudent.hoTen : ''}"
                   data-class-id="${pageData.selectedStudent != null ? pageData.selectedStudent.classId : ''}"
                   data-class-name="${pageData.selectedStudent != null ? pageData.selectedStudent.tenLop : ''}"
                   data-grade="${pageData.selectedStudent != null ? pageData.selectedStudent.khoi : ''}"
                   data-course-id="${pageData.selectedStudent != null ? pageData.selectedStudent.courseId : ''}"
                   data-course-name="${pageData.selectedStudent != null ? pageData.selectedStudent.khoaHoc : ''}">
                <p class="selected-empty-text">Chưa chọn học sinh. Hãy nhập từ khóa và chọn từ danh sách gợi ý.</p>
                <div class="selected-student-content">
                  <strong id="selectedStudentName"></strong>
                  <span id="selectedStudentCode"></span>
                  <span id="selectedStudentClass"></span>
                  <span id="selectedStudentGradeCourse"></span>
                </div>
              </div>
            </div>

            <div>
              <div class="form-row">
                <label for="loaiChiTiet">Loại khen thưởng</label>
                <select id="loaiChiTiet" name="loaiChiTiet">
                  <option value="Học tập">Học tập</option>
                  <option value="Phong trào">Phong trào</option>
                  <option value="Đạo đức">Đạo đức</option>
                  <option value="Khác">Khác</option>
                </select>
              </div>
              <div class="form-row">
                <label for="ngayBanHanh">Ngày ban hành</label>
                <input id="ngayBanHanh" type="date" name="ngayBanHanh" autocomplete="off">
              </div>
              <div class="form-row">
                <label for="soQuyetDinh">Số quyết định</label>
                <input id="soQuyetDinh" type="text" name="soQuyetDinh" placeholder="Ví dụ: 123/QĐ-KT">
              </div>
              <div class="form-row">
                <label for="noiDung">Nội dung khen thưởng</label>
                <textarea id="noiDung" name="noiDung" placeholder="Nhập chi tiết thành tích, hình thức khen thưởng..."></textarea>
              </div>
              <div class="form-row">
                <label for="ghiChu">Ghi chú bổ sung</label>
                <textarea id="ghiChu" name="ghiChu" placeholder="Thông tin nội bộ khác..."></textarea>
              </div>
              <div class="form-row">
                <label for="namHoc">Năm học</label>
                <input id="namHoc" type="text" name="namHoc" placeholder="Ví dụ: 2025-2026">
              </div>
            </div>
          </div>

          <div class="form-actions-bottom">
            <a class="btn btn-outline" href="<c:url value='/admin/conduct'/>">Quay lại</a>
            <button class="btn btn-primary" type="submit">Lưu quyết định</button>
          </div>
        </form>
      </section>
    </section>
  </main>
</div>

<script>
  (function () {
    const filterForm = document.getElementById('rewardFilterForm');
    const qInput = document.getElementById('q');
    const khoiSelect = document.getElementById('khoi');
    const khoaSelect = document.getElementById('khoa');
    const lopSelect = document.getElementById('lop');
    const filterStudentId = document.getElementById('filterStudentId');
    const formStudentId = document.getElementById('formStudentId');
    const suggestBox = document.getElementById('studentSuggestBox');
    const selectedCard = document.getElementById('selectedStudentCard');
    const selectedName = document.getElementById('selectedStudentName');
    const selectedCode = document.getElementById('selectedStudentCode');
    const selectedClass = document.getElementById('selectedStudentClass');
    const selectedGradeCourse = document.getElementById('selectedStudentGradeCourse');
    const postQ = document.getElementById('postQ');
    const postKhoi = document.getElementById('postKhoi');
    const postKhoa = document.getElementById('postKhoa');
    const postLop = document.getElementById('postLop');
    const rewardCreateForm = document.getElementById('rewardCreateForm');
    const ngayBanHanhInput = document.getElementById('ngayBanHanh');

    if (!filterForm || !qInput || !khoiSelect || !khoaSelect || !lopSelect || !suggestBox || !selectedCard) {
      return;
    }

    let suggestItems = [];
    let suggestIndex = -1;
    let debounceTimer = null;
    let applyingSelection = false;

    function hasOption(selectElement, value) {
      if (!selectElement || !value) {
        return false;
      }
      return Array.from(selectElement.options).some(option => option.value === value);
    }

    function syncPostFilterFields() {
      postQ.value = qInput.value || '';
      postKhoi.value = khoiSelect.value || '';
      postKhoa.value = khoaSelect.value || '';
      postLop.value = lopSelect.value || '';
    }

    function pad2(value) {
      return String(value).padStart(2, '0');
    }

    function toIsoDate(rawValue) {
      const value = (rawValue || '').trim();
      if (!value) {
        return '';
      }

      const isoMatch = value.match(/^(\d{4})-(\d{2})-(\d{2})$/);
      const vnMatch = value.match(/^(\d{1,2})\/(\d{1,2})\/(\d{4})$/);

      let year;
      let month;
      let day;

      if (isoMatch) {
        year = Number(isoMatch[1]);
        month = Number(isoMatch[2]);
        day = Number(isoMatch[3]);
      } else if (vnMatch) {
        day = Number(vnMatch[1]);
        month = Number(vnMatch[2]);
        year = Number(vnMatch[3]);
      } else {
        return null;
      }

      const candidate = new Date(year, month - 1, day);
      const valid = candidate.getFullYear() === year
        && candidate.getMonth() === month - 1
        && candidate.getDate() === day;
      if (!valid) {
        return null;
      }

      return year + '-' + pad2(month) + '-' + pad2(day);
    }

    function hideSuggestBox() {
      suggestBox.hidden = true;
      suggestBox.innerHTML = '';
      suggestItems = [];
      suggestIndex = -1;
    }

    function renderStudentCard(student) {
      if (!student || !student.idHocSinh) {
        selectedCard.classList.add('is-empty');
        if (selectedName) selectedName.textContent = '';
        if (selectedCode) selectedCode.textContent = '';
        if (selectedClass) selectedClass.textContent = '';
        if (selectedGradeCourse) selectedGradeCourse.textContent = '';
        return;
      }

      selectedCard.classList.remove('is-empty');
      if (selectedName) selectedName.textContent = student.hoTen || '-';
      if (selectedCode) selectedCode.textContent = student.idHocSinh || '-';
      if (selectedClass) selectedClass.textContent = student.tenLop ? ('Lớp ' + student.tenLop) : '-';
      const gradeText = student.khoi ? ('Khối ' + student.khoi) : 'Khối -';
      const courseText = student.khoaHoc || '-';
      if (selectedGradeCourse) selectedGradeCourse.textContent = gradeText + ' • ' + courseText;
    }

    function clearSelectedStudent() {
      filterStudentId.value = '';
      if (formStudentId) {
        formStudentId.value = '';
      }
      renderStudentCard(null);
    }

    function applyStudentSelection(student, shouldUpdateQuery) {
      if (!student || !student.idHocSinh) {
        clearSelectedStudent();
        syncPostFilterFields();
        return;
      }

      applyingSelection = true;
      if (shouldUpdateQuery) {
        qInput.value = (student.hoTen || '') + ' (' + student.idHocSinh + ')';
      }
      if (student.khoi && hasOption(khoiSelect, String(student.khoi))) {
        khoiSelect.value = String(student.khoi);
      }
      if (student.courseId && hasOption(khoaSelect, student.courseId)) {
        khoaSelect.value = student.courseId;
      }
      if (student.classId && hasOption(lopSelect, student.classId)) {
        lopSelect.value = student.classId;
      }
      applyingSelection = false;

      filterStudentId.value = student.idHocSinh;
      if (formStudentId) {
        formStudentId.value = student.idHocSinh;
      }
      renderStudentCard(student);
      syncPostFilterFields();
      hideSuggestBox();
    }

    function renderSuggestItems(items) {
      suggestBox.innerHTML = '';
      suggestItems = items || [];
      suggestIndex = -1;

      if (!suggestItems.length) {
        hideSuggestBox();
        return;
      }

      suggestItems.forEach((student, index) => {
        const button = document.createElement('button');
        button.type = 'button';
        button.className = 'student-suggest-item';
        button.dataset.index = String(index);

        const strong = document.createElement('strong');
        strong.textContent = student.hoTen || '-';
        const line1 = document.createElement('span');
        line1.textContent = (student.idHocSinh || '-') + ' • ' + (student.tenLop || '-');
        const line2 = document.createElement('span');
        const gradeText = student.khoi ? ('Khối ' + student.khoi) : 'Khối -';
        line2.textContent = gradeText + ' • ' + (student.khoaHoc || '-');

        button.appendChild(strong);
        button.appendChild(line1);
        button.appendChild(line2);
        button.addEventListener('click', function () {
          applyStudentSelection(student, true);
        });
        suggestBox.appendChild(button);
      });

      suggestBox.hidden = false;
    }

    function highlightSuggestItem(index) {
      const items = suggestBox.querySelectorAll('.student-suggest-item');
      items.forEach((item, itemIndex) => {
        if (itemIndex === index) {
          item.classList.add('is-active');
          item.scrollIntoView({ block: 'nearest' });
        } else {
          item.classList.remove('is-active');
        }
      });
    }

    function fetchSuggestStudents() {
      const keyword = (qInput.value || '').trim();
      if (keyword.length < 1) {
        hideSuggestBox();
        return;
      }

      const params = new URLSearchParams();
      params.set('q', keyword);
      if (khoiSelect.value) params.set('khoi', khoiSelect.value);
      if (khoaSelect.value) params.set('khoa', khoaSelect.value);
      if (lopSelect.value) params.set('lop', lopSelect.value);

      fetch('<c:url value="/admin/conduct/reward/suggest-students"/>' + '?' + params.toString(), {
        headers: {
          'Accept': 'application/json'
        }
      })
        .then(response => response.ok ? response.json() : [])
        .then(data => {
          if (!Array.isArray(data)) {
            hideSuggestBox();
            return;
          }
          renderSuggestItems(data);
        })
        .catch(() => hideSuggestBox());
    }

    qInput.addEventListener('input', function () {
      clearSelectedStudent();
      syncPostFilterFields();
      if (debounceTimer) {
        clearTimeout(debounceTimer);
      }
      debounceTimer = setTimeout(fetchSuggestStudents, 180);
    });

    qInput.addEventListener('keydown', function (event) {
      if (suggestBox.hidden || !suggestItems.length) {
        return;
      }
      if (event.key === 'ArrowDown') {
        event.preventDefault();
        suggestIndex = (suggestIndex + 1) % suggestItems.length;
        highlightSuggestItem(suggestIndex);
        return;
      }
      if (event.key === 'ArrowUp') {
        event.preventDefault();
        suggestIndex = (suggestIndex - 1 + suggestItems.length) % suggestItems.length;
        highlightSuggestItem(suggestIndex);
        return;
      }
      if (event.key === 'Enter' && suggestIndex >= 0 && suggestIndex < suggestItems.length) {
        event.preventDefault();
        applyStudentSelection(suggestItems[suggestIndex], true);
      }
    });

    [khoiSelect, khoaSelect, lopSelect].forEach(function (selectElement) {
      selectElement.addEventListener('change', function () {
        if (!applyingSelection) {
          clearSelectedStudent();
        }
        syncPostFilterFields();
      });
    });

    document.addEventListener('click', function (event) {
      if (!event.target.closest('.search-item')) {
        hideSuggestBox();
      }
    });

    filterForm.addEventListener('submit', function () {
      syncPostFilterFields();
    });

    if (rewardCreateForm && ngayBanHanhInput) {
      rewardCreateForm.addEventListener('submit', function (event) {
        const normalized = toIsoDate(ngayBanHanhInput.value);
        if (normalized === null) {
          event.preventDefault();
          alert('Ngày ban hành không hợp lệ.');
          ngayBanHanhInput.focus();
          return;
        }
        ngayBanHanhInput.value = normalized;
      });
    }

    const initialStudent = {
      idHocSinh: selectedCard.dataset.studentId || '',
      hoTen: selectedCard.dataset.studentName || '',
      classId: selectedCard.dataset.classId || '',
      tenLop: selectedCard.dataset.className || '',
      khoi: selectedCard.dataset.grade || '',
      courseId: selectedCard.dataset.courseId || '',
      khoaHoc: selectedCard.dataset.courseName || ''
    };

    if (initialStudent.idHocSinh) {
      applyStudentSelection(initialStudent, false);
    } else {
      renderStudentCard(null);
      syncPostFilterFields();
    }
  })();
</script>
</body>
</html>
