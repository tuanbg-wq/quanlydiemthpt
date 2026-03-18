<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>${pageTitle}</title>

  <link rel="stylesheet" href="<c:url value='/css/admin-layout.css'/>">
  <link rel="stylesheet" href="<c:url value='/css/class-list.css'/>">
</head>
<body>
<div class="layout">
  <jsp:include page="/WEB-INF/views/admin/_sidebar.jsp"/>

  <main class="main class-list-page">
    <header class="class-header">
      <div class="header-left">
        <h1>Qu&#7843;n l&#253; l&#7899;p h&#7885;c</h1>
        <div class="breadcrumbs">
          <a href="<c:url value='/admin/dashboard'/>">Trang ch&#7911;</a>
          <span>/</span>
          <span>L&#7899;p h&#7885;c</span>
        </div>
      </div>
      <div class="header-right">
        <div class="course-manage-box">
          <div class="course-manage-label">Kh&#243;a h&#7885;c</div>
          <div class="course-manage-controls">
            <input id="courseManageSearch"
                   class="course-manage-search"
                   type="text"
                   list="courseSuggestionList"
                   placeholder="T&#236;m m&#227;/t&#234;n kh&#243;a h&#7885;c..."
                   aria-label="T&#236;m kh&#243;a h&#7885;c &#273;&#7875; thao t&#225;c"/>
            <input id="courseManageSelectedId" type="hidden" value="${search.khoa}">
            <datalist id="courseSuggestionList">
              <c:forEach var="course" items="${courses}">
                <option value="${course.id}( ${course.name})"></option>
              </c:forEach>
            </datalist>
            <div class="action-menu course-action-menu">
              <button type="button"
                      class="action-toggle"
                      aria-label="M&#7903; menu thao t&#225;c kh&#243;a h&#7885;c"
                      onclick="toggleCourseActionMenu(this)">
                &#8942;
              </button>
              <div class="action-dropdown" id="courseActionDropdown" role="menu">
                <a class="action-item" href="<c:url value='/admin/class/course/create'/>">Th&#234;m kh&#243;a h&#7885;c</a>
                <a class="action-item" href="#" id="editCourseActionItem">Ch&#7881;nh s&#7917;a kh&#243;a h&#7885;c</a>
                <button type="button" class="action-item danger" id="deleteCourseActionItem">X&#243;a kh&#243;a h&#7885;c</button>
              </div>
            </div>
          </div>
        </div>
        <a class="btn primary" href="<c:url value='/admin/class/create'/>">
          + Th&#234;m l&#7899;p h&#7885;c m&#7899;i
        </a>
      </div>
    </header>

    <section class="content">
      <c:if test="${not empty flashMessage}">
        <div class="alert ${flashType == 'error' ? 'alert-error' : (flashType == 'info' ? 'alert-info' : 'alert-success')}">
          ${flashMessage}
        </div>
      </c:if>

      <section class="stats-grid">
        <article class="stats-card">
          <div class="stats-icon icon-blue" aria-hidden="true">
            <svg viewBox="0 0 24 24" fill="none">
              <path d="M5 21V7a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2v14" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              <path d="M9 10h6M9 14h6M9 18h6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </div>
          <div>
            <p>T&#7893;ng s&#7889; l&#7899;p</p>
            <h3><fmt:formatNumber value="${stats.totalClasses}" groupingUsed="true"/></h3>
          </div>
        </article>

        <article class="stats-card">
          <div class="stats-icon icon-green" aria-hidden="true">
            <svg viewBox="0 0 24 24" fill="none">
              <circle cx="8" cy="8" r="3" stroke="currentColor" stroke-width="2"/>
              <circle cx="16" cy="9" r="3" stroke="currentColor" stroke-width="2"/>
              <path d="M3 19c0-2.2 2.2-4 5-4s5 1.8 5 4" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              <path d="M13 19c.3-1.8 2.1-3.2 4.4-3.2 2.5 0 4.6 1.6 4.6 3.6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </div>
          <div>
            <p>T&#7893;ng s&#7889; h&#7885;c sinh</p>
            <h3><fmt:formatNumber value="${stats.totalStudents}" groupingUsed="true"/></h3>
          </div>
        </article>

        <article class="stats-card">
          <div class="stats-icon icon-violet" aria-hidden="true">
            <svg viewBox="0 0 24 24" fill="none">
              <circle cx="12" cy="8" r="4" stroke="currentColor" stroke-width="2"/>
              <path d="M5 20c0-3 3.2-5 7-5s7 2 7 5" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </div>
          <div>
            <p>S&#7889; gi&#225;o vi&#234;n ch&#7911; nhi&#7879;m</p>
            <h3><fmt:formatNumber value="${stats.totalHomeroomTeachers}" groupingUsed="true"/></h3>
          </div>
        </article>
      </section>

      <section class="card filter-card">
        <form class="filters" id="classFilterForm" method="get" action="<c:url value='/admin/class'/>" autocomplete="off">
          <div class="filter-item search-item">
            <label for="q">T&#236;m ki&#7871;m</label>
            <input id="q" type="text" name="q" value="${search.q}" placeholder="Nh&#7853;p t&#234;n l&#7899;p...">
          </div>

          <div class="filter-item">
            <label for="khoi">Kh&#7889;i</label>
            <select id="khoi" name="khoi">
              <option value="">T&#7845;t c&#7843; kh&#7889;i</option>
              <c:forEach var="grade" items="${grades}">
                <option value="${grade}" ${search.khoi == grade ? 'selected' : ''}>Kh&#7889;i ${grade}</option>
              </c:forEach>
            </select>
          </div>

          <div class="filter-item">
            <label for="khoaFilterSearch">Kh&#243;a h&#7885;c</label>
            <input id="khoaFilterSearch"
                   type="text"
                   list="courseSuggestionList"
                   placeholder="T&#236;m m&#227;/t&#234;n kh&#243;a h&#7885;c..."
                   aria-label="T&#236;m kh&#243;a h&#7885;c trong b&#7897; l&#7885;c">
            <input id="khoaFilterSelectedId" type="hidden" name="khoa" value="${search.khoa}">
          </div>

          <div class="filter-actions">
            <button class="btn filter-btn" type="submit">L&#7885;c d&#7919; li&#7879;u</button>
          </div>
        </form>
      </section>

      <section class="card table-card">
        <div class="table-wrap">
          <table class="table">
            <thead>
            <tr>
              <th>T&#234;n l&#7899;p</th>
              <th>Kh&#7889;i</th>
              <th>Kh&#243;a h&#7885;c</th>
              <th>GV ch&#7911; nhi&#7879;m</th>
              <th>S&#297; s&#7889;</th>
              <th>N&#259;m h&#7885;c</th>
              <th class="th-actions">Thao t&#225;c</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="item" items="${classes}">
              <tr>
                <td><span class="class-name">${item.tenLop}</span></td>
                <td><span class="grade-badge">Kh&#7889;i ${item.khoi}</span></td>
                <td>${item.khoaHoc}</td>
                <td>
                  <c:choose>
                    <c:when test="${item.gvcnTen != '-'}">
                      <div class="teacher-cell">
                        <c:choose>
                          <c:when test="${not empty item.gvcnAvatarUrl}">
                            <c:url var="gvcnAvatarUrl" value="${item.gvcnAvatarUrl}"/>
                            <img class="teacher-avatar-img"
                                 src="${gvcnAvatarUrl}"
                                 alt="avatar homeroom teacher ${item.gvcnTen}">
                          </c:when>
                          <c:otherwise>
                            <span class="teacher-avatar">${item.gvcnInitials}</span>
                          </c:otherwise>
                        </c:choose>
                        <div class="teacher-meta">
                          <span class="teacher-name">${item.gvcnTen}</span>
                          <span class="teacher-email">${item.gvcnEmail}</span>
                        </div>
                      </div>
                    </c:when>
                    <c:otherwise>
                      <span class="muted-value">Ch&#432;a ph&#226;n c&#244;ng</span>
                    </c:otherwise>
                  </c:choose>
                </td>
                <td><span class="size-badge">${item.siSo}</span></td>
                <td>${item.namHoc}</td>
                <td class="actions">
                  <div class="action-menu">
                    <button type="button"
                            class="action-toggle"
                            aria-label="M&#7903; menu thao t&#225;c"
                            onclick="toggleClassActionMenu(this)">
                      &#8942;
                    </button>
                    <div class="action-dropdown" role="menu">
                      <a class="action-item" href="<c:url value='/admin/class/${item.idLop}/info'/>">Xem chi ti&#7871;t</a>
                      <a class="action-item" href="<c:url value='/admin/class/${item.idLop}/edit'/>">Ch&#7881;nh s&#7917;a</a>
                      <form class="class-delete-form"
                            method="post"
                            action="<c:url value='/admin/class/${item.idLop}/delete'/>"
                            data-class-name="${item.tenLop}">
                        <button class="action-item danger" type="submit">X&#243;a</button>
                      </form>
                    </div>
                  </div>
                </td>
              </tr>
            </c:forEach>

            <c:if test="${empty classes}">
              <tr>
                <td class="empty-message" colspan="7">Kh&#244;ng c&#243; l&#7899;p h&#7885;c ph&#249; h&#7907;p v&#7899;i b&#7897; l&#7885;c.</td>
              </tr>
            </c:if>
            </tbody>
          </table>
        </div>

        <div class="table-footer">
          <div class="table-count">
            Hi&#7875;n th&#7883; ${pageData.fromRecord}-${pageData.toRecord} tr&#234;n t&#7893;ng s&#7889; ${pageData.totalItems} l&#7899;p h&#7885;c
          </div>

          <div class="pagination">
            <c:url var="prevUrl" value="/admin/class">
              <c:param name="page" value="${pageData.page - 1}"/>
              <c:if test="${not empty search.q}">
                <c:param name="q" value="${search.q}"/>
              </c:if>
              <c:if test="${not empty search.khoi}">
                <c:param name="khoi" value="${search.khoi}"/>
              </c:if>
              <c:if test="${not empty search.khoa}">
                <c:param name="khoa" value="${search.khoa}"/>
              </c:if>
            </c:url>

            <c:choose>
              <c:when test="${pageData.page > 1}">
                <a class="page-btn" href="${prevUrl}" aria-label="Trang tr&#432;&#7899;c">&lsaquo;</a>
              </c:when>
              <c:otherwise>
                <span class="page-btn disabled">&lsaquo;</span>
              </c:otherwise>
            </c:choose>

            <c:forEach var="p" begin="1" end="${pageData.totalPages}">
              <c:url var="pageUrl" value="/admin/class">
                <c:param name="page" value="${p}"/>
                <c:if test="${not empty search.q}">
                  <c:param name="q" value="${search.q}"/>
                </c:if>
                <c:if test="${not empty search.khoi}">
                  <c:param name="khoi" value="${search.khoi}"/>
                </c:if>
                <c:if test="${not empty search.khoa}">
                  <c:param name="khoa" value="${search.khoa}"/>
                </c:if>
              </c:url>
              <a class="page-btn ${pageData.page == p ? 'active' : ''}" href="${pageUrl}">${p}</a>
            </c:forEach>

            <c:url var="nextUrl" value="/admin/class">
              <c:param name="page" value="${pageData.page + 1}"/>
              <c:if test="${not empty search.q}">
                <c:param name="q" value="${search.q}"/>
              </c:if>
              <c:if test="${not empty search.khoi}">
                <c:param name="khoi" value="${search.khoi}"/>
              </c:if>
              <c:if test="${not empty search.khoa}">
                <c:param name="khoa" value="${search.khoa}"/>
              </c:if>
            </c:url>

            <c:choose>
              <c:when test="${pageData.page < pageData.totalPages}">
                <a class="page-btn" href="${nextUrl}" aria-label="Trang sau">&rsaquo;</a>
              </c:when>
              <c:otherwise>
                <span class="page-btn disabled">&rsaquo;</span>
              </c:otherwise>
            </c:choose>
          </div>
        </div>
      </section>
    </section>
  </main>
