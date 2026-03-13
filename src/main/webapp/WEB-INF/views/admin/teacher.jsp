<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

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
        <h1>Quản lý giáo viên</h1>
        <p>Danh sách giáo viên trong hệ thống</p>
      </div>
      <div class="teacher-header-right">
        <a class="btn teacher-add-btn" href="<c:url value='/admin/teacher/create'/>">
          <i class="bi bi-plus-lg"></i>
          <span>Thêm giáo viên</span>
        </a>
      </div>
    </section>

    <section class="content">
      <c:if test="${not empty flashMessage}">
        <c:set var="isFlashSuccess" value="${flashType != 'error'}"/>
        <div class="alert teacher-flash-message ${isFlashSuccess ? 'alert-success teacher-flash-success' : 'alert-error'}">
          <c:if test="${isFlashSuccess}">
            <i class="bi bi-check-circle-fill" aria-hidden="true"></i>
          </c:if>
          <span>${flashMessage}</span>
        </div>
      </c:if>

      <!-- Filter section -->
      <section class="card teacher-filter-section">
        <form class="row g-3 align-items-end"
              method="get"
              action="<c:url value='/admin/teacher'/>"
              autocomplete="off">
          <div class="col-12 col-lg-3">
            <label for="teacherSearchKeyword" class="form-label">Tìm kiếm</label>
            <input id="teacherSearchKeyword"
                   name="q"
                   class="form-control teacher-input-search"
                   type="text"
                   value="${search.q}"
                   placeholder="Nhập tên giáo viên hoặc mã giáo viên...">
          </div>

          <div class="col-12 col-md-4 col-lg-3">
            <label for="teacherSubjectFilter" class="form-label">Bộ môn</label>
            <select id="teacherSubjectFilter" name="boMon" class="form-select">
              <option value="">Tất cả bộ môn</option>
              <c:forEach var="subject" items="${subjects}">
                <option value="${subject}" ${search.boMon == subject ? 'selected' : ''}>${subject}</option>
              </c:forEach>
            </select>
          </div>

          <div class="col-12 col-md-4 col-lg-2">
            <label for="teacherGradeFilter" class="form-label">Khối lớp</label>
            <select id="teacherGradeFilter" name="khoi" class="form-select">
              <option value="">Tất cả khối</option>
              <c:forEach var="grade" items="${grades}">
                <option value="${grade}" ${search.khoi == grade ? 'selected' : ''}>Khối ${grade}</option>
              </c:forEach>
            </select>
          </div>

          <div class="col-12 col-md-4 col-lg-2">
            <label for="teacherStatusFilter" class="form-label">Trạng thái</label>
            <select id="teacherStatusFilter" name="trangThai" class="form-select">
              <option value="">Tất cả trạng thái</option>
              <c:forEach var="status" items="${statuses}">
                <option value="${status}" ${search.trangThai == status ? 'selected' : ''}>
                  <c:choose>
                    <c:when test="${status == 'dang_lam'}">Đang làm</c:when>
                    <c:when test="${status == 'nghi_huu'}">Nghỉ hưu</c:when>
                    <c:when test="${status == 'nghi_viec'}">Nghỉ việc</c:when>
                    <c:otherwise>${status}</c:otherwise>
                  </c:choose>
                </option>
              </c:forEach>
            </select>
          </div>

          <div class="col-12 col-lg-2 filter-actions">
            <button type="submit" class="btn teacher-filter-btn">
              <i class="bi bi-funnel"></i>
              Lọc dữ liệu
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
              <th>Ảnh</th>
              <th>Mã giáo viên</th>
              <th>Họ và tên</th>
              <th>Giới tính</th>
              <th>Số điện thoại</th>
              <th>Môn dạy</th>
              <th>Chủ nhiệm lớp</th>
              <th>Lớp bộ môn</th>
              <th>Vai trò</th>
              <th class="text-center">Thao tác</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="t" items="${teachers}">
              <tr>
                <td>
                  <c:choose>
                    <c:when test="${not empty t.avatar}">
                      <c:choose>
                        <c:when test="${fn:startsWith(t.avatar, '/')}">
                          <c:url var="avatarUrl" value="${t.avatar}"/>
                        </c:when>
                        <c:otherwise>
                          <c:url var="avatarUrl" value="/uploads/${t.avatar}"/>
                        </c:otherwise>
                      </c:choose>
                      <img class="teacher-avatar-img"
                           src="${avatarUrl}"
                           alt="avatar giáo viên ${t.hoTen}">
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
                            aria-label="Mở menu hành động"
                            onclick="toggleTeacherActionMenu(this)">
                      <i class="bi bi-three-dots-vertical"></i>
                    </button>
                    <div class="teacher-action-dropdown" role="menu">
                      <a class="teacher-action-item" href="<c:url value='/admin/teacher/${t.idGiaoVien}/info'/>">
                        <i class="bi bi-person-vcard"></i>
                        <span>Thông tin giáo viên</span>
                      </a>
                      <a class="teacher-action-item" href="<c:url value='/admin/teacher/${t.idGiaoVien}/edit'/>">
                        <i class="bi bi-pencil-square"></i>
                        <span>Chỉnh sửa</span>
                      </a>
                      <form class="teacher-delete-form"
                            method="post"
                            action="<c:url value='/admin/teacher/${t.idGiaoVien}/delete'/>"
                            data-teacher-code="${t.idGiaoVien}"
                            data-teacher-name="${t.hoTen}">
                        <button class="teacher-action-item teacher-delete" type="submit">
                          <i class="bi bi-trash3"></i>
                          <span>Xóa</span>
                        </button>
                      </form>
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
                      Không có giáo viên phù hợp với bộ lọc.
                    </c:when>
                    <c:otherwise>
                      Không có giáo viên trong hệ thống.
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
            Hiển thị ${pageData.fromRecord}-${pageData.toRecord} trên tổng số ${pageData.totalItems} giáo viên
          </div>
          <nav aria-label="Phân trang danh sách giáo viên">
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
                <a class="page-link" href="${pageData.page == 1 ? '#' : prevUrl}" aria-label="Trang trước">
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

