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
  <link rel="stylesheet" href="<c:url value='/css/admin/score-list.css'/>">
</head>
<body>
<div class="layout">
  <jsp:include page="/WEB-INF/views/admin/_sidebar.jsp"/>

  <main class="main score-list-page">
    <header class="score-header">
      <div class="header-left">
        <h1>Quáº£n lĂ½ Ä‘iá»ƒm sá»‘</h1>
        <p>Tá»•ng há»£p Ä‘iá»ƒm theo há»c sinh, mĂ´n há»c vĂ  nÄƒm há»c.</p>
      </div>
      <div class="header-right">
        <a class="btn primary" href="<c:url value='/admin/score/create'/>">+ ThĂªm Ä‘iá»ƒm sá»‘</a>
      </div>
    </header>

    <section class="content">
      <c:if test="${not empty flashMessage}">
        <div class="alert ${flashType == 'error' ? 'alert-error' : (flashType == 'info' ? 'alert-info' : 'alert-success')}">
          ${flashMessage}
        </div>
      </c:if>
      <c:set var="isAnnualView" value="${search.hocKy == '0'}"/>

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
            <p>Tá»•ng há»c sinh cĂ³ Ä‘iá»ƒm</p>
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
            <p>Äiá»ƒm trung bĂ¬nh toĂ n trÆ°á»ng</p>
            <h3>${stats.schoolAverageDisplay}</h3>
          </div>
        </article>

        <article class="stats-card stats-card-rate">
          <div class="stats-rate-content">
            <p>Tá»· lá»‡ xáº¿p loáº¡i mĂ´n</p>
            <h3>${stats.goodRateDisplay}</h3>
            <small>Giá»i + KhĂ¡</small>
            <span class="rate-subline">Trung bĂ¬nh: ${stats.averageRateDisplay} â€¢ Yáº¿u: ${stats.weakRateDisplay}</span>
          </div>
          <div class="rate-donut"
               data-good-rate="${stats.goodRateDisplay}"
               data-excellent-rate="${stats.excellentRateValue}"
               data-good-only-rate="${stats.goodOnlyRateValue}"
               data-average-rate="${stats.averageRateValue}"
               data-weak-rate="${stats.weakRateValue}"
               aria-label="Tá»· lá»‡ giá»i ${stats.excellentRateDisplay}, khĂ¡ ${stats.goodOnlyRateDisplay}, trung bĂ¬nh ${stats.averageRateDisplay}, yáº¿u ${stats.weakRateDisplay}">
            <span class="rate-donut-value">0%</span>
          </div>
          <div class="rate-legend">
            <span class="legend-item"><i class="legend-swatch legend-excellent"></i>Giá»i (${stats.excellentRateDisplay})</span>
            <span class="legend-item"><i class="legend-swatch legend-good"></i>KhĂ¡ (${stats.goodOnlyRateDisplay})</span>
            <span class="legend-item"><i class="legend-swatch legend-average"></i>Trung bĂ¬nh (${stats.averageRateDisplay})</span>
            <span class="legend-item"><i class="legend-swatch legend-weak"></i>Yáº¿u (${stats.weakRateDisplay})</span>
          </div>
        </article>
      </section>

      <section class="card filter-card">
        <form class="filters" method="get" action="<c:url value='/admin/score'/>" autocomplete="off">
          <div class="filter-item search-item">
            <label for="q">TĂ¬m kiáº¿m</label>
            <input id="q" type="text" name="q" value="${search.q}" placeholder="Nháº­p mĂ£ há»c sinh, tĂªn há»c sinh hoáº·c mĂ´n há»c...">
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
            <label for="mon">MĂ´n há»c</label>
            <select id="mon" name="mon">
              <option value="">Táº¥t cáº£ mĂ´n</option>
              <c:forEach var="item" items="${subjectOptions}">
                <option value="${item.id}" ${search.mon == item.id ? 'selected' : ''}>${item.name}</option>
              </c:forEach>
            </select>
          </div>

          <div class="filter-item">
            <label for="hocKy">Há»c ká»³</label>
            <select id="hocKy" name="hocKy">
              <option value="">Táº¥t cáº£ há»c ká»³</option>
              <option value="0" ${search.hocKy == '0' ? 'selected' : ''}>Cáº£ nÄƒm</option>
              <option value="1" ${search.hocKy == '1' ? 'selected' : ''}>Há»c ká»³ 1</option>
              <option value="2" ${search.hocKy == '2' ? 'selected' : ''}>Há»c ká»³ 2</option>
            </select>
          </div>

          <div class="filter-item">
            <label for="khoa">KhĂ³a há»c</label>
            <select id="khoa" name="khoa">
              <option value="">Táº¥t cáº£ khĂ³a há»c</option>
              <c:forEach var="item" items="${courseOptions}">
                <option value="${item.id}" ${search.khoa == item.id ? 'selected' : ''}>${item.name}</option>
              </c:forEach>
            </select>
          </div>

          <div class="filter-actions">
            <div class="export-actions" role="group" aria-label="NhĂ³m nĂºt xuáº¥t bĂ¡o cĂ¡o">
              <button class="btn filter-btn export-btn export-btn-excel"
                      type="submit"
                      formaction="<c:url value='/admin/score/export/excel'/>"
                      formmethod="get">
                Excel
              </button>
              <button class="btn filter-btn export-btn export-btn-pdf"
                      type="submit"
                      formaction="<c:url value='/admin/score/export/pdf'/>"
                      formmethod="get">
                PDF
              </button>
            </div>
            <button class="btn filter-btn action-btn-search" type="submit">TĂ¬m</button>
          </div>
        </form>
      </section>

      <section class="card table-card">
        <div class="table-wrap">
          <table class="table">
            <thead>
            <tr>
              <th>MĂ£ há»c sinh</th>
              <th>TĂªn há»c sinh</th>
              <th>Lá»›p</th>
              <th>MĂ´n</th>
              <c:choose>
                <c:when test="${isAnnualView}">
                  <th>Tá»•ng káº¿t ká»³ 1</th>
                  <th>Tá»•ng káº¿t ká»³ 2</th>
                  <th>Cáº£ nÄƒm</th>
                </c:when>
                <c:otherwise>
                  <th>Giá»¯a ká»³</th>
                  <th>Cuá»‘i ká»³</th>
                  <th>Tá»•ng káº¿t</th>
                </c:otherwise>
              </c:choose>
              <th>Há»c ká»³</th>
              <th>NÄƒm há»c</th>
              <th class="th-actions">Thao tĂ¡c</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="item" items="${scores}">
              <tr>
                <td>${item.idHocSinh}</td>
                <td class="student-name">${item.tenHocSinh}</td>
                <td>${item.tenLop}</td>
                <td>${item.tenMon}</td>
                <c:choose>
                  <c:when test="${isAnnualView}">
                    <td><span class="total-badge">${item.tongKetHocKy1Display}</span></td>
                    <td><span class="total-badge">${item.tongKetHocKy2Display}</span></td>
                    <td><span class="total-badge">${item.tongKetCaNamDisplay}</span></td>
                  </c:when>
                  <c:otherwise>
                    <td>${item.diemGiuaKyDisplay}</td>
                    <td>${item.diemCuoiKyDisplay}</td>
                    <td><span class="total-badge">${item.tongKetDisplay}</span></td>
                  </c:otherwise>
                </c:choose>
                <td>${item.hocKyDisplay}</td>
                <td>${item.namHocDisplay}</td>
                <td class="actions">
                  <div class="action-menu">
                    <button type="button"
                            class="action-toggle"
                            aria-label="Má»Ÿ menu thao tĂ¡c"
                            onclick="toggleScoreActionMenu(this)">
                      &#8942;
                    </button>
                    <div class="action-dropdown" role="menu">
                      <c:url var="detailUrl" value="/admin/score/detail">
                        <c:param name="studentId" value="${item.idHocSinh}"/>
                        <c:param name="subjectId" value="${item.idMon}"/>
                        <c:param name="namHoc" value="${item.namHoc}"/>
                        <c:param name="hocKy" value="${empty search.hocKy ? item.hocKy : search.hocKy}"/>
                      </c:url>
                      <a class="action-item" href="${detailUrl}">Chi tiáº¿t Ä‘iá»ƒm</a>

                      <c:url var="editUrl" value="/admin/score/edit">
                        <c:param name="studentId" value="${item.idHocSinh}"/>
                        <c:param name="subjectId" value="${item.idMon}"/>
                        <c:param name="namHoc" value="${item.namHoc}"/>
                        <c:param name="hocKy" value="${empty search.hocKy ? item.hocKy : search.hocKy}"/>
                      </c:url>
                      <a class="action-item" href="${editUrl}">Chá»‰nh sá»­a</a>

                      <form class="score-delete-form" method="post" action="<c:url value='/admin/score/delete'/>"
                            data-student-name="${item.tenHocSinh}" data-subject-name="${item.tenMon}">
                        <input type="hidden" name="studentId" value="${item.idHocSinh}">
                        <input type="hidden" name="subjectId" value="${item.idMon}">
                        <input type="hidden" name="namHoc" value="${item.namHoc}">
                        <button class="action-item danger" type="submit">XĂ³a</button>
                      </form>
                    </div>
                  </div>
                </td>
              </tr>
            </c:forEach>

            <c:if test="${empty scores}">
              <tr>
                <td class="empty-message" colspan="10">ChÆ°a cĂ³ dá»¯ liá»‡u Ä‘iá»ƒm phĂ¹ há»£p vá»›i bá»™ lá»c.</td>
              </tr>
            </c:if>
            </tbody>
          </table>
        </div>

        <div class="table-footer">
          <div class="table-count">
            Hiá»ƒn thá»‹ ${pageData.fromRecord}-${pageData.toRecord} trĂªn tá»•ng sá»‘ ${pageData.totalItems} káº¿t quáº£
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
                <a class="page-btn" href="${prevUrl}" aria-label="Trang trÆ°á»›c">&lsaquo;</a>
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

