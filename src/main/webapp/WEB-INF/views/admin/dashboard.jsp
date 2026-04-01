<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>DIEMLYTA - Quản lý điểm THPT</title>

  <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&display=swap" rel="stylesheet">
  <link rel="stylesheet" href="<c:url value='/css/admin-layout.css'/>">
  <link rel="stylesheet" href="<c:url value='/css/dashboard.css'/>">
</head>
<body>
<div class="layout">
  <jsp:include page="/WEB-INF/views/admin/_sidebar.jsp" />

  <main class="main dashboard-page">
    <div class="dashboard-hero">
      <div class="main-header">
        <h1>Quản lý điểm THPT</h1>
        <p>Xin chào <strong>${displayName}</strong>. Bảng điều khiển tổng hợp dữ liệu thật từ hệ thống theo khóa, khối và lớp.</p>
      </div>
      <div class="hero-pulse" aria-hidden="true"></div>
    </div>

    <c:if test="${not empty flashMessage}">
      <div class="alert ${flashType == 'error' ? 'alert-error' : (flashType == 'info' ? 'alert-info' : 'alert-success')}">
        ${flashMessage}
      </div>
    </c:if>

    <section class="filter-panel card">
      <form class="filters" method="get" action="<c:url value='/admin/dashboard'/>" autocomplete="off">
        <div class="filter-item search-item">
          <label for="q">Tìm kiếm</label>
          <input id="q"
                 type="text"
                 name="q"
                 value="${search.q}"
                 placeholder="Nhập mã học sinh, tên học sinh hoặc nội dung hoạt động...">
        </div>

        <div class="filter-item">
          <label for="khoa">Khóa học</label>
          <select id="khoa" name="khoa">
            <option value="">Tất cả khóa</option>
            <c:forEach var="item" items="${courseOptions}">
              <option value="${item.id}" ${search.khoa == item.id ? 'selected' : ''}>${item.name}</option>
            </c:forEach>
          </select>
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

        <div class="filter-actions">
          <button class="btn btn-filter" type="submit">Lọc dữ liệu</button>
          <a class="btn btn-light" href="<c:url value='/admin/dashboard'/>">Đặt lại</a>
        </div>
      </form>
    </section>

    <section class="stats">
      <article class="stat-card">
        <div class="stat-icon blue">
          <svg fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.8">
            <path stroke-linecap="round" stroke-linejoin="round"
                  d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z"/>
          </svg>
        </div>
        <div>
          <div class="stat-value"><fmt:formatNumber value="${soHocSinh}" groupingUsed="true"/></div>
          <div class="stat-label">Học sinh</div>
        </div>
      </article>

      <article class="stat-card">
        <div class="stat-icon green">
          <svg fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.8">
            <path stroke-linecap="round" stroke-linejoin="round"
                  d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z"/>
          </svg>
        </div>
        <div>
          <div class="stat-value"><fmt:formatNumber value="${soGiaoVien}" groupingUsed="true"/></div>
          <div class="stat-label">Giáo viên</div>
        </div>
      </article>

      <article class="stat-card">
        <div class="stat-icon orange">
          <svg fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.8">
            <path stroke-linecap="round" stroke-linejoin="round"
                  d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"/>
          </svg>
        </div>
        <div>
          <div class="stat-value"><fmt:formatNumber value="${soMonHoc}" groupingUsed="true"/></div>
          <div class="stat-label">Môn học</div>
        </div>
      </article>

      <article class="stat-card">
        <div class="stat-icon violet">
          <svg fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.8">
            <path stroke-linecap="round" stroke-linejoin="round"
                  d="M3 7a2 2 0 012-2h14a2 2 0 012 2v10a2 2 0 01-2 2H5a2 2 0 01-2-2V7zm4 3h10M7 14h6"/>
          </svg>
        </div>
        <div>
          <div class="stat-value"><fmt:formatNumber value="${soLop}" groupingUsed="true"/></div>
          <div class="stat-label">Lớp học</div>
        </div>
      </article>
    </section>

    <section class="analytics-grid">
      <article class="card score-panel">
        <div class="panel-head">
          <h2>Tổng quan điểm số</h2>
          <small>Theo bộ lọc hiện tại</small>
        </div>

        <div class="score-summary">
          <div class="summary-metric">
            <span>Điểm trung bình toàn trường</span>
            <strong>${scoreStats.schoolAverageDisplay}</strong>
          </div>
          <div class="summary-metric">
            <span>Tỷ lệ Giỏi + Khá</span>
            <strong>${scoreStats.goodRateDisplay}</strong>
          </div>
        </div>

        <div class="score-distribution">
          <div class="score-donut"
               data-good-rate="${scoreStats.goodRateDisplay}"
               data-excellent-rate="${scoreStats.excellentRateValue}"
               data-good-only-rate="${scoreStats.goodOnlyRateValue}"
               data-average-rate="${scoreStats.averageRateValue}"
               data-weak-rate="${scoreStats.weakRateValue}">
            <span class="score-donut-value">0%</span>
          </div>
          <div class="score-legend">
            <span class="legend-item"><i class="legend-dot dot-excellent"></i>Giỏi (${scoreStats.excellentRateDisplay})</span>
            <span class="legend-item"><i class="legend-dot dot-good"></i>Khá (${scoreStats.goodOnlyRateDisplay})</span>
            <span class="legend-item"><i class="legend-dot dot-average"></i>Trung bình (${scoreStats.averageRateDisplay})</span>
            <span class="legend-item"><i class="legend-dot dot-weak"></i>Yếu (${scoreStats.weakRateDisplay})</span>
          </div>
        </div>
      </article>

      <article class="card conduct-panel">
        <div class="panel-head with-actions">
          <div>
            <h2>Tỷ lệ khen thưởng / kỷ luật</h2>
            <small>Cập nhật theo khóa, khối, lớp đang chọn</small>
          </div>
          <div class="chart-switch" role="group" aria-label="Chuyển loại biểu đồ">
            <button type="button" class="chart-switch-btn active" data-chart-type="donut">Donut</button>
            <button type="button" class="chart-switch-btn" data-chart-type="bar">Cột</button>
            <button type="button" class="chart-switch-btn" data-chart-type="line">Đường</button>
          </div>
        </div>

        <div class="conduct-chart"
             data-reward-rate="${conductStats.rewardRateValue}"
             data-discipline-rate="${conductStats.disciplineRateValue}">
          <canvas id="conductChartCanvas" aria-label="Biểu đồ tỷ lệ khen thưởng và kỷ luật"></canvas>
        </div>

        <div class="conduct-meta">
          <span><i class="legend-dot dot-reward"></i>Khen thưởng: ${conductStats.rewardRateDisplay}</span>
          <span><i class="legend-dot dot-discipline"></i>Kỷ luật: ${conductStats.disciplineRateDisplay}</span>
          <span>Tổng quyết định: <strong><fmt:formatNumber value="${conductStats.totalRecords}" groupingUsed="true"/></strong></span>
        </div>
      </article>
    </section>

    <div class="section-title">Hoạt động gần đây</div>
    <section class="activity-layout">
      <article class="card activity-card">
        <div class="table-header">
          Lịch sử thao tác khen thưởng / kỷ luật
          <a href="<c:url value='/admin/conduct'/>">Xem chi tiết →</a>
        </div>
        <div class="activity-list">
          <c:forEach var="item" items="${activityItems}">
            <article class="activity-item activity-${item.actionKind}">
              <span class="activity-dot" aria-hidden="true"></span>
              <div class="activity-body">
                <div class="activity-top">
                  <div>
                    <span class="activity-role">${item.actorRole}</span>
                    <strong>${item.actorName}</strong>
                  </div>
                  <span class="activity-time">${item.actionTime}</span>
                </div>
                <p>${item.actionDetail}</p>
              </div>
            </article>
          </c:forEach>

          <c:if test="${empty activityItems}">
            <div class="empty-note">Chưa có hoạt động mới trong hệ thống.</div>
          </c:if>
        </div>
      </article>

      <article class="card recent-students-card">
        <div class="table-header">
          Học sinh thêm gần đây
          <a href="<c:url value='/admin/student'/>">Xem tất cả →</a>
        </div>
        <div class="recent-table-wrap">
          <table class="recent-table">
            <thead>
            <tr>
              <th>Mã HS</th>
              <th>Họ tên</th>
              <th>Lớp</th>
              <th>Thời gian tạo</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="item" items="${recentStudents}">
              <tr>
                <td>${item.idHocSinh}</td>
                <td><strong>${item.hoTen}</strong></td>
                <td>${item.tenLop}</td>
                <td>${empty item.thoiGianTao ? '-' : item.thoiGianTao}</td>
              </tr>
            </c:forEach>
            <c:if test="${empty recentStudents}">
              <tr>
                <td class="empty-note" colspan="4">Chưa có dữ liệu học sinh mới.</td>
              </tr>
            </c:if>
            </tbody>
          </table>
        </div>
      </article>
    </section>

    <div class="section-title">Truy cập nhanh</div>
    <section class="cards-grid">
      <article class="card quick-card">
        <h3>Thêm học sinh</h3>
        <p>Đăng ký hồ sơ học sinh mới vào hệ thống quản lý tập trung.</p>
        <a class="card-btn btn-navy" href="<c:url value='/admin/student/create'/>">Bắt đầu ngay</a>
      </article>

      <article class="card quick-card">
        <h3>Giáo viên</h3>
        <p>Quản lý danh sách, phân công giảng dạy và thông tin giáo viên.</p>
        <a class="card-btn btn-teal" href="<c:url value='/admin/teacher'/>">Xem danh sách</a>
      </article>

      <article class="card quick-card">
        <h3>Điểm</h3>
        <p>Nhập điểm, cập nhật kết quả học tập và xuất báo cáo định kỳ.</p>
        <a class="card-btn btn-green" href="<c:url value='/admin/score'/>">Quản lý điểm</a>
      </article>

      <article class="card quick-card">
        <h3>Tài khoản</h3>
        <p>Cấu hình tài khoản người dùng, phân quyền và bảo mật hệ thống.</p>
        <a class="card-btn btn-slate" href="<c:url value='/admin/account'/>">Cài đặt</a>
      </article>

      <article class="card quick-card">
        <h3>Môn học</h3>
        <p>Thêm, chỉnh sửa thông tin môn học và chương trình giảng dạy.</p>
        <a class="card-btn btn-blue" href="<c:url value='/admin/subject'/>">Xem môn học</a>
      </article>

      <article class="card quick-card">
        <h3>Đăng xuất</h3>
        <p>Kết thúc phiên làm việc và đăng xuất khỏi hệ thống an toàn.</p>
        <form method="post" action="<c:url value='/logout'/>">
          <button class="card-btn btn-red" type="submit">Đăng xuất</button>
        </form>
      </article>
    </section>
  </main>
