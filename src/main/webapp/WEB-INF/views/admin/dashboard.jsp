<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>DIEMLYTA - Dashboard</title>

  <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&display=swap" rel="stylesheet">
  <link rel="stylesheet" href="<c:url value='/css/admin-layout.css'/>">
  <link rel="stylesheet" href="<c:url value='/css/dashboard.css'/>">
</head>
<body>

<div class="layout">
  <jsp:include page="/WEB-INF/views/admin/_sidebar.jsp" />

  <main class="main dashboard-page">
    <div class="main-header">
      <h1>Tổng quan Dashboard</h1>
      <p>Chào mừng trở lại! Dưới đây là các lối tắt nhanh và thông báo mới nhất cho hôm nay.</p>
    </div>

    <!-- Stats -->
    <div class="stats">
      <div class="stat-card">
        <div class="stat-icon blue">👨‍🎓</div>
        <div>
          <div class="stat-value">${soHocSinh}</div>
          <div class="stat-label">Học sinh</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon green">👩‍🏫</div>
        <div>
          <div class="stat-value">${soGiaoVien}</div>
          <div class="stat-label">Giáo viên</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon orange">📚</div>
        <div>
          <div class="stat-value">${soMonHoc}</div>
          <div class="stat-label">Môn học</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon purple">📝</div>
        <div>
          <div class="stat-value">${soBaiKiemTra}</div>
          <div class="stat-label">Bài kiểm tra</div>
        </div>
      </div>
    </div>

    <!-- Quick access -->
    <div class="section-title">Truy cập nhanh</div>
    <div class="cards-grid">
      <div class="card">
        <div class="card-icon">
          <svg fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.8">
            <path stroke-linecap="round" stroke-linejoin="round"
                  d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z"/>
          </svg>
        </div>
        <h3>Thêm học sinh</h3>
        <p>Đăng ký hồ sơ học sinh mới vào hệ thống quản lý tập trung.</p>
        <a class="card-btn btn-navy" href="<c:url value='/admin/student'/>">Bắt đầu ngay</a>
      </div>

      <div class="card">
        <div class="card-icon">
          <svg fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.8">
            <path stroke-linecap="round" stroke-linejoin="round"
                  d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z"/>
          </svg>
        </div>
        <h3>Giáo viên</h3>
        <p>Quản lý danh sách, phân công giảng dạy và thông tin giáo viên.</p>
        <a class="card-btn btn-teal" href="<c:url value='/admin/teachers'/>">Xem danh sách</a>
      </div>

      <div class="card">
        <div class="card-icon">
          <svg fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.8">
            <path stroke-linecap="round" stroke-linejoin="round"
                  d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01"/>
          </svg>
        </div>
        <h3>Điểm</h3>
        <p>Nhập điểm, cập nhật kết quả học tập và xuất báo cáo định kỳ.</p>
        <a class="card-btn btn-green" href="<c:url value='/admin/scores'/>">Quản lý điểm</a>
      </div>

      <div class="card">
        <div class="card-icon">
          <svg fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.8">
            <path stroke-linecap="round" stroke-linejoin="round"
                  d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/>
          </svg>
        </div>
        <h3>Tài khoản</h3>
        <p>Cấu hình tài khoản người dùng, phân quyền và bảo mật hệ thống.</p>
        <a class="card-btn btn-slate" href="<c:url value='/admin/account'/>">Cài đặt</a>
      </div>

      <div class="card">
        <div class="card-icon">
          <svg fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.8">
            <path stroke-linecap="round" stroke-linejoin="round"
                  d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"/>
          </svg>
        </div>
        <h3>Môn học</h3>
        <p>Thêm, chỉnh sửa thông tin môn học và chương trình giảng dạy.</p>
        <a class="card-btn btn-blue" href="<c:url value='/admin/subjects'/>">Xem môn học</a>
      </div>

      <div class="card">
        <div class="card-icon danger">
          <svg fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.8">
            <path stroke-linecap="round" stroke-linejoin="round"
                  d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"/>
          </svg>
        </div>
        <h3>Đăng xuất</h3>
        <p>Kết thúc phiên làm việc và đăng xuất khỏi hệ thống an toàn.</p>

        <form method="post" action="<c:url value='/logout'/>">
          <button class="card-btn btn-red" type="submit">Đăng xuất</button>
        </form>
      </div>
    </div>

    <!-- Recent activity -->
    <div class="section-title">Hoạt động gần đây</div>
    <div class="table-card">
      <div class="table-header">
        Danh sách học sinh mới
        <a href="<c:url value='/admin/students'/>">Xem tất cả →</a>
      </div>

      <table>
        <thead>
          <tr>
            <th>Tên học sinh</th>
            <th>Lớp</th>
            <th>Môn học</th>
            <th>Điểm TB</th>
            <th>Trạng thái</th>
          </tr>
        </thead>

        <tbody>
          <tr>
            <td><strong>Nguyễn Văn An</strong></td>
            <td>10A1</td>
            <td>Toán, Lý, Hóa</td>
            <td>8.5</td>
            <td><span class="badge badge-green">Xuất sắc</span></td>
          </tr>
          <tr>
            <td><strong>Trần Thị Bình</strong></td>
            <td>11B2</td>
            <td>Văn, Sử, Địa</td>
            <td>7.8</td>
            <td><span class="badge badge-blue">Khá</span></td>
          </tr>
          <tr>
            <td><strong>Lê Minh Cường</strong></td>
            <td>12A3</td>
            <td>Anh, Tin, Toán</td>
            <td>6.4</td>
            <td><span class="badge badge-yellow">Trung bình</span></td>
          </tr>
          <tr>
            <td><strong>Phạm Thị Dung</strong></td>
            <td>10C1</td>
            <td>Lý, Hóa, Sinh</td>
            <td>9.1</td>
            <td><span class="badge badge-green">Xuất sắc</span></td>
          </tr>
          <tr>
            <td><strong>Hoàng Quốc Đạt</strong></td>
            <td>11A4</td>
            <td>Toán, Anh, Tin</td>
            <td>8.0</td>
            <td><span class="badge badge-blue">Khá</span></td>
          </tr>
        </tbody>
      </table>
    </div>
  </main>
</div>

</body>
</html>
