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

  <main class="main account-page">
    <header class="topbar">
      <div class="topbar-left">
        <h1>Quản lí tài khoản</h1>
        <p>Admin cấp tài khoản giáo viên trực tiếp tại trang này.</p>
      </div>
    </header>

    <section class="content">
      <c:if test="${not empty flashMessage}">
        <div class="alert ${flashType == 'success' ? 'alert-success' : 'alert-error'}">${flashMessage}</div>
      </c:if>
      <c:if test="${not empty error}">
        <div class="alert alert-error">${error}</div>
      </c:if>

      <section class="card account-form-card">
        <h2>Tạo tài khoản giáo viên</h2>
        <p class="card-description">Role gắn mặc định là <strong>Giao_vien</strong>.</p>

        <form:form method="post"
                   action="${pageContext.request.contextPath}/admin/account/teacher/create"
                   modelAttribute="teacherAccountForm"
                   cssClass="account-form">
          <div class="field-grid">
            <div class="field">
              <label for="tenDangNhap">Tên đăng nhập</label>
              <form:input path="tenDangNhap" id="tenDangNhap" maxlength="50" autocomplete="off"/>
              <form:errors path="tenDangNhap" cssClass="field-error"/>
            </div>

            <div class="field">
              <label for="matKhau">Mật khẩu</label>
              <form:password path="matKhau" id="matKhau" maxlength="72" autocomplete="new-password"/>
              <form:errors path="matKhau" cssClass="field-error"/>
              <span class="field-help">Tối thiểu 6 ký tự, bắt buộc có số và ký tự @.</span>
            </div>

            <div class="field">
              <label for="email">Email (không bắt buộc)</label>
              <form:input path="email" id="email" maxlength="100" autocomplete="off"/>
              <form:errors path="email" cssClass="field-error"/>
            </div>

            <div class="field">
              <label for="trangThai">Trạng thái</label>
              <form:select path="trangThai" id="trangThai">
                <form:option value="hoat_dong">Hoạt động</form:option>
                <form:option value="khoa">Khóa</form:option>
              </form:select>
              <form:errors path="trangThai" cssClass="field-error"/>
            </div>
          </div>

          <div class="form-actions">
            <button type="submit" class="btn primary">Tạo tài khoản</button>
          </div>
        </form:form>
      </section>

      <section class="card account-table-card">
        <h2>Danh sách tài khoản</h2>
        <div class="table-wrap">
          <table class="table align-middle mb-0">
            <thead>
            <tr>
              <th>Tên đăng nhập</th>
              <th>Email</th>
              <th>Role</th>
              <th>Trạng thái</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="account" items="${accounts}">
              <tr>
                <td>${account.tenDangNhap}</td>
                <td>
                  <c:choose>
                    <c:when test="${not empty account.email}">${account.email}</c:when>
                    <c:otherwise><span class="empty-text">Chưa cập nhật</span></c:otherwise>
                  </c:choose>
                </td>
                <td>
                  <c:set var="roleBadge" value="badge-role-default"/>
                  <c:if test="${account.vaiTro == 'Admin'}">
                    <c:set var="roleBadge" value="badge-role-admin"/>
                  </c:if>
                  <c:if test="${account.vaiTro == 'Giao_vien'}">
                    <c:set var="roleBadge" value="badge-role-teacher"/>
                  </c:if>
                  <span class="badge-role ${roleBadge}">${account.vaiTro}</span>
                </td>
                <td>
                  <c:choose>
                    <c:when test="${account.trangThai == 'khoa'}">
                      <span class="badge-status badge-status-locked">Khóa</span>
                    </c:when>
                    <c:otherwise>
                      <span class="badge-status badge-status-active">Hoạt động</span>
                    </c:otherwise>
                  </c:choose>
                </td>
              </tr>
            </c:forEach>

            <c:if test="${empty accounts}">
              <tr>
                <td class="empty-text" colspan="4">Chưa có tài khoản nào.</td>
              </tr>
            </c:if>
            </tbody>
          </table>
        </div>
      </section>
    </section>
  </main>
</div>
</body>
</html>
