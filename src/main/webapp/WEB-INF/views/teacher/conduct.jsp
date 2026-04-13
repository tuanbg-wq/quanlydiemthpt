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
  <link rel="stylesheet" href="<c:url value='/css/teacher/conduct/conduct-list.css'/>">
</head>
<body>
<div class="layout">
  <jsp:include page="/WEB-INF/views/teacher/_sidebar.jsp"/>

  <main class="main conduct-page">
    <header class="conduct-header">
      <div class="header-left">
        <h1>Khen thưởng / Kỷ luật</h1>
        <p>
          Phạm vi lớp chủ nhiệm:
          <strong>${empty scope.className ? 'Chưa phân công' : scope.className}</strong>
          <span> | Năm học: <strong>${empty scope.schoolYear ? '-' : scope.schoolYear}</strong></span>
        </p>
      </div>
    </header>

    <section class="content">
      <c:if test="${not empty flashMessage}">
        <div class="alert ${flashType == 'error' ? 'alert-error' : (flashType == 'info' ? 'alert-info' : 'alert-success')}">
          ${flashMessage}
        </div>
      </c:if>
      <c:if test="${not empty warningMessage}">
        <div class="alert alert-error">${warningMessage}</div>
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
            <p>Tỷ lệ khen thưởng / kỷ luật</p>
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
        <form class="filters" method="get" action="<c:url value='/teacher/conduct'/>" autocomplete="off">
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
            <div class="export-actions">
              <button class="btn filter-btn export-btn export-btn-excel"
                      type="submit"
                      formaction="<c:url value='/teacher/conduct/export/excel'/>"
                      formmethod="get"
                      ${pageData.totalItems == 0 ? 'disabled' : ''}
                      title="${pageData.totalItems == 0 ? 'Cần có dữ liệu để xuất Excel' : 'Xuất Excel theo bộ lọc hiện tại'}">
                Xuất Excel
              </button>
              <button class="btn filter-btn export-btn export-btn-pdf"
                      type="submit"
                      formaction="<c:url value='/teacher/conduct/export/pdf'/>"
                      formmethod="get"
                      ${pageData.totalItems == 0 ? 'disabled' : ''}
                      title="${pageData.totalItems == 0 ? 'Cần có dữ liệu để xuất PDF' : 'Xuất PDF theo bộ lọc hiện tại'}">
                Xuất PDF
              </button>
            </div>
            <button class="btn filter-btn action-btn-search" type="submit">Lọc dữ liệu</button>
          </div>
        </form>
      </section>

      <section class="card table-card">
        <div class="table-head">
          <h2>Danh sách chi tiết</h2>
          <div class="table-actions">
            <a class="btn btn-khen" href="<c:url value='/teacher/conduct/reward/create'/>">+ Thêm khen thưởng</a>
            <a class="btn btn-ky-luat" href="<c:url value='/teacher/conduct/discipline/create'/>">+ Thêm kỷ luật</a>
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
                      <a class="action-item" href="<c:url value='/teacher/conduct/${item.eventId}/info'/>">Thông tin</a>
                      <a class="action-item" href="<c:url value='/teacher/conduct/${item.eventId}/edit'/>">Sửa</a>
                      <form class="conduct-delete-form" method="post" action="<c:url value='/teacher/conduct/${item.eventId}/delete'/>"
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
            <c:url var="prevUrl" value="/teacher/conduct">
              <c:param name="page" value="${pageData.page - 1}"/>
              <c:if test="${not empty search.q}"><c:param name="q" value="${search.q}"/></c:if>
              <c:if test="${not empty search.loai}"><c:param name="loai" value="${search.loai}"/></c:if>
            </c:url>
            <c:choose>
              <c:when test="${pageData.page > 1}">
                <a class="page-btn" href="${prevUrl}" aria-label="Trang trước">&lsaquo;</a>
              </c:when>
              <c:otherwise><span class="page-btn disabled">&lsaquo;</span></c:otherwise>
            </c:choose>

            <c:forEach var="p" begin="1" end="${pageData.totalPages}">
              <c:url var="pageUrl" value="/teacher/conduct">
                <c:param name="page" value="${p}"/>
                <c:if test="${not empty search.q}"><c:param name="q" value="${search.q}"/></c:if>
                <c:if test="${not empty search.loai}"><c:param name="loai" value="${search.loai}"/></c:if>
              </c:url>
              <a class="page-btn ${pageData.page == p ? 'active' : ''}" href="${pageUrl}">${p}</a>
            </c:forEach>

            <c:url var="nextUrl" value="/teacher/conduct">
              <c:param name="page" value="${pageData.page + 1}"/>
              <c:if test="${not empty search.q}"><c:param name="q" value="${search.q}"/></c:if>
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
            <p>Ghi nhận thao tác khen thưởng, kỷ luật trong lớp chủ nhiệm hiện tại.</p>
          </div>
        </div>
        <div class="history-filter-grid">
          <div class="activity-search-wrap">
            <input id="activitySearchInput" type="text" placeholder="Tìm người thực hiện, vai trò, hành động...">
          </div>
          <div class="filter-item compact-filter">
            <label for="activityRoleFilter">Vai trò</label>
            <select id="activityRoleFilter">
              <option value="">Tất cả</option>
              <option value="Admin">Admin</option>
              <option value="GVCN">GVCN</option>
              <option value="GVBM">GVBM</option>
            </select>
          </div>
          <div class="filter-item compact-filter">
            <label for="activityDateFilter">Ngày</label>
            <input id="activityDateFilter" type="date">
          </div>
          <div class="filter-item compact-filter">
            <label for="activityMonthFilter">Tháng</label>
            <input id="activityMonthFilter" type="month">
          </div>
          <div class="filter-item compact-filter">
            <label for="activityYearFilter">Năm</label>
            <input id="activityYearFilter" type="number" min="2000" max="2100" step="1" placeholder="2026">
          </div>
          <div class="history-filter-actions">
            <button type="button" class="btn filter-btn action-btn-search history-apply-btn" id="activityApplyFilterButton">Tìm</button>
          </div>
        </div>
        <div class="activity-list" id="conductActivityList">
          <c:forEach var="log" items="${activityLogs}">
            <article class="activity-item activity-${log.actionKind}"
                     data-actor-name="${log.actorName}"
                     data-actor-role="${log.actorRole}"
                     data-action-detail="${log.actionDetail}"
                     data-action-time="${log.actionTime}">
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
            <div class="activity-empty-note">Chưa có lịch sử tạo/sửa/xóa khen thưởng, kỷ luật trong lớp chủ nhiệm.</div>
          </c:if>
          <div id="activityEmptyHint" class="activity-empty-note" hidden>Không tìm thấy hoạt động phù hợp.</div>
        </div>
        <div class="history-pagination" id="activityPagination" hidden></div>
      </section>
      <section class="card export-history-card">
        <div class="activity-head">
          <div>
            <h3>Lịch sử xuất báo cáo</h3>
            <p>Chỉ hiển thị các lần xuất Excel/PDF của chính giáo viên chủ nhiệm đang đăng nhập.</p>
          </div>
        </div>

        <div class="history-filter-grid">
          <div class="activity-search-wrap">
            <input id="exportHistorySearchInput" type="text" placeholder="Tìm định dạng, trạng thái, bộ lọc...">
          </div>
          <div class="filter-item compact-filter">
            <label for="exportHistoryRoleFilter">Vai trò</label>
            <select id="exportHistoryRoleFilter">
              <option value="">Tất cả</option>
              <option value="GVCN">GVCN</option>
              <option value="Admin">Admin</option>
              <option value="GVBM">GVBM</option>
            </select>
          </div>
          <div class="filter-item compact-filter">
            <label for="exportHistoryDateFilter">Ngày</label>
            <input id="exportHistoryDateFilter" type="date">
          </div>
          <div class="filter-item compact-filter">
            <label for="exportHistoryMonthFilter">Tháng</label>
            <input id="exportHistoryMonthFilter" type="month">
          </div>
          <div class="filter-item compact-filter">
            <label for="exportHistoryYearFilter">Năm</label>
            <input id="exportHistoryYearFilter" type="number" min="2000" max="2100" step="1" placeholder="2026">
          </div>
          <div class="history-filter-actions">
            <button type="button" class="btn filter-btn action-btn-search history-apply-btn" id="exportHistoryApplyFilterButton">Tìm</button>
          </div>
        </div>

        <div class="table-wrap">
          <table class="table export-history-table">
            <thead>
            <tr>
              <th>#</th>
              <th>Vai trò</th>
              <th>Định dạng</th>
              <th>Trạng thái</th>
              <th>Số bản ghi</th>
              <th>Bộ lọc</th>
              <th>Thời gian xuất</th>
            </tr>
            </thead>
            <tbody id="exportHistoryBody">
            <c:forEach var="item" items="${exportHistory}">
              <tr class="export-history-row"
                  data-role="${item.createdRole}"
                  data-format="${item.format}"
                  data-status="${item.status}"
                  data-filter-summary="${item.filterSummary}"
                  data-created-at="${item.createdAt}">
                <td>${item.id}</td>
                <td>${item.createdRole}</td>
                <td><span class="format-badge">${item.format}</span></td>
                <td><span class="status-badge success">${item.status}</span></td>
                <td>${item.totalRows}</td>
                <td class="detail-text">${item.filterSummary}</td>
                <td>${item.createdAt}</td>
              </tr>
            </c:forEach>

            <c:if test="${empty exportHistory}">
              <tr>
                <td class="empty-message" colspan="7">Chưa có lịch sử xuất báo cáo nào của bạn.</td>
              </tr>
            </c:if>
            <tr id="exportHistoryEmptyHintRow" hidden>
              <td class="empty-message" colspan="7">Không tìm thấy lịch sử xuất báo cáo phù hợp.</td>
            </tr>
            </tbody>
          </table>
        </div>
        <div class="history-pagination" id="exportHistoryPagination" hidden></div>
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

      requestAnimationFrame(function () {
        const btnRect = button.getBoundingClientRect();
        const menuRect = menu.getBoundingClientRect();
        const margin = 8;

        let left = btnRect.right - menuRect.width;
        left = Math.max(margin, Math.min(left, window.innerWidth - menuRect.width - margin));

        let top;
        const spaceBelow = window.innerHeight - btnRect.bottom - margin;
        const spaceAbove = btnRect.top - margin;
        if (spaceBelow >= menuRect.height || spaceBelow >= spaceAbove) {
          top = btnRect.bottom + margin;
        } else {
          top = btnRect.top - menuRect.height - margin;
        }
        top = Math.max(margin, Math.min(top, window.innerHeight - menuRect.height - margin));

        menu.style.left = left + 'px';
        menu.style.top = top + 'px';
      });
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
    const studentSuggestBox = document.getElementById('conductStudentSuggestBox');
    let suggestItems = [];
    let suggestIndex = -1;
    let suggestTimer = null;
    const activitySearchInput = document.getElementById('activitySearchInput');
    const activityRoleFilter = document.getElementById('activityRoleFilter');
    const activityDateFilter = document.getElementById('activityDateFilter');
    const activityMonthFilter = document.getElementById('activityMonthFilter');
    const activityYearFilter = document.getElementById('activityYearFilter');
    const activityApplyFilterButton = document.getElementById('activityApplyFilterButton');
    const activityItems = Array.from(document.querySelectorAll('.activity-item'));
    const activityEmptyHint = document.getElementById('activityEmptyHint');
    const activityPagination = document.getElementById('activityPagination');
    const exportHistorySearchInput = document.getElementById('exportHistorySearchInput');
    const exportHistoryRoleFilter = document.getElementById('exportHistoryRoleFilter');
    const exportHistoryDateFilter = document.getElementById('exportHistoryDateFilter');
    const exportHistoryMonthFilter = document.getElementById('exportHistoryMonthFilter');
    const exportHistoryYearFilter = document.getElementById('exportHistoryYearFilter');
    const exportHistoryApplyFilterButton = document.getElementById('exportHistoryApplyFilterButton');
    const exportHistoryRows = Array.from(document.querySelectorAll('.export-history-row'));
    const exportHistoryEmptyHintRow = document.getElementById('exportHistoryEmptyHintRow');
    const exportHistoryPagination = document.getElementById('exportHistoryPagination');

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
      if (!student || !student.idHocSinh || !searchInput) {
        return;
      }
      searchInput.value = student.idHocSinh;
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
        metaOne.textContent = (student.idHocSinh || '-') + ' - ' + (student.tenLop || '-');

        const metaTwo = document.createElement('span');
        const gradeText = student.khoi ? ('Khối ' + student.khoi) : 'Khối -';
        metaTwo.textContent = gradeText + ' - ' + (student.khoaHoc || '-');

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

      fetch('<c:url value="/teacher/conduct/suggest-students"/>' + '?' + params.toString(), {
        headers: { 'Accept': 'application/json' }
      })
        .then(response => response.ok ? response.json() : [])
        .then(data => renderStudentSuggest(data))
        .catch(() => hideStudentSuggestBox());
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
    function normalizeText(value) {
      return (value || '')
        .toLowerCase()
        .normalize('NFD')
        .replace(/[\u0300-\u036f]/g, '')
        .replace(/đ/g, 'd')
        .replace(/[^a-z0-9/\s:-]/g, ' ')
        .replace(/\s+/g, ' ')
        .trim();
    }

    function parseDisplayDateParts(value) {
      const match = (value || '').match(/(\d{2})\/(\d{2})\/(\d{4})/);
      if (!match) {
        return null;
      }
      return {
        day: match[1],
        month: match[2],
        year: match[3],
        iso: match[3] + '-' + match[2] + '-' + match[1],
        monthKey: match[3] + '-' + match[2]
      };
    }

    function createPaginationState(itemsPerPage) {
      return {
        page: 1,
        pageSize: itemsPerPage
      };
    }

    function renderPagination(container, totalItems, state, onChange) {
      if (!container) {
        return;
      }
      const totalPages = Math.max(1, Math.ceil(totalItems / state.pageSize));
      state.page = Math.min(Math.max(1, state.page), totalPages);

      container.innerHTML = '';
      container.hidden = totalItems <= state.pageSize;
      if (container.hidden) {
        return;
      }

      function createButton(label, page, disabled, active) {
        const button = document.createElement('button');
        button.type = 'button';
        button.className = 'page-btn' + (active ? ' active' : '') + (disabled ? ' disabled' : '');
        button.textContent = label;
        button.disabled = !!disabled;
        if (!disabled) {
          button.addEventListener('click', function () {
            state.page = page;
            onChange();
          });
        }
        container.appendChild(button);
      }

      createButton('‹', state.page - 1, state.page <= 1, false);
      for (let page = 1; page <= totalPages; page++) {
        createButton(String(page), page, false, page === state.page);
      }
      createButton('›', state.page + 1, state.page >= totalPages, false);
    }

    function bindPagedFilter(config) {
      const state = createPaginationState(4);

      function apply() {
        const filteredItems = config.items.filter(function (item) {
          return config.matches(item);
        });

        const totalPages = Math.max(1, Math.ceil(filteredItems.length / state.pageSize));
        state.page = Math.min(state.page, totalPages);
        const start = (state.page - 1) * state.pageSize;
        const end = start + state.pageSize;
        const visibleItems = filteredItems.slice(start, end);

        config.items.forEach(function (item) {
          item.hidden = visibleItems.indexOf(item) === -1;
        });

        if (config.emptyHint) {
          config.emptyHint.hidden = filteredItems.length > 0;
        }

        renderPagination(config.pagination, filteredItems.length, state, apply);
      }

      config.triggers.forEach(function (trigger) {
        if (!trigger) {
          return;
        }
        const eventName = trigger.tagName === 'BUTTON' ? 'click' : 'input';
        trigger.addEventListener(eventName, function () {
          state.page = 1;
          apply();
        });
        if (trigger.tagName === 'SELECT' || (trigger.tagName === 'INPUT' && (trigger.type === 'date' || trigger.type === 'month' || trigger.type === 'number'))) {
          trigger.addEventListener('change', function () {
            state.page = 1;
            apply();
          });
        }
      });

      apply();
    }

    if (activityItems.length) {
      bindPagedFilter({
        items: activityItems,
        emptyHint: activityEmptyHint,
        pagination: activityPagination,
        triggers: [
          activitySearchInput,
          activityRoleFilter,
          activityDateFilter,
          activityMonthFilter,
          activityYearFilter,
          activityApplyFilterButton
        ],
        matches: function (item) {
          const keyword = normalizeText(activitySearchInput ? activitySearchInput.value : '');
          const roleValue = normalizeText(activityRoleFilter ? activityRoleFilter.value : '');
          const yearValue = (activityYearFilter ? activityYearFilter.value : '').trim();
          const dateParts = parseDisplayDateParts(item.dataset.actionTime || '');
          const actorName = normalizeText(item.dataset.actorName);
          const actorRole = normalizeText(item.dataset.actorRole);
          const actionDetail = normalizeText(item.dataset.actionDetail);
          const actionTime = normalizeText(item.dataset.actionTime);

          if (keyword && !(actorName.includes(keyword) || actorRole.includes(keyword) || actionDetail.includes(keyword) || actionTime.includes(keyword))) {
            return false;
          }
          if (roleValue && actorRole !== roleValue) {
            return false;
          }
          if (activityDateFilter && activityDateFilter.value && (!dateParts || dateParts.iso !== activityDateFilter.value)) {
            return false;
          }
          if (activityMonthFilter && activityMonthFilter.value && (!dateParts || dateParts.monthKey !== activityMonthFilter.value)) {
            return false;
          }
          if (yearValue && (!dateParts || dateParts.year !== yearValue)) {
            return false;
          }
          return true;
        }
      });
    }

    if (exportHistoryRows.length) {
      bindPagedFilter({
        items: exportHistoryRows,
        emptyHint: exportHistoryEmptyHintRow,
        pagination: exportHistoryPagination,
        triggers: [
          exportHistorySearchInput,
          exportHistoryRoleFilter,
          exportHistoryDateFilter,
          exportHistoryMonthFilter,
          exportHistoryYearFilter,
          exportHistoryApplyFilterButton
        ],
        matches: function (row) {
          const keyword = normalizeText(exportHistorySearchInput ? exportHistorySearchInput.value : '');
          const roleValue = normalizeText(exportHistoryRoleFilter ? exportHistoryRoleFilter.value : '');
          const yearValue = (exportHistoryYearFilter ? exportHistoryYearFilter.value : '').trim();
          const dateParts = parseDisplayDateParts(row.dataset.createdAt || '');
          const role = normalizeText(row.dataset.role);
          const format = normalizeText(row.dataset.format);
          const status = normalizeText(row.dataset.status);
          const filterSummary = normalizeText(row.dataset.filterSummary);
          const createdAt = normalizeText(row.dataset.createdAt);

          if (keyword && !(role.includes(keyword) || format.includes(keyword) || status.includes(keyword) || filterSummary.includes(keyword) || createdAt.includes(keyword))) {
            return false;
          }
          if (roleValue && role !== roleValue) {
            return false;
          }
          if (exportHistoryDateFilter && exportHistoryDateFilter.value && (!dateParts || dateParts.iso !== exportHistoryDateFilter.value)) {
            return false;
          }
          if (exportHistoryMonthFilter && exportHistoryMonthFilter.value && (!dateParts || dateParts.monthKey !== exportHistoryMonthFilter.value)) {
            return false;
          }
          if (yearValue && (!dateParts || dateParts.year !== yearValue)) {
            return false;
          }
          return true;
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


