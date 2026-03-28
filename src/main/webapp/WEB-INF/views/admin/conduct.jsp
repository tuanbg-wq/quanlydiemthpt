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
  <link rel="stylesheet" href="<c:url value='/css/conduct-list.css'/>">
</head>
<body>
<div class="layout">
  <jsp:include page="/WEB-INF/views/admin/_sidebar.jsp"/>

  <main class="main conduct-page">
    <header class="conduct-header">
      <div class="header-left">
        <h1>Khen thưởng / Kỷ luật</h1>
        <p>Theo dõi rèn luyện học sinh với tông màu đồng bộ trang điểm.</p>
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
               data-discipline-rate="${stats.disciplineRateValue}"
               aria-label="Tỉ lệ khen thưởng ${stats.rewardRateDisplay}, kỷ luật ${stats.disciplineRateDisplay}">
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
            <input id="q" type="text" name="q" value="${search.q}" placeholder="Nhập tên hoặc mã học sinh...">
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

          <div class="filter-actions">
            <div class="export-actions" role="group" aria-label="Xuất báo cáo">
              <button class="btn filter-btn export-btn export-btn-excel"
                      type="submit"
                      formaction="<c:url value='/admin/conduct/export/excel'/>"
                      formmethod="get">
                Excel
              </button>
              <button class="btn filter-btn export-btn export-btn-pdf"
                      type="submit"
                      formaction="<c:url value='/admin/conduct/export/pdf'/>"
                      formmethod="get">
                PDF
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
            <button type="button" class="btn btn-khen" disabled>+ Thêm khen thưởng</button>
            <button type="button" class="btn btn-ky-luat" disabled>+ Thêm kỷ luật</button>
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
              <th>Nội dung chi tiết</th>
              <th>Ngày quyết định</th>
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
                <td class="detail-text">${item.noiDungChiTiet}</td>
                <td>${item.ngayQuyetDinh}</td>
                <td class="actions">
                  <c:url var="studentInfoUrl" value="/admin/student/${item.idHocSinh}/info"/>
                  <a class="action-link" href="${studentInfoUrl}" aria-label="Xem thông tin học sinh">⋮</a>
                </td>
              </tr>
            </c:forEach>

            <c:if test="${empty records}">
              <tr>
                <td class="empty-message" colspan="7">Chưa có dữ liệu phù hợp với bộ lọc hiện tại.</td>
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
              <c:if test="${not empty search.q}">
                <c:param name="q" value="${search.q}"/>
              </c:if>
              <c:if test="${not empty search.khoi}">
                <c:param name="khoi" value="${search.khoi}"/>
              </c:if>
              <c:if test="${not empty search.khoa}">
                <c:param name="khoa" value="${search.khoa}"/>
              </c:if>
              <c:if test="${not empty search.lop}">
                <c:param name="lop" value="${search.lop}"/>
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
              <c:url var="pageUrl" value="/admin/conduct">
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
                <c:if test="${not empty search.lop}">
                  <c:param name="lop" value="${search.lop}"/>
                </c:if>
              </c:url>
              <a class="page-btn ${pageData.page == p ? 'active' : ''}" href="${pageUrl}">${p}</a>
            </c:forEach>

            <c:url var="nextUrl" value="/admin/conduct">
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
              <c:if test="${not empty search.lop}">
                <c:param name="lop" value="${search.lop}"/>
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

<script>
  (function () {
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
