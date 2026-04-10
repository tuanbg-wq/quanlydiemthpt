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
  <link rel="stylesheet" href="<c:url value='/css/admin/conduct-list.css'/>">
</head>
<body>
<div class="layout">
  <jsp:include page="/WEB-INF/views/admin/_sidebar.jsp"/>

  <main class="main conduct-page">
    <header class="conduct-header">
      <div class="header-left">
        <h1>Khen thưởng / Kỷ luật</h1>
        <p>Quản lý quyết định theo học sinh, khối, lớp và khóa học.</p>
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
          <div class="stats-icon icon-khen" aria-hidden="true">
            <svg viewBox="0 0 24 24" fill="none">
              <path d="M12 3l2.4 4.9 5.4.8-3.9 3.8.9 5.4L12 15.5 7.2 18l.9-5.4L4.2 8.7l5.4-.8L12 3z" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"/>
            </svg>
          </div>
          <div>
            <p>Tổng khen thưởng</p>
            <h3><fmt:formatNumber value="${stats.totalReward}" groupingUsed="true"/></h3>
          </div>
        </article>

        <article class="stats-card">
          <div class="stats-icon icon-ky-luat" aria-hidden="true">
            <svg viewBox="0 0 24 24" fill="none">
              <path d="M7 6h10M9 6V4h6v2m-8 0 1 13h8l1-13M10 10v6M14 10v6" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <div>
            <p>Tổng kỷ luật</p>
            <h3><fmt:formatNumber value="${stats.totalDiscipline}" groupingUsed="true"/></h3>
          </div>
        </article>

        <article class="stats-card stats-card-rate">
          <div class="stats-rate-content">
            <p>Tỉ lệ khen thưởng / kỷ luật</p>
            <h3>${stats.rewardRateDisplay}</h3>
            <small>Khen thưởng</small>
            <span class="rate-subline">Kỷ luật: ${stats.disciplineRateDisplay}</span>
          </div>
          <div class="rate-donut"
               data-reward-rate="${stats.rewardRateValue}"
               data-discipline-rate="${stats.disciplineRateValue}">
            <span class="rate-donut-value">0%</span>
          </div>
          <div class="rate-legend">
            <span class="legend-item"><i class="legend-swatch legend-reward"></i>Khen thưởng (${stats.rewardRateDisplay})</span>
            <span class="legend-item"><i class="legend-swatch legend-discipline"></i>Kỷ luật (${stats.disciplineRateDisplay})</span>
          </div>
        </article>
      </section>

      <section class="card filter-card">
        <form class="filters" method="get" action="<c:url value='/admin/conduct'/>" autocomplete="off">
          <div class="filter-item search-item">
            <label for="q">Tìm kiếm học sinh</label>
            <input id="q" type="text" name="q" value="${search.q}" placeholder="Nhập tên, mã học sinh, số quyết định...">
            <div id="conductStudentSuggestBox" class="student-suggest-box" hidden></div>
          </div>

          <div class="filter-item">
            <label for="khoi">Khối</label>
            <select id="khoi" name="khoi">
              <option value="">Tất cả</option>
              <c:forEach var="grade" items="${grades}">
                <option value="${grade}" ${search.khoi == grade ? 'selected' : ''}>Khối ${grade}</option>
              </c:forEach>
            </select>
          </div>

          <div class="filter-item">
            <label for="khoa">Khóa</label>
            <select id="khoa" name="khoa">
              <option value="">Tất cả</option>
              <c:forEach var="item" items="${courseOptions}">
                <option value="${item.id}" ${search.khoa == item.id ? 'selected' : ''}>${item.name}</option>
              </c:forEach>
            </select>
          </div>

          <div class="filter-item">
            <label for="lop">Lớp</label>
            <select id="lop" name="lop">
              <option value="">Tất cả</option>
              <c:forEach var="item" items="${classOptions}">
                <option value="${item.id}" ${search.lop == item.id ? 'selected' : ''}>${item.name}</option>
              </c:forEach>
            </select>
          </div>

          <div class="filter-item">
            <label for="loai">Loại</label>
            <select id="loai" name="loai">
              <option value="">Tất cả</option>
              <option value="KHEN_THUONG" ${search.loai == 'KHEN_THUONG' ? 'selected' : ''}>Khen thưởng</option>
              <option value="KY_LUAT" ${search.loai == 'KY_LUAT' ? 'selected' : ''}>Kỷ luật</option>
            </select>
          </div>

          <div class="filter-actions">
            <button class="btn filter-btn action-btn-search" type="submit">Lọc dữ liệu</button>
          </div>
        </form>
      </section>

      <section class="card table-card">
        <div class="table-head">
          <h2>Danh sách chi tiết</h2>
          <div class="table-actions">
            <a class="btn btn-khen" href="<c:url value='/admin/conduct/reward/create'/>">+ Thêm khen thưởng</a>
            <a class="btn btn-ky-luat" href="<c:url value='/admin/conduct/discipline/create'/>">+ Thêm kỷ luật</a>
          </div>
        </div>

        <div class="table-wrap">
          <table class="table">
            <thead>
            <tr>
              <th>Mã HS</th>
              <th>Họ tên</th>
              <th>Lớp</th>
              <th>Loại</th>
              <th>Số quyết định</th>
              <th>Nội dung chi tiết</th>
              <th>Ngày ban hành</th>
              <th class="th-actions">Thao tác</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="item" items="${records}">
              <tr>
                <td>${item.idHocSinh}</td>
                <td class="student-name">${item.tenHocSinh}</td>
                <td>${item.tenLop}</td>
                <td><span class="type-badge ${item.loaiBadgeClass}">${item.loaiDisplay}</span></td>
                <td>${empty item.soQuyetDinh ? '-' : item.soQuyetDinh}</td>
                <td class="detail-text">${item.noiDungChiTiet}</td>
                <td>${item.ngayBanHanh}</td>
                <td class="actions">
                  <div class="action-menu">
                    <button type="button" class="action-toggle" aria-label="Mở menu thao tác" aria-expanded="false" onclick="toggleConductActionMenu(this)">&#8942;</button>
                    <div class="action-dropdown">
                      <a class="action-item" href="<c:url value='/admin/conduct/${item.eventId}/info'/>">Thông tin</a>
                      <a class="action-item" href="<c:url value='/admin/conduct/${item.eventId}/edit'/>">Sửa</a>
                      <form class="conduct-delete-form" method="post" action="<c:url value='/admin/conduct/${item.eventId}/delete'/>"
                            data-student-name="${item.tenHocSinh}" data-so-quyet-dinh="${item.soQuyetDinh}">
                        <button class="action-item danger" type="submit">Xóa</button>
                      </form>
                    </div>
                  </div>
                </td>
              </tr>
            </c:forEach>

            <c:if test="${empty records}">
              <tr>
                <td class="empty-message" colspan="8">Chưa có dữ liệu phù hợp với bộ lọc hiện tại.</td>
              </tr>
            </c:if>
            </tbody>
          </table>
        </div>

        <div class="table-footer">
          <div class="table-count">
            Hiển thị ${pageData.fromRecord}-${pageData.toRecord} trên tổng số ${pageData.totalItems} kết quả
          </div>

          <div class="pagination">
            <c:url var="prevUrl" value="/admin/conduct">
              <c:param name="page" value="${pageData.page - 1}"/>
              <c:if test="${not empty search.q}"><c:param name="q" value="${search.q}"/></c:if>
              <c:if test="${not empty search.khoi}"><c:param name="khoi" value="${search.khoi}"/></c:if>
              <c:if test="${not empty search.khoa}"><c:param name="khoa" value="${search.khoa}"/></c:if>
              <c:if test="${not empty search.lop}"><c:param name="lop" value="${search.lop}"/></c:if>
              <c:if test="${not empty search.loai}"><c:param name="loai" value="${search.loai}"/></c:if>
            </c:url>
            <c:choose>
              <c:when test="${pageData.page > 1}">
                <a class="page-btn" href="${prevUrl}" aria-label="Trang trước">&lsaquo;</a>
              </c:when>
              <c:otherwise><span class="page-btn disabled">&lsaquo;</span></c:otherwise>
            </c:choose>

            <c:forEach var="p" begin="1" end="${pageData.totalPages}">
              <c:url var="pageUrl" value="/admin/conduct">
                <c:param name="page" value="${p}"/>
                <c:if test="${not empty search.q}"><c:param name="q" value="${search.q}"/></c:if>
                <c:if test="${not empty search.khoi}"><c:param name="khoi" value="${search.khoi}"/></c:if>
                <c:if test="${not empty search.khoa}"><c:param name="khoa" value="${search.khoa}"/></c:if>
                <c:if test="${not empty search.lop}"><c:param name="lop" value="${search.lop}"/></c:if>
                <c:if test="${not empty search.loai}"><c:param name="loai" value="${search.loai}"/></c:if>
              </c:url>
              <a class="page-btn ${pageData.page == p ? 'active' : ''}" href="${pageUrl}">${p}</a>
            </c:forEach>

            <c:url var="nextUrl" value="/admin/conduct">
              <c:param name="page" value="${pageData.page + 1}"/>
              <c:if test="${not empty search.q}"><c:param name="q" value="${search.q}"/></c:if>
              <c:if test="${not empty search.khoi}"><c:param name="khoi" value="${search.khoi}"/></c:if>
              <c:if test="${not empty search.khoa}"><c:param name="khoa" value="${search.khoa}"/></c:if>
              <c:if test="${not empty search.lop}"><c:param name="lop" value="${search.lop}"/></c:if>
              <c:if test="${not empty search.loai}"><c:param name="loai" value="${search.loai}"/></c:if>
            </c:url>
            <c:choose>
              <c:when test="${pageData.page < pageData.totalPages}">
                <a class="page-btn" href="${nextUrl}" aria-label="Trang sau">&rsaquo;</a>
              </c:when>
              <c:otherwise><span class="page-btn disabled">&rsaquo;</span></c:otherwise>
            </c:choose>
          </div>
        </div>
      </section>

      <section class="card activity-card">
        <div class="activity-head">
          <div>
            <h3>Lịch sử hoạt động</h3>
            <p>Ghi nhận thao tác từ tài khoản Admin hoặc GVCN.</p>
          </div>
        </div>
        <div class="activity-search-wrap">
          <input id="activitySearchInput" type="text" placeholder="Tìm hành động, người thực hiện...">
        </div>
        <div class="activity-list" id="conductActivityList">
          <c:forEach var="log" items="${activityLogs}">
            <article class="activity-item activity-${log.actionKind}">
              <span class="activity-dot" aria-hidden="true"></span>
              <div class="activity-item-body">
                <div class="activity-line-top">
                  <div class="activity-actor">
                    <span class="activity-role">${log.actorRole}</span>
                    <strong class="activity-name">${log.actorName}</strong>
                  </div>
                  <span class="activity-time">${log.actionTime}</span>
                </div>
                <p class="activity-detail">${log.actionDetail}</p>
              </div>
            </article>
          </c:forEach>

          <c:if test="${empty activityLogs}">
            <div class="activity-empty-note">Chưa có lịch sử tạo/sửa/xóa khen thưởng, kỷ luật.</div>
          </c:if>
          <div id="activityEmptyHint" class="activity-empty-note" hidden>Không tìm thấy hoạt động phù hợp.</div>
        </div>
      </section>
    </section>
  </main>
