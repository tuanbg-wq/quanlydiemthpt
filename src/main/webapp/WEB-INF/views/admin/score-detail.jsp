<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

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
        <h1>Chi tiáº¿t Ä‘iá»ƒm sá»‘: ${summary.subjectName}</h1>
        <p>Xem chi tiáº¿t káº¿t quáº£ há»c táº­p theo há»c ká»³ hoáº·c cáº£ nÄƒm.</p>
      </div>
      <div class="header-right detail-header-actions">
        <a class="btn" href="<c:url value='/admin/score'/>">Quay láº¡i</a>
        <a class="btn detail-export-excel" href="${excelUrl}">Xuáº¥t Excel</a>
        <a class="btn primary" href="${pdfUrl}">Xuáº¥t PDF</a>
      </div>
    </header>

    <section class="content">
      <c:if test="${not empty flashMessage}">
        <div class="alert ${flashType == 'error' ? 'alert-error' : (flashType == 'info' ? 'alert-info' : 'alert-success')}">
          ${flashMessage}
        </div>
      </c:if>

      <section class="card score-detail-overview">
        <div class="overview-main">
          <h2>${summary.studentName}</h2>
          <p>MĂ£ HS: ${summary.studentId} â€¢ Lá»›p: ${summary.className} â€¢ Khá»‘i: ${empty summary.grade ? '-' : summary.grade} â€¢ KhĂ³a: ${empty summary.courseDisplay ? '-' : summary.courseDisplay} â€¢ NÄƒm há»c: ${summary.namHoc}</p>
        </div>
        <div class="overview-side">
          <span class="overview-label">MĂ´n há»c</span>
          <strong>${summary.subjectName}</strong>
        </div>
      </section>

      <section class="card score-detail-tabs">
        <a class="detail-tab ${viewSemester == '1' ? 'active' : ''}" href="${tabHk1Url}">Há»c ká»³ I</a>
        <a class="detail-tab ${viewSemester == '2' ? 'active' : ''}" href="${tabHk2Url}">Há»c ká»³ II</a>
        <a class="detail-tab ${viewSemester == '0' ? 'active' : ''}" href="${tabYearUrl}">Cáº£ nÄƒm</a>
      </section>

      <section class="card score-detail-body">
        <div class="rule-inline">
          <strong>Sá»‘ cá»™t Ä‘iá»ƒm thÆ°á»ng xuyĂªn cho mĂ´n: ${detailData.frequentColumns}</strong>
          <span>${detailData.formulaText}</span>
          <span>ÄTBmcn = (ÄTBhkI + 2 Ă— ÄTBhkII) / 3</span>
        </div>

        <div class="detail-semester-grid ${viewSemester == '0' ? '' : 'single'}">
          <c:if test="${viewSemester == '0' || viewSemester == '1'}">
            <article class="detail-semester-card">
              <header>
                <h3>Há»c ká»³ I</h3>
                <p>GiĂ¡o viĂªn cháº¥m: ${empty detailData.filter.teacherHk1 ? '-' : detailData.filter.teacherHk1}</p>
              </header>
              <div class="detail-score-table">
                <table>
                  <thead>
                  <tr>
                    <c:forEach begin="1" end="${detailData.frequentColumns}" var="txIndex">
                      <th>TX ${txIndex}</th>
                    </c:forEach>
                    <th>Giá»¯a ká»³</th>
                    <th>Cuá»‘i ká»³</th>
                    <th>ÄTB HKI</th>
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
            </article>
          </c:if>

          <c:if test="${viewSemester == '0' || viewSemester == '2'}">
            <article class="detail-semester-card">
              <header>
                <h3>Há»c ká»³ II</h3>
                <p>GiĂ¡o viĂªn cháº¥m: ${empty detailData.filter.teacherHk2 ? '-' : detailData.filter.teacherHk2}</p>
              </header>
              <div class="detail-score-table">
                <table>
                  <thead>
                  <tr>
                    <c:forEach begin="1" end="${detailData.frequentColumns}" var="txIndex">
                      <th>TX ${txIndex}</th>
                    </c:forEach>
                    <th>Giá»¯a ká»³</th>
                    <th>Cuá»‘i ká»³</th>
                    <th>ÄTB HKII</th>
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
            </article>
          </c:if>
        </div>

        <c:if test="${viewSemester == '0'}">
          <div class="detail-year-summary">
            <h3>Káº¿t quáº£ cáº£ nÄƒm</h3>
            <div class="year-metrics">
              <div>
                <span>ÄTB HKI</span>
                <strong>${detailData.hk1Input.averageDisplay}</strong>
              </div>
              <div>
                <span>ÄTB HKII</span>
                <strong>${detailData.hk2Input.averageDisplay}</strong>
              </div>
              <div>
                <span>ÄTB cáº£ nÄƒm</span>
                <strong>${detailData.yearAverageDisplay}</strong>
              </div>
            </div>
          </div>
        </c:if>
      </section>
    </section>
  </main>
</div>
</body>
</html>

