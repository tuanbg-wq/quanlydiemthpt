<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

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

  <c:set var="viewSemester" value="${empty selectedHocKy ? '0' : selectedHocKy}"/>
  <c:url var="tabHk1Url" value="/admin/score/detail">
    <c:param name="studentId" value="${summary.studentId}"/>
    <c:param name="subjectId" value="${summary.subjectId}"/>
    <c:param name="namHoc" value="${summary.namHoc}"/>
    <c:param name="hocKy" value="1"/>
  </c:url>
  <c:url var="tabHk2Url" value="/admin/score/detail">
    <c:param name="studentId" value="${summary.studentId}"/>
    <c:param name="subjectId" value="${summary.subjectId}"/>
    <c:param name="namHoc" value="${summary.namHoc}"/>
    <c:param name="hocKy" value="2"/>
  </c:url>
  <c:url var="tabYearUrl" value="/admin/score/detail">
    <c:param name="studentId" value="${summary.studentId}"/>
    <c:param name="subjectId" value="${summary.subjectId}"/>
    <c:param name="namHoc" value="${summary.namHoc}"/>
    <c:param name="hocKy" value="0"/>
  </c:url>
  <c:url var="excelUrl" value="/admin/score/detail/export/excel">
    <c:param name="studentId" value="${summary.studentId}"/>
    <c:param name="subjectId" value="${summary.subjectId}"/>
    <c:param name="namHoc" value="${summary.namHoc}"/>
    <c:param name="hocKy" value="${viewSemester}"/>
  </c:url>
  <c:url var="pdfUrl" value="/admin/score/detail/export/pdf">
    <c:param name="studentId" value="${summary.studentId}"/>
    <c:param name="subjectId" value="${summary.subjectId}"/>
    <c:param name="namHoc" value="${summary.namHoc}"/>
    <c:param name="hocKy" value="${viewSemester}"/>
  </c:url>

  <main class="main score-detail-page">
    <header class="score-header score-detail-header">
      <div class="header-left">
        <h1>Chi tiết điểm số: ${summary.subjectName}</h1>
        <p>Xem chi tiết kết quả học tập theo học kỳ hoặc cả năm.</p>
      </div>
      <div class="header-right detail-header-actions">
        <a class="btn" href="<c:url value='/admin/score'/>">Quay lại</a>
        <a class="btn detail-export-excel" href="${excelUrl}">Xuất Excel</a>
        <a class="btn primary" href="${pdfUrl}">Xuất PDF</a>
      </div>
    </header>

    <section class="content">
      <section class="card score-detail-overview">
        <div class="overview-main">
          <h2>${summary.studentName}</h2>
          <p>Mã HS: ${summary.studentId} • Lớp: ${summary.className} • Năm học: ${summary.namHoc}</p>
        </div>
        <div class="overview-side">
          <span class="overview-label">Môn học</span>
          <strong>${summary.subjectName}</strong>
        </div>
      </section>

      <section class="card score-detail-tabs">
        <a class="detail-tab ${viewSemester == '1' ? 'active' : ''}" href="${tabHk1Url}">Học kỳ I</a>
        <a class="detail-tab ${viewSemester == '2' ? 'active' : ''}" href="${tabHk2Url}">Học kỳ II</a>
        <a class="detail-tab ${viewSemester == '0' ? 'active' : ''}" href="${tabYearUrl}">Cả năm</a>
      </section>

      <section class="card score-detail-body">
        <div class="rule-inline">
          <strong>Số cột điểm thường xuyên cho môn: ${detailData.frequentColumns}</strong>
          <span>${detailData.formulaText}</span>
        </div>

        <div class="detail-semester-grid ${viewSemester == '0' ? '' : 'single'}">
          <c:if test="${viewSemester == '0' || viewSemester == '1'}">
            <article class="detail-semester-card">
              <header>
                <h3>Học kỳ I</h3>
                <p>Giáo viên chấm: ${empty detailData.filter.teacherHk1 ? '-' : detailData.filter.teacherHk1}</p>
              </header>
              <div class="detail-score-table">
                <table>
                  <thead>
                  <tr>
                    <c:forEach begin="1" end="${detailData.frequentColumns}" var="txIndex">
                      <th>TX ${txIndex}</th>
                    </c:forEach>
                    <th>Giữa kỳ</th>
                    <th>Cuối kỳ</th>
                    <th>ĐTB HKI</th>
                  </tr>
                  </thead>
                  <tbody>
                  <tr>
                    <c:forEach begin="1" end="${detailData.frequentColumns}" var="txIndex">
                      <td>${empty detailData.hk1Input.frequentScores[txIndex - 1] ? '-' : detailData.hk1Input.frequentScores[txIndex - 1]}</td>
                    </c:forEach>
                    <td>${empty detailData.hk1Input.midterm ? '-' : detailData.hk1Input.midterm}</td>
                    <td>${empty detailData.hk1Input.finalScore ? '-' : detailData.hk1Input.finalScore}</td>
                    <td><span class="total-badge">${detailData.hk1Input.averageDisplay}</span></td>
                  </tr>
                  </tbody>
                </table>
              </div>
              <div class="detail-conduct-box">
                <div>
                  <span>Hạnh kiểm</span>
                  <strong>${detailData.hk1Conduct.displayValue}</strong>
                </div>
                <div>
                  <span>Nhận xét</span>
                  <p>${empty detailData.hk1Conduct.comment ? '-' : detailData.hk1Conduct.comment}</p>
                </div>
              </div>
            </article>
          </c:if>

          <c:if test="${viewSemester == '0' || viewSemester == '2'}">
            <article class="detail-semester-card">
              <header>
                <h3>Học kỳ II</h3>
                <p>Giáo viên chấm: ${empty detailData.filter.teacherHk2 ? '-' : detailData.filter.teacherHk2}</p>
              </header>
              <div class="detail-score-table">
                <table>
                  <thead>
                  <tr>
                    <c:forEach begin="1" end="${detailData.frequentColumns}" var="txIndex">
                      <th>TX ${txIndex}</th>
                    </c:forEach>
                    <th>Giữa kỳ</th>
                    <th>Cuối kỳ</th>
                    <th>ĐTB HKII</th>
                  </tr>
                  </thead>
                  <tbody>
                  <tr>
                    <c:forEach begin="1" end="${detailData.frequentColumns}" var="txIndex">
                      <td>${empty detailData.hk2Input.frequentScores[txIndex - 1] ? '-' : detailData.hk2Input.frequentScores[txIndex - 1]}</td>
                    </c:forEach>
                    <td>${empty detailData.hk2Input.midterm ? '-' : detailData.hk2Input.midterm}</td>
                    <td>${empty detailData.hk2Input.finalScore ? '-' : detailData.hk2Input.finalScore}</td>
                    <td><span class="total-badge">${detailData.hk2Input.averageDisplay}</span></td>
                  </tr>
                  </tbody>
                </table>
              </div>
              <div class="detail-conduct-box">
                <div>
                  <span>Hạnh kiểm</span>
                  <strong>${detailData.hk2Conduct.displayValue}</strong>
                </div>
                <div>
                  <span>Nhận xét</span>
                  <p>${empty detailData.hk2Conduct.comment ? '-' : detailData.hk2Conduct.comment}</p>
                </div>
              </div>
            </article>
          </c:if>
        </div>

        <c:if test="${viewSemester == '0'}">
          <div class="detail-year-summary">
            <h3>Kết quả cả năm</h3>
            <div class="year-metrics">
              <div>
                <span>ĐTB HKI</span>
                <strong>${detailData.hk1Input.averageDisplay}</strong>
              </div>
              <div>
                <span>ĐTB HKII</span>
                <strong>${detailData.hk2Input.averageDisplay}</strong>
              </div>
              <div>
                <span>ĐTB cả năm</span>
                <strong>${detailData.yearAverageDisplay}</strong>
              </div>
              <div>
                <span>Hạnh kiểm cả năm</span>
                <strong>${detailData.yearConduct.displayValue}</strong>
              </div>
            </div>
            <div class="year-comment">
              <span>Nhận xét cả năm</span>
              <p>${empty detailData.yearConduct.comment ? '-' : detailData.yearConduct.comment}</p>
            </div>
          </div>
        </c:if>
      </section>
    </section>
  </main>
</div>
</body>
</html>
