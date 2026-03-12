<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>${pageTitle}</title>

  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
  <link rel="stylesheet" href="<c:url value='/css/admin-layout.css'/>">
  <link rel="stylesheet" href="<c:url value='/css/teacher-list.css'/>">
</head>
<body>
<div class="layout">
  <jsp:include page="/WEB-INF/views/admin/_sidebar.jsp"/>

  <main class="main teacher-list-page">
    <!-- Header section -->
    <section class="teacher-header-section">
      <div class="teacher-header-left">
        <h1>Quáº£n lĂ½ giĂ¡o viĂªn</h1>
        <p>Danh sĂ¡ch giĂ¡o viĂªn trong há»‡ thá»‘ng</p>
      </div>
      <div class="teacher-header-right">
        <a class="btn teacher-add-btn" href="<c:url value='/admin/teacher/create'/>">
          <i class="bi bi-plus-lg"></i>
          <span>ThĂªm giĂ¡o viĂªn</span>
        </a>
      </div>
    </section>

    <section class="content">
      <c:if test="${not empty flashMessage}">
        <div class="alert ${flashType == 'error' ? 'alert-error' : 'alert-success'}">
          ${flashMessage}
        </div>
      </c:if>

      <!-- Filter section -->
      <section class="card teacher-filter-section">
        <form class="row g-3 align-items-end"
              method="get"
              action="<c:url value='/admin/teacher'/>"
              autocomplete="off">
          <div class="col-12 col-lg-3">
            <label for="teacherSearchKeyword" class="form-label">TĂ¬m kiáº¿m</label>
            <input id="teacherSearchKeyword"
                   name="q"
                   class="form-control teacher-input-search"
                   type="text"
                   value="${search.q}"
                   placeholder="Nháº­p tĂªn giĂ¡o viĂªn hoáº·c mĂ£ giĂ¡o viĂªn...">
          </div>

          <div class="col-12 col-md-4 col-lg-3">
            <label for="teacherSubjectFilter" class="form-label">Bá»™ mĂ´n</label>
            <select id="teacherSubjectFilter" name="boMon" class="form-select">
              <option value="">Táº¥t cáº£ bá»™ mĂ´n</option>
              <c:forEach var="subject" items="${subjects}">
                <option value="${subject}" ${search.boMon == subject ? 'selected' : ''}>${subject}</option>
              </c:forEach>
            </select>
          </div>

          <div class="col-12 col-md-4 col-lg-2">
            <label for="teacherGradeFilter" class="form-label">Khá»‘i lá»›p</label>
            <select id="teacherGradeFilter" name="khoi" class="form-select">
              <option value="">Táº¥t cáº£ khá»‘i</option>
              <c:forEach var="grade" items="${grades}">
                <option value="${grade}" ${search.khoi == grade ? 'selected' : ''}>Khá»‘i ${grade}</option>
              </c:forEach>
            </select>
          </div>

          <div class="col-12 col-md-4 col-lg-2">
            <label for="teacherStatusFilter" class="form-label">Tráº¡ng thĂ¡i</label>
            <select id="teacherStatusFilter" name="trangThai" class="form-select">
              <option value="">Táº¥t cáº£ tráº¡ng thĂ¡i</option>
              <c:forEach var="status" items="${statuses}">
                <option value="${status}" ${search.trangThai == status ? 'selected' : ''}>
                  <c:choose>
                    <c:when test="${status == 'dang_lam'}">Äang lĂ m</c:when>
                    <c:when test="${status == 'nghi_huu'}">Nghá»‰ hÆ°u</c:when>
                    <c:when test="${status == 'nghi_viec'}">Nghá»‰ viá»‡c</c:when>
                    <c:otherwise>${status}</c:otherwise>
                  </c:choose>
                </option>
              </c:forEach>
            </select>
          </div>

          <div class="col-12 col-lg-2 filter-actions">
            <button type="submit" class="btn teacher-filter-btn">
              <i class="bi bi-funnel"></i>
              Lá»c dá»¯ liá»‡u
            </button>
          </div>
        </form>
      </section>

      <!-- Table section -->
      <section class="card teacher-table-section">
        <div class="table-wrap">
          <table class="table align-middle mb-0 teacher-table">
            <thead>
            <tr>
              <th>áº¢nh</th>
              <th>MĂ£ giĂ¡o viĂªn</th>
              <th>Há» vĂ  tĂªn</th>
              <th>Giá»›i tĂ­nh</th>
              <th>Sá»‘ Ä‘iá»‡n thoáº¡i</th>
              <th>MĂ´n dáº¡y</th>
              <th>Chá»§ nhiá»‡m lá»›p</th>
              <th>Lá»›p bá»™ mĂ´n</th>
              <th>Vai trĂ²</th>
              <th class="text-center">Thao tĂ¡c</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="t" items="${teachers}">
              <tr>
                <td>
                  <c:choose>
                    <c:when test="${not empty t.avatar}">
                      <img class="teacher-avatar-img"
                           src="<c:url value='/uploads/${t.avatar}'/>"
                           alt="avatar giĂ¡o viĂªn ${t.hoTen}">
                    </c:when>
                    <c:otherwise>
                      <div class="teacher-avatar">${t.avatarInitials}</div>
                    </c:otherwise>
                  </c:choose>
                </td>
                <td><span class="teacher-code-badge">${t.idGiaoVien}</span></td>
                <td><span class="teacher-name">${t.hoTen}</span></td>
                <td>${t.gioiTinh}</td>
                <td>${t.soDienThoai}</td>
                <td>${t.monDay}</td>
                <td>${t.chuNhiemLop}</td>
                <td>${t.lopBoMon}</td>
                <td><span class="teacher-role-badge">${t.vaiTro}</span></td>
                <td class="teacher-action-cell">
                  <div class="teacher-action-wrap">
                    <button type="button"
                            class="teacher-action-btn"
                            aria-label="Má»Ÿ menu hĂ nh Ä‘á»™ng"
                            onclick="toggleTeacherActionMenu(this)">
                      <i class="bi bi-three-dots-vertical"></i>
                    </button>
                    <div class="teacher-action-dropdown" role="menu">
                      <a class="teacher-action-item" href="<c:url value='/admin/teacher/${t.idGiaoVien}/edit'/>">
                        <i class="bi bi-pencil-square"></i>
                        <span>Chá»‰nh sá»­a</span>
                      </a>
                      <button class="teacher-action-item teacher-delete" type="button">
                        <i class="bi bi-trash3"></i>
                        <span>XĂ³a</span>
                      </button>
                    </div>
                  </div>
                </td>
              </tr>
            </c:forEach>

            <c:if test="${empty teachers}">
              <tr>
                <td class="teacher-empty" colspan="10">
                  <c:choose>
                    <c:when test="${not empty search.q or not empty search.boMon or not empty search.khoi or not empty search.trangThai}">
                      KhĂ´ng cĂ³ giĂ¡o viĂªn phĂ¹ há»£p vá»›i bá»™ lá»c.
                    </c:when>
                    <c:otherwise>
                      KhĂ´ng cĂ³ giĂ¡o viĂªn trong há»‡ thá»‘ng.
                    </c:otherwise>
                  </c:choose>
                </td>
              </tr>
            </c:if>
            </tbody>
          </table>
        </div>

        <!-- Pagination section -->
        <section class="teacher-pagination-section">
          <div class="teacher-pagination-summary">
            Hiá»ƒn thá»‹ ${pageData.fromRecord}-${pageData.toRecord} trĂªn tá»•ng sá»‘ ${pageData.totalItems} giĂ¡o viĂªn
          </div>
          <nav aria-label="PhĂ¢n trang danh sĂ¡ch giĂ¡o viĂªn">
            <ul class="pagination pagination-sm mb-0 teacher-pagination">
              <c:url var="prevUrl" value="/admin/teacher">
                <c:param name="page" value="${pageData.page - 1}"/>
                <c:if test="${not empty search.q}">
                  <c:param name="q" value="${search.q}"/>
                </c:if>
                <c:if test="${not empty search.boMon}">
                  <c:param name="boMon" value="${search.boMon}"/>
                </c:if>
                <c:if test="${not empty search.khoi}">
                  <c:param name="khoi" value="${search.khoi}"/>
                </c:if>
                <c:if test="${not empty search.trangThai}">
                  <c:param name="trangThai" value="${search.trangThai}"/>
                </c:if>
              </c:url>

              <li class="page-item ${pageData.page == 1 ? 'disabled' : ''}">
                <a class="page-link" href="${pageData.page == 1 ? '#' : prevUrl}" aria-label="Trang trÆ°á»›c">
                  <i class="bi bi-chevron-left"></i>
                </a>
              </li>

              <c:forEach var="p" begin="1" end="${pageData.totalPages}">
                <c:url var="pageUrl" value="/admin/teacher">
                  <c:param name="page" value="${p}"/>
                  <c:if test="${not empty search.q}">
                    <c:param name="q" value="${search.q}"/>
                  </c:if>
                  <c:if test="${not empty search.boMon}">
                    <c:param name="boMon" value="${search.boMon}"/>
                  </c:if>
                  <c:if test="${not empty search.khoi}">
                    <c:param name="khoi" value="${search.khoi}"/>
                  </c:if>
                  <c:if test="${not empty search.trangThai}">
                    <c:param name="trangThai" value="${search.trangThai}"/>
                  </c:if>
                </c:url>
                <li class="page-item ${pageData.page == p ? 'active' : ''}">
                  <a class="page-link" href="${pageUrl}">${p}</a>
                </li>
              </c:forEach>

              <c:url var="nextUrl" value="/admin/teacher">
                <c:param name="page" value="${pageData.page + 1}"/>
                <c:if test="${not empty search.q}">
                  <c:param name="q" value="${search.q}"/>
                </c:if>
                <c:if test="${not empty search.boMon}">
                  <c:param name="boMon" value="${search.boMon}"/>
                </c:if>
                <c:if test="${not empty search.khoi}">
                  <c:param name="khoi" value="${search.khoi}"/>
                </c:if>
                <c:if test="${not empty search.trangThai}">
                  <c:param name="trangThai" value="${search.trangThai}"/>
                </c:if>
              </c:url>

              <li class="page-item ${pageData.page >= pageData.totalPages ? 'disabled' : ''}">
                <a class="page-link" href="${pageData.page >= pageData.totalPages ? '#' : nextUrl}" aria-label="Trang sau">
                  <i class="bi bi-chevron-right"></i>
                </a>
              </li>
            </ul>
          </nav>
        </section>
      </section>
    </section>
  </main>
