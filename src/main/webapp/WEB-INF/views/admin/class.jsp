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
  <link rel="stylesheet" href="<c:url value='/css/class-list.css'/>">
</head>
<body>
<div class="layout">
  <jsp:include page="/WEB-INF/views/admin/_sidebar.jsp"/>

  <main class="main class-list-page">
    <header class="class-header">
      <div class="header-left">
        <h1>Quản lý lớp học</h1>
        <div class="breadcrumbs">
          <a href="<c:url value='/admin/dashboard'/>">Trang chủ</a>
          <span>/</span>
          <span>Lớp học</span>
        </div>
      </div>
      <div class="header-right">
        <button class="btn primary" type="button" title="Chức năng đang phát triển">
          + Thêm lớp học mới
        </button>
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
          <div class="stats-icon icon-blue" aria-hidden="true">
            <svg viewBox="0 0 24 24" fill="none">
              <path d="M5 21V7a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2v14" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              <path d="M9 10h6M9 14h6M9 18h6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </div>
          <div>
            <p>Tổng số lớp</p>
            <h3><fmt:formatNumber value="${stats.totalClasses}" groupingUsed="true"/></h3>
          </div>
        </article>

        <article class="stats-card">
          <div class="stats-icon icon-green" aria-hidden="true">
            <svg viewBox="0 0 24 24" fill="none">
              <circle cx="8" cy="8" r="3" stroke="currentColor" stroke-width="2"/>
              <circle cx="16" cy="9" r="3" stroke="currentColor" stroke-width="2"/>
              <path d="M3 19c0-2.2 2.2-4 5-4s5 1.8 5 4" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              <path d="M13 19c.3-1.8 2.1-3.2 4.4-3.2 2.5 0 4.6 1.6 4.6 3.6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </div>
          <div>
            <p>Tổng số học sinh</p>
            <h3><fmt:formatNumber value="${stats.totalStudents}" groupingUsed="true"/></h3>
          </div>
        </article>

        <article class="stats-card">
          <div class="stats-icon icon-violet" aria-hidden="true">
            <svg viewBox="0 0 24 24" fill="none">
              <circle cx="12" cy="8" r="4" stroke="currentColor" stroke-width="2"/>
              <path d="M5 20c0-3 3.2-5 7-5s7 2 7 5" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </div>
          <div>
            <p>Số giáo viên chủ nhiệm</p>
            <h3><fmt:formatNumber value="${stats.totalHomeroomTeachers}" groupingUsed="true"/></h3>
          </div>
        </article>
      </section>

      <section class="card filter-card">
        <form class="filters" method="get" action="<c:url value='/admin/class'/>" autocomplete="off">
          <div class="filter-item search-item">
            <label for="q">Tìm kiếm</label>
            <input id="q" type="text" name="q" value="${search.q}" placeholder="Nhập tên lớp...">
          </div>

          <div class="filter-item">
            <label for="khoi">Khối</label>
            <select id="khoi" name="khoi">
              <option value="">Tất cả khối</option>
              <c:forEach var="grade" items="${grades}">
                <option value="${grade}" ${search.khoi == grade ? 'selected' : ''}>Khối ${grade}</option>
              </c:forEach>
            </select>
          </div>

          <div class="filter-item">
            <label for="khoa">Khóa học</label>
            <select id="khoa" name="khoa">
              <option value="">Tất cả khóa</option>
              <c:forEach var="course" items="${courses}">
                <option value="${course.id}" ${search.khoa == course.id ? 'selected' : ''}>${course.name}</option>
              </c:forEach>
            </select>
          </div>

          <div class="filter-actions">
            <button class="btn filter-btn" type="submit">Lọc dữ liệu</button>
          </div>
        </form>
      </section>

      <section class="card table-card">
        <div class="table-wrap">
          <table class="table">
            <thead>
            <tr>
              <th>Tên lớp</th>
              <th>Khối</th>
              <th>Khóa học</th>
              <th>GV chủ nhiệm</th>
              <th>Sĩ số</th>
              <th>Năm học</th>
              <th class="th-actions">Thao tác</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="item" items="${classes}">
              <tr>
                <td><span class="class-name">${item.tenLop}</span></td>
                <td><span class="grade-badge">Khối ${item.khoi}</span></td>
                <td>${item.khoaHoc}</td>
                <td>
                  <c:choose>
                    <c:when test="${item.gvcnTen != '-'}">
                      <div class="teacher-cell">
                        <span class="teacher-avatar">${item.gvcnInitials}</span>
                        <div class="teacher-meta">
                          <span class="teacher-name">${item.gvcnTen}</span>
                          <span class="teacher-email">${item.gvcnEmail}</span>
                        </div>
                      </div>
                    </c:when>
                    <c:otherwise>
                      <span class="muted-value">Chưa phân công</span>
                    </c:otherwise>
                  </c:choose>
                </td>
                <td><span class="size-badge">${item.siSo}</span></td>
                <td>${item.namHoc}</td>
                <td class="actions">
                  <div class="action-inline">
                    <button class="icon-btn" type="button" title="Xem chi tiết lớp (đang phát triển)" aria-label="Xem chi tiết lớp">
                      <svg viewBox="0 0 24 24" fill="none">
                        <path d="M2 12s3.5-6 10-6 10 6 10 6-3.5 6-10 6S2 12 2 12Z" stroke="currentColor" stroke-width="1.8"/>
                        <circle cx="12" cy="12" r="2.8" stroke="currentColor" stroke-width="1.8"/>
                      </svg>
                    </button>
                    <button class="icon-btn" type="button" title="Chỉnh sửa lớp (đang phát triển)" aria-label="Chỉnh sửa lớp">
                      <svg viewBox="0 0 24 24" fill="none">
                        <path d="M4 20h4l10-10-4-4L4 16v4Z" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"/>
                        <path d="M13 7l4 4" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
                      </svg>
                    </button>
                    <button class="icon-btn danger" type="button" title="Xóa lớp (đang phát triển)" aria-label="Xóa lớp">
                      <svg viewBox="0 0 24 24" fill="none">
                        <path d="M4 7h16M10 3h4M7 7v13h10V7" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
                        <path d="M10 11v6M14 11v6" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
                      </svg>
                    </button>
                  </div>
                </td>
              </tr>
            </c:forEach>

            <c:if test="${empty classes}">
              <tr>
                <td class="empty-message" colspan="7">Không có lớp học phù hợp với bộ lọc.</td>
              </tr>
            </c:if>
            </tbody>
          </table>
        </div>

        <div class="table-footer">
          <div class="table-count">
            Hiển thị ${pageData.fromRecord}-${pageData.toRecord} trên tổng số ${pageData.totalItems} lớp học
          </div>

          <div class="pagination">
            <c:url var="prevUrl" value="/admin/class">
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
              <c:url var="pageUrl" value="/admin/class">
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
              </c:url>
              <a class="page-btn ${pageData.page == p ? 'active' : ''}" href="${pageUrl}">${p}</a>
            </c:forEach>

            <c:url var="nextUrl" value="/admin/class">
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
</body>
</html>
