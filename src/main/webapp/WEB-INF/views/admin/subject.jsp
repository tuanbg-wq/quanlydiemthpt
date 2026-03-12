<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>${pageTitle}</title>

  <link rel="stylesheet" href="<c:url value='/css/admin-layout.css'/>">
  <link rel="stylesheet" href="<c:url value='/css/subject-list.css'/>">
</head>
<body>
<div class="layout">
  <jsp:include page="/WEB-INF/views/admin/_sidebar.jsp"/>

  <main class="main subject-list-page">
    <header class="topbar">
      <div class="topbar-left">
        <h1>Quản Lý Môn Học</h1>
        <p>Danh sách các môn học trong chương trình đào tạo</p>
      </div>

      <div class="topbar-right">
        <a class="btn primary" href="<c:url value='/admin/subject/create'/>">
          + Thêm môn học
        </a>
      </div>
    </header>

    <section class="content">
      <c:if test="${not empty flashMessage}">
        <div class="flash-message alert ${flashType == 'error' ? 'alert-error' : (flashType == 'info' ? 'alert-info' : 'alert-success')}">
          ${flashMessage}
        </div>
      </c:if>

      <div class="card filter-card">
        <form class="filters" method="get" action="<c:url value='/admin/subject'/>">
          <div class="filter-item search-item">
            <label for="q">Tìm kiếm</label>
            <input id="q"
                   type="text"
                   name="q"
                   value="${search.q}"
                   placeholder="Nhập tên hoặc mã môn học...">
          </div>

          <div class="filter-item">
            <label for="khoi">Khối lớp</label>
            <select id="khoi" name="khoi">
              <option value="">Tất cả các khối</option>
              <c:forEach var="g" items="${grades}">
                <option value="${g}" ${search.khoi == g.toString() ? 'selected' : ''}>Khối ${g}</option>
              </c:forEach>
            </select>
          </div>

          <div class="filter-item">
            <label for="toBoMon">Tổ bộ môn</label>
            <select id="toBoMon" name="toBoMon">
              <option value="">Tất cả tổ bộ môn</option>
              <c:forEach var="dept" items="${departments}">
                <option value="${dept}" ${search.toBoMon == dept ? 'selected' : ''}>${dept}</option>
              </c:forEach>
            </select>
          </div>

          <div class="filter-actions">
            <button class="btn" type="submit">Lọc dữ liệu</button>
          </div>
        </form>
      </div>

      <div class="card table-card">
        <div class="table-wrap">
          <table class="table">
            <thead>
            <tr>
              <th>Mã môn</th>
              <th>Tên môn học</th>
              <th>Khối lớp</th>
              <th>Năm học</th>
              <th>Học kỳ</th>
              <th>Tổ bộ môn</th>
              <th>Giáo viên</th>
              <th>Thao tác</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="s" items="${subjects}">
              <tr>
                <td><span class="subject-code">${s.idMonHoc}</span></td>
                <td><span class="subject-name">${s.tenMonHoc}</span></td>
                <td>
                  <div class="grade-chips">
                    <c:forEach var="khoi" items="${s.khoiLopList}">
                      <span class="grade-chip">${khoi}</span>
                    </c:forEach>
                    <c:if test="${empty s.khoiLopList}">
                      <span class="muted-value">-</span>
                    </c:if>
                  </div>
                </td>
                <td>${s.namHoc}</td>
                <td>
                  <c:choose>
                    <c:when test="${s.hocKy == 'CA_NAM'}">Cả năm</c:when>
                    <c:when test="${s.hocKy == 'HK1'}">Học kỳ 1</c:when>
                    <c:when test="${s.hocKy == 'HK2'}">Học kỳ 2</c:when>
                    <c:otherwise>${s.hocKy}</c:otherwise>
                  </c:choose>
                </td>
                <td>${s.toBoMon}</td>
                <td>
                  <div class="teacher-name">${s.giaoVienChinh}</div>
                  <c:if test="${s.soGiaoVienKhac > 0}">
                    <div class="teacher-more">+ ${s.soGiaoVienKhac} giáo viên khác</div>
                  </c:if>
                </td>
                <td class="actions">
                  <div class="action-menu">
                    <button type="button" class="action-toggle" aria-label="Mở menu hành động" onclick="toggleActionMenu(this)">&#8942;</button>
                    <div class="action-dropdown" role="menu">
                      <a class="dropdown-item edit-item" href="<c:url value='/admin/subject/${s.idMonHoc}/edit'/>">
                        <span class="item-icon edit-icon" aria-hidden="true">
                          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M3 17.5V21h3.5L18 9.5 14.5 6 3 17.5Z" stroke="currentColor" stroke-width="1.7" stroke-linejoin="round"/>
                            <path d="M12.9 7.6 16.4 11.1" stroke="currentColor" stroke-width="1.7" stroke-linecap="round"/>
                            <path d="M3 21h18" stroke="currentColor" stroke-width="1.7" stroke-linecap="round"/>
                          </svg>
                        </span>
                        <span class="item-label">Chỉnh sửa</span>
                      </a>

                      <form class="delete-subject-form"
                            method="post"
                            action="<c:url value='/admin/subject/${s.idMonHoc}/delete'/>"
                            data-subject-code="${s.idMonHoc}">
                        <button type="submit" class="dropdown-item danger-item">
                          <span class="item-icon delete-icon" aria-hidden="true">
                            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                              <path d="M5 7h14" stroke="currentColor" stroke-width="1.7" stroke-linecap="round"/>
                              <path d="M10 3h4" stroke="currentColor" stroke-width="1.7" stroke-linecap="round"/>
                              <path d="M8 7v12h8V7" stroke="currentColor" stroke-width="1.7" stroke-linejoin="round"/>
                              <path d="M10 11v5M14 11v5" stroke="currentColor" stroke-width="1.7" stroke-linecap="round"/>
                            </svg>
                          </span>
                          <span class="item-label">Xóa</span>
                        </button>
                      </form>
                    </div>
                  </div>
                </td>
              </tr>
            </c:forEach>

            <c:if test="${empty subjects}">
              <tr>
                <td class="empty-message" colspan="8">Không có môn học phù hợp với bộ lọc.</td>
              </tr>
            </c:if>
            </tbody>
          </table>
        </div>

        <div class="table-footer">
          <div class="table-count">
            Hiển thị ${pageData.fromRecord}-${pageData.toRecord} trên ${pageData.totalItems} môn học
          </div>

          <div class="pagination">
            <c:url var="prevUrl" value="/admin/subject">
              <c:param name="page" value="${pageData.page - 1}"/>
              <c:if test="${not empty search.q}">
                <c:param name="q" value="${search.q}"/>
              </c:if>
              <c:if test="${not empty search.khoi}">
                <c:param name="khoi" value="${search.khoi}"/>
              </c:if>
              <c:if test="${not empty search.toBoMon}">
                <c:param name="toBoMon" value="${search.toBoMon}"/>
              </c:if>
            </c:url>

            <c:choose>
              <c:when test="${pageData.page > 1}">
                <a class="page-btn" href="${prevUrl}" aria-label="Trang trước">&lsaquo;</a>
              </c:when>
              <c:otherwise>
                <span class="page-btn disabled">&lsaquo;</span>
              </c:otherwise>
            </c:choose>

            <c:forEach var="p" begin="1" end="${pageData.totalPages}">
              <c:url var="pageUrl" value="/admin/subject">
                <c:param name="page" value="${p}"/>
                <c:if test="${not empty search.q}">
                  <c:param name="q" value="${search.q}"/>
                </c:if>
                <c:if test="${not empty search.khoi}">
                  <c:param name="khoi" value="${search.khoi}"/>
                </c:if>
                <c:if test="${not empty search.toBoMon}">
                  <c:param name="toBoMon" value="${search.toBoMon}"/>
                </c:if>
              </c:url>
              <a class="page-btn ${pageData.page == p ? 'active' : ''}" href="${pageUrl}">${p}</a>
            </c:forEach>

            <c:url var="nextUrl" value="/admin/subject">
              <c:param name="page" value="${pageData.page + 1}"/>
              <c:if test="${not empty search.q}">
                <c:param name="q" value="${search.q}"/>
              </c:if>
              <c:if test="${not empty search.khoi}">
                <c:param name="khoi" value="${search.khoi}"/>
              </c:if>
              <c:if test="${not empty search.toBoMon}">
                <c:param name="toBoMon" value="${search.toBoMon}"/>
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
      </div>
    </section>
  </main>