</div>
<div id="classDeleteModal" class="class-delete-modal" hidden>
  <div class="class-delete-backdrop" data-close-class-delete-modal></div>
  <div class="class-delete-dialog" role="dialog" aria-modal="true" aria-labelledby="classDeleteModalTitle">
    <h3 id="classDeleteModalTitle">X&#225;c nh&#7853;n x&#243;a l&#7899;p h&#7885;c</h3>
    <p id="classDeleteModalMessage">B&#7841;n c&#243; ch&#7855;c ch&#7855;n mu&#7889;n x&#243;a l&#7899;p h&#7885;c n&#224;y kh&#244;ng?</p>
    <div class="class-delete-actions">
      <button type="button" class="btn" id="cancelClassDeleteButton">H&#7911;y</button>
      <button type="button" class="btn btn-danger" id="confirmClassDeleteButton">X&#243;a l&#7899;p</button>
    </div>
  </div>
</div>
<div id="courseDeleteModal" class="class-delete-modal" hidden>
  <div class="class-delete-backdrop" data-close-course-delete-modal></div>
  <div class="class-delete-dialog" role="dialog" aria-modal="true" aria-labelledby="courseDeleteModalTitle">
    <h3 id="courseDeleteModalTitle">X&#225;c nh&#7853;n x&#243;a kh&#243;a h&#7885;c</h3>
    <p id="courseDeleteModalMessage">B&#7841;n c&#243; ch&#7855;c ch&#7855;n mu&#7889;n x&#243;a kh&#243;a h&#7885;c n&#224;y kh&#244;ng?</p>
    <div class="class-delete-actions">
      <button type="button" class="btn" id="cancelCourseDeleteButton">H&#7911;y</button>
      <button type="button" class="btn btn-danger" id="confirmCourseDeleteButton">X&#243;a kh&#243;a h&#7885;c</button>
    </div>
  </div>