</div>

<div id="conductDeleteModal" class="score-delete-modal" hidden>
  <div class="score-delete-backdrop" data-close-conduct-delete-modal></div>
  <div class="score-delete-dialog" role="dialog" aria-modal="true" aria-labelledby="conductDeleteModalTitle">
    <h3 id="conductDeleteModalTitle">Xác nhận xóa quyết định</h3>
    <p id="conductDeleteModalMessage">Bạn có chắc chắn muốn xóa bản ghi này không?</p>
    <div class="score-delete-actions">
      <button type="button" class="btn" id="cancelConductDeleteButton">Hủy</button>
      <button type="button" class="btn btn-danger" id="confirmConductDeleteButton">Xóa</button>
    </div>
  </div>
</div>

<script>
  (function () {
    function closeAllMenus() {
      document.querySelectorAll('.action-dropdown').forEach(menu => menu.classList.remove('show'));
      document.querySelectorAll('.action-menu.is-open').forEach(menu => menu.classList.remove('is-open'));
      document.querySelectorAll('.action-toggle[aria-expanded="true"]').forEach(btn => btn.setAttribute('aria-expanded', 'false'));
    }

    window.toggleConductActionMenu = function (button) {
      const menu = button.nextElementSibling;
      const shouldShow = !menu.classList.contains('show');
      closeAllMenus();
      if (!shouldShow) {
        return;
      }
      menu.classList.add('show');
      button.setAttribute('aria-expanded', 'true');
      const wrapper = button.closest('.action-menu');
      if (wrapper) {
        wrapper.classList.add('is-open');
      }
    };

    document.addEventListener('click', function (event) {
      if (!event.target.closest('.action-menu')) {
        closeAllMenus();
      }
    });
    window.addEventListener('resize', closeAllMenus);
    document.addEventListener('scroll', closeAllMenus, true);

    const deleteModal = document.getElementById('conductDeleteModal');
    const deleteModalMessage = document.getElementById('conductDeleteModalMessage');
    const cancelDeleteButton = document.getElementById('cancelConductDeleteButton');
    const confirmDeleteButton = document.getElementById('confirmConductDeleteButton');
    let pendingDeleteForm = null;
    const searchInput = document.getElementById('q');
    const gradeSelect = document.getElementById('khoi');
    const courseSelect = document.getElementById('khoa');
    const classSelect = document.getElementById('lop');
    const studentSuggestBox = document.getElementById('conductStudentSuggestBox');
    let suggestItems = [];
    let suggestIndex = -1;
    let suggestTimer = null;
    const activitySearchInput = document.getElementById('activitySearchInput');
    const activityItems = Array.from(document.querySelectorAll('.activity-item'));
    const activityEmptyHint = document.getElementById('activityEmptyHint');

    function openDeleteModal(studentName, decisionNo) {
      const who = studentName ? ' của học sinh "' + studentName + '"' : '';
      const qd = decisionNo ? ' (số quyết định: ' + decisionNo + ')' : '';
      deleteModalMessage.textContent = 'Bạn có chắc chắn muốn xóa quyết định' + who + qd + ' không?';
      deleteModal.hidden = false;
      document.body.classList.add('modal-open');
      confirmDeleteButton.focus();
      closeAllMenus();
    }

    function closeDeleteModal() {
      deleteModal.hidden = true;
      document.body.classList.remove('modal-open');
      pendingDeleteForm = null;
    }

    document.querySelectorAll('.conduct-delete-form').forEach(form => {
      form.addEventListener('submit', function (event) {
        if (form.dataset.confirmed === 'true') {
          form.dataset.confirmed = 'false';
          return;
        }
        event.preventDefault();
        pendingDeleteForm = form;
        openDeleteModal(form.dataset.studentName || '', form.dataset.soQuyetDinh || '');
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
      deleteModal.querySelectorAll('[data-close-conduct-delete-modal]').forEach(button => {
        button.addEventListener('click', closeDeleteModal);
      });
    }

    function hasOption(selectElement, value) {
      if (!selectElement || value === null || value === undefined || value === '') {
        return false;
      }
      return Array.from(selectElement.options).some(option => option.value === String(value));
    }

    function hideStudentSuggestBox() {
      if (!studentSuggestBox) {
        return;
      }
      studentSuggestBox.hidden = true;
      studentSuggestBox.innerHTML = '';
      suggestItems = [];
      suggestIndex = -1;
    }

    function highlightStudentSuggest(index) {
      if (!studentSuggestBox) {
        return;
      }
      studentSuggestBox.querySelectorAll('.student-suggest-item').forEach((item, itemIndex) => {
        if (itemIndex === index) {
          item.classList.add('is-active');
          item.scrollIntoView({ block: 'nearest' });
        } else {
          item.classList.remove('is-active');
        }
      });
    }

    function applyStudentSelection(student) {
      if (!student || !student.idHocSinh) {
        return;
      }
      if (searchInput) {
        searchInput.value = student.idHocSinh;
      }
      if (student.khoi && hasOption(gradeSelect, student.khoi)) {
        gradeSelect.value = String(student.khoi);
      }
      if (student.courseId && hasOption(courseSelect, student.courseId)) {
        courseSelect.value = student.courseId;
      }
      if (student.classId && hasOption(classSelect, student.classId)) {
        classSelect.value = student.classId;
      }
      hideStudentSuggestBox();
    }

    function renderStudentSuggest(items) {
      if (!studentSuggestBox) {
        return;
      }
      studentSuggestBox.innerHTML = '';
      suggestItems = Array.isArray(items) ? items : [];
      suggestIndex = -1;
      if (!suggestItems.length) {
        hideStudentSuggestBox();
        return;
      }

      suggestItems.forEach((student, index) => {
        const button = document.createElement('button');
        button.type = 'button';
        button.className = 'student-suggest-item';
        button.dataset.index = String(index);

        const name = document.createElement('strong');
        name.textContent = student.hoTen || '-';

        const metaOne = document.createElement('span');
        metaOne.textContent = (student.idHocSinh || '-') + ' • ' + (student.tenLop || '-');

        const metaTwo = document.createElement('span');
        const gradeText = student.khoi ? ('Khối ' + student.khoi) : 'Khối -';
        metaTwo.textContent = gradeText + ' • ' + (student.khoaHoc || '-');

        button.appendChild(name);
        button.appendChild(metaOne);
        button.appendChild(metaTwo);
        button.addEventListener('click', function () {
          applyStudentSelection(student);
        });
        studentSuggestBox.appendChild(button);
      });

      studentSuggestBox.hidden = false;
    }

    function fetchStudentSuggest() {
      if (!searchInput || !studentSuggestBox) {
        return;
      }
      const keyword = (searchInput.value || '').trim();
      if (keyword.length < 1) {
        hideStudentSuggestBox();
        return;
      }

      const params = new URLSearchParams();
      params.set('q', keyword);
      if (gradeSelect && gradeSelect.value) {
        params.set('khoi', gradeSelect.value);
      }
      if (courseSelect && courseSelect.value) {
        params.set('khoa', courseSelect.value);
      }
      if (classSelect && classSelect.value) {
        params.set('lop', classSelect.value);
      }

      fetch('<c:url value="/admin/conduct/suggest-students"/>' + '?' + params.toString(), {
        headers: { 'Accept': 'application/json' }
      })
        .then(response => response.ok ? response.json() : [])
        .then(data => {
          renderStudentSuggest(data);
        })
        .catch(() => {
          hideStudentSuggestBox();
        });
    }

    if (searchInput && studentSuggestBox) {
      searchInput.addEventListener('input', function () {
        if (suggestTimer) {
          clearTimeout(suggestTimer);
        }
        suggestTimer = setTimeout(fetchStudentSuggest, 180);
      });

      searchInput.addEventListener('keydown', function (event) {
        if (studentSuggestBox.hidden || !suggestItems.length) {
          return;
        }
        if (event.key === 'ArrowDown') {
          event.preventDefault();
          suggestIndex = (suggestIndex + 1) % suggestItems.length;
          highlightStudentSuggest(suggestIndex);
          return;
        }
        if (event.key === 'ArrowUp') {
          event.preventDefault();
          suggestIndex = (suggestIndex - 1 + suggestItems.length) % suggestItems.length;
          highlightStudentSuggest(suggestIndex);
          return;
        }
        if (event.key === 'Enter' && suggestIndex >= 0 && suggestIndex < suggestItems.length) {
          event.preventDefault();
          applyStudentSelection(suggestItems[suggestIndex]);
          return;
        }
        if (event.key === 'Escape') {
          hideStudentSuggestBox();
        }
      });

      document.addEventListener('click', function (event) {
        if (!event.target.closest('.search-item')) {
          hideStudentSuggestBox();
        }
      });
    }

    if (activitySearchInput && activityItems.length) {
      activitySearchInput.addEventListener('input', function () {
        const keyword = (activitySearchInput.value || '').trim().toLowerCase();
        let visibleCount = 0;
        activityItems.forEach(item => {
          const text = (item.textContent || '').toLowerCase();
          const isVisible = !keyword || text.includes(keyword);
          item.hidden = !isVisible;
          if (isVisible) {
            visibleCount++;
          }
        });
        if (activityEmptyHint) {
          activityEmptyHint.hidden = visibleCount > 0;
        }
      });
    }

    document.addEventListener('keydown', function (event) {
      if (event.key === 'Escape') {
        if (deleteModal && !deleteModal.hidden) {
          closeDeleteModal();
          return;
        }
        closeAllMenus();
      }
    });

    const donut = document.querySelector('.rate-donut');
    if (!donut) {
      return;
    }
    const valueElement = donut.querySelector('.rate-donut-value');
    const parseRate = function (value) {
      const parsed = parseFloat((value || '0').replace(',', '.'));
      return Number.isFinite(parsed) ? Math.max(0, Math.min(100, parsed)) : 0;
    };
    const rewardRate = parseRate(donut.dataset.rewardRate);
    const disciplineRate = parseRate(donut.dataset.disciplineRate);

    const render = function (progress) {
      const reward = rewardRate * progress;
      const discipline = disciplineRate * progress;
      donut.style.setProperty('--reward', reward.toFixed(2));
      donut.style.setProperty('--discipline', discipline.toFixed(2));
      if (valueElement) {
        valueElement.textContent = (rewardRate * progress).toFixed(1).replace('.0', '') + '%';
      }
    };

    if (window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
      render(1);
      return;
    }

    const durationMs = 900;
    const start = performance.now();
    function tick(now) {
      const progress = Math.min((now - start) / durationMs, 1);
      const eased = 1 - Math.pow(1 - progress, 3);
      render(eased);
      if (progress < 1) {
        requestAnimationFrame(tick);
      } else {
        render(1);
      }
    }
    requestAnimationFrame(tick);
  })();
</script>
</body>
</html>

