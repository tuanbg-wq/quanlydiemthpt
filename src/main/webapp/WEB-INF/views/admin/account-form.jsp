<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>${pageTitle}</title>

  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
  <link rel="stylesheet" href="<c:url value='/css/admin-layout.css'/>">
  <link rel="stylesheet" href="<c:url value='/css/account.css'/>">
</head>
<body>
<div class="layout">
  <jsp:include page="/WEB-INF/views/admin/_sidebar.jsp"/>

  <main class="main account-form-page">
    <header class="topbar">
      <div class="topbar-left">
        <h1>${pageTitle}</h1>
        <p>${creatingMode ? 'Tạo tài khoản mới cho hệ thống.' : 'Cập nhật thông tin tài khoản.'}</p>
      </div>
    </header>

    <section class="content">
      <c:if test="${not empty error}">
        <div class="alert alert-error">${error}</div>
      </c:if>

      <section class="card">
        <c:choose>
          <c:when test="${creatingMode}">
            <c:url var="submitUrl" value="/admin/account/create"/>
          </c:when>
          <c:otherwise>
            <c:url var="submitUrl" value="/admin/account/${accountId}/edit"/>
          </c:otherwise>
        </c:choose>
        <form:form method="post"
                   modelAttribute="accountForm"
                   action="${submitUrl}"
                   cssClass="account-edit-form">
          <div class="row g-3">
            <div class="col-12 col-md-6">
              <label class="form-label" for="tenDangNhap">Tên đăng nhập</label>
              <form:input path="tenDangNhap" id="tenDangNhap" cssClass="form-control" maxlength="50"/>
              <form:errors path="tenDangNhap" cssClass="field-error"/>
            </div>

            <div class="col-12 col-md-6">
              <label class="form-label" for="matKhau">
                Mật khẩu
                <c:if test="${creatingMode}">
                  <span class="required">*</span>
                </c:if>
                <c:if test="${not creatingMode}">
                  <span class="optional">(để trống nếu giữ nguyên)</span>
                </c:if>
              </label>
              <form:password path="matKhau" id="matKhau" cssClass="form-control" maxlength="72"/>
              <form:errors path="matKhau" cssClass="field-error"/>
            </div>

            <div class="col-12 col-md-6">
              <label class="form-label" for="email">Email</label>
              <form:input path="email" id="email" cssClass="form-control" maxlength="100"/>
              <form:errors path="email" cssClass="field-error"/>
            </div>

            <div class="col-12 col-md-6">
              <label class="form-label" for="idVaiTro">Vai trò</label>
              <form:select path="idVaiTro" id="idVaiTro" cssClass="form-select">
                <form:option value="">-- Chọn vai trò --</form:option>
                <c:forEach var="role" items="${roleSelections}">
                  <form:option value="${role.id}">${role.name}</form:option>
                </c:forEach>
              </form:select>
              <form:errors path="idVaiTro" cssClass="field-error"/>
            </div>

            <div class="col-12 col-md-6">
              <label class="form-label" for="trangThai">Trạng thái</label>
              <form:select path="trangThai" id="trangThai" cssClass="form-select">
                <form:option value="hoat_dong">Hoạt động</form:option>
                <form:option value="khoa">Đã khóa</form:option>
              </form:select>
              <form:errors path="trangThai" cssClass="field-error"/>
            </div>

            <div class="col-12 col-md-6">
              <label class="form-label" for="idGiaoVien">Mã giáo viên (tùy chọn)</label>
              <form:input path="idGiaoVien" id="idGiaoVien" cssClass="form-control" maxlength="10"/>
              <form:errors path="idGiaoVien" cssClass="field-error"/>
              <small class="field-help">Nếu là tài khoản giáo viên, nhập mã giáo viên để liên kết hồ sơ.</small>
            </div>
          </div>

          <div class="form-actions">
            <a class="btn" href="<c:url value='/admin/account'/>">Quay lại</a>
            <button type="submit" class="btn primary">${creatingMode ? 'Tạo tài khoản' : 'Lưu thay đổi'}</button>
          </div>
        </form:form>
      </section>
    </section>
  </main>
</div>
</body>
</html>
