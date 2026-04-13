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
        <p>Chọn loại báo cáo, bấm Lọc để xem trước dữ liệu và theo dõi lịch sử xuất.</p>
      </div>
    </header>

    <section class="content">
      <c:if test="${not empty sanitizedFlashMessage}">
        <div class="alert ${flashType == 'error' ? 'alert-error' : (flashType == 'info' ? 'alert-info' : 'alert-success')}">
          ${sanitizedFlashMessage}
        </div>
      </c:if>

      <section class="card report-type-card">
        <h2>Chọn loại báo cáo</h2>
        <div class="type-grid">
          <c:forEach var="item" items="${typeCards}">
            <c:url var="typeUrl" value="/admin/report">
              <c:param name="type" value="${item.code}"/>
            </c:url>
            <a class="type-item ${selectedType.code == item.code ? 'active' : ''}" href="${typeUrl}">
              <span class="report-icon ${item.icon}" aria-hidden="true"></span>
              <h3>${item.title}</h3>
              <p>${item.description}</p>
            </a>
          </c:forEach>
        </div>
      </section>

      <section class="card report-filter-card">
        <div class="filter-head">
          <h2>Bộ lọc & tùy chỉnh</h2>
          <p>Bấm nút Lọc để cập nhật bản xem trước dữ liệu.</p>
        </div>

        <form class="report-filters" method="get" action="<c:url value='/admin/report'/>">
          <input type="hidden" name="type" value="${selectedType.code}">
          <input type="hidden" name="historyType" value="${search.historyType}">
          <input type="hidden" name="historyFormat" value="${search.historyFormat}">
          <input type="hidden" name="historyTime" value="${search.historyTime}">
          <input type="hidden" name="historyRole" value="${search.historyRole}">
          <input type="hidden" name="historyDate" value="${search.historyDate}">
          <input type="hidden" name="historyMonth" value="${search.historyMonth}">
          <input type="hidden" name="historyYear" value="${search.historyYear}">
          <input type="hidden" name="previewPage" value="1">

          <c:choose>
            <c:when test="${selectedType.code == 'score'}">
              <div class="filter-item search-item">
                <label for="q">Tìm kiếm</label>
                <input id="q" type="text" name="q" value="${search.q}" placeholder="Nhập mã học sinh, tên học sinh hoặc môn học...">
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

            <c:when test="${selectedType.code == 'student_list'}">
              <div class="filter-item search-item">
                <label for="q">Tìm kiếm</label>
                <input id="q" type="text" name="q" value="${search.q}" placeholder="Tìm theo mã HS, tên, email...">
              </div>

              <div class="filter-item">
                <label for="khoa">Khóa học</label>
                <select id="khoa" name="khoa">
                  <c:forEach var="item" items="${filters.khoaOptions}">
                    <option value="${item.value}" ${search.khoa == item.value ? 'selected' : ''}>${item.label}</option>
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
                <label for="hanhKiem">Hạnh kiểm</label>
                <select id="hanhKiem" name="hanhKiem">
                  <c:forEach var="item" items="${filters.loaiOptions}">
                    <option value="${item.value}" ${search.hanhKiem == item.value ? 'selected' : ''}>${item.label}</option>
                  </c:forEach>
                </select>
              </div>

              <div class="filter-item">
                <label for="lichSuChuyen">Lịch sử chuyển</label>
                <select id="lichSuChuyen" name="lichSuChuyen">
                  <c:forEach var="item" items="${filters.trangThaiOptions}">
                    <option value="${item.value}" ${search.lichSuChuyen == item.value ? 'selected' : ''}>${item.label}</option>
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
            <button type="submit" name="applyPreview" value="1" class="btn primary filter-inline-btn">Lọc</button>
            <div class="create-report-group">
              <select name="format" class="export-format">
                <option value="PDF" ${empty param.format or param.format == 'PDF' ? 'selected' : ''}>PDF</option>
                <option value="XLSX" ${param.format == 'XLSX' ? 'selected' : ''}>Excel</option>
              </select>
              <button type="submit"
                      name="applyPreview"
                      value="${search.applyPreview}"
                      class="btn primary create-btn"
                      formaction="<c:url value='/admin/report/export'/>"
                      formmethod="post">
                Tạo báo cáo ngay
              </button>
            </div>
          </div>
        </form>
      </section>

      <c:choose>
        <c:when test="${previewVisible}">
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
                  <c:forEach var="columnHeader" items="${preview.headers}">
                    <th>${columnHeader}</th>
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

            <c:if test="${not empty preview.rows and totalPreviewPages > 1}">
              <form class="preview-pagination" method="get" action="<c:url value='/admin/report'/>">
                <input type="hidden" name="type" value="${selectedType.code}">
                <input type="hidden" name="q" value="${search.q}">
                <input type="hidden" name="namHoc" value="${search.namHoc}">
                <input type="hidden" name="hocKy" value="${search.hocKy}">
                <input type="hidden" name="khoi" value="${search.khoi}">
                <input type="hidden" name="lop" value="${search.lop}">
                <input type="hidden" name="mon" value="${search.mon}">
                <input type="hidden" name="khoa" value="${search.khoa}">
                <input type="hidden" name="loai" value="${search.loai}">
                <input type="hidden" name="boMon" value="${search.boMon}">
                <input type="hidden" name="trangThai" value="${search.trangThai}">
                <input type="hidden" name="vaiTro" value="${search.vaiTro}">
                <input type="hidden" name="hanhKiem" value="${search.hanhKiem}">
                <input type="hidden" name="lichSuChuyen" value="${search.lichSuChuyen}">
                <input type="hidden" name="applyPreview" value="1">
                <input type="hidden" name="historyType" value="${search.historyType}">
                <input type="hidden" name="historyFormat" value="${search.historyFormat}">
                <input type="hidden" name="historyTime" value="${search.historyTime}">
                <input type="hidden" name="historyRole" value="${search.historyRole}">
                <input type="hidden" name="historyDate" value="${search.historyDate}">
                <input type="hidden" name="historyMonth" value="${search.historyMonth}">
                <input type="hidden" name="historyYear" value="${search.historyYear}">

                <button type="submit"
                        class="btn secondary"
                        name="previewPage"
                        value="${previewPage - 1}"
                        ${previewPage <= 1 ? 'disabled' : ''}>
                  Trang trước
                </button>
                <span class="preview-page-indicator">Trang ${previewPage}/${totalPreviewPages}</span>
                <button type="submit"
                        class="btn secondary"
                        name="previewPage"
                        value="${previewPage + 1}"
                        ${previewPage >= totalPreviewPages ? 'disabled' : ''}>
                  Trang sau
                </button>
              </form>
            </c:if>
          </section>
        </c:when>
        <c:otherwise>
          <section class="card preview-card preview-placeholder">
            <h2>Bản xem trước dữ liệu</h2>
            <p>Chưa hiển thị dữ liệu. Vui lòng nhấn <strong>Lọc</strong> để xem bản xem trước theo bộ lọc hiện tại.</p>
          </section>
        </c:otherwise>
      </c:choose>

      <section class="card history-card">
        <h2>Lịch sử xuất báo cáo</h2>

        <form class="history-filters" method="get" action="<c:url value='/admin/report'/>">
          <input type="hidden" name="type" value="${selectedType.code}">
          <input type="hidden" name="q" value="${search.q}">
          <input type="hidden" name="namHoc" value="${search.namHoc}">
          <input type="hidden" name="hocKy" value="${search.hocKy}">
          <input type="hidden" name="khoi" value="${search.khoi}">
          <input type="hidden" name="lop" value="${search.lop}">
          <input type="hidden" name="mon" value="${search.mon}">
          <input type="hidden" name="khoa" value="${search.khoa}">
          <input type="hidden" name="loai" value="${search.loai}">
          <input type="hidden" name="boMon" value="${search.boMon}">
          <input type="hidden" name="trangThai" value="${search.trangThai}">
          <input type="hidden" name="vaiTro" value="${search.vaiTro}">
          <input type="hidden" name="hanhKiem" value="${search.hanhKiem}">
          <input type="hidden" name="lichSuChuyen" value="${search.lichSuChuyen}">
          <input type="hidden" name="applyPreview" value="${search.applyPreview}">
          <input type="hidden" name="previewPage" value="${search.previewPage}">

          <div class="filter-item">
            <label for="historyType">Loại báo cáo</label>
            <select id="historyType" name="historyType">
              <c:forEach var="item" items="${historyTypeOptions}">
                <option value="${item.value}" ${search.historyType == item.value ? 'selected' : ''}>${item.label}</option>
              </c:forEach>
            </select>
          </div>

          <div class="filter-item">
            <label for="historyFormat">Định dạng</label>
            <select id="historyFormat" name="historyFormat">
              <c:forEach var="item" items="${historyFormatOptions}">
                <option value="${item.value}" ${search.historyFormat == item.value ? 'selected' : ''}>${item.label}</option>
              </c:forEach>
            </select>
          </div>

          <div class="filter-item">
            <label for="historyTime">Thời gian nhanh</label>
            <select id="historyTime" name="historyTime">
              <c:forEach var="item" items="${historyTimeOptions}">
                <option value="${item.value}" ${search.historyTime == item.value ? 'selected' : ''}>${item.label}</option>
              </c:forEach>
            </select>
          </div>

          <div class="filter-item">
            <label for="historyRole">Vai trò xuất</label>
            <select id="historyRole" name="historyRole">
              <c:forEach var="item" items="${historyRoleOptions}">
                <option value="${item.value}" ${search.historyRole == item.value ? 'selected' : ''}>${item.label}</option>
              </c:forEach>
            </select>
          </div>

          <div class="filter-item">
            <label for="historyDate">Ngày</label>
            <input id="historyDate" name="historyDate" type="date" value="${search.historyDate}">
          </div>

          <div class="filter-item">
            <label for="historyMonth">Tháng</label>
            <input id="historyMonth" name="historyMonth" type="month" value="${search.historyMonth}">
          </div>

          <div class="filter-item">
            <label for="historyYear">Năm</label>
            <input id="historyYear" name="historyYear" type="number" min="2000" max="2100" step="1" value="${search.historyYear}" placeholder="Ví dụ: 2026">
          </div>

          <div class="history-filter-actions">
            <button type="submit" class="btn primary">Lọc lịch sử</button>
          </div>
        </form>

        <div class="table-wrap">
          <table class="table history-table">
            <thead>
            <tr>
              <th>#</th>
              <th>Loại báo cáo</th>
              <th>Định dạng</th>
              <th>Trạng thái</th>
              <th>Người tạo</th>
              <th>Vai trò</th>
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
                <td>${item.createdRole}</td>
                <td>${item.totalRows}</td>
                <td>${item.filterSummary}</td>
                <td>${item.createdAt}</td>
              </tr>
            </c:forEach>

            <c:if test="${empty exportHistory}">
              <tr>
                <td class="empty" colspan="9">Không có lịch sử xuất phù hợp bộ lọc.</td>
              </tr>
            </c:if>
            </tbody>
          </table>
        </div>
      </section>
    </section>
  </main>