</div>

<script>
  (function () {
    const scoreDonut = document.querySelector('.score-donut');
    if (scoreDonut) {
      const scoreValueElement = scoreDonut.querySelector('.score-donut-value');
      const parseRate = function (value) {
        const parsed = parseFloat((value || '0').replace('%', '').replace(',', '.'));
        return Number.isFinite(parsed) ? Math.max(0, Math.min(100, parsed)) : 0;
      };
      const excellentRate = parseRate(scoreDonut.dataset.excellentRate);
      const goodOnlyRate = parseRate(scoreDonut.dataset.goodOnlyRate);
      const averageRate = parseRate(scoreDonut.dataset.averageRate);
      const weakRate = parseRate(scoreDonut.dataset.weakRate);
      const goodRate = parseRate(scoreDonut.dataset.goodRate);

      const renderDonut = function (progress) {
        const excellent = excellentRate * progress;
        const goodOnly = goodOnlyRate * progress;
        const average = averageRate * progress;
        const weak = weakRate * progress;

        scoreDonut.style.setProperty('--excellent-end', excellent.toFixed(2));
        scoreDonut.style.setProperty('--good-end', (excellent + goodOnly).toFixed(2));
        scoreDonut.style.setProperty('--average-end', (excellent + goodOnly + average).toFixed(2));
        scoreDonut.style.setProperty('--weak-end', (excellent + goodOnly + average + weak).toFixed(2));

        if (scoreValueElement) {
          scoreValueElement.textContent = (goodRate * progress).toFixed(1).replace('.0', '') + '%';
        }
      };

      if (window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
        renderDonut(1);
      } else {
        const duration = 1100;
        const start = performance.now();
        const tick = function (now) {
          const progress = Math.min((now - start) / duration, 1);
          const eased = 1 - Math.pow(1 - progress, 3);
          renderDonut(eased);
          if (progress < 1) {
            requestAnimationFrame(tick);
          } else {
            renderDonut(1);
          }
        };
        requestAnimationFrame(tick);
      }
    }

    const chartContainer = document.querySelector('.conduct-chart');
    const chartCanvas = document.getElementById('conductChartCanvas');
    const switchButtons = Array.from(document.querySelectorAll('.chart-switch-btn'));
    if (!chartContainer || !chartCanvas || !switchButtons.length) {
      return;
    }

    const context = chartCanvas.getContext('2d');
    const parseNumber = function (value) {
      const parsed = parseFloat((value || '0').replace(',', '.'));
      return Number.isFinite(parsed) ? Math.max(0, Math.min(100, parsed)) : 0;
    };

    const rewardRate = parseNumber(chartContainer.dataset.rewardRate);
    const disciplineRate = parseNumber(chartContainer.dataset.disciplineRate);
    const labels = ['Khen thưởng', 'Kỷ luật'];
    const colors = ['#14b8a6', '#ef4444'];

    let activeType = 'donut';
    let activeProgress = 0;
    let frameId = null;

    function canvasSize() {
      const ratio = window.devicePixelRatio || 1;
      const rect = chartCanvas.getBoundingClientRect();
      const width = Math.max(280, rect.width);
      const height = Math.max(220, rect.height);
      chartCanvas.width = Math.floor(width * ratio);
      chartCanvas.height = Math.floor(height * ratio);
      context.setTransform(ratio, 0, 0, ratio, 0, 0);
      return { width: width, height: height };
    }

    function clear(width, height) {
      context.clearRect(0, 0, width, height);
    }

    function drawDonut(width, height, progress) {
      const centerX = width / 2;
      const centerY = height / 2;
      const radius = Math.min(width, height) * 0.28;
      const lineWidth = Math.max(18, radius * 0.3);
      const values = [rewardRate * progress, disciplineRate * progress];

      context.lineCap = 'round';
      context.lineWidth = lineWidth;
      context.beginPath();
      context.strokeStyle = '#e5e7eb';
      context.arc(centerX, centerY, radius, 0, Math.PI * 2);
      context.stroke();

      let startAngle = -Math.PI / 2;
      values.forEach(function (value, index) {
        const angle = (value / 100) * Math.PI * 2;
        context.beginPath();
        context.strokeStyle = colors[index];
        context.arc(centerX, centerY, radius, startAngle, startAngle + angle);
        context.stroke();
        startAngle += angle;
      });

      context.fillStyle = '#0f172a';
      context.font = '700 26px "Be Vietnam Pro"';
      context.textAlign = 'center';
      context.textBaseline = 'middle';
      context.fillText((rewardRate * progress).toFixed(1).replace('.0', '') + '%', centerX, centerY);

      context.fillStyle = '#64748b';
      context.font = '500 12px "Be Vietnam Pro"';
      context.fillText('Khen thưởng', centerX, centerY + 24);
    }

    function drawBar(width, height, progress) {
      const paddingX = 40;
      const chartTop = 26;
      const chartBottom = height - 38;
      const chartHeight = chartBottom - chartTop;
      const values = [rewardRate, disciplineRate];
      const barWidth = Math.min(120, (width - paddingX * 2) / 3);
      const gap = barWidth * 0.6;
      const startX = (width - (barWidth * 2 + gap)) / 2;

      context.strokeStyle = '#e2e8f0';
      context.lineWidth = 1;
      context.beginPath();
      context.moveTo(paddingX, chartBottom);
      context.lineTo(width - paddingX, chartBottom);
      context.stroke();

      values.forEach(function (value, index) {
        const scaled = (value * progress / 100) * chartHeight;
        const x = startX + index * (barWidth + gap);
        const y = chartBottom - scaled;

        const gradient = context.createLinearGradient(0, y, 0, chartBottom);
        gradient.addColorStop(0, colors[index]);
        gradient.addColorStop(1, '#0f172a');
        context.fillStyle = gradient;
        context.fillRect(x, y, barWidth, scaled);

        context.fillStyle = '#0f172a';
        context.font = '700 13px "Be Vietnam Pro"';
        context.textAlign = 'center';
        context.fillText((value * progress).toFixed(1).replace('.0', '') + '%', x + barWidth / 2, y - 8);

        context.fillStyle = '#64748b';
        context.font = '500 12px "Be Vietnam Pro"';
        context.fillText(labels[index], x + barWidth / 2, chartBottom + 18);
      });
    }

    function drawLine(width, height, progress) {
      const points = [
        { label: labels[0], value: rewardRate * progress },
        { label: labels[1], value: disciplineRate * progress }
      ];
      const paddingX = 44;
      const top = 24;
      const bottom = height - 44;
      const left = paddingX;
      const right = width - paddingX;
      const chartHeight = bottom - top;

      context.strokeStyle = '#e2e8f0';
      context.lineWidth = 1;
      context.beginPath();
      context.moveTo(left, bottom);
      context.lineTo(right, bottom);
      context.stroke();

      const coordinates = points.map(function (point, index) {
        const x = points.length === 1
                ? (left + right) / 2
                : left + (index * (right - left) / (points.length - 1));
        const y = bottom - (point.value / 100) * chartHeight;
        return { x: x, y: y, label: point.label, value: point.value };
      });

      const areaGradient = context.createLinearGradient(0, top, 0, bottom);
      areaGradient.addColorStop(0, 'rgba(45, 106, 143, 0.30)');
      areaGradient.addColorStop(1, 'rgba(45, 106, 143, 0.02)');

      context.beginPath();
      context.moveTo(coordinates[0].x, bottom);
      coordinates.forEach(function (point) {
        context.lineTo(point.x, point.y);
      });
      context.lineTo(coordinates[coordinates.length - 1].x, bottom);
      context.closePath();
      context.fillStyle = areaGradient;
      context.fill();

      context.beginPath();
      coordinates.forEach(function (point, index) {
        if (index === 0) {
          context.moveTo(point.x, point.y);
        } else {
          context.lineTo(point.x, point.y);
        }
      });
      context.strokeStyle = '#0f766e';
      context.lineWidth = 3;
      context.stroke();

      coordinates.forEach(function (point, index) {
        context.beginPath();
        context.fillStyle = colors[index];
        context.arc(point.x, point.y, 6, 0, Math.PI * 2);
        context.fill();

        context.fillStyle = '#0f172a';
        context.font = '700 13px "Be Vietnam Pro"';
        context.textAlign = 'center';
        context.fillText(point.value.toFixed(1).replace('.0', '') + '%', point.x, point.y - 12);

        context.fillStyle = '#64748b';
        context.font = '500 12px "Be Vietnam Pro"';
        context.fillText(point.label, point.x, bottom + 20);
      });
    }

    function draw() {
      const size = canvasSize();
      clear(size.width, size.height);
      if (activeType === 'bar') {
        drawBar(size.width, size.height, activeProgress);
        return;
      }
      if (activeType === 'line') {
        drawLine(size.width, size.height, activeProgress);
        return;
      }
      drawDonut(size.width, size.height, activeProgress);
    }

    function animate() {
      if (frameId) {
        cancelAnimationFrame(frameId);
      }
      const reduceMotion = window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches;
      if (reduceMotion) {
        activeProgress = 1;
        draw();
        return;
      }
      const duration = 900;
      const start = performance.now();
      const step = function (now) {
        const progress = Math.min((now - start) / duration, 1);
        activeProgress = 1 - Math.pow(1 - progress, 3);
        draw();
        if (progress < 1) {
          frameId = requestAnimationFrame(step);
        } else {
          activeProgress = 1;
          draw();
        }
      };
      frameId = requestAnimationFrame(step);
    }

    switchButtons.forEach(function (button) {
      button.addEventListener('click', function () {
        const nextType = button.dataset.chartType;
        if (!nextType || nextType === activeType) {
          return;
        }
        activeType = nextType;
        switchButtons.forEach(function (item) {
          item.classList.toggle('active', item === button);
        });
        activeProgress = 0;
        animate();
      });
    });

    window.addEventListener('resize', draw);
    animate();
  })();
</script>
</body>
</html>
