<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>${pageTitle}</title>

  <link rel="stylesheet" href="<c:url value='/css/admin-layout.css'/>">
  <link rel="stylesheet" href="<c:url value='/css/admin/subject-create.css'/>">
</head>
<body>
<div class="layout">
  <jsp:include page="/WEB-INF/views/admin/_sidebar.jsp"/>

  <main class="main subject-create-page">
    <header class="topbar">
      <div class="topbar-left">
        <h1>Chá»‰nh Sá»­a MĂ´n Há»c</h1>
        <p>Cáº­p nháº­t thĂ´ng tin mĂ´n há»c</p>
      </div>
    </header>

    <section class="content">
      <c:if test="${not empty error}">
        <div class="alert alert-error">${error}</div>
      </c:if>

      <div class="card">
        <form id="subjectEditForm" method="post" action="<c:url value='/admin/subject/${subjectId}/edit'/>">
          <div class="form-grid">
            <div class="field">
              <label for="idMonHoc">MĂ£ mĂ´n há»c <span class="required">*</span></label>
              <input id="idMonHoc"
                     name="idMonHoc"
                     value="${subjectForm.idMonHoc}"
                     type="text"
                     maxlength="10"
                     placeholder="VĂ­ dá»¥: MH001"
                     required>
            </div>

            <div class="field">
              <label for="tenMonHoc">TĂªn mĂ´n há»c <span class="required">*</span></label>
              <input id="tenMonHoc"
                     name="tenMonHoc"
                     value="${subjectForm.tenMonHoc}"
                     type="text"
                     maxlength="100"
                     placeholder="VĂ­ dá»¥: ToĂ¡n há»c"
                     required>
            </div>

            <div class="field autocomplete"
                 data-url="<c:url value='/admin/subject/suggest/courses'/>"
                 data-hidden-target="courseId">
              <label for="courseKeyword">KhĂ³a há»c <span class="required">*</span></label>
              <input id="courseKeyword"
                     class="autocomplete-input"
                     type="text"
                     autocomplete="off"
                     value="${subjectForm.courseId}"
                     placeholder="GĂµ K06, K12..."
                     required>
              <input id="courseId" type="hidden" name="courseId" value="${subjectForm.courseId}">
              <div class="autocomplete-list" hidden></div>
            </div>

            <div class="field autocomplete" data-url="<c:url value='/admin/subject/suggest/school-years'/>">
              <label for="namHoc">NÄƒm há»c <span class="required">*</span></label>
              <input id="namHoc"
                     class="autocomplete-input"
                     name="namHoc"
                     type="text"
                     autocomplete="off"
                     value="${subjectForm.namHoc}"
                     placeholder="GĂµ 2025, 2026..."
                     required>
              <div class="autocomplete-list" hidden></div>
            </div>

            <div class="field">
              <label for="hocKy">Ká»³ há»c <span class="required">*</span></label>
              <select id="hocKy" name="hocKy" required>
                <option value="">Chá»n ká»³ há»c</option>
                <option value="HK1" ${subjectForm.hocKy == 'HK1' ? 'selected' : ''}>Há»c ká»³ 1</option>
                <option value="HK2" ${subjectForm.hocKy == 'HK2' ? 'selected' : ''}>Há»c ká»³ 2</option>
                <option value="CA_NAM" ${subjectForm.hocKy == 'CA_NAM' ? 'selected' : ''}>Cáº£ nÄƒm</option>
              </select>
            </div>

            <div class="field">
              <label for="soDiemThuongXuyen">Sá»‘ Ä‘iá»ƒm thÆ°á»ng xuyĂªn <span class="required">*</span></label>
              <select id="soDiemThuongXuyen" name="soDiemThuongXuyen" required>
                <option value="2" ${subjectForm.soDiemThuongXuyen == 2 ? 'selected' : ''}>2</option>
                <option value="3" ${subjectForm.soDiemThuongXuyen == 3 || empty subjectForm.soDiemThuongXuyen ? 'selected' : ''}>3</option>
                <option value="4" ${subjectForm.soDiemThuongXuyen == 4 ? 'selected' : ''}>4</option>
              </select>
            </div>

            <div class="field">
              <label for="khoiApDung">Khá»‘i lá»›p <span class="required">*</span></label>
              <input id="khoiApDung"
                     name="khoiApDung"
                     value="${subjectForm.khoiApDung}"
                     type="text"
                     placeholder="VĂ­ dá»¥: 10,11,12"
                     required>
            </div>

            <div class="field">
              <label for="toBoMon">Tá»• bá»™ mĂ´n <span class="required">*</span></label>
              <input id="toBoMon"
                     name="toBoMon"
                     value="${subjectForm.toBoMon}"
                     list="departmentList"
                     placeholder="VĂ­ dá»¥: Tá»± nhiĂªn"
                     required>
              <datalist id="departmentList">
                <c:forEach var="dept" items="${departments}">
                  <option value="${dept}"/>
                </c:forEach>
              </datalist>
            </div>

            <div class="field autocomplete"
                 data-url="<c:url value='/admin/subject/suggest/teachers'/>"
                 data-hidden-target="giaoVienPhuTrach">
              <label for="teacherKeyword">GiĂ¡o viĂªn phá»¥ trĂ¡ch</label>
              <input id="teacherKeyword"
                     class="autocomplete-input"
                     type="text"
                     autocomplete="off"
                     value="${subjectForm.giaoVienPhuTrach}"
                     placeholder="GĂµ tĂªn hoáº·c mĂ£ giĂ¡o viĂªn">
              <input id="giaoVienPhuTrach" type="hidden" name="giaoVienPhuTrach" value="${subjectForm.giaoVienPhuTrach}">
              <div class="autocomplete-list" hidden></div>
              <small class="field-note">Má»™t mĂ´n há»c cĂ³ thá»ƒ cĂ³ nhiá»u giĂ¡o viĂªn qua phĂ¢n cĂ´ng giáº£ng dáº¡y.</small>
            </div>

            <div class="field field-full">
              <label for="moTa">MĂ´ táº£</label>
              <textarea id="moTa"
                        name="moTa"
                        rows="4"
                        placeholder="MĂ´ táº£ ngáº¯n vá» mĂ´n há»c">${subjectForm.moTa}</textarea>
            </div>
          </div>

          <div class="form-bottom">
            <div class="required-note">* TrÆ°á»ng báº¯t buá»™c</div>
            <div class="actions">
              <a class="btn" href="<c:url value='/admin/subject'/>">Quay láº¡i danh sĂ¡ch</a>
              <button class="btn primary" type="submit">LÆ°u cáº­p nháº­t</button>
            </div>
          </div>
        </form>
      </div>
    </section>
  </main>
