<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>${pageTitle}</title>

  <link rel="stylesheet" href="<c:url value='/css/admin-layout.css'/>">
  <link rel="stylesheet" href="<c:url value='/css/admin/class-info.css'/>">
</head>
<body>
<div class="layout">
  <jsp:include page="/WEB-INF/views/admin/_sidebar.jsp"/>

  <main class="main class-info-page">
    <header class="class-info-header">
      <div class="header-left">
        <h1>Lớp ${classInfo.maVaTenLop}</h1>
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
          <p>Tổng học sinh</p>
          <h3>${classInfo.totalStudents}</h3>
        </article>

        <article class="stats-card">
          <p>Tỷ lệ nam/nữ</p>
          <h3>${classInfo.maleStudents}/${classInfo.femaleStudents}</h3>
        </article>
      </section>

      <section class="main-grid">
        <article class="card class-summary-card">
          <h2>Thông tin chi tiết</h2>

          <dl class="summary-list">
            <dt>Tên lớp</dt>
            <dd>${classInfo.tenLopHienThi}</dd>

            <dt>Mã lớp</dt>
            <dd>${classInfo.maLop}</dd>

            <dt>Khối lớp</dt>
            <dd><c:choose><c:when test="${not empty classInfo.khoi}">Khối ${classInfo.khoi}</c:when><c:otherwise>-</c:otherwise></c:choose></dd>

            <dt>Năm học</dt>
            <dd>${classInfo.namHoc}</dd>

            <dt>Khóa học</dt>
            <dd>${classInfo.khoaHocDisplay}</dd>

            <dt>Sĩ số hiện tại</dt>
            <dd>${classInfo.totalStudents}</dd>

            <dt>Giáo viên chủ nhiệm</dt>
            <dd>
              <c:choose>
                <c:when test="${not empty classInfo.idGvcn}">
                  <div class="teacher-box">
                    <c:choose>
                      <c:when test="${not empty classInfo.gvcnAvatarUrl}">
                        <img class="teacher-avatar" src="<c:url value='${classInfo.gvcnAvatarUrl}'/>" alt="avatar gvcn ${classInfo.gvcnTen}">
                      </c:when>
                      <c:otherwise>
                        <span class="teacher-avatar-fallback">${classInfo.gvcnInitials}</span>
                      </c:otherwise>
                    </c:choose>

                    <div class="teacher-meta">
                      <strong>${classInfo.gvcnTen}</strong>
                      <span>${classInfo.idGvcn}</span>
                      <c:if test="${not empty classInfo.gvcnEmail}">
                        <span>${classInfo.gvcnEmail}</span>
                      </c:if>
                      <c:if test="${not empty classInfo.gvcnPhone}">
                        <span>${classInfo.gvcnPhone}</span>
                      </c:if>
                    </div>
                  </div>
                </c:when>
                <c:otherwise>
                  Chưa phân công
                </c:otherwise>
              </c:choose>
            </dd>

            <dt>Ghi chú</dt>
            <dd>${empty classInfo.ghiChu ? '-' : classInfo.ghiChu}</dd>
          </dl>
        </article>

        <article class="card student-list-card">
          <div class="card-head">
            <h2>Danh sách học sinh</h2>
            <a class="btn primary" href="<c:url value='/admin/student/create'/>">+ Thêm học sinh</a>
          </div>

          <div class="table-wrap">
            <table class="table student-table">
              <thead>
              <tr>
                <th>Học sinh</th>
                <th>Mã HS</th>
                <th>Giới tính</th>
                <th>Email</th>
                <th>Ngày nhập học</th>
                <th>Trạng thái</th>
                <th class="th-actions">Thao tác</th>
              </tr>
              </thead>
              <tbody>
              <c:forEach var="student" items="${classInfo.students}">
                <tr>
                  <td>
                    <div class="student-cell">
                      <c:choose>
                        <c:when test="${not empty student.avatarUrl}">
                          <img class="student-avatar" src="<c:url value='${student.avatarUrl}'/>" alt="avatar ${student.hoTen}">
                        </c:when>
                        <c:otherwise>
                          <span class="student-avatar-fallback">${student.initials}</span>
                        </c:otherwise>
                      </c:choose>
                      <span class="student-name">${student.hoTen}</span>
                    </div>
                  </td>
                  <td>${student.idHocSinh}</td>
                  <td>${student.gioiTinhDisplay}</td>
                  <td>${student.email}</td>
                  <td>${student.ngayNhapHocDisplay}</td>
                  <td>${student.trangThaiDisplay}</td>
                  <td class="actions">
                    <div class="action-menu">
                      <button type="button"
                              class="action-toggle"
                              aria-label="Mở menu thao tác"
                              onclick="toggleClassInfoActionMenu(this)">
                        ⋮
                      </button>
                      <div class="action-dropdown" role="menu">
                        <a class="action-item" href="<c:url value='/admin/student/${student.idHocSinh}/info'/>">Chi tiết</a>
                        <a class="action-item" href="<c:url value='/admin/student/${student.idHocSinh}/edit'/>">Chỉnh sửa</a>
                        <form class="student-delete-form"
                              method="post"
                              action="<c:url value='/admin/student/${student.idHocSinh}/delete'/>"
                              data-student-name="${student.hoTen}">
                          <input type="hidden" name="classId" value="${classInfo.idLop}">
                          <button class="action-item danger" type="submit">Xóa</button>
                        </form>
                      </div>
                    </div>
                  </td>
                </tr>
              </c:forEach>

              <c:if test="${empty classInfo.students}">
                <tr>
                  <td class="empty-note" colspan="7">Lớp này chưa có học sinh.</td>
                </tr>
              </c:if>
              </tbody>
            </table>
          </div>
        </article>
      </section>

      <article class="card history-card">
        <div class="card-head">
          <h2>Lịch sử chuyển lớp</h2>
        </div>

        <div class="table-wrap">
          <table class="table history-table">
            <thead>
            <tr>
              <th>Tên học sinh</th>
              <th>Từ lớp</th>
              <th>Đến lớp</th>
              <th>Ngày thực hiện</th>
              <th>Loại chuyển</th>
              <th>Ghi chú</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="item" items="${classInfo.transferHistory}">
              <tr>
                <td>
                  <div class="history-student">
                    <strong>${item.studentName}</strong>
                    <span>${item.studentId}</span>
                  </div>
                </td>
                <td>${item.fromClass}</td>
                <td>${item.toClass}</td>
                <td>${item.transferDateDisplay}</td>
                <td>
                  <span class="transfer-badge ${item.transferBadgeClass}">${item.transferTypeDisplay}</span>
                </td>
                <td>${empty item.note ? '-' : item.note}</td>
              </tr>
            </c:forEach>

            <c:if test="${empty classInfo.transferHistory}">
              <tr>
                <td class="empty-note" colspan="6">Chưa có lịch sử chuyển lớp cho lớp này.</td>
              </tr>
            </c:if>
            </tbody>
          </table>
        </div>
      </article>

      <div class="page-actions">
        <a class="btn" href="<c:url value='/admin/class'/>">Quay lại danh sách lớp</a>
        <a class="btn primary" href="<c:url value='/admin/class/${classInfo.idLop}/edit'/>">Chỉnh sửa lớp</a>
      </div>
    </section>
  </main>
