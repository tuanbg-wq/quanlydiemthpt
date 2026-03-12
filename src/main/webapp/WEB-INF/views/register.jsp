<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>Đăng ký | EduPortal</title>

  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Playfair+Display:wght@700;900&family=DM+Sans:wght@300;400;500;600&display=swap" rel="stylesheet">
  <link rel="stylesheet" href="<c:url value='/css/auth-common.css'/>">
  <link rel="stylesheet" href="<c:url value='/css/register.css'/>">
</head>
<body class="auth-page register-page">

<div class="bg">
  <div class="orb orb1"></div>
  <div class="orb orb2"></div>
  <div class="stripe"></div>
</div>

<div class="page">
  <div class="welcome">
    <div class="welcome-badge">Nền tảng giáo dục</div>

    <h1 class="welcome-title">
      Chào mừng<br>đến với <span>EduPortal</span>
    </h1>

    <p class="welcome-desc">
      Hệ thống quản lý giảng dạy thông minh dành cho giáo viên và học sinh.
      Tạo tài khoản để bắt đầu hành trình giáo dục của bạn hôm nay.
    </p>

    <div class="features">
      <div class="feature-item">
        <div class="feature-icon">📚</div>
        <div class="feature-text">
          <h4>Quản lý lớp học</h4>
          <p>Tổ chức và theo dõi học sinh, bài tập, điểm số dễ dàng</p>
        </div>
      </div>
      <div class="feature-item">
        <div class="feature-icon">📊</div>
        <div class="feature-text">
          <h4>Báo cáo thông minh</h4>
          <p>Phân tích kết quả học tập theo thời gian thực</p>
        </div>
      </div>
      <div class="feature-item">
        <div class="feature-icon">🔒</div>
        <div class="feature-text">
          <h4>Bảo mật tuyệt đối</h4>
          <p>Dữ liệu được mã hóa và bảo vệ theo tiêu chuẩn cao nhất</p>
        </div>
      </div>
    </div>
  </div>

  <div class="card">
    <div class="card-header">
      <div class="card-icon">🎓</div>
      <h2 class="card-title">Tạo tài khoản</h2>
      <p class="card-sub">Đăng ký với tư cách Giáo viên</p>
    </div>

    <c:if test="${not empty error}">
      <div class="alert alert-error">
        <span>⚠️</span>
        <span>${error}</span>
      </div>
    </c:if>

    <form:form method="post"
               action="${pageContext.request.contextPath}/register"
               modelAttribute="registerRequest"
               id="registerForm"
               data-auth-form="true">

      <div class="field">
        <label for="username">Tên đăng nhập</label>
        <div class="input-wrap">
          <span class="input-icon">👤</span>
          <form:input path="username" id="username" placeholder="Nhập tên đăng nhập..."/>
        </div>
        <form:errors path="username" cssClass="field-error"/>
      </div>

      <div class="field">
        <label for="password">Mật khẩu</label>
        <div class="input-wrap">
          <span class="input-icon">🔑</span>
          <form:password path="password" id="password" placeholder="Nhập mật khẩu..."/>
          <button type="button" class="eye-btn" id="eyeBtn" data-password-toggle data-password-input="password" title="Hiện/ẩn mật khẩu">👁</button>
        </div>
        <form:errors path="password" cssClass="field-error"/>
      </div>

      <div class="field">
        <label for="email">
          Email
          <span class="optional-tag">(không bắt buộc)</span>
        </label>
        <div class="input-wrap">
          <span class="input-icon">✉️</span>
          <form:input path="email" id="email" placeholder="example@email.com"/>
        </div>
        <form:errors path="email" cssClass="field-error"/>
      </div>

      <button type="submit" class="btn-submit" id="submitBtn">
        <span class="btn-text">Tạo tài khoản</span>
        <span class="spinner"></span>
      </button>

    </form:form>

    <div class="card-footer">
      <span class="footer-text">Đã có tài khoản?</span>
      <a href="<%=request.getContextPath()%>/login" class="footer-link">Đăng nhập ngay</a>
    </div>
  </div>
</div>

<script src="<c:url value='/js/auth-form.js'/>"></script>
</body>
</html>
