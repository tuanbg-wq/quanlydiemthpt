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
        <h1>Lá»›p ${classInfo.maVaTenLop}</h1>
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
          <p>Tá»•ng há»c sinh</p>
          <h3>${classInfo.totalStudents}</h3>
        </article>

        <article class="stats-card">
          <p>Tá»· lá»‡ nam/ná»¯</p>
          <h3>${classInfo.maleStudents}/${classInfo.femaleStudents}</h3>
        </article>
      </section>

      <section class="main-grid">
        <article class="card class-summary-card">
          <h2>ThĂ´ng tin chi tiáº¿t</h2>

          <dl class="summary-list">
            <dt>TĂªn lá»›p</dt>
            <dd>${classInfo.tenLopHienThi}</dd>

            <dt>MĂ£ lá»›p</dt>
            <dd>${classInfo.maLop}</dd>

            <dt>Khá»‘i lá»›p</dt>
            <dd><c:choose><c:when test="${not empty classInfo.khoi}">Khá»‘i ${classInfo.khoi}</c:when><c:otherwise>-</c:otherwise></c:choose></dd>

            <dt>NÄƒm há»c</dt>
            <dd>${classInfo.namHoc}</dd>

            <dt>KhĂ³a há»c</dt>
            <dd>${classInfo.khoaHocDisplay}</dd>

            <dt>SÄ© sá»‘ hiá»‡n táº¡i</dt>
            <dd>${classInfo.totalStudents}</dd>

            <dt>GiĂ¡o viĂªn chá»§ nhiá»‡m</dt>
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
                  ChÆ°a phĂ¢n cĂ´ng
                </c:otherwise>
              </c:choose>
            </dd>

            <dt>Ghi chĂº</dt>
            <dd>${empty classInfo.ghiChu ? '-' : classInfo.ghiChu}</dd>
          </dl>
        </article>

        <article class="card student-list-card">
          <div class="card-head">
            <h2>Danh sĂ¡ch há»c sinh</h2>
            <a class="btn primary" href="<c:url value='/admin/student/create'/>">+ ThĂªm há»c sinh</a>
          </div>

          <div class="table-wrap">
            <table class="table student-table">
              <thead>
              <tr>
                <th>Há»c sinh</th>
                <th>MĂ£ HS</th>
                <th>Giá»›i tĂ­nh</th>
                <th>Email</th>
                <th>NgĂ y nháº­p há»c</th>
                <th>Tráº¡ng thĂ¡i</th>
                <th class="th-actions">Thao tĂ¡c</th>
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
                              aria-label="Má»Ÿ menu thao tĂ¡c"
                              onclick="toggleClassInfoActionMenu(this)">
                        â‹®
                      </button>
                      <div class="action-dropdown" role="menu">
                        <a class="action-item" href="<c:url value='/admin/student/${student.idHocSinh}/info'/>">Chi tiáº¿t</a>
                        <a class="action-item" href="<c:url value='/admin/student/${student.idHocSinh}/edit'/>">Chá»‰nh sá»­a</a>
                        <form class="student-delete-form"
                              method="post"
                              action="<c:url value='/admin/student/${student.idHocSinh}/delete'/>"
                              data-student-name="${student.hoTen}">
                          <input type="hidden" name="classId" value="${classInfo.idLop}">
                          <button class="action-item danger" type="submit">XĂ³a</button>
                        </form>
                      </div>
                    </div>
                  </td>
                </tr>
              </c:forEach>

              <c:if test="${empty classInfo.students}">
                <tr>
                  <td class="empty-note" colspan="7">Lá»›p nĂ y chÆ°a cĂ³ há»c sinh.</td>
                </tr>
              </c:if>
              </tbody>
            </table>
          </div>
        </article>
      </section>

      <article class="card history-card">
        <div class="card-head">
          <h2>Lá»‹ch sá»­ chuyá»ƒn lá»›p</h2>
        </div>

        <div class="table-wrap">
          <table class="table history-table">
            <thead>
            <tr>
              <th>TĂªn há»c sinh</th>
              <th>Tá»« lá»›p</th>
              <th>Äáº¿n lá»›p</th>
              <th>NgĂ y thá»±c hiá»‡n</th>
              <th>Loáº¡i chuyá»ƒn</th>
              <th>Ghi chĂº</th>
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
                <td class="empty-note" colspan="6">ChÆ°a cĂ³ lá»‹ch sá»­ chuyá»ƒn lá»›p cho lá»›p nĂ y.</td>
              </tr>
            </c:if>
            </tbody>
          </table>
        </div>
      </article>

      <div class="page-actions">
        <a class="btn" href="<c:url value='/admin/class'/>">Quay láº¡i danh sĂ¡ch lá»›p</a>
        <a class="btn primary" href="<c:url value='/admin/class/${classInfo.idLop}/edit'/>">Chá»‰nh sá»­a lá»›p</a>
      </div>
    </section>
  </main>
</div>

<div id="studentDeleteModal" class="student-delete-modal" hidden>
  <div class="student-delete-backdrop" data-close-student-delete-modal></div>
  <div class="student-delete-dialog" role="dialog" aria-modal="true" aria-labelledby="studentDeleteModalTitle">
    <h3 id="studentDeleteModalTitle">XĂ¡c nháº­n xĂ³a há»c sinh</h3>
    <p id="studentDeleteModalMessage">Báº¡n cĂ³ cháº¯c cháº¯n muá»‘n xĂ³a há»c sinh nĂ y khĂ´ng?</p>
    <div class="student-delete-actions">
      <button type="button" class="btn" id="cancelStudentDeleteButton">Há»§y bá»</button>
      <button type="button" class="btn btn-danger" id="confirmStudentDeleteButton">XĂ¡c nháº­n xĂ³a</button>
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
      deleteModalMessage.textContent = 'Báº¡n cĂ³ cháº¯c cháº¯n muá»‘n xĂ³a há»c sinh' + safeName + ' khá»i lá»›p há»c nĂ y khĂ´ng?';
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

