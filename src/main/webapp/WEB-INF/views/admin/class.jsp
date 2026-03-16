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
        <h1>Quáº£n lĂ½ lá»›p há»c</h1>
        <div class="breadcrumbs">
          <a href="<c:url value='/admin/dashboard'/>">Trang chá»§</a>
          <span>/</span>
          <span>Lá»›p há»c</span>
        </div>
      </div>
      <div class="header-right">
        <a class="btn primary" href="<c:url value='/admin/class/create'/>">
          + ThĂªm lá»›p há»c má»›i
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
            <p>Tá»•ng sá»‘ lá»›p</p>
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
            <p>Tá»•ng sá»‘ há»c sinh</p>
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
            <p>Sá»‘ giĂ¡o viĂªn chá»§ nhiá»‡m</p>
            <h3><fmt:formatNumber value="${stats.totalHomeroomTeachers}" groupingUsed="true"/></h3>
          </div>
        </article>
      </section>

      <section class="card filter-card">
        <form class="filters" method="get" action="<c:url value='/admin/class'/>" autocomplete="off">
          <div class="filter-item search-item">
            <label for="q">TĂ¬m kiáº¿m</label>
            <input id="q" type="text" name="q" value="${search.q}" placeholder="Nháº­p tĂªn lá»›p...">
          </div>

          <div class="filter-item">
            <label for="khoi">Khá»‘i</label>
            <select id="khoi" name="khoi">
              <option value="">Táº¥t cáº£ khá»‘i</option>
              <c:forEach var="grade" items="${grades}">
                <option value="${grade}" ${search.khoi == grade ? 'selected' : ''}>Khá»‘i ${grade}</option>
              </c:forEach>
            </select>
          </div>

          <div class="filter-item">
            <label for="khoa">KhĂ³a há»c</label>
            <select id="khoa" name="khoa">
              <option value="">Táº¥t cáº£ khĂ³a</option>
              <c:forEach var="course" items="${courses}">
                <option value="${course.id}" ${search.khoa == course.id ? 'selected' : ''}>${course.name}</option>
              </c:forEach>
            </select>
          </div>

          <div class="filter-actions">
            <button class="btn filter-btn" type="submit">Lá»c dá»¯ liá»‡u</button>
          </div>
        </form>
      </section>

      <section class="card table-card">
        <div class="table-wrap">
          <table class="table">
            <thead>
            <tr>
              <th>TĂªn lá»›p</th>
              <th>Khá»‘i</th>
              <th>KhĂ³a há»c</th>
              <th>GV chá»§ nhiá»‡m</th>
              <th>SÄ© sá»‘</th>
              <th>NÄƒm há»c</th>
              <th class="th-actions">Thao tĂ¡c</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="item" items="${classes}">
              <tr>
                <td><span class="class-name">${item.tenLop}</span></td>
                <td><span class="grade-badge">Khá»‘i ${item.khoi}</span></td>
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
                      <span class="muted-value">ChÆ°a phĂ¢n cĂ´ng</span>
                    </c:otherwise>
                  </c:choose>
                </td>
                <td><span class="size-badge">${item.siSo}</span></td>
                <td>${item.namHoc}</td>
                <td class="actions">
                  <div class="action-menu">
                    <button type="button"
                            class="action-toggle"
                            aria-label="Má»Ÿ menu thao tĂ¡c"
                            onclick="toggleClassActionMenu(this)">
                      &#8942;
                    </button>
                    <div class="action-dropdown" role="menu">
                      <a class="action-item" href="<c:url value='/admin/class/${item.idLop}/info'/>">Xem chi tiáº¿t</a>
                      <a class="action-item" href="<c:url value='/admin/class/${item.idLop}/edit'/>">Chá»‰nh sá»­a</a>
                      <form class="class-delete-form"
                            method="post"
                            action="<c:url value='/admin/class/${item.idLop}/delete'/>"
                            data-class-name="${item.tenLop}">
                        <button class="action-item danger" type="submit">XĂ³a</button>
                      </form>
                    </div>
                  </div>
                </td>
              </tr>
            </c:forEach>

            <c:if test="${empty classes}">
              <tr>
                <td class="empty-message" colspan="7">KhĂ´ng cĂ³ lá»›p há»c phĂ¹ há»£p vá»›i bá»™ lá»c.</td>
              </tr>
            </c:if>
            </tbody>
          </table>
        </div>

        <div class="table-footer">
          <div class="table-count">
            Hiá»ƒn thá»‹ ${pageData.fromRecord}-${pageData.toRecord} trĂªn tá»•ng sá»‘ ${pageData.totalItems} lá»›p há»c
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
                <a class="page-btn" href="${prevUrl}" aria-label="Trang trÆ°á»›c">&lsaquo;</a>
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
    <h3 id="classDeleteModalTitle">XĂ¡c nháº­n xĂ³a lá»›p há»c</h3>
    <p id="classDeleteModalMessage">Báº¡n cĂ³ cháº¯c cháº¯n muá»‘n xĂ³a lá»›p há»c nĂ y khĂ´ng?</p>
    <div class="class-delete-actions">
      <button type="button" class="btn" id="cancelClassDeleteButton">Há»§y</button>
      <button type="button" class="btn btn-danger" id="confirmClassDeleteButton">XĂ³a lá»›p</button>
    </div>
  </div>
</div>
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

    document.addEventListener('click', function (event) {
      if (!event.target.closest('.action-menu')) {
        closeAllClassMenus();
      }
    });

    window.addEventListener('resize', closeAllClassMenus);
    document.addEventListener('scroll', closeAllClassMenus, true);
    const deleteModal = document.getElementById('classDeleteModal');
    const deleteModalMessage = document.getElementById('classDeleteModalMessage');
    const cancelDeleteButton = document.getElementById('cancelClassDeleteButton');
    const confirmDeleteButton = document.getElementById('confirmClassDeleteButton');
    let pendingDeleteForm = null;

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

    document.addEventListener('keydown', function (event) {
      if (event.key === 'Escape') {
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
