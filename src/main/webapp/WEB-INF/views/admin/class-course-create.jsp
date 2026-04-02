<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>${pageTitle}</title>

  <link rel="stylesheet" href="<c:url value='/css/admin-layout.css'/>">
  <link rel="stylesheet" href="<c:url value='/css/admin/class-create.css'/>">
</head>
<body>
<div class="layout">
  <jsp:include page="/WEB-INF/views/admin/_sidebar.jsp"/>

  <main class="main class-create-page">
    <header class="create-header">
      <div class="header-left">
        <h1>ThĂªm khĂ³a há»c má»›i</h1>
        <p>Vui lĂ²ng nháº­p Ä‘áº§y Ä‘á»§ thĂ´ng tin Ä‘á»ƒ táº¡o khĂ³a há»c phá»¥c vá»¥ quáº£n lĂ½ lá»›p.</p>
      </div>
    </header>

    <section class="content">
      <c:if test="${not empty error}">
        <div class="alert alert-error">${error}</div>
      </c:if>

      <section class="card form-card">
        <form method="post"
              action="<c:url value='/admin/class/course/create'/>"
              class="class-create-form"
              autocomplete="off">
          <div class="form-grid">
            <div class="field">
              <label for="idKhoa">MĂ£ khĂ³a há»c <span>*</span></label>
              <input id="idKhoa"
                     type="text"
                     name="idKhoa"
                     maxlength="10"
                     value="${courseForm.idKhoa}"
                     placeholder="VD: K07"
                     required>
            </div>

            <div class="field">
              <label for="tenKhoa">TĂªn khĂ³a há»c <span>*</span></label>
              <input id="tenKhoa"
                     type="text"
                     name="tenKhoa"
                     maxlength="100"
                     value="${courseForm.tenKhoa}"
                     placeholder="VD: KhĂ³a 2025-2028"
                     required>
            </div>

            <div class="field">
              <label for="ngayBatDau">NgĂ y báº¯t Ä‘áº§u <span>*</span></label>
              <input id="ngayBatDau"
                     type="date"
                     name="ngayBatDau"
                     value="${courseForm.ngayBatDau}"
                     required>
            </div>

            <div class="field">
              <label for="ngayKetThuc">NgĂ y káº¿t thĂºc <span>*</span></label>
              <input id="ngayKetThuc"
                     type="date"
                     name="ngayKetThuc"
                     value="${courseForm.ngayKetThuc}"
                     required>
            </div>

            <div class="field">
              <label for="trangThai">Tráº¡ng thĂ¡i <span>*</span></label>
              <select id="trangThai" name="trangThai" required>
                <c:forEach var="status" items="${courseStatusOptions}">
                  <option value="${status.value}" ${courseForm.trangThai == status.value ? 'selected' : ''}>
                    ${status.label}
                  </option>
                </c:forEach>
              </select>
            </div>
          </div>

          <div class="form-actions">
            <a class="btn" href="<c:url value='/admin/class'/>">Quay láº¡i</a>
            <button class="btn primary" type="submit">LÆ°u khĂ³a há»c</button>
          </div>
        </form>
      </section>
    </section>
  </main>
</div>
</body>
</html>

