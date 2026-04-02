<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>${pageTitle}</title>

  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
  <link rel="stylesheet" href="<c:url value='/css/admin-layout.css'/>">
  <link rel="stylesheet" href="<c:url value='/css/admin/account.css'/>">
</head>
<body>
<div class="layout">
  <jsp:include page="/WEB-INF/views/admin/_sidebar.jsp"/>

  <main class="main account-list-page">
    <section class="account-header">
      <div>
        <h1>Quáº£n lĂ½ tĂ i khoáº£n</h1>
        <p>ThĂªm, sá»­a, khĂ³a hoáº·c xĂ³a tĂ i khoáº£n ngay táº¡i trang quáº£n lĂ½.</p>
      </div>
      <a class="btn primary add-account-btn" href="<c:url value='/admin/account/create'/>">+ ThĂªm tĂ i khoáº£n</a>
    </section>

    <section class="content">
      <c:if test="${not empty flashMessage}">
        <div class="alert ${flashType == 'error' ? 'alert-error' : 'alert-success'}">${flashMessage}</div>
      </c:if>

      <section class="account-stats-grid">
        <article class="card stat-card">
          <p class="stat-label">TĂ i khoáº£n há»‡ thá»‘ng</p>
          <p class="stat-value">${stats.totalAccounts}</p>
        </article>
        <article class="card stat-card">
          <p class="stat-label">Quáº£n trá»‹ viĂªn</p>
          <p class="stat-value">${stats.adminCount}</p>
        </article>
        <article class="card stat-card">
          <p class="stat-label">GiĂ¡o viĂªn chá»§ nhiá»‡m</p>
          <p class="stat-value">${stats.homeroomTeacherCount}</p>
        </article>
        <article class="card stat-card">
          <p class="stat-label">GiĂ¡o viĂªn bá»™ mĂ´n</p>
          <p class="stat-value">${stats.subjectTeacherCount}</p>
        </article>
      </section>

      <section class="card account-filter-card">
        <form method="get" action="<c:url value='/admin/account'/>" class="row g-3 align-items-end">
          <div class="col-12 col-lg-4">
            <label class="form-label" for="q">TĂ¬m kiáº¿m</label>
            <input id="q" name="q" type="text" class="form-control search-input"
                   value="${search.q}"
                   placeholder="TĂªn Ä‘Äƒng nháº­p, há» tĂªn hoáº·c email...">
          </div>

          <div class="col-12 col-md-4 col-lg-2">
            <label class="form-label" for="vaiTro">Vai trĂ²</label>
            <select id="vaiTro" name="vaiTro" class="form-select">
              <option value="">Táº¥t cáº£</option>
              <c:forEach var="role" items="${roleFilters}">
                <option value="${role.value}" ${search.vaiTro == role.value ? 'selected' : ''}>${role.label}</option>
              </c:forEach>
            </select>
          </div>

          <div class="col-12 col-md-4 col-lg-2">
            <label class="form-label" for="trangThai">Tráº¡ng thĂ¡i</label>
            <select id="trangThai" name="trangThai" class="form-select">
              <option value="">Táº¥t cáº£</option>
              <c:forEach var="status" items="${statusFilters}">
                <option value="${status}" ${search.trangThai == status ? 'selected' : ''}>
                  <c:choose>
                    <c:when test="${status == 'hoat_dong'}">Hoáº¡t Ä‘á»™ng</c:when>
                    <c:otherwise>ÄĂ£ khĂ³a</c:otherwise>
                  </c:choose>
                </option>
              </c:forEach>
            </select>
          </div>

          <div class="col-12 col-md-4 col-lg-2">
            <label class="form-label" for="khoi">Khá»‘i lá»›p</label>
            <select id="khoi" name="khoi" class="form-select">
              <option value="">Táº¥t cáº£</option>
              <c:forEach var="grade" items="${gradeFilters}">
                <option value="${grade}" ${search.khoi == grade ? 'selected' : ''}>Khá»‘i ${grade}</option>
              </c:forEach>
            </select>
          </div>

          <div class="col-12 col-lg-2 filter-actions">
            <button type="submit" class="btn filter-btn">Lá»c</button>
            <a class="btn" href="<c:url value='/admin/account'/>">Äáº·t láº¡i</a>
          </div>
        </form>
      </section>

      <section class="card account-table-card">
        <div class="table-wrap">
          <table class="table align-middle mb-0 account-table">
            <thead>
            <tr>
              <th>TĂªn Ä‘Äƒng nháº­p</th>
              <th>Há» vĂ  tĂªn</th>
              <th>Vai trĂ²</th>
              <th>Tráº¡ng thĂ¡i</th>
              <th>Khá»‘i lá»›p</th>
              <th>Email</th>
              <th class="text-center">Thao tĂ¡c</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="account" items="${accounts}">
              <tr>
                <td>${account.tenDangNhap}</td>
                <td>${account.hoTen}</td>
                <td><span class="role-badge">${account.vaiTro}</span></td>
                <td>
                  <span class="status-badge ${account.trangThai == 'khoa' ? 'locked' : 'active'}">
                    ${account.trangThai == 'khoa' ? 'ÄĂ£ khĂ³a' : 'Hoáº¡t Ä‘á»™ng'}
                  </span>
                </td>
                <td>${account.khoiLop}</td>
                <td>
                  <c:choose>
                    <c:when test="${not empty account.email}">${account.email}</c:when>
                    <c:otherwise>-</c:otherwise>
                  </c:choose>
                </td>
                <td class="action-cell">
                  <div class="action-wrap">
                    <button type="button" class="action-btn" onclick="toggleAccountActionMenu(this)" aria-label="Má»Ÿ menu thao tĂ¡c">
                      <span class="dots">â‹®</span>
                    </button>
                    <div class="action-dropdown">
                      <a class="action-item" href="<c:url value='/admin/account/${account.idTaiKhoan}/info'/>">ThĂ´ng tin tĂ i khoáº£n</a>
                      <a class="action-item" href="<c:url value='/admin/account/${account.idTaiKhoan}/edit'/>">Sá»­a</a>

                      <form method="post" action="<c:url value='/admin/account/${account.idTaiKhoan}/toggle-lock'/>">
                        <button type="submit" class="action-item">
                          ${account.trangThai == 'khoa' ? 'Má»Ÿ khĂ³a' : 'KhĂ³a tĂ i khoáº£n'}
                        </button>
                      </form>

                      <form method="post"
                            action="<c:url value='/admin/account/${account.idTaiKhoan}/delete'/>"
                            class="delete-account-form"
                            data-username="${account.tenDangNhap}">
                        <button type="submit" class="action-item delete">XĂ³a tĂ i khoáº£n</button>
                      </form>
                    </div>
                  </div>
                </td>
              </tr>
            </c:forEach>

            <c:if test="${empty accounts}">
              <tr>
                <td colspan="7" class="empty-row">KhĂ´ng cĂ³ tĂ i khoáº£n phĂ¹ há»£p bá»™ lá»c.</td>
              </tr>
            </c:if>
            </tbody>
          </table>
        </div>

        <section class="pagination-section">
          <div class="pagination-summary">
            Hiá»ƒn thá»‹ ${pageData.fromRecord}-${pageData.toRecord} trĂªn tá»•ng ${pageData.totalItems} tĂ i khoáº£n
          </div>
          <nav aria-label="PhĂ¢n trang tĂ i khoáº£n">
            <ul class="pagination pagination-sm mb-0">
              <c:url var="prevUrl" value="/admin/account">
                <c:param name="page" value="${pageData.page - 1}"/>
                <c:if test="${not empty search.q}"><c:param name="q" value="${search.q}"/></c:if>
                <c:if test="${not empty search.vaiTro}"><c:param name="vaiTro" value="${search.vaiTro}"/></c:if>
                <c:if test="${not empty search.trangThai}"><c:param name="trangThai" value="${search.trangThai}"/></c:if>
                <c:if test="${not empty search.khoi}"><c:param name="khoi" value="${search.khoi}"/></c:if>
              </c:url>
              <li class="page-item ${pageData.page == 1 ? 'disabled' : ''}">
                <a class="page-link" href="${pageData.page == 1 ? '#' : prevUrl}">â€¹</a>
              </li>

              <c:forEach var="p" begin="1" end="${pageData.totalPages}">
                <c:url var="pageUrl" value="/admin/account">
                  <c:param name="page" value="${p}"/>
                  <c:if test="${not empty search.q}"><c:param name="q" value="${search.q}"/></c:if>
                  <c:if test="${not empty search.vaiTro}"><c:param name="vaiTro" value="${search.vaiTro}"/></c:if>
                  <c:if test="${not empty search.trangThai}"><c:param name="trangThai" value="${search.trangThai}"/></c:if>
                  <c:if test="${not empty search.khoi}"><c:param name="khoi" value="${search.khoi}"/></c:if>
                </c:url>
                <li class="page-item ${pageData.page == p ? 'active' : ''}">
                  <a class="page-link" href="${pageUrl}">${p}</a>
                </li>
              </c:forEach>

              <c:url var="nextUrl" value="/admin/account">
                <c:param name="page" value="${pageData.page + 1}"/>
                <c:if test="${not empty search.q}"><c:param name="q" value="${search.q}"/></c:if>
                <c:if test="${not empty search.vaiTro}"><c:param name="vaiTro" value="${search.vaiTro}"/></c:if>
                <c:if test="${not empty search.trangThai}"><c:param name="trangThai" value="${search.trangThai}"/></c:if>
                <c:if test="${not empty search.khoi}"><c:param name="khoi" value="${search.khoi}"/></c:if>
              </c:url>
              <li class="page-item ${pageData.page >= pageData.totalPages ? 'disabled' : ''}">
                <a class="page-link" href="${pageData.page >= pageData.totalPages ? '#' : nextUrl}">â€º</a>
              </li>
            </ul>
          </nav>
        </section>
      </section>
    </section>
  </main>