</div>

<script>
  (function () {
    function initAutocomplete(container) {
      const input = container.querySelector('.autocomplete-input');
      const list = container.querySelector('.autocomplete-list');
      const url = container.dataset.url;
      const hiddenTargetId = container.dataset.hiddenTarget;
      const hiddenInput = hiddenTargetId ? document.getElementById(hiddenTargetId) : null;

      let debounceTimer = null;
      let abortController = null;
      let currentItems = [];

      function closeList() {
        list.hidden = true;
        list.innerHTML = '';
        currentItems = [];
      }

      function selectItem(item) {
        input.value = item.label;
        if (hiddenInput) {
          hiddenInput.value = item.value;
        } else {
          input.value = item.value;
        }
        closeList();
      }

      function renderList(items) {
        if (!items || items.length === 0) {
          closeList();
          return;
        }

        currentItems = items;
        list.innerHTML = '';
        items.forEach((item, index) => {
          const button = document.createElement('button');
          button.type = 'button';
          button.className = 'autocomplete-item';
          button.textContent = item.label;
          button.dataset.index = String(index);
          list.appendChild(button);
        });
        list.hidden = false;
      }

      async function fetchSuggestions(keyword) {
        if (abortController) {
          abortController.abort();
        }
        abortController = new AbortController();

        const response = await fetch(url + '?q=' + encodeURIComponent(keyword), {
          signal: abortController.signal
        });

        if (!response.ok) {
          return [];
        }
        return await response.json();
      }

      function scheduleFetch() {
        const keyword = input.value.trim();
        clearTimeout(debounceTimer);
        debounceTimer = setTimeout(async () => {
          try {
            const items = await fetchSuggestions(keyword);
            renderList(items);
          } catch (error) {
            closeList();
          }
        }, 180);
      }

      input.addEventListener('input', function () {
        if (hiddenInput) {
          hiddenInput.value = '';
        }
        scheduleFetch();
      });

      input.addEventListener('focus', function () {
        scheduleFetch();
      });

      list.addEventListener('mousedown', function (event) {
        const itemButton = event.target.closest('.autocomplete-item');
        if (!itemButton) {
          return;
        }
        event.preventDefault();
        const index = Number(itemButton.dataset.index);
        const item = currentItems[index];
        if (item) {
          selectItem(item);
        }
      });

      document.addEventListener('click', function (event) {
        if (!container.contains(event.target)) {
          closeList();
        }
      });
    }

    document.querySelectorAll('.autocomplete').forEach(initAutocomplete);

    const form = document.getElementById('subjectEditForm');
    const courseKeywordInput = document.getElementById('courseKeyword');
    const courseIdInput = document.getElementById('courseId');

    form.addEventListener('submit', function (event) {
      if (!courseIdInput.value) {
        const raw = courseKeywordInput.value.trim();
        if (/^[A-Za-z0-9_-]+$/.test(raw)) {
          courseIdInput.value = raw;
        } else {
          event.preventDefault();
          alert('Vui lĂ²ng chá»n khĂ³a há»c tá»« danh sĂ¡ch gá»£i Ă½.');
          courseKeywordInput.focus();
        }
      }
    });
  })();
</script>
</body>
</html>