</div>
<form id="courseDeleteForm" method="post" hidden></form>
<c:url var="courseEditBaseUrl" value="/admin/class/course/__COURSE_ID__/edit"/>
<c:url var="courseDeleteBaseUrl" value="/admin/class/course/__COURSE_ID__/delete"/>
<script>
  (function () {
    function closeAllClassMenus() {
      document.querySelectorAll('.action-dropdown').forEach(menu => {
        menu.classList.remove('show');
        menu.classList.remove('open-up');
      });
      document.querySelectorAll('.action-menu.is-open').forEach(menu => {
        menu.classList.remove('is-open');
      });
      document.querySelectorAll('.table tbody tr.menu-open').forEach(row => {
        row.classList.remove('menu-open');
      });
      document.querySelectorAll('.action-toggle[aria-expanded="true"]').forEach(button => {
        button.setAttribute('aria-expanded', 'false');
      });
    }

    function positionClassMenu(button, menu) {
      const spacing = 8;

      menu.classList.add('show');
      menu.classList.remove('open-up');
      menu.style.left = '0px';
      menu.style.top = '0px';

      const buttonRect = button.getBoundingClientRect();
      const menuRect = menu.getBoundingClientRect();

      let left = buttonRect.right - menuRect.width;
      if (left < spacing) {
        left = spacing;
      }
      if (left + menuRect.width > window.innerWidth - spacing) {
        left = window.innerWidth - menuRect.width - spacing;
      }

      let top = buttonRect.bottom + spacing;
      const canOpenUp = buttonRect.top - menuRect.height - spacing >= spacing;
      const willOverflowDown = top + menuRect.height > window.innerHeight - spacing;

      if (willOverflowDown && canOpenUp) {
        top = buttonRect.top - menuRect.height - spacing;
        menu.classList.add('open-up');
      } else if (willOverflowDown) {
        top = Math.max(spacing, window.innerHeight - menuRect.height - spacing);
      }

      menu.style.left = left + 'px';
      menu.style.top = top + 'px';
    }

    window.toggleClassActionMenu = function (button) {
      const currentMenu = button.nextElementSibling;
      const currentRow = button.closest('tr');
      const shouldShow = !currentMenu.classList.contains('show');

      closeAllClassMenus();

      if (!shouldShow) {
        return;
      }

      positionClassMenu(button, currentMenu);
      button.setAttribute('aria-expanded', 'true');

      if (currentRow) {
        currentRow.classList.add('menu-open');
      }
      const currentWrap = button.closest('.action-menu');
      if (currentWrap) {
        currentWrap.classList.add('is-open');
      }
    };

    window.toggleCourseActionMenu = function (button) {
      const currentMenu = button.nextElementSibling;
      const shouldShow = !currentMenu.classList.contains('show');

      updateCourseActionLinks();
      closeAllClassMenus();

      if (!shouldShow) {
        return;
      }

      positionClassMenu(button, currentMenu);
      button.setAttribute('aria-expanded', 'true');

      const currentWrap = button.closest('.action-menu');
      if (currentWrap) {
        currentWrap.classList.add('is-open');
      }
    };

    document.addEventListener('click', function (event) {
      if (!event.target.closest('.action-menu')) {
        closeAllClassMenus();
      }
    });

    window.addEventListener('resize', closeAllClassMenus);
    document.addEventListener('scroll', closeAllClassMenus, true);
    const classFilterForm = document.getElementById('classFilterForm');
    const courseManageSearch = document.getElementById('courseManageSearch');
    const courseManageSelectedId = document.getElementById('courseManageSelectedId');
    const khoaFilterSearch = document.getElementById('khoaFilterSearch');
    const khoaFilterSelectedId = document.getElementById('khoaFilterSelectedId');
    const courseSuggestionList = document.getElementById('courseSuggestionList');
    const editCourseActionItem = document.getElementById('editCourseActionItem');
    const deleteCourseActionItem = document.getElementById('deleteCourseActionItem');
    const courseEditBaseUrl = '${courseEditBaseUrl}';
    const courseDeleteBaseUrl = '${courseDeleteBaseUrl}';
    const courseDeleteForm = document.getElementById('courseDeleteForm');
    const courseDeleteModal = document.getElementById('courseDeleteModal');
    const courseDeleteModalMessage = document.getElementById('courseDeleteModalMessage');
    const cancelCourseDeleteButton = document.getElementById('cancelCourseDeleteButton');
    const confirmCourseDeleteButton = document.getElementById('confirmCourseDeleteButton');
    const deleteModal = document.getElementById('classDeleteModal');
    const deleteModalMessage = document.getElementById('classDeleteModalMessage');
    const cancelDeleteButton = document.getElementById('cancelClassDeleteButton');
    const confirmDeleteButton = document.getElementById('confirmClassDeleteButton');
    let pendingCourseDeleteId = null;
    let pendingDeleteForm = null;
    const courseDisplayById = {};
    const courseIdByDisplay = {};
    const courseIdByNormalizedId = {};

    function normalizeText(value) {
      return (value || '').trim().toLowerCase();
    }

    function parseCourseIdFromInput(rawValue) {
      const text = (rawValue || '').trim();
      if (!text) {
        return null;
      }

      const normalizedText = normalizeText(text);
      if (courseIdByDisplay[normalizedText]) {
        return courseIdByDisplay[normalizedText];
      }

      if (courseIdByNormalizedId[normalizedText]) {
        return courseIdByNormalizedId[normalizedText];
      }

      const idPrefix = text.split('(')[0].trim();
      if (!idPrefix) {
        return null;
      }
      const normalizedIdPrefix = normalizeText(idPrefix);
      return courseIdByNormalizedId[normalizedIdPrefix] || null;
    }

    function syncCourseSelectionFromInput(inputElement, hiddenElement, normalizeDisplay) {
      if (!inputElement || !hiddenElement) {
        return;
      }

      const parsedCourseId = parseCourseIdFromInput(inputElement.value);
      if (parsedCourseId) {
        hiddenElement.value = parsedCourseId;
        if (normalizeDisplay && courseDisplayById[parsedCourseId]) {
          inputElement.value = courseDisplayById[parsedCourseId];
        }
      } else {
        hiddenElement.value = '';
      }
    }

    function buildCourseLookup() {
      if (!courseSuggestionList) {
        return;
      }

      courseSuggestionList.querySelectorAll('option').forEach(option => {
        const display = (option.value || '').trim();
        if (!display) {
          return;
        }
        const id = display.split('(')[0].trim();
        if (!id) {
          return;
        }
        courseDisplayById[id] = display;
        courseIdByDisplay[normalizeText(display)] = id;
        courseIdByNormalizedId[normalizeText(id)] = id;
      });
    }

    function getSelectedCourse() {
      if (!courseManageSelectedId || !courseManageSelectedId.value) {
        return null;
      }

      const selectedId = courseManageSelectedId.value;
      return {
        id: selectedId,
        name: courseDisplayById[selectedId] || selectedId
      };
    }

    function updateCourseActionLinks() {
      const selectedCourse = getSelectedCourse();
      if (!editCourseActionItem || !deleteCourseActionItem) {
        return;
      }

      if (!selectedCourse) {
        editCourseActionItem.setAttribute('href', '#');
        editCourseActionItem.classList.add('disabled');
        deleteCourseActionItem.classList.add('disabled');
        return;
      }

      editCourseActionItem.setAttribute(
        'href',
        courseEditBaseUrl.replace('__COURSE_ID__', encodeURIComponent(selectedCourse.id))
      );
      editCourseActionItem.classList.remove('disabled');
      deleteCourseActionItem.classList.remove('disabled');
    }

    function openDeleteModal(className) {
      const safeName = className ? ' "' + className + '"' : '';
      deleteModalMessage.textContent =
        'Bạn có chắc chắn muốn xóa lớp học' + safeName + ' không?';
      deleteModal.hidden = false;
      document.body.classList.add('modal-open');
      confirmDeleteButton.focus();
      closeAllClassMenus();
    }

    function closeDeleteModal() {
      deleteModal.hidden = true;
      document.body.classList.remove('modal-open');
      pendingDeleteForm = null;
    }

    function openCourseDeleteModal(courseName) {
      const safeName = courseName ? ' "' + courseName + '"' : '';
      courseDeleteModalMessage.textContent =
        'Bạn có chắc chắn muốn xóa khóa học' + safeName + ' không?';
      courseDeleteModal.hidden = false;
      document.body.classList.add('modal-open');
      confirmCourseDeleteButton.focus();
      closeAllClassMenus();
    }

    function closeCourseDeleteModal() {
      courseDeleteModal.hidden = true;
      document.body.classList.remove('modal-open');
      pendingCourseDeleteId = null;
    }

    document.querySelectorAll('.class-delete-form').forEach(form => {
      form.addEventListener('submit', function (event) {
        if (form.dataset.confirmed === 'true') {
          form.dataset.confirmed = 'false';
          return;
        }
        event.preventDefault();
        pendingDeleteForm = form;
        openDeleteModal(form.dataset.className || '');
      });
    });

    buildCourseLookup();

    if (courseManageSearch) {
      courseManageSearch.addEventListener('input', function () {
        syncCourseSelectionFromInput(courseManageSearch, courseManageSelectedId, false);
        updateCourseActionLinks();
      });
      courseManageSearch.addEventListener('change', function () {
        syncCourseSelectionFromInput(courseManageSearch, courseManageSelectedId, true);
        updateCourseActionLinks();
      });
      courseManageSearch.addEventListener('blur', function () {
        syncCourseSelectionFromInput(courseManageSearch, courseManageSelectedId, true);
        updateCourseActionLinks();
      });
    }

    if (khoaFilterSearch) {
      khoaFilterSearch.addEventListener('input', function () {
        syncCourseSelectionFromInput(khoaFilterSearch, khoaFilterSelectedId, false);
      });
      khoaFilterSearch.addEventListener('change', function () {
        syncCourseSelectionFromInput(khoaFilterSearch, khoaFilterSelectedId, true);
      });
      khoaFilterSearch.addEventListener('blur', function () {
        syncCourseSelectionFromInput(khoaFilterSearch, khoaFilterSelectedId, true);
      });
    }

    if (classFilterForm) {
      classFilterForm.addEventListener('submit', function () {
        syncCourseSelectionFromInput(khoaFilterSearch, khoaFilterSelectedId, true);
      });
    }

    if (courseManageSearch && courseManageSelectedId && courseManageSelectedId.value) {
      const selectedId = courseManageSelectedId.value;
      if (courseDisplayById[selectedId]) {
        courseManageSearch.value = courseDisplayById[selectedId];
      }
    }

    if (khoaFilterSearch && khoaFilterSelectedId && khoaFilterSelectedId.value) {
      const selectedFilterCourseId = khoaFilterSelectedId.value;
      if (courseDisplayById[selectedFilterCourseId]) {
        khoaFilterSearch.value = courseDisplayById[selectedFilterCourseId];
      }
    }

    if (courseManageSearch) {
      syncCourseSelectionFromInput(courseManageSearch, courseManageSelectedId, true);
    }
    if (khoaFilterSearch) {
      syncCourseSelectionFromInput(khoaFilterSearch, khoaFilterSelectedId, true);
    }

    if (editCourseActionItem) {
      editCourseActionItem.addEventListener('click', function (event) {
        if (!getSelectedCourse()) {
          event.preventDefault();
          alert('Vui lòng chọn khóa học trước khi chỉnh sửa.');
        }
      });
    }

    if (deleteCourseActionItem) {
      deleteCourseActionItem.addEventListener('click', function (event) {
        event.preventDefault();
        const selectedCourse = getSelectedCourse();
        if (!selectedCourse) {
          alert('Vui lòng chọn khóa học trước khi xóa.');
          return;
        }
        pendingCourseDeleteId = selectedCourse.id;
        openCourseDeleteModal(selectedCourse.name);
      });
    }

    if (confirmDeleteButton) {
      confirmDeleteButton.addEventListener('click', function () {
        if (!pendingDeleteForm) {
          closeDeleteModal();
          return;
        }
        pendingDeleteForm.dataset.confirmed = 'true';
        pendingDeleteForm.submit();
      });
    }

    if (cancelDeleteButton) {
      cancelDeleteButton.addEventListener('click', closeDeleteModal);
    }

    if (deleteModal) {
      deleteModal.querySelectorAll('[data-close-class-delete-modal]').forEach(button => {
        button.addEventListener('click', closeDeleteModal);
      });
    }

    if (confirmCourseDeleteButton) {
      confirmCourseDeleteButton.addEventListener('click', function () {
        if (!pendingCourseDeleteId || !courseDeleteForm) {
          closeCourseDeleteModal();
          return;
        }
        courseDeleteForm.action = courseDeleteBaseUrl.replace(
          '__COURSE_ID__',
          encodeURIComponent(pendingCourseDeleteId)
        );
        courseDeleteForm.submit();
      });
    }

    if (cancelCourseDeleteButton) {
      cancelCourseDeleteButton.addEventListener('click', closeCourseDeleteModal);
    }

    if (courseDeleteModal) {
      courseDeleteModal.querySelectorAll('[data-close-course-delete-modal]').forEach(button => {
        button.addEventListener('click', closeCourseDeleteModal);
      });
    }

    updateCourseActionLinks();

    document.addEventListener('keydown', function (event) {
      if (event.key === 'Escape') {
        if (courseDeleteModal && !courseDeleteModal.hidden) {
          closeCourseDeleteModal();
          return;
        }
        if (deleteModal && !deleteModal.hidden) {
          closeDeleteModal();
          return;
        }
        closeAllClassMenus();
      }
    });
  })();
</script>
</body>
</html>