</div>

<div id="deleteModal" class="delete-modal" hidden>
  <div class="delete-backdrop" data-close-delete-modal></div>
  <div class="delete-dialog">
    <h3>XĂ¡c nháº­n xĂ³a tĂ i khoáº£n</h3>
    <p id="deleteModalMessage"></p>
    <div class="delete-actions">
      <button type="button" class="btn" id="cancelDeleteBtn">Há»§y</button>
      <button type="button" class="btn btn-danger" id="confirmDeleteBtn">XĂ³a</button>
    </div>
  </div>
</div>

<script>
  (function () {
    function closeAllMenus() {
      document.querySelectorAll('.action-dropdown').forEach(menu => menu.classList.remove('show'));
    }

    window.toggleAccountActionMenu = function (button) {
      const menu = button.nextElementSibling;
      const shouldOpen = !menu.classList.contains('show');
      closeAllMenus();
      if (shouldOpen) {
        menu.classList.add('show');
      }
    };

    document.addEventListener('click', function (event) {
      if (!event.target.closest('.action-wrap')) {
        closeAllMenus();
      }
    });

    const deleteModal = document.getElementById('deleteModal');
    const deleteModalMessage = document.getElementById('deleteModalMessage');
    const confirmDeleteBtn = document.getElementById('confirmDeleteBtn');
    const cancelDeleteBtn = document.getElementById('cancelDeleteBtn');
    let pendingDeleteForm = null;

    function closeDeleteModal() {
      deleteModal.hidden = true;
      pendingDeleteForm = null;
      document.body.classList.remove('modal-open');
    }

    document.querySelectorAll('.delete-account-form').forEach(form => {
      form.addEventListener('submit', function (event) {
        event.preventDefault();
        pendingDeleteForm = form;
        deleteModalMessage.textContent = 'Báº¡n cháº¯c cháº¯n muá»‘n xĂ³a tĂ i khoáº£n "' + (form.dataset.username || '') + '"?';
        deleteModal.hidden = false;
        document.body.classList.add('modal-open');
      });
    });

    confirmDeleteBtn.addEventListener('click', function () {
      if (!pendingDeleteForm) {
        closeDeleteModal();
        return;
      }
      pendingDeleteForm.submit();
    });

    cancelDeleteBtn.addEventListener('click', closeDeleteModal);
    deleteModal.querySelectorAll('[data-close-delete-modal]').forEach(el => {
      el.addEventListener('click', closeDeleteModal);
    });

    document.addEventListener('keydown', function (event) {
      if (event.key === 'Escape') {
        closeAllMenus();
        if (!deleteModal.hidden) {
          closeDeleteModal();
        }
      }
    });
  })();
</script>
</body>
</html>

