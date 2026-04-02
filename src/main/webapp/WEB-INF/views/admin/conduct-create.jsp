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
        <h1>ThĂªm khen thÆ°á»Ÿng</h1>
        <p>Chá»n há»c sinh theo khá»‘i, khĂ³a, lá»›p rá»“i nháº­p quyáº¿t Ä‘á»‹nh.</p>
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
            <label for="q">TĂ¬m kiáº¿m há»c sinh</label>
            <input id="q" type="text" name="q" value="${filter.q}" placeholder="TĂªn hoáº·c mĂ£ há»c sinh">
            <input id="filterStudentId" type="hidden" name="studentId"
                   value="${pageData.selectedStudent != null ? pageData.selectedStudent.idHocSinh : filter.studentId}">
            <div id="studentSuggestBox" class="student-suggest-box" hidden></div>
          </div>
          <div class="filter-item">
            <label for="khoi">Khá»‘i</label>
            <select id="khoi" name="khoi">
              <option value="">Táº¥t cáº£</option>
              <c:forEach var="grade" items="${pageData.grades}">
                <option value="${grade}" ${filter.khoi == grade ? 'selected' : ''}>Khá»‘i ${grade}</option>
              </c:forEach>
            </select>
          </div>
          <div class="filter-item">
            <label for="khoa">KhĂ³a</label>
            <select id="khoa" name="khoa">
              <option value="">Táº¥t cáº£</option>
              <c:forEach var="item" items="${pageData.courseOptions}">
                <option value="${item.id}" ${filter.khoa == item.id ? 'selected' : ''}>${item.name}</option>
              </c:forEach>
            </select>
          </div>
          <div class="filter-item">
            <label for="lop">Lá»›p</label>
            <select id="lop" name="lop">
              <option value="">Táº¥t cáº£</option>
              <c:forEach var="item" items="${pageData.classOptions}">
                <option value="${item.id}" ${filter.lop == item.id ? 'selected' : ''}>${item.name}</option>
              </c:forEach>
            </select>
          </div>
          <div class="filter-actions">
            <button class="btn filter-btn action-btn-search" type="submit">TĂ¬m há»c sinh</button>
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
              <h3>ThĂ´ng tin há»c sinh</h3>
              <div id="selectedStudentCard"
                   class="selected-student-card ${pageData.selectedStudent == null ? 'is-empty' : ''}"
                   data-student-id="${pageData.selectedStudent != null ? pageData.selectedStudent.idHocSinh : ''}"
                   data-student-name="${pageData.selectedStudent != null ? pageData.selectedStudent.hoTen : ''}"
                   data-class-id="${pageData.selectedStudent != null ? pageData.selectedStudent.classId : ''}"
                   data-class-name="${pageData.selectedStudent != null ? pageData.selectedStudent.tenLop : ''}"
                   data-grade="${pageData.selectedStudent != null ? pageData.selectedStudent.khoi : ''}"
                   data-course-id="${pageData.selectedStudent != null ? pageData.selectedStudent.courseId : ''}"
                   data-course-name="${pageData.selectedStudent != null ? pageData.selectedStudent.khoaHoc : ''}">
                <p class="selected-empty-text">ChÆ°a chá»n há»c sinh. HĂ£y nháº­p tá»« khĂ³a vĂ  chá»n tá»« danh sĂ¡ch gá»£i Ă½.</p>
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
                <label for="loaiChiTiet">Loáº¡i khen thÆ°á»Ÿng</label>
                <select id="loaiChiTiet" name="loaiChiTiet">
                  <option value="Há»c táº­p">Há»c táº­p</option>
                  <option value="Phong trĂ o">Phong trĂ o</option>
                  <option value="Äáº¡o Ä‘á»©c">Äáº¡o Ä‘á»©c</option>
                  <option value="KhĂ¡c">KhĂ¡c</option>
                </select>
              </div>
              <div class="form-row">
                <label for="ngayBanHanh">NgĂ y ban hĂ nh</label>
                <input id="ngayBanHanh" type="date" name="ngayBanHanh">
              </div>
              <div class="form-row">
                <label for="soQuyetDinh">Sá»‘ quyáº¿t Ä‘á»‹nh</label>
                <input id="soQuyetDinh" type="text" name="soQuyetDinh" placeholder="VĂ­ dá»¥: 123/QÄ-KT">
              </div>
              <div class="form-row">
                <label for="noiDung">Ná»™i dung khen thÆ°á»Ÿng</label>
                <textarea id="noiDung" name="noiDung" placeholder="Nháº­p chi tiáº¿t thĂ nh tĂ­ch, hĂ¬nh thá»©c khen thÆ°á»Ÿng..."></textarea>
              </div>
              <div class="form-row">
                <label for="ghiChu">Ghi chĂº bá»• sung</label>
                <textarea id="ghiChu" name="ghiChu" placeholder="ThĂ´ng tin ná»™i bá»™ khĂ¡c..."></textarea>
              </div>
              <div class="form-row">
                <label for="namHoc">NÄƒm há»c</label>
                <input id="namHoc" type="text" name="namHoc" placeholder="VĂ­ dá»¥: 2025-2026">
              </div>
            </div>
          </div>

          <div class="form-actions-bottom">
            <a class="btn btn-outline" href="<c:url value='/admin/conduct'/>">Quay láº¡i</a>
            <button class="btn btn-primary" type="submit">LÆ°u quyáº¿t Ä‘á»‹nh</button>
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
      if (selectedClass) selectedClass.textContent = student.tenLop ? ('Lá»›p ' + student.tenLop) : '-';
      const gradeText = student.khoi ? ('Khá»‘i ' + student.khoi) : 'Khá»‘i -';
      const courseText = student.khoaHoc || '-';
      if (selectedGradeCourse) selectedGradeCourse.textContent = gradeText + ' â€¢ ' + courseText;
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
        line1.textContent = (student.idHocSinh || '-') + ' â€¢ ' + (student.tenLop || '-');
        const line2 = document.createElement('span');
        const gradeText = student.khoi ? ('Khá»‘i ' + student.khoi) : 'Khá»‘i -';
        line2.textContent = gradeText + ' â€¢ ' + (student.khoaHoc || '-');

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