<div id="teacherDeleteModal" class="teacher-delete-modal" hidden>
  <div class="teacher-delete-backdrop" data-close-delete-modal></div>
  <div class="teacher-delete-dialog" role="dialog" aria-modal="true" aria-labelledby="teacherDeleteModalTitle">
    <h3 id="teacherDeleteModalTitle">Xác nhận xóa giáo viên</h3>
    <p id="teacherDeleteModalMessage">Bạn có chắc chắn muốn xóa giáo viên này không?</p>
    <div class="teacher-delete-actions">
      <button type="button" class="btn" id="cancelTeacherDeleteButton">Hủy</button>
      <button type="button" class="btn btn-danger" id="confirmTeacherDeleteButton">Xóa giáo viên</button>
    </div>
  </div>
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

    const deleteModal = document.getElementById('teacherDeleteModal');
    const deleteModalMessage = document.getElementById('teacherDeleteModalMessage');
    const cancelDeleteButton = document.getElementById('cancelTeacherDeleteButton');
    const confirmDeleteButton = document.getElementById('confirmTeacherDeleteButton');
    let pendingDeleteForm = null;

    function openDeleteModal(teacherCode, teacherName) {
      const safeCode = teacherCode ? ' (' + teacherCode + ')' : '';
      const safeName = teacherName ? ' "' + teacherName + '"' : '';
      deleteModalMessage.textContent = 'Bạn có chắc chắn muốn xóa giáo viên' + safeName + safeCode + ' không?';
      deleteModal.hidden = false;
      document.body.classList.add('modal-open');
      confirmDeleteButton.focus();
      closeAllTeacherMenus();
    }

    function closeDeleteModal() {
      deleteModal.hidden = true;
      document.body.classList.remove('modal-open');
      pendingDeleteForm = null;
    }

    document.querySelectorAll('.teacher-delete-form').forEach(form => {
      form.addEventListener('submit', function (event) {
        if (form.dataset.confirmed === 'true') {
          form.dataset.confirmed = 'false';
          return;
        }

        event.preventDefault();
        pendingDeleteForm = form;
        openDeleteModal(form.dataset.teacherCode || '', form.dataset.teacherName || '');
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
      deleteModal.querySelectorAll('[data-close-delete-modal]').forEach(button => {
        button.addEventListener('click', closeDeleteModal);
      });
    }

    document.addEventListener('keydown', function (event) {
      if (event.key === 'Escape') {
        if (deleteModal && !deleteModal.hidden) {
          closeDeleteModal();
          return;
        }
        closeAllTeacherMenus();
      }
    });

    const flashMessage = document.querySelector('.teacher-list-page .teacher-flash-message');
    if (flashMessage) {
      setTimeout(function () {
        flashMessage.classList.add('is-hidden');
      }, 3200);
    }
  })();
</script>
</body>
</html>