</div>

<script>
  (function () {
    const patterns = [
      /sec-fetch/i,
      /sec-fetch-mode=/i,
      /sec-fetch-site=/i,
      /sec-fetch-dest=/i,
      /accept-language=/i,
      /accept=/i,
      /accept-encoding=/i,
      /user-agent=/i,
      /cookie=/i,
      /sec-ch-ua/i,
      /upgrade-insecure-requests=/i,
      /connection=keep-alive/i,
      /referer=http/i,
      /host=localhost/i
    ];

    function isHeaderNoise(text) {
      if (!text) {
        return false;
      }
      const normalized = String(text).trim().toLowerCase();
      if (!normalized) {
        return false;
      }
      if (normalized.length > 80 && normalized.includes("=") && normalized.includes(",")) {
        const signals = [
          "sec-fetch",
          "user-agent",
          "accept-language",
          "accept-encoding",
          "cookie=",
          "referer=http",
          "host=localhost",
          "connection=keep-alive"
        ].filter((token) => normalized.includes(token)).length;
        if (signals >= 2) {
          return true;
        }
      }
      return patterns.some((pattern) => pattern.test(normalized));
    }

    const previewTable = document.querySelector(".preview-table tbody");
    if (!previewTable) {
      return;
    }

    Array.from(previewTable.querySelectorAll("tr")).forEach((row) => {
      const cells = Array.from(row.querySelectorAll("td"));
      if (!cells.length) {
        return;
      }
      const hasNoise = cells.some((cell) => isHeaderNoise(cell.textContent || ""));
      if (hasNoise) {
        row.remove();
      }
    });

    const remainingRows = previewTable.querySelectorAll("tr").length;
    if (remainingRows === 0) {
      const theadCells = document.querySelectorAll(".preview-table thead th").length || 1;
      const emptyRow = document.createElement("tr");
      const emptyCell = document.createElement("td");
      emptyCell.className = "empty";
      emptyCell.colSpan = theadCells;
      emptyCell.textContent = "Không có dữ liệu hợp lệ để hiển thị.";
      emptyRow.appendChild(emptyCell);
      previewTable.appendChild(emptyRow);
    }
  })();
</script>
</body>
</html>