</div>

<div id="studentDeleteModal" class="student-delete-modal" hidden>
  <div class="student-delete-backdrop" data-close-student-delete-modal></div>
  <div class="student-delete-dialog" role="dialog" aria-modal="true" aria-labelledby="studentDeleteModalTitle">
    <h3 id="studentDeleteModalTitle">Xác nhận xóa học sinh</h3>
    <p id="studentDeleteModalMessage">Bạn có chắc chắn muốn xóa học sinh này không?</p>
    <div class="student-delete-actions">
      <button type="button" class="btn" id="cancelStudentDeleteButton">Hủy bỏ</button>
      <button type="button" class="btn btn-danger" id="confirmStudentDeleteButton">Xác nhận xóa</button>
    </div>
  </div>
</div>

<script>
  (function () {
    const deleteModal = document.getElementById('studentDeleteModal');
    const deleteModalMessage = document.getElementById('studentDeleteModalMessage');
    const cancelDeleteButton = document.getElementById('cancelStudentDeleteButton');
    const confirmDeleteButton = document.getElementById('confirmStudentDeleteButton');
    let pendingDeleteForm = null;

    function closeAllClassInfoMenus() {
      document.querySelectorAll('.action-dropdown').forEach(menu => {
        menu.classList.remove('show');
        menu.classList.remove('open-up');
      });
      document.querySelectorAll('.action-menu.is-open').forEach(menu => {
        menu.classList.remove('is-open');
      });
      document.querySelectorAll('.student-table tbody tr.menu-open').forEach(row => {
        row.classList.remove('menu-open');
      });
      document.querySelectorAll('.action-toggle[aria-expanded="true"]').forEach(button => {
        button.setAttribute('aria-expanded', 'false');
      });
    }

    function positionClassInfoMenu(button, menu) {
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

    window.toggleClassInfoActionMenu = function (button) {
      const currentMenu = button.nextElementSibling;
      const currentRow = button.closest('tr');
      const shouldShow = !currentMenu.classList.contains('show');

      closeAllClassInfoMenus();

      if (!shouldShow) {
        return;
      }

      positionClassInfoMenu(button, currentMenu);
      button.setAttribute('aria-expanded', 'true');

      if (currentRow) {
        currentRow.classList.add('menu-open');
      }
      const currentWrap = button.closest('.action-menu');
      if (currentWrap) {
        currentWrap.classList.add('is-open');
      }
    };

    function openDeleteModal(studentName) {
      const safeName = studentName ? ' "' + studentName + '"' : '';
      deleteModalMessage.textContent = 'Bạn có chắc chắn muốn xóa học sinh' + safeName + ' khỏi lớp học này không?';
      deleteModal.hidden = false;
      document.body.classList.add('modal-open');
      confirmDeleteButton.focus();
      closeAllClassInfoMenus();
    }

    function closeDeleteModal() {
      deleteModal.hidden = true;
      document.body.classList.remove('modal-open');
      pendingDeleteForm = null;
    }

    document.querySelectorAll('.student-delete-form').forEach(form => {
      form.addEventListener('submit', function (event) {
        if (form.dataset.confirmed === 'true') {
          form.dataset.confirmed = 'false';
          return;
        }
        event.preventDefault();
        pendingDeleteForm = form;
        openDeleteModal(form.dataset.studentName || '');
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
      deleteModal.querySelectorAll('[data-close-student-delete-modal]').forEach(button => {
        button.addEventListener('click', closeDeleteModal);
      });
    }

    document.addEventListener('click', function (event) {
      if (!event.target.closest('.action-menu')) {
        closeAllClassInfoMenus();
      }
    });

    window.addEventListener('resize', closeAllClassInfoMenus);
    document.addEventListener('scroll', closeAllClassInfoMenus, true);

    document.addEventListener('keydown', function (event) {
      if (event.key === 'Escape') {
        if (deleteModal && !deleteModal.hidden) {
          closeDeleteModal();
          return;
        }
        closeAllClassInfoMenus();
      }
    });
  })();
</script>
</body>
</html>