</div>

<div id="deleteConfirmModal" class="delete-modal" hidden>
  <div class="delete-modal-backdrop" data-close-delete-modal></div>
  <div class="delete-modal-dialog" role="dialog" aria-modal="true" aria-labelledby="deleteModalTitle">
    <h3 id="deleteModalTitle">Xác nhận xóa môn học</h3>
    <p id="deleteModalMessage">Bạn có chắc chắn muốn xóa môn học này không?</p>
    <div class="delete-modal-actions">
      <button type="button" class="btn" id="cancelDeleteButton">Hủy</button>
      <button type="button" class="btn btn-danger" id="confirmDeleteButton">Xóa môn học</button>
    </div>
  </div>
</div>

<script>
  (function () {
    function closeAllActionMenus() {
      document.querySelectorAll('.action-dropdown').forEach(menu => {
        menu.classList.remove('show');
        menu.classList.remove('open-up');
      });
      document.querySelectorAll('.table tbody tr.menu-open').forEach(row => {
        row.classList.remove('menu-open');
      });
      document.querySelectorAll('.action-toggle[aria-expanded="true"]').forEach(button => {
        button.setAttribute('aria-expanded', 'false');
      });
    }

    function positionActionMenu(button, menu) {
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

    window.toggleActionMenu = function (button) {
      const currentMenu = button.nextElementSibling;
      const currentRow = button.closest('tr');
      const shouldShow = !currentMenu.classList.contains('show');

      closeAllActionMenus();

      if (!shouldShow) {
        return;
      }

      positionActionMenu(button, currentMenu);
      button.setAttribute('aria-expanded', 'true');

      if (currentRow) {
        currentRow.classList.add('menu-open');
      }
    };

    document.addEventListener('click', function (event) {
      if (!event.target.closest('.action-menu')) {
        closeAllActionMenus();
      }
    });

    window.addEventListener('resize', closeAllActionMenus);
    document.addEventListener('scroll', closeAllActionMenus, true);

    const deleteModal = document.getElementById('deleteConfirmModal');
    const deleteModalMessage = document.getElementById('deleteModalMessage');
    const cancelDeleteButton = document.getElementById('cancelDeleteButton');
    const confirmDeleteButton = document.getElementById('confirmDeleteButton');
    let pendingDeleteForm = null;

    function openDeleteModal(subjectCode) {
      const codeText = subjectCode ? ' "' + subjectCode + '"' : '';
      deleteModalMessage.textContent = 'Bạn có chắc chắn muốn xóa môn học' + codeText + ' không?';
      deleteModal.hidden = false;
      document.body.classList.add('modal-open');
      confirmDeleteButton.focus();
      closeAllActionMenus();
    }

    function closeDeleteModal() {
      deleteModal.hidden = true;
      document.body.classList.remove('modal-open');
      pendingDeleteForm = null;
    }

    document.querySelectorAll('.delete-subject-form').forEach(form => {
      form.addEventListener('submit', function (event) {
        if (form.dataset.confirmed === 'true') {
          form.dataset.confirmed = 'false';
          return;
        }

        event.preventDefault();
        pendingDeleteForm = form;
        openDeleteModal(form.dataset.subjectCode || '');
      });
    });

    confirmDeleteButton.addEventListener('click', function () {
      if (!pendingDeleteForm) {
        closeDeleteModal();
        return;
      }

      pendingDeleteForm.dataset.confirmed = 'true';
      pendingDeleteForm.submit();
    });

    cancelDeleteButton.addEventListener('click', closeDeleteModal);

    deleteModal.querySelectorAll('[data-close-delete-modal]').forEach(button => {
      button.addEventListener('click', closeDeleteModal);
    });

    document.addEventListener('keydown', function (event) {
      if (event.key === 'Escape') {
        if (!deleteModal.hidden) {
          closeDeleteModal();
          return;
        }
        closeAllActionMenus();
      }
    });

    const flashMessage = document.querySelector('.subject-list-page .flash-message');
    if (flashMessage) {
      setTimeout(function () {
        flashMessage.classList.add('is-hidden');
      }, 3200);
    }
  })();
</script>
</body>
</html>
