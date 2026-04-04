<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>${pageTitle}</title>

  <link rel="stylesheet" href="<c:url value='/css/admin-layout.css'/>">
  <link rel="stylesheet" href="<c:url value='/css/admin/score-list.css'/>">
</head>
<body>
<div class="layout">
  <jsp:include page="/WEB-INF/views/admin/_sidebar.jsp"/>

  <main class="main score-list-page score-history-page">
    <header class="score-header">
      <div class="header-left">
        <h1>Lịch sử thao tác điểm</h1>
        <p>Theo dõi nhập, sửa, xóa điểm theo Admin, GVCN và GVBM.</p>
      </div>
      <div class="header-right history-header-actions">
        <a class="btn" href="<c:url value='/admin/score'/>">Quay lại quản lý điểm</a>
      </div>
    </header>

    <section class="content">
      <c:if test="${not empty flashMessage}">
        <div class="alert ${flashType == 'error' ? 'alert-error' : (flashType == 'info' ? 'alert-info' : 'alert-success')}">
          ${flashMessage}
        </div>
      </c:if>

      <section class="card filter-card history-filter-card">
        <form class="filters history-filters" method="get" action="<c:url value='/admin/score/history'/>" autocomplete="off">
          <div class="filter-item search-item">
            <label for="q">Tìm kiếm</label>
            <input id="q" type="text" name="q" value="${search.q}" placeholder="Nhập tên người thao tác, học sinh, môn học...">
          </div>

          <div class="filter-item">
            <label for="hanhDong">Hành động</label>
            <select id="hanhDong" name="hanhDong">
              <option value="">Tất cả hành động</option>
              <option value="THEM_DIEM" ${search.hanhDong == 'THEM_DIEM' ? 'selected' : ''}>Nhập điểm</option>
              <option value="SUA_DIEM" ${search.hanhDong == 'SUA_DIEM' ? 'selected' : ''}>Sửa điểm</option>
              <option value="XOA_DIEM" ${search.hanhDong == 'XOA_DIEM' ? 'selected' : ''}>Xóa điểm</option>
            </select>
          </div>

          <div class="filter-item">
            <label for="vaiTro">Vai trò</label>
            <select id="vaiTro" name="vaiTro">
              <option value="">Tất cả vai trò</option>
              <option value="ADMIN" ${search.vaiTro == 'ADMIN' ? 'selected' : ''}>Admin</option>
              <option value="GVCN" ${search.vaiTro == 'GVCN' ? 'selected' : ''}>GVCN</option>
              <option value="GVBM" ${search.vaiTro == 'GVBM' ? 'selected' : ''}>GVBM</option>
            </select>
          </div>

          <div class="filter-actions history-filter-actions">
            <a class="btn" href="<c:url value='/admin/score/history'/>">Xóa lọc</a>
            <button class="btn filter-btn action-btn-search" type="submit">Lọc lịch sử</button>
          </div>
        </form>
      </section>

      <section class="card history-summary-card">
        <div class="history-summary">
          <strong>${historyCount}</strong>
          <span>bản ghi phù hợp bộ lọc hiện tại</span>
        </div>
        <p>Trang này hiển thị tối đa ${historyLimit} thao tác điểm gần nhất để giữ tốc độ tải ổn định.</p>
      </section>

      <section class="card history-list-card">
        <div class="history-list">
          <c:forEach var="item" items="${historyItems}">
            <article class="history-item history-item-${item.actionKind}">
              <div class="history-item-top">
                <div class="history-item-meta">
                  <span class="history-role">${item.actorRole}</span>
                  <strong class="history-name">${item.actorName}</strong>
                  <span class="history-action-badge">${item.actionLabel}</span>
                </div>
                <span class="history-time">${item.actionTime}</span>
              </div>
              <p class="history-detail">${item.actionDetail}</p>
            </article>
          </c:forEach>

          <c:if test="${empty historyItems}">
            <div class="history-empty">
              Chưa có lịch sử nhập, sửa, xóa điểm phù hợp với bộ lọc hiện tại.
            </div>
          </c:if>
        </div>
      </section>
    </section>
  </main>
</div>
</body>
</html>