<div id="scoreDeleteModal" class="score-delete-modal" hidden>
  <div class="score-delete-backdrop" data-close-score-delete-modal></div>
  <div class="score-delete-dialog" role="dialog" aria-modal="true" aria-labelledby="scoreDeleteModalTitle">
    <h3 id="scoreDeleteModalTitle">XĂ¡c nháº­n xĂ³a Ä‘iá»ƒm</h3>
    <p id="scoreDeleteModalMessage">Báº¡n cĂ³ cháº¯c cháº¯n muá»‘n xĂ³a nhĂ³m Ä‘iá»ƒm nĂ y khĂ´ng?</p>
    <div class="score-delete-actions">
      <button type="button" class="btn" id="cancelScoreDeleteButton">Há»§y</button>
      <button type="button" class="btn btn-danger" id="confirmScoreDeleteButton">XĂ³a</button>
    </div>
  </div>
</div>

<script>
  (function () {
    function closeAllMenus() {
      document.querySelectorAll('.action-dropdown').forEach(menu => {
        menu.classList.remove('show');
        menu.classList.remove('open-up');
      });
      document.querySelectorAll('.action-menu.is-open').forEach(menu => {
        menu.classList.remove('is-open');
      });
      document.querySelectorAll('.table tbody tr.menu-open').forEach(row => {
        row.classList.remove('menu-open');
      });
      document.querySelectorAll('.action-toggle[aria-expanded="true"]').forEach(button => {
        button.setAttribute('aria-expanded', 'false');
      });
    }

    function positionMenu(button, menu) {
      const spacing = 8;
      menu.classList.add('show');
      menu.classList.remove('open-up');
      menu.style.left = '0px';
      menu.style.top = '0px';

      const buttonRect = button.getBoundingClientRect();
      const menuRect = menu.getBoundingClientRect();

      let left = buttonRect.right - menuRect.width;
      if (left < spacing) {
        left = spacing;
      }
      if (left + menuRect.width > window.innerWidth - spacing) {
        left = window.innerWidth - menuRect.width - spacing;
      }

      let top = buttonRect.bottom + spacing;
      const canOpenUp = buttonRect.top - menuRect.height - spacing >= spacing;
      const willOverflowDown = top + menuRect.height > window.innerHeight - spacing;

      if (willOverflowDown && canOpenUp) {
        top = buttonRect.top - menuRect.height - spacing;
        menu.classList.add('open-up');
      } else if (willOverflowDown) {
        top = Math.max(spacing, window.innerHeight - menuRect.height - spacing);
      }

      menu.style.left = left + 'px';
      menu.style.top = top + 'px';
    }

    window.toggleScoreActionMenu = function (button) {
      const currentMenu = button.nextElementSibling;
      const currentRow = button.closest('tr');
      const shouldShow = !currentMenu.classList.contains('show');

      closeAllMenus();
      if (!shouldShow) {
        return;
      }

      positionMenu(button, currentMenu);
      button.setAttribute('aria-expanded', 'true');
      if (currentRow) {
        currentRow.classList.add('menu-open');
      }
      const currentWrap = button.closest('.action-menu');
      if (currentWrap) {
        currentWrap.classList.add('is-open');
      }
    };

    document.addEventListener('click', function (event) {
      if (!event.target.closest('.action-menu')) {
        closeAllMenus();
      }
    });
    window.addEventListener('resize', closeAllMenus);
    document.addEventListener('scroll', closeAllMenus, true);

    const deleteModal = document.getElementById('scoreDeleteModal');
    const deleteModalMessage = document.getElementById('scoreDeleteModalMessage');
    const cancelDeleteButton = document.getElementById('cancelScoreDeleteButton');
    const confirmDeleteButton = document.getElementById('confirmScoreDeleteButton');
    let pendingDeleteForm = null;

    function openDeleteModal(studentName, subjectName) {
      const who = studentName ? ' cá»§a há»c sinh "' + studentName + '"' : '';
      const subject = subjectName ? ' mĂ´n "' + subjectName + '"' : '';
      deleteModalMessage.textContent = 'Báº¡n cĂ³ cháº¯c cháº¯n muá»‘n xĂ³a Ä‘iá»ƒm' + who + subject + ' khĂ´ng?';
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

    document.querySelectorAll('.score-delete-form').forEach(form => {
      form.addEventListener('submit', function (event) {
        if (form.dataset.confirmed === 'true') {
          form.dataset.confirmed = 'false';
          return;
        }
        event.preventDefault();
        pendingDeleteForm = form;
        openDeleteModal(form.dataset.studentName || '', form.dataset.subjectName || '');
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
      deleteModal.querySelectorAll('[data-close-score-delete-modal]').forEach(button => {
        button.addEventListener('click', closeDeleteModal);
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

    function animateGoodRateDonut() {
      const donut = document.querySelector('.rate-donut');
      if (!donut) {
        return;
      }

      const valueElement = donut.querySelector('.rate-donut-value');
      const parseRate = function (value) {
        const raw = (value || '0').replace('%', '').replace(',', '.');
        const parsed = parseFloat(raw);
        return Number.isFinite(parsed) ? Math.max(0, Math.min(100, parsed)) : 0;
      };
      const excellentRate = parseRate(donut.dataset.excellentRate);
      const goodOnlyRate = parseRate(donut.dataset.goodOnlyRate);
      const averageRate = parseRate(donut.dataset.averageRate);
      const weakRate = parseRate(donut.dataset.weakRate);
      const goodRate = parseRate(donut.dataset.goodRate);

      function render(progress) {
        const excellent = excellentRate * progress;
        const goodOnly = goodOnlyRate * progress;
        const average = averageRate * progress;
        const weak = weakRate * progress;
        donut.style.setProperty('--excellent', excellent.toFixed(2));
        donut.style.setProperty('--good', goodOnly.toFixed(2));
        donut.style.setProperty('--average', average.toFixed(2));
        donut.style.setProperty('--weak', weak.toFixed(2));
        if (valueElement) {
          const display = (goodRate * progress).toFixed(1).replace('.0', '');
          valueElement.textContent = display + '%';
        }
      }

      if (window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
        render(1);
        return;
      }

      const durationMs = 1200;
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
    }

    animateGoodRateDonut();
  })();
</script>
</body>
</html>

