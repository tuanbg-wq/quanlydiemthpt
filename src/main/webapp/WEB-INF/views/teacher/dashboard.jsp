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
    <link rel="stylesheet" href="<c:url value='/css/teacher/teacher-home.css'/>">
</head>
<body>
<div class="layout">
    <jsp:include page="/WEB-INF/views/teacher/_sidebar.jsp"/>

    <main class="main teacher-page">
        <c:set var="population" value="${dashboardData.classPopulation}"/>
        <c:set var="scoreOverview" value="${dashboardData.scoreOverview}"/>
        <c:set var="conductOverview" value="${dashboardData.conductOverview}"/>

        <section class="hero card">
            <div class="hero-content">
                <h1>Trang chủ giáo viên chủ nhiệm</h1>
                <p>Bảng điều khiển theo dõi dữ liệu thực tế của lớp chủ nhiệm.</p>
            </div>
            <div class="hero-meta">
                <span><strong>Giáo viên:</strong> ${empty scope.teacherName ? 'Chưa xác định' : scope.teacherName}</span>
                <span><strong>Lớp:</strong> ${empty scope.className ? 'Chưa phân công' : scope.className}</span>
                <span><strong>Năm học:</strong> ${empty scope.schoolYear ? 'Chưa xác định' : scope.schoolYear}</span>
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

        <section class="dashboard-grid">
            <article class="card score-card">
                <div class="panel-head">
                    <h2>Tổng quan điểm số</h2>
                    <small>Theo dữ liệu lớp chủ nhiệm</small>
                </div>

                <div class="score-summary">
                    <div class="summary-metric">
                        <span>Điểm trung bình lớp chủ nhiệm</span>
                        <strong>${scoreOverview.classAverageDisplay}</strong>
                    </div>
                    <div class="summary-metric">
                        <span>Tỷ lệ Giỏi + Khá</span>
                        <strong>${scoreOverview.goodPlusRateDisplay}</strong>
                    </div>
                </div>

                <div class="score-distribution">
                    <div class="score-donut"
                         data-excellent-rate="${scoreOverview.excellentRateValue}"
                         data-good-rate="${scoreOverview.goodRateValue}"
                         data-average-rate="${scoreOverview.averageRateValue}"
                         data-weak-rate="${scoreOverview.weakRateValue}">
                        <canvas id="scoreChartCanvas" aria-label="Biểu đồ donut tổng quan điểm số"></canvas>
                        <div class="donut-center">
                            <strong class="score-good-plus-value">0%</strong>
                            <span>Giỏi + Khá</span>
                        </div>
                    </div>

                    <div class="score-legend">
                        <span><i class="dot dot-excellent"></i>Giỏi (${scoreOverview.excellentRateDisplay})</span>
                        <span><i class="dot dot-good"></i>Khá (${scoreOverview.goodRateDisplay})</span>
                        <span><i class="dot dot-average-score"></i>Trung bình (${scoreOverview.averageRateDisplay})</span>
                        <span><i class="dot dot-weak"></i>Yếu (${scoreOverview.weakRateDisplay})</span>
                    </div>
                </div>
            </article>

            <article class="card conduct-card">
                <div class="panel-head with-actions">
                    <div>
                        <h2>Tỷ lệ khen thưởng / kỷ luật</h2>
                        <small>Cập nhật theo dữ liệu lớp chủ nhiệm</small>
                    </div>
                    <div class="chart-switch" role="group" aria-label="Chuyển loại biểu đồ">
                        <button type="button" class="chart-switch-btn active" data-chart-type="donut">Donut</button>
                        <button type="button" class="chart-switch-btn" data-chart-type="bar">Cột</button>
                        <button type="button" class="chart-switch-btn" data-chart-type="line">Đường</button>
                    </div>
                </div>

                <div class="chart-holder conduct-chart"
                     data-reward-rate="${conductOverview.rewardRateValue}"
                     data-discipline-rate="${conductOverview.disciplineRateValue}">
                    <canvas id="conductChartCanvas" aria-label="Biểu đồ khen thưởng và kỷ luật"></canvas>
                </div>

                <div class="chart-legend">
                    <span><i class="dot dot-reward"></i>Số khen thưởng: ${conductOverview.rewardCount} (${conductOverview.rewardRateDisplay})</span>
                    <span><i class="dot dot-discipline"></i>Số kỷ luật: ${conductOverview.disciplineCount} (${conductOverview.disciplineRateDisplay})</span>
                    <span class="total-line">Tổng quyết định: ${conductOverview.totalCount}</span>
                </div>
            </article>
        </section>

        <section class="card class-overview">
            <div class="section-head">
                <h2>Bảng sĩ số lớp theo thực tế</h2>
                <small>Sĩ số và giới tính hiện tại của lớp chủ nhiệm</small>
            </div>

            <div class="overview-grid">
                <div class="overview-table-wrap">
                    <table class="overview-table">
                        <thead>
                        <tr>
                            <th>Chỉ số</th>
                            <th>Giá trị</th>
                            <th>Tỷ lệ</th>
                        </tr>
                        </thead>
                        <tbody>
                        <tr>
                            <td>Sĩ số thực tế</td>
                            <td><strong><fmt:formatNumber value="${population.totalStudents}" groupingUsed="true"/></strong></td>
                            <td>100%</td>
                        </tr>
                        <tr>
                            <td>Nam</td>
                            <td><fmt:formatNumber value="${population.maleStudents}" groupingUsed="true"/></td>
                            <td>${population.maleRateDisplay}</td>
                        </tr>
                        <tr>
                            <td>Nữ</td>
                            <td><fmt:formatNumber value="${population.femaleStudents}" groupingUsed="true"/></td>
                            <td>${population.femaleRateDisplay}</td>
                        </tr>
                        </tbody>
                    </table>
                </div>

                <div class="chart-card">
                    <h3>Biểu đồ giới tính nam / nữ</h3>
                    <div class="chart-holder gender-chart"
                         data-male="${population.maleStudents}"
                         data-female="${population.femaleStudents}">
                        <canvas id="genderChartCanvas" aria-label="Biểu đồ giới tính"></canvas>
                    </div>
                    <div class="chart-legend">
                        <span><i class="dot dot-male"></i>Nam: ${population.maleStudents}</span>
                        <span><i class="dot dot-female"></i>Nữ: ${population.femaleStudents}</span>
                    </div>
                </div>
            </div>
        </section>

        <section class="card quick-actions">
            <a class="btn quick-btn" href="<c:url value='/teacher/student'/>">Quản lý học sinh</a>
            <a class="btn quick-btn" href="<c:url value='/teacher/score'/>">Quản lý điểm</a>
            <a class="btn quick-btn" href="<c:url value='/teacher/conduct'/>">Khen thưởng / Kỷ luật</a>
        </section>
    </main>
</div>

<script src="<c:url value='/js/teacher/teacher-home.js'/>"></script>
</body>
</html>
