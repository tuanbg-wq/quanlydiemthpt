<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>${pageTitle}</title>
    <link rel="stylesheet" href="<c:url value='/css/admin-layout.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/teacher-subject/dashboard.css'/>">
</head>
<body>
<div class="layout">
    <jsp:include page="/WEB-INF/views/teacher-subject/_sidebar.jsp"/>

    <main class="main subject-dashboard-page">
        <c:set var="dashboard" value="${dashboardData}"/>
        <c:set var="summary" value="${dashboard.teacherSummary}"/>
        <c:set var="metrics" value="${dashboard.summaryMetrics}"/>
        <c:set var="distribution" value="${dashboard.scoreDistribution}"/>

        <section class="hero-panel card reveal">
            <div class="hero-copy">
                <span class="hero-kicker">Dashboard GVBM</span>
                <h1>Trang chủ giáo viên bộ môn</h1>
                <p>
                    Theo dõi nhanh mặt bằng điểm số của các lớp được phân công nhập điểm,
                    xem tiến độ học lực và truy cập tức thì vào quản lý điểm, hồ sơ cá nhân.
                </p>

                <div class="hero-meta">
                    <span><strong>Giáo viên:</strong> ${empty summary.teacherName ? 'Chưa xác định' : summary.teacherName}</span>
                    <span><strong>Mã GV:</strong> ${empty summary.teacherId ? '-' : summary.teacherId}</span>
                    <span><strong>Năm học:</strong> ${empty summary.schoolYear ? '-' : summary.schoolYear}</span>
                </div>

                <div class="hero-tags">
                    <span class="hero-tag">Môn dạy: ${empty summary.subjectDisplay ? 'Chưa có môn được phân công' : summary.subjectDisplay}</span>
                    <span class="hero-tag">Lớp phụ trách: ${empty summary.classDisplay ? 'Chưa có lớp được phân công' : summary.classDisplay}</span>
                </div>
            </div>

            <div class="hero-profile">
                <c:choose>
                    <c:when test="${not empty summary.avatar}">
                        <img class="hero-avatar" src="<c:url value='${summary.avatar}'/>" alt="Ảnh giáo viên"/>
                    </c:when>
                    <c:otherwise>
                        <div class="hero-avatar hero-avatar-fallback">GV</div>
                    </c:otherwise>
                </c:choose>

                <div class="hero-profile-card">
                    <span class="mini-label">Tài khoản</span>
                    <strong>${empty summary.username ? '-' : summary.username}</strong>
                    <small>Tổng quan dữ liệu các lớp bộ môn đang phụ trách trong năm học hiện tại.</small>
                </div>
            </div>
        </section>

        <c:if test="${not empty flashMessage}">
            <div class="alert ${flashType == 'error' ? 'alert-error' : (flashType == 'info' ? 'alert-info' : 'alert-success')}">
                ${flashMessage}
            </div>
        </c:if>

        <c:if test="${not empty warningMessage}">
            <div class="alert alert-info">${warningMessage}</div>
        </c:if>

        <section class="metric-grid">
            <article class="metric-card card reveal">
                <span class="metric-label">Điểm trung bình chung</span>
                <strong class="metric-value">${empty metrics ? '0' : metrics.overallAverageDisplay}</strong>
                <small>Dựa trên học sinh thuộc các lớp được phân công nhập điểm.</small>
            </article>

            <article class="metric-card card reveal">
                <span class="metric-label">Tỷ lệ Giỏi + Khá</span>
                <strong class="metric-value accent-good">${empty metrics ? '0%' : metrics.goodPlusRateDisplay}</strong>
                <small>${empty metrics ? '0' : metrics.studentCount} học sinh đã có dữ liệu xếp loại.</small>
            </article>

            <article class="metric-card card reveal">
                <span class="metric-label">Số lớp đang dạy</span>
                <strong class="metric-value accent-warm">${empty summary ? '0' : summary.classCount}</strong>
                <small>${empty summary ? '0' : summary.subjectCount} môn đang được phân công.</small>
            </article>
        </section>

        <div id="subjectDashboardClassData" hidden>
            <c:forEach var="item" items="${dashboard.classSummaries}">
                <span class="class-data-item"
                      data-label="${item.classId}"
                      data-class-name="${item.classLabel}"
                      data-excellent="${item.excellentCount}"
                      data-good="${item.goodCount}"
                      data-average="${item.averageCount}"
                      data-weak="${item.weakCount}"
                      data-good-plus="${item.goodPlusRateValue}"
                      data-average-score="${item.averageScoreDisplay}"></span>
            </c:forEach>
        </div>

        <section class="chart-grid">
            <article class="card chart-card reveal">
                <div class="section-head">
                    <div>
                        <h2>Tổng quan điểm số</h2>
                        <p>Phân bố học lực theo từng lớp đang phụ trách nhập điểm.</p>
                    </div>
                    <div class="chart-switch" role="group" aria-label="Chuyển biểu đồ tổng quan">
                        <button type="button" class="chart-switch-btn active" data-overview-chart="bar">Cột</button>
                        <button type="button" class="chart-switch-btn" data-overview-chart="line">Đường</button>
                    </div>
                </div>

                <div class="chart-stage">
                    <canvas id="subjectOverviewChart" aria-label="Biểu đồ tổng quan điểm số theo lớp"></canvas>
                </div>

                <div class="chart-legend">
                    <span><i class="legend-dot legend-excellent"></i>Giỏi</span>
                    <span><i class="legend-dot legend-good"></i>Khá</span>
                    <span><i class="legend-dot legend-average"></i>Trung bình</span>
                    <span><i class="legend-dot legend-weak"></i>Yếu</span>
                </div>
            </article>

            <article class="card chart-card reveal">
                <div class="section-head">
                    <div>
                        <h2>Phân bố học lực toàn khối</h2>
                        <p>Tổng hợp tất cả học sinh thuộc các lớp bộ môn đang phụ trách.</p>
                    </div>
                </div>

                <div class="donut-shell"
                     data-total-students="${empty metrics ? '0' : metrics.studentCount}"
                     data-excellent-rate="${empty distribution ? '0' : distribution.excellentRateValue}"
                     data-good-rate="${empty distribution ? '0' : distribution.goodRateValue}"
                     data-average-rate="${empty distribution ? '0' : distribution.averageRateValue}"
                     data-weak-rate="${empty distribution ? '0' : distribution.weakRateValue}">
                    <canvas id="subjectDistributionChart" aria-label="Biểu đồ donut phân bố học lực"></canvas>
                    <div class="donut-center">
                        <strong>${empty metrics ? '0' : metrics.studentCount}</strong>
                        <span>Học sinh</span>
                    </div>
                </div>

                <div class="distribution-legend">
                    <span><i class="legend-dot legend-excellent"></i>Giỏi ${empty distribution ? '0%' : distribution.excellentRateDisplay}</span>
                    <span><i class="legend-dot legend-good"></i>Khá ${empty distribution ? '0%' : distribution.goodRateDisplay}</span>
                    <span><i class="legend-dot legend-average"></i>TB ${empty distribution ? '0%' : distribution.averageRateDisplay}</span>
                    <span><i class="legend-dot legend-weak"></i>Yếu ${empty distribution ? '0%' : distribution.weakRateDisplay}</span>
                </div>
            </article>
        </section>

        <section class="card progress-card reveal">
            <div class="section-head">
                <div>
                    <h2>Tiến độ học lực từng lớp</h2>
                    <p>Tỷ lệ Giỏi + Khá (%) và điểm trung bình của từng lớp bộ môn.</p>
                </div>
            </div>

            <div class="progress-list">
                <c:forEach var="item" items="${dashboard.classSummaries}">
                    <article class="progress-item">
                        <div class="progress-line">
                            <strong>${item.classId}</strong>
                            <span>${item.goodPlusRateDisplay}</span>
                        </div>
                        <div class="progress-track">
                            <div class="progress-fill" style="--progress:${item.goodPlusRateValue}%;"></div>
                        </div>
                        <div class="progress-meta">
                            <small>${item.classLabel}</small>
                            <small>Điểm TB: ${item.averageScoreDisplay}</small>
                        </div>
                    </article>
                </c:forEach>

                <c:if test="${empty dashboard.classSummaries}">
                    <div class="empty-state">Chưa có lớp bộ môn hoặc chưa có dữ liệu để hiển thị tiến độ.</div>
                </c:if>
            </div>
        </section>

        <section class="shortcut-grid">
            <a class="shortcut-card card reveal" href="<c:url value='/teacher-subject/score'/>">
                <div class="shortcut-icon shortcut-score">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path stroke-linecap="round" stroke-linejoin="round"
                              d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"/>
                    </svg>
                </div>
                <div>
                    <h3>Quản lý điểm</h3>
                    <p>Mở nhanh danh sách điểm, lọc lớp/môn và nhập hoặc chỉnh sửa điểm.</p>
                </div>
            </a>

            <a class="shortcut-card card reveal" href="<c:url value='/teacher-subject/score/create'/>">
                <div class="shortcut-icon shortcut-create">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M12 4v16m8-8H4"/>
                    </svg>
                </div>
                <div>
                    <h3>Nhập điểm mới</h3>
                    <p>Đi thẳng đến màn hình thêm điểm cho lớp bộ môn đang được phân công.</p>
                </div>
            </a>

            <a class="shortcut-card card reveal" href="<c:url value='/teacher-subject/profile'/>">
                <div class="shortcut-icon shortcut-profile">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path stroke-linecap="round" stroke-linejoin="round"
                              d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/>
                    </svg>
                </div>
                <div>
                    <h3>Thông tin cá nhân</h3>
                    <p>Cập nhật hồ sơ, email, số điện thoại và ảnh đại diện của giáo viên.</p>
                </div>
            </a>
        </section>

        <section class="card spotlight-card reveal">
            <div class="section-head">
                <div>
                    <h2>Bảng điểm nổi bật</h2>
                    <p>Hiển thị tối đa 5 học sinh nổi bật và 5 học sinh yếu trong các lớp được phụ trách.</p>
                </div>
                <div class="spotlight-tabs" role="tablist" aria-label="Chuyển danh sách học sinh nổi bật">
                    <button type="button" class="spotlight-tab active" data-spotlight-tab="top">Top xuất sắc</button>
                    <button type="button" class="spotlight-tab" data-spotlight-tab="support">Yếu</button>
                </div>
            </div>

            <div class="spotlight-table-wrap" data-spotlight-table="top">
                <table class="spotlight-table">
                    <thead>
                    <tr>
                        <th>Họ tên</th>
                        <th>Lớp</th>
                        <th>Điểm TB</th>
                        <th>Xếp loại</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="item" items="${dashboard.topStudents}">
                        <tr>
                            <td>
                                <strong>${item.studentName}</strong>
                                <small>${item.studentId}</small>
                            </td>
                            <td>${item.classId}</td>
                            <td><strong>${item.averageScoreDisplay}</strong></td>
                            <td><span class="rank-chip ${item.performanceCssClass}">${item.performanceLabel}</span></td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty dashboard.topStudents}">
                        <tr>
                            <td colspan="4" class="empty-state">Chưa có dữ liệu để hiển thị top học sinh.</td>
                        </tr>
                    </c:if>
                    </tbody>
                </table>
            </div>

            <div class="spotlight-table-wrap" data-spotlight-table="support" hidden>
                <table class="spotlight-table">
                    <thead>
                    <tr>
                        <th>Họ tên</th>
                        <th>Lớp</th>
                        <th>Điểm TB</th>
                        <th>Xếp loại</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="item" items="${dashboard.supportStudents}">
                        <tr>
                            <td>
                                <strong>${item.studentName}</strong>
                                <small>${item.studentId}</small>
                            </td>
                            <td>${item.classId}</td>
                            <td><strong>${item.averageScoreDisplay}</strong></td>
                            <td><span class="rank-chip ${item.performanceCssClass}">${item.performanceLabel}</span></td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty dashboard.supportStudents}">
                        <tr>
                            <td colspan="4" class="empty-state">Chưa có dữ liệu để hiển thị danh sách học sinh yếu.</td>
                        </tr>
                    </c:if>
                    </tbody>
                </table>
            </div>
        </section>
    </main>
</div>

<script src="<c:url value='/js/teacher-subject/dashboard.js'/>"></script>
</body>
</html>
