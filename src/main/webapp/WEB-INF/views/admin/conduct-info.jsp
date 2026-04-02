<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>${pageTitle}</title>
  <link rel="stylesheet" href="<c:url value='/css/admin-layout.css'/>">
  <link rel="stylesheet" href="<c:url value='/css/admin/conduct-list.css'/>">
</head>
<body>
<div class="layout">
  <jsp:include page="/WEB-INF/views/admin/_sidebar.jsp"/>

  <main class="main conduct-info-page">
    <section class="info-card">
      <h2>ThĂ´ng tin quyáº¿t Ä‘á»‹nh</h2>
      <div class="info-grid">
        <div class="info-item"><span>MĂ£ há»c sinh</span><strong>${detail.idHocSinh}</strong></div>
        <div class="info-item"><span>Há» tĂªn</span><strong>${detail.tenHocSinh}</strong></div>
        <div class="info-item"><span>Lá»›p</span><strong>${detail.tenLop}</strong></div>
        <div class="info-item"><span>Loáº¡i</span><strong>${detail.loaiDisplay}</strong></div>
        <div class="info-item"><span>Loáº¡i chi tiáº¿t</span><strong>${empty detail.loaiChiTiet ? '-' : detail.loaiChiTiet}</strong></div>
        <div class="info-item"><span>Sá»‘ quyáº¿t Ä‘á»‹nh</span><strong>${empty detail.soQuyetDinh ? '-' : detail.soQuyetDinh}</strong></div>
        <div class="info-item"><span>NgĂ y ban hĂ nh</span><strong>${detail.ngayBanHanh}</strong></div>
        <div class="info-item"><span>NÄƒm há»c / Há»c ká»³</span><strong>${detail.namHoc} â€¢ ${detail.hocKyDisplay}</strong></div>
        <div class="info-item full"><span>Ná»™i dung chi tiáº¿t</span><p>${detail.noiDungChiTiet}</p></div>
        <div class="info-item full"><span>Ghi chĂº</span><p>${empty detail.ghiChu ? '-' : detail.ghiChu}</p></div>
      </div>

      <div class="form-actions-bottom">
        <a class="btn btn-outline" href="<c:url value='/admin/conduct'/>">Quay láº¡i danh sĂ¡ch</a>
        <a class="btn btn-primary" href="<c:url value='/admin/conduct/${detail.eventId}/edit'/>">Chá»‰nh sá»­a</a>
      </div>
    </section>
  </main>
</div>
</body>
</html>

