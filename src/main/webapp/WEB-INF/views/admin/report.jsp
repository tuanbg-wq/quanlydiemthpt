<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>${pageTitle}</title>

  <link rel="stylesheet" href="<c:url value='/css/admin-layout.css'/>">
  <link rel="stylesheet" href="<c:url value='/css/admin/report.css'/>">
</head>
<body>
<div class="layout">
  <jsp:include page="/WEB-INF/views/admin/_sidebar.jsp"/>

  <main class="main admin-report-page">
    <header class="topbar">
      <div class="topbar-left">
        <h1>Báo cáo & thống kê</h1>
        <p>Chọn loại báo cáo, tùy chỉnh bộ lọc, xem trước dữ liệu và theo dõi lịch sử xuất.</p>
      </div>
    </header>

    <section class="content">
      <c:if test="${not empty flashMessage}">
        <div class="alert ${flashType == 'error' ? 'alert-error' : (flashType == 'info' ? 'alert-info' : 'alert-success')}">
          ${flashMessage}
        </div>
      </c:if>

      <section class="card report-type-card">
        <h2>Chọn loại báo cáo</h2>
        <div class="type-grid">
          <c:forEach var="item" items="${typeCards}">
            <c:url var="typeUrl" value="/admin/report">
              <c:param name="type" value="${item.code}"/>
            </c:url>
            <a class="type-item ${selectedType.code == item.code ? 'active' : ''}"
               href="${typeUrl}">
              <span class="report-icon ${item.icon}" aria-hidden="true"></span>
              <h3>${item.title}</h3>
              <p>${item.description}</p>
            </a>
          </c:forEach>
        </div>
      </section>

      <section class="card report-filter-card">
        <h2>Bộ lọc & tùy chỉnh</h2>
        <form class="report-filters" method="get" action="<c:url value='/admin/report'/>">
          <input type="hidden" name="type" value="${selectedType.code}">

          <c:choose>
            <c:when test="${selectedType.code == 'score'}">
              <div class="filter-item search-item">
                <label for="q">Tìm kiếm</label>
                <input id="q" type="text" name="q" value="${search.q}" placeholder="Nhập mã học sinh, tên học sinh hoặc môn học">
              </div>

              <div class="filter-item">
                <label for="namHoc">Năm học</label>
                <select id="namHoc" name="namHoc">
                  <c:forEach var="item" items="${filters.namHocOptions}">
                    <option value="${item.value}" ${search.namHoc == item.value ? 'selected' : ''}>${item.label}</option>
                  </c:forEach>
                </select>
              </div>

              <div class="filter-item">
                <label for="khoi">Khối</label>
                <select id="khoi" name="khoi">
                  <c:forEach var="item" items="${filters.khoiOptions}">
                    <option value="${item.value}" ${search.khoi == item.value ? 'selected' : ''}>${item.label}</option>
                  </c:forEach>
                </select>
              </div>

              <div class="filter-item">
                <label for="lop">Lớp</label>
                <select id="lop" name="lop">
                  <c:forEach var="item" items="${filters.lopOptions}">
                    <option value="${item.value}" ${search.lop == item.value ? 'selected' : ''}>${item.label}</option>
                  </c:forEach>
                </select>
              </div>

              <div class="filter-item">
                <label for="mon">Môn học</label>
                <select id="mon" name="mon">
                  <c:forEach var="item" items="${filters.monOptions}">
                    <option value="${item.value}" ${search.mon == item.value ? 'selected' : ''}>${item.label}</option>
                  </c:forEach>
                </select>
              </div>

              <div class="filter-item">
                <label for="hocKy">Học kỳ</label>
                <select id="hocKy" name="hocKy">
                  <c:forEach var="item" items="${filters.hocKyOptions}">
                    <option value="${item.value}" ${search.hocKy == item.value ? 'selected' : ''}>${item.label}</option>
                  </c:forEach>
                </select>
              </div>

              <div class="filter-item">
                <label for="khoa">Khóa học</label>
                <select id="khoa" name="khoa">
                  <c:forEach var="item" items="${filters.khoaOptions}">
                    <option value="${item.value}" ${search.khoa == item.value ? 'selected' : ''}>${item.label}</option>
                  </c:forEach>
                </select>
              </div>
            </c:when>

            <c:when test="${selectedType.code == 'reward_discipline'}">
              <div class="filter-item search-item">
                <label for="q">Tìm kiếm</label>
                <input id="q" type="text" name="q" value="${search.q}" placeholder="Nhập mã/tên học sinh hoặc nội dung quyết định">
              </div>

              <div class="filter-item">
                <label for="namHoc">Năm học</label>
                <select id="namHoc" name="namHoc">
                  <c:forEach var="item" items="${filters.namHocOptions}">
                    <option value="${item.value}" ${search.namHoc == item.value ? 'selected' : ''}>${item.label}</option>
                  </c:forEach>
                </select>
              </div>

              <div class="filter-item">
                <label for="hocKy">Học kỳ</label>
                <select id="hocKy" name="hocKy">
                  <c:forEach var="item" items="${filters.hocKyOptions}">
                    <option value="${item.value}" ${search.hocKy == item.value ? 'selected' : ''}>${item.label}</option>
                  </c:forEach>
                </select>
              </div>

              <div class="filter-item">
                <label for="khoi">Khối</label>
                <select id="khoi" name="khoi">
                  <c:forEach var="item" items="${filters.khoiOptions}">
                    <option value="${item.value}" ${search.khoi == item.value ? 'selected' : ''}>${item.label}</option>
                  </c:forEach>
                </select>
              </div>

              <div class="filter-item">
                <label for="lop">Lớp</label>
                <select id="lop" name="lop">
                  <c:forEach var="item" items="${filters.lopOptions}">
                    <option value="${item.value}" ${search.lop == item.value ? 'selected' : ''}>${item.label}</option>
                  </c:forEach>
                </select>
              </div>

              <div class="filter-item">
                <label for="loai">Loại quyết định</label>
                <select id="loai" name="loai">
                  <c:forEach var="item" items="${filters.loaiOptions}">
                    <option value="${item.value}" ${search.loai == item.value ? 'selected' : ''}>${item.label}</option>
                  </c:forEach>
                </select>
              </div>

              <div class="filter-item">
                <label for="khoa">Khóa học</label>
                <select id="khoa" name="khoa">
                  <c:forEach var="item" items="${filters.khoaOptions}">
                    <option value="${item.value}" ${search.khoa == item.value ? 'selected' : ''}>${item.label}</option>
                  </c:forEach>
                </select>
              </div>
            </c:when>

            <c:otherwise>
              <div class="filter-item search-item">
                <label for="q">Tìm kiếm</label>
                <input id="q" type="text" name="q" value="${search.q}" placeholder="Nhập mã giáo viên hoặc họ tên">
              </div>

              <div class="filter-item">
                <label for="boMon">Bộ môn</label>
                <select id="boMon" name="boMon">
                  <c:forEach var="item" items="${filters.boMonOptions}">
                    <option value="${item.value}" ${search.boMon == item.value ? 'selected' : ''}>${item.label}</option>
                  </c:forEach>
                </select>
              </div>

              <div class="filter-item">
                <label for="khoi">Khối</label>
                <select id="khoi" name="khoi">
                  <c:forEach var="item" items="${filters.khoiOptions}">
                    <option value="${item.value}" ${search.khoi == item.value ? 'selected' : ''}>${item.label}</option>
                  </c:forEach>
                </select>
              </div>

              <div class="filter-item">
                <label for="trangThai">Trạng thái</label>
                <select id="trangThai" name="trangThai">
                  <c:forEach var="item" items="${filters.trangThaiOptions}">
                    <option value="${item.value}" ${search.trangThai == item.value ? 'selected' : ''}>${item.label}</option>
                  </c:forEach>
                </select>
              </div>

              <div class="filter-item">
                <label for="vaiTro">Vai trò</label>
                <select id="vaiTro" name="vaiTro">
                  <c:forEach var="item" items="${filters.vaiTroOptions}">
                    <option value="${item.value}" ${search.vaiTro == item.value ? 'selected' : ''}>${item.label}</option>
                  </c:forEach>
                </select>
              </div>
            </c:otherwise>
          </c:choose>

          <div class="filter-actions">
            <button type="submit" class="btn preview-btn">Xem trước</button>

            <div class="create-report-group">
              <select name="format" class="export-format">
                <option value="PDF">PDF</option>
                <option value="XLSX">XLSX</option>
                <option value="CSV">CSV</option>
              </select>
              <button type="submit"
                      class="btn primary create-btn"
                      formaction="<c:url value='/admin/report/export'/>"
                      formmethod="post">
                Tạo báo cáo ngay
              </button>
            </div>
          </div>
        </form>
      </section>

      <section class="card preview-card">
        <h2>Bản xem trước dữ liệu</h2>

        <div class="metric-grid">
          <c:forEach var="metric" items="${preview.metrics}">
            <article class="metric-item ${metric.tone}">
              <span>${metric.label}</span>
              <strong>${metric.value}</strong>
            </article>
          </c:forEach>
        </div>

        <div class="table-wrap">
          <table class="table preview-table">
            <thead>
            <tr>
              <c:forEach var="header" items="${preview.headers}">
                <th>${header}</th>
              </c:forEach>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="row" items="${preview.rows}">
              <tr>
                <c:forEach var="cell" items="${row}">
                  <td>${cell}</td>
                </c:forEach>
              </tr>
            </c:forEach>

            <c:if test="${empty preview.rows}">
              <tr>
                <td class="empty" colspan="${preview.headers.size()}">${preview.emptyMessage}</td>
              </tr>
            </c:if>
            </tbody>
          </table>
        </div>

        <p class="preview-count">Tổng bản ghi phù hợp: ${preview.totalRows}</p>
      </section>

      <section class="card history-card">
        <h2>Lịch sử xuất báo cáo</h2>

        <div class="table-wrap">
          <table class="table history-table">
            <thead>
            <tr>
              <th>#</th>
              <th>Loại báo cáo</th>
              <th>Định dạng</th>
              <th>Trạng thái</th>
              <th>Người tạo</th>
              <th>Bản ghi</th>
              <th>Bộ lọc</th>
              <th>Thời gian</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="item" items="${exportHistory}">
              <tr>
                <td>${item.id}</td>
                <td>${item.reportType}</td>
                <td><span class="format-badge">${item.format}</span></td>
                <td><span class="status-badge success">${item.status}</span></td>
                <td>${item.createdBy}</td>
                <td>${item.totalRows}</td>
                <td>${item.filterSummary}</td>
                <td>${item.createdAt}</td>
              </tr>
            </c:forEach>

            <c:if test="${empty exportHistory}">
              <tr>
                <td class="empty" colspan="8">Chưa có lịch sử xuất báo cáo.</td>
              </tr>
            </c:if>
            </tbody>
          </table>
        </div>
      </section>
    </section>
  </main>
</div>
</body>
</html>
