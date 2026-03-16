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
  <link rel="stylesheet" href="<c:url value='/css/score-list.css'/>">
</head>
<body>
<div class="layout">
  <jsp:include page="/WEB-INF/views/admin/_sidebar.jsp"/>

  <main class="main score-list-page">
    <header class="score-header">
      <div class="header-left">
        <h1>Quản lý điểm số</h1>
        <p>Tổng hợp điểm theo học sinh, môn học, học kỳ và năm học.</p>
      </div>
      <div class="header-right">
        <a class="btn primary" href="<c:url value='/admin/score/create'/>">+ Thêm điểm số</a>
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
              <circle cx="9" cy="8" r="3.3" stroke="currentColor" stroke-width="2"/>
              <path d="M3.8 18.2c0-2.5 2.4-4.6 5.3-4.6s5.3 2.1 5.3 4.6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              <path d="M15.5 7h4.7M17.9 4.6v4.8" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </div>
          <div>
            <p>Tổng học sinh có điểm</p>
            <h3><fmt:formatNumber value="${stats.totalStudentsWithScores}" groupingUsed="true"/></h3>
          </div>
        </article>

        <article class="stats-card">
          <div class="stats-icon icon-orange" aria-hidden="true">
            <svg viewBox="0 0 24 24" fill="none">
              <path d="M4 16l5-5 4 4 7-7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M18 8h2v2" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </div>
          <div>
            <p>Điểm trung bình toàn trường</p>
            <h3>${stats.schoolAverageDisplay}</h3>
          </div>
        </article>

        <article class="stats-card">
          <div class="stats-icon icon-violet" aria-hidden="true">
            <svg viewBox="0 0 24 24" fill="none">
              <path d="M12 3 14.7 8.4 20.7 9.2 16.3 13.4 17.4 19.4 12 16.6 6.6 19.4 7.7 13.4 3.3 9.2 9.3 8.4 12 3Z" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"/>
            </svg>
          </div>
          <div>
            <p>Tỷ lệ khá giỏi</p>
            <h3>${stats.goodRateDisplay}</h3>
          </div>
        </article>
      </section>

      <section class="card filter-card">
        <form class="filters" method="get" action="<c:url value='/admin/score'/>" autocomplete="off">
          <div class="filter-item search-item">
            <label for="q">Tìm kiếm</label>
            <input id="q" type="text" name="q" value="${search.q}" placeholder="Nhập mã học sinh, tên học sinh hoặc môn học...">
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
            <label for="lop">Lớp</label>
            <select id="lop" name="lop">
              <option value="">Tất cả lớp</option>
              <c:forEach var="item" items="${classOptions}">
                <option value="${item.id}" ${search.lop == item.id ? 'selected' : ''}>${item.name}</option>
              </c:forEach>
            </select>
          </div>

          <div class="filter-item">
            <label for="mon">Môn học</label>
            <select id="mon" name="mon">
              <option value="">Tất cả môn</option>
              <c:forEach var="item" items="${subjectOptions}">
                <option value="${item.id}" ${search.mon == item.id ? 'selected' : ''}>${item.name}</option>
              </c:forEach>
            </select>
          </div>

          <div class="filter-item">
            <label for="hocKy">Học kỳ</label>
            <select id="hocKy" name="hocKy">
              <option value="">Tất cả học kỳ</option>
              <option value="1" ${search.hocKy == '1' ? 'selected' : ''}>Học kỳ 1</option>
              <option value="2" ${search.hocKy == '2' ? 'selected' : ''}>Học kỳ 2</option>
            </select>
          </div>

          <div class="filter-item">
            <label for="khoa">Khóa học</label>
            <select id="khoa" name="khoa">
              <option value="">Tất cả khóa học</option>
              <c:forEach var="item" items="${courseOptions}">
                <option value="${item.id}" ${search.khoa == item.id ? 'selected' : ''}>${item.name}</option>
              </c:forEach>
            </select>
          </div>

          <div class="filter-actions">
            <button class="btn filter-btn" type="submit">Tìm</button>
          </div>
        </form>
      </section>

      <section class="card table-card">
        <div class="table-wrap">
          <table class="table">
            <thead>
            <tr>
              <th>Mã học sinh</th>
              <th>Tên học sinh</th>
              <th>Lớp</th>
              <th>Môn</th>
              <th>Miệng</th>
              <th>15 phút</th>
              <th>1 tiết</th>
              <th>Giữa kỳ</th>
              <th>Cuối kỳ</th>
              <th>Tổng kết</th>
              <th>Hạnh kiểm</th>
              <th>Học kỳ</th>
              <th>Năm học</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="item" items="${scores}">
              <tr>
                <td>${item.idHocSinh}</td>
                <td class="student-name">${item.tenHocSinh}</td>
                <td>${item.tenLop}</td>
                <td>${item.tenMon}</td>
                <td>${item.diemMiengDisplay}</td>
                <td>${item.diem15PhutDisplay}</td>
                <td>${item.diem1TietDisplay}</td>
                <td>${item.diemGiuaKyDisplay}</td>
                <td>${item.diemCuoiKyDisplay}</td>
                <td>
                  <span class="total-badge">${item.tongKetDisplay}</span>
                </td>
                <td>
                  <span class="conduct-badge ${item.hanhKiemBadgeClass}">${item.hanhKiem}</span>
                </td>
                <td>${item.hocKyDisplay}</td>
                <td>${item.namHocDisplay}</td>
              </tr>
            </c:forEach>

            <c:if test="${empty scores}">
              <tr>
                <td class="empty-message" colspan="13">Chưa có dữ liệu điểm phù hợp với bộ lọc.</td>
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
            <c:url var="prevUrl" value="/admin/score">
              <c:param name="page" value="${pageData.page - 1}"/>
              <c:if test="${not empty search.q}">
                <c:param name="q" value="${search.q}"/>
              </c:if>
              <c:if test="${not empty search.khoi}">
                <c:param name="khoi" value="${search.khoi}"/>
              </c:if>
              <c:if test="${not empty search.lop}">
                <c:param name="lop" value="${search.lop}"/>
              </c:if>
              <c:if test="${not empty search.mon}">
                <c:param name="mon" value="${search.mon}"/>
              </c:if>
              <c:if test="${not empty search.hocKy}">
                <c:param name="hocKy" value="${search.hocKy}"/>
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
              <c:url var="pageUrl" value="/admin/score">
                <c:param name="page" value="${p}"/>
                <c:if test="${not empty search.q}">
                  <c:param name="q" value="${search.q}"/>
                </c:if>
                <c:if test="${not empty search.khoi}">
                  <c:param name="khoi" value="${search.khoi}"/>
                </c:if>
                <c:if test="${not empty search.lop}">
                  <c:param name="lop" value="${search.lop}"/>
                </c:if>
                <c:if test="${not empty search.mon}">
                  <c:param name="mon" value="${search.mon}"/>
                </c:if>
                <c:if test="${not empty search.hocKy}">
                  <c:param name="hocKy" value="${search.hocKy}"/>
                </c:if>
                <c:if test="${not empty search.khoa}">
                  <c:param name="khoa" value="${search.khoa}"/>
                </c:if>
              </c:url>
              <a class="page-btn ${pageData.page == p ? 'active' : ''}" href="${pageUrl}">${p}</a>
            </c:forEach>

            <c:url var="nextUrl" value="/admin/score">
              <c:param name="page" value="${pageData.page + 1}"/>
              <c:if test="${not empty search.q}">
                <c:param name="q" value="${search.q}"/>
              </c:if>
              <c:if test="${not empty search.khoi}">
                <c:param name="khoi" value="${search.khoi}"/>
              </c:if>
              <c:if test="${not empty search.lop}">
                <c:param name="lop" value="${search.lop}"/>
              </c:if>
              <c:if test="${not empty search.mon}">
                <c:param name="mon" value="${search.mon}"/>
              </c:if>
              <c:if test="${not empty search.hocKy}">
                <c:param name="hocKy" value="${search.hocKy}"/>
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