</div>

<script>
  (function () {
    function closeAllTeacherMenus() {
      document.querySelectorAll('.teacher-action-dropdown').forEach(menu => {
        menu.classList.remove('show');
        menu.classList.remove('open-up');
      });
      document.querySelectorAll('.teacher-table tbody tr.menu-open').forEach(row => {
        row.classList.remove('menu-open');
      });
      document.querySelectorAll('.teacher-action-btn[aria-expanded="true"]').forEach(button => {
        button.setAttribute('aria-expanded', 'false');
      });
    }

    function positionTeacherMenu(button, menu) {
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

    window.toggleTeacherActionMenu = function (button) {
      const currentMenu = button.nextElementSibling;
      const currentRow = button.closest('tr');
      const shouldShow = !currentMenu.classList.contains('show');

      closeAllTeacherMenus();

      if (!shouldShow) {
        return;
      }

      positionTeacherMenu(button, currentMenu);
      button.setAttribute('aria-expanded', 'true');

      if (currentRow) {
        currentRow.classList.add('menu-open');
      }
    };

    document.addEventListener('click', function (event) {
      if (!event.target.closest('.teacher-action-wrap')) {
        closeAllTeacherMenus();
      }
    });

    window.addEventListener('resize', closeAllTeacherMenus);
    document.addEventListener('scroll', closeAllTeacherMenus, true);

    document.addEventListener('keydown', function (event) {
      if (event.key === 'Escape') {
        closeAllTeacherMenus();
      }
    });
  })();
</script>
</body>
</html>

