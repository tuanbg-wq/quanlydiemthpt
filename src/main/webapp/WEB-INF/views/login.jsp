<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>Đăng nhập - Hệ thống Quản lý Điểm THPT</title>

  <link href="https://fonts.googleapis.com/css2?family=Playfair+Display:wght@700;900&family=DM+Sans:wght@300;400;500&display=swap" rel="stylesheet"/>
  <link rel="stylesheet" href="<c:url value='/css/auth-common.css'/>">
  <link rel="stylesheet" href="<c:url value='/css/login.css'/>">
</head>
<body class="auth-page login-page">
<div class="bg">
  <div class="orb orb1"></div>
  <div class="orb orb2"></div>
  <div class="stripe"></div>
</div>

<div class="page">
  <div class="brand">
    <div class="brand-badge">Hệ thống quản lý điểm</div>
    <h1 class="brand-title">Cổng thông tin<br/><span>Học sinh THPT</span></h1>
    <p class="brand-desc">Nền tảng quản lý kết quả học tập toàn diện, cập nhật điểm số và theo dõi học lực theo thời gian thực.</p>

    <div class="brand-stats">
      <div class="stat">
        <span class="stat-num">2,400+</span>
        <span class="stat-lbl">Học sinh</span>
      </div>
      <div class="stat">
        <span class="stat-num">120+</span>
        <span class="stat-lbl">Giáo viên</span>
      </div>
      <div class="stat">
        <span class="stat-num">36</span>
        <span class="stat-lbl">Lớp học</span>
      </div>
    </div>
  </div>

  <div class="card">
    <div class="card-header">
      <div class="card-icon">🎓</div>
      <h2 class="card-title">Chào mừng trở lại</h2>
      <p class="card-sub">Đăng nhập để tiếp tục vào hệ thống</p>
    </div>

    <% if (request.getParameter("error") != null) { %>
    <div class="alert alert-error">
      <span>⚠️</span>
      <span>Sai tài khoản hoặc mật khẩu. Vui lòng thử lại.</span>
    </div>
    <% } %>

    <% if (request.getParameter("logout") != null) { %>
    <div class="alert alert-success">
      <span>✅</span>
      <span>Bạn đã đăng xuất thành công.</span>
    </div>
    <% } %>

    <% if (request.getParameter("expired") != null) { %>
    <div class="alert alert-error">
      <span>⏱️</span>
      <span>Phiên đăng nhập đã hết hạn (12 giờ). Vui lòng đăng nhập lại.</span>
    </div>
    <% } %>

    <form method="post" action="<%=request.getContextPath()%>/login" id="loginForm" data-auth-form="true">
      <div class="field">
        <label for="username">Tên đăng nhập</label>
        <div class="input-wrap">
          <span class="input-icon">👤</span>
          <input type="text" id="username" name="username"
                 placeholder="Nhập tên đăng nhập..." required autocomplete="username"/>
        </div>
      </div>

      <div class="field">
        <label for="password">Mật khẩu</label>
        <div class="input-wrap">
          <span class="input-icon">🔒</span>
          <input type="password" id="password" name="password"
                 placeholder="Nhập mật khẩu..." required autocomplete="current-password"/>
          <button type="button" class="eye-btn" data-password-toggle data-password-input="password" title="Hiện/ẩn mật khẩu">👁</button>
        </div>
      </div>

      <button type="submit" class="btn-submit" id="submitBtn">
        <span class="btn-text">Đăng nhập</span>
        <span class="spinner"></span>
      </button>
    </form>

    <div class="card-footer">
      <span class="footer-text">Chưa có tài khoản? Liên hệ quản trị viên để được cấp.</span>
    </div>
  </div>

  <div class="deco">
    <div class="deco-card">
      <div class="deco-card-label">Năm học</div>
      <div class="deco-card-val">2025-26</div>
      <div class="deco-card-sub">Học kỳ II</div>
    </div>
    <div class="deco-card">
      <div class="deco-card-label">Cập nhật lần cuối</div>
      <div class="deco-card-val">Hôm nay</div>
      <div class="deco-card-sub">08:30 SA</div>
    </div>
    <div class="deco-card">
      <div class="deco-card-label">Trạng thái hệ thống</div>
      <div class="deco-card-val success">Hoạt động</div>
      <div class="deco-card-sub">Uptime 99.9%</div>
    </div>
  </div>
</div>

<script src="<c:url value='/js/auth-form.js'/>"></script>
</body>
</html>
