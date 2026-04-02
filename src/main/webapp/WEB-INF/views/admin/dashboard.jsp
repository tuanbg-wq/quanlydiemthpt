<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>DIEMLYTA - Quáº£n lĂ½ Ä‘iá»ƒm THPT</title>

  <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&display=swap" rel="stylesheet">
  <link rel="stylesheet" href="<c:url value='/css/admin-layout.css'/>">
  <link rel="stylesheet" href="<c:url value='/css/admin/dashboard.css'/>">
</head>
<body>
<div class="layout">
  <jsp:include page="/WEB-INF/views/admin/_sidebar.jsp" />

  <main class="main dashboard-page">
    <div class="dashboard-hero">
      <div class="main-header">
        <h1>Quáº£n lĂ½ Ä‘iá»ƒm THPT</h1>
        <p>Xin chĂ o <strong>${displayName}</strong>. Báº£ng Ä‘iá»u khiá»ƒn tá»•ng há»£p dá»¯ liá»‡u tháº­t tá»« há»‡ thá»‘ng theo khĂ³a, khá»‘i vĂ  lá»›p.</p>
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
        <div class="filter-item">
          <label for="khoa">KhĂ³a há»c</label>
          <select id="khoa" name="khoa">
            <option value="">Táº¥t cáº£ khĂ³a</option>
            <c:forEach var="item" items="${courseOptions}">
              <option value="${item.id}" ${search.khoa == item.id ? 'selected' : ''}>${item.name}</option>
            </c:forEach>
          </select>
        </div>

        <div class="filter-item">
          <label for="khoi">Khá»‘i</label>
          <select id="khoi" name="khoi">
            <option value="">Táº¥t cáº£ khá»‘i</option>
            <c:forEach var="grade" items="${grades}">
              <option value="${grade}" ${search.khoi == grade ? 'selected' : ''}>Khá»‘i ${grade}</option>
            </c:forEach>
          </select>
        </div>

        <div class="filter-item">
          <label for="lop">Lá»›p</label>
          <select id="lop" name="lop">
            <option value="">Táº¥t cáº£ lá»›p</option>
            <c:forEach var="item" items="${classOptions}">
              <option value="${item.id}" ${search.lop == item.id ? 'selected' : ''}>${item.name}</option>
            </c:forEach>
          </select>
        </div>

        <div class="filter-item">
          <label for="loai">Loáº¡i (KT/KL)</label>
          <select id="loai" name="loai">
            <option value="">Táº¥t cáº£ loáº¡i</option>
            <option value="KHEN_THUONG" ${search.loai == 'KHEN_THUONG' ? 'selected' : ''}>Khen thÆ°á»Ÿng</option>
            <option value="KY_LUAT" ${search.loai == 'KY_LUAT' ? 'selected' : ''}>Ká»· luáº­t</option>
          </select>
        </div>

        <div class="filter-actions">
          <button class="btn btn-filter" type="submit">Lá»c dá»¯ liá»‡u</button>
          <a class="btn btn-light" href="<c:url value='/admin/dashboard'/>">Äáº·t láº¡i</a>
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
          <div class="stat-label">Há»c sinh</div>
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
          <div class="stat-label">GiĂ¡o viĂªn</div>
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
          <div class="stat-label">MĂ´n há»c</div>
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
          <div class="stat-label">Lá»›p há»c</div>
        </div>
      </article>
    </section>

    <section class="analytics-grid">
      <article class="card score-panel">
        <div class="panel-head">
          <h2>Tá»•ng quan Ä‘iá»ƒm sá»‘</h2>
          <small>Theo bá»™ lá»c hiá»‡n táº¡i</small>
        </div>

        <div class="score-summary">
          <div class="summary-metric">
            <span>Äiá»ƒm trung bĂ¬nh toĂ n trÆ°á»ng</span>
            <strong>${scoreStats.schoolAverageDisplay}</strong>
          </div>
          <div class="summary-metric">
            <span>Tá»· lá»‡ Giá»i + KhĂ¡</span>
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
            <span class="legend-item"><i class="legend-dot dot-excellent"></i>Giá»i (${scoreStats.excellentRateDisplay})</span>
            <span class="legend-item"><i class="legend-dot dot-good"></i>KhĂ¡ (${scoreStats.goodOnlyRateDisplay})</span>
            <span class="legend-item"><i class="legend-dot dot-average"></i>Trung bĂ¬nh (${scoreStats.averageRateDisplay})</span>
            <span class="legend-item"><i class="legend-dot dot-weak"></i>Yáº¿u (${scoreStats.weakRateDisplay})</span>
          </div>
        </div>
      </article>

      <article class="card conduct-panel">
        <div class="panel-head with-actions">
          <div>
            <h2>Tá»· lá»‡ khen thÆ°á»Ÿng / ká»· luáº­t</h2>
            <small>Cáº­p nháº­t theo khĂ³a, khá»‘i, lá»›p vĂ  loáº¡i Ä‘ang chá»n</small>
          </div>
          <div class="chart-switch" role="group" aria-label="Chuyá»ƒn loáº¡i biá»ƒu Ä‘á»“">
            <button type="button" class="chart-switch-btn active" data-chart-type="donut">Donut</button>
            <button type="button" class="chart-switch-btn" data-chart-type="bar">Cá»™t</button>
            <button type="button" class="chart-switch-btn" data-chart-type="line">ÄÆ°á»ng</button>
          </div>
        </div>

        <div class="conduct-chart"
             data-reward-rate="${conductStats.rewardRateValue}"
             data-discipline-rate="${conductStats.disciplineRateValue}">
          <canvas id="conductChartCanvas" aria-label="Biá»ƒu Ä‘á»“ tá»· lá»‡ khen thÆ°á»Ÿng vĂ  ká»· luáº­t"></canvas>
        </div>

        <div class="conduct-meta">
          <span>
            <i class="legend-dot dot-reward"></i>
            Sá»‘ khen thÆ°á»Ÿng: <strong><fmt:formatNumber value="${conductStats.totalReward}" groupingUsed="true"/></strong>
            (${conductStats.rewardRateDisplay})
          </span>
          <span>
            <i class="legend-dot dot-discipline"></i>
            Sá»‘ ká»· luáº­t: <strong><fmt:formatNumber value="${conductStats.totalDiscipline}" groupingUsed="true"/></strong>
            (${conductStats.disciplineRateDisplay})
          </span>
          <span>Tá»•ng quyáº¿t Ä‘á»‹nh: <strong><fmt:formatNumber value="${conductStats.totalRecords}" groupingUsed="true"/></strong></span>
        </div>
      </article>
    </section>

    <div class="section-title">Hoáº¡t Ä‘á»™ng gáº§n Ä‘Ă¢y</div>
    <section class="activity-layout">
      <article class="card activity-card">
        <div class="table-header">
          Lá»‹ch sá»­ thao tĂ¡c khen thÆ°á»Ÿng / ká»· luáº­t
          <a href="<c:url value='/admin/conduct'/>">Xem chi tiáº¿t â†’</a>
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
            <div class="empty-note">ChÆ°a cĂ³ hoáº¡t Ä‘á»™ng má»›i trong há»‡ thá»‘ng.</div>
          </c:if>
        </div>
      </article>

      <article class="card recent-students-card">
        <div class="table-header">
          Há»c sinh thĂªm gáº§n Ä‘Ă¢y
          <a href="<c:url value='/admin/student'/>">Xem táº¥t cáº£ â†’</a>
        </div>
        <div class="recent-table-wrap">
          <table class="recent-table">
            <thead>
            <tr>
              <th>MĂ£ HS</th>
              <th>Há» tĂªn</th>
              <th>Lá»›p</th>
              <th>Thá»i gian táº¡o</th>
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
                <td class="empty-note" colspan="4">ChÆ°a cĂ³ dá»¯ liá»‡u há»c sinh má»›i.</td>
              </tr>
            </c:if>
            </tbody>
          </table>
        </div>
      </article>
    </section>

    <div class="section-title">Truy cáº­p nhanh</div>
    <section class="cards-grid">
      <article class="card quick-card">
        <h3>ThĂªm há»c sinh</h3>
        <p>ÄÄƒng kĂ½ há»“ sÆ¡ há»c sinh má»›i vĂ o há»‡ thá»‘ng quáº£n lĂ½ táº­p trung.</p>
        <a class="card-btn btn-navy" href="<c:url value='/admin/student/create'/>">Báº¯t Ä‘áº§u ngay</a>
      </article>

      <article class="card quick-card">
        <h3>GiĂ¡o viĂªn</h3>
        <p>Quáº£n lĂ½ danh sĂ¡ch, phĂ¢n cĂ´ng giáº£ng dáº¡y vĂ  thĂ´ng tin giĂ¡o viĂªn.</p>
        <a class="card-btn btn-teal" href="<c:url value='/admin/teacher'/>">Xem danh sĂ¡ch</a>
      </article>

      <article class="card quick-card">
        <h3>Äiá»ƒm</h3>
        <p>Nháº­p Ä‘iá»ƒm, cáº­p nháº­t káº¿t quáº£ há»c táº­p vĂ  xuáº¥t bĂ¡o cĂ¡o Ä‘á»‹nh ká»³.</p>
        <a class="card-btn btn-green" href="<c:url value='/admin/score'/>">Quáº£n lĂ½ Ä‘iá»ƒm</a>
      </article>

      <article class="card quick-card">
        <h3>TĂ i khoáº£n</h3>
        <p>Cáº¥u hĂ¬nh tĂ i khoáº£n ngÆ°á»i dĂ¹ng, phĂ¢n quyá»n vĂ  báº£o máº­t há»‡ thá»‘ng.</p>
        <a class="card-btn btn-slate" href="<c:url value='/admin/account'/>">CĂ i Ä‘áº·t</a>
      </article>

      <article class="card quick-card">
        <h3>MĂ´n há»c</h3>
        <p>ThĂªm, chá»‰nh sá»­a thĂ´ng tin mĂ´n há»c vĂ  chÆ°Æ¡ng trĂ¬nh giáº£ng dáº¡y.</p>
        <a class="card-btn btn-blue" href="<c:url value='/admin/subject'/>">Xem mĂ´n há»c</a>
      </article>

      <article class="card quick-card">
        <h3>ÄÄƒng xuáº¥t</h3>
        <p>Káº¿t thĂºc phiĂªn lĂ m viá»‡c vĂ  Ä‘Äƒng xuáº¥t khá»i há»‡ thá»‘ng an toĂ n.</p>
        <form method="post" action="<c:url value='/logout'/>">
          <button class="card-btn btn-red" type="submit">ÄÄƒng xuáº¥t</button>
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
    const labels = ['Khen thÆ°á»Ÿng', 'Ká»· luáº­t'];
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
      context.fillText('Khen thÆ°á»Ÿng', centerX, centerY + 24);
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

