<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>${pageTitle}</title>
    <link rel="stylesheet" href="<c:url value='/css/admin-layout.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/admin/teacher-info.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/teacher/profile.css'/>">
</head>
<body>
<div class="layout">
    <jsp:include page="/WEB-INF/views/teacher/_sidebar.jsp"/>

    <main class="main teacher-info-page teacher-profile-page">
        <header class="topbar">
            <div class="topbar-left">
                <h1>Thông tin cá nhân</h1>
                <p>Xem toàn bộ hồ sơ giáo viên chủ nhiệm và cập nhật email, ảnh đại diện, số điện thoại.</p>
            </div>
        </header>

        <section class="content">
            <c:if test="${not empty flashMessage}">
                <div class="alert ${flashType == 'error' ? 'alert-error' : (flashType == 'info' ? 'alert-info' : 'alert-success')}">
                    ${flashMessage}
                </div>
            </c:if>

            <c:if test="${not profileData.editable}">
                <div class="alert alert-info">
                    Tài khoản hiện chưa liên kết đầy đủ với hồ sơ giáo viên, nên chưa thể cập nhật thông tin cá nhân.
                </div>
            </c:if>

            <div class="card profile-summary">
                <c:choose>
                    <c:when test="${not empty teacherInfo.avatar}">
                        <img class="profile-avatar" src="<c:url value='${teacherInfo.avatar}'/>" alt="Ảnh giáo viên"/>
                    </c:when>
                    <c:otherwise>
                        <div class="avatar-fallback">GV</div>
                    </c:otherwise>
                </c:choose>
                <div class="profile-main">
                    <h2>${teacherInfo.hoTen}</h2>
                    <div class="profile-pills">
                        <span class="info-pill">Tài khoản: ${empty profileData.username ? '-' : profileData.username}</span>
                        <span class="info-pill">Mã GV: ${teacherInfo.idGiaoVien}</span>
                        <span class="info-pill">Trạng thái tài khoản: ${profileData.accountStatus}</span>
                        <span class="info-pill">Vai trò hiện tại: ${teacherInfo.currentRole}</span>
                        <span class="info-pill">Lớp chủ nhiệm: ${empty scope.className ? 'Chưa phân công' : scope.className}</span>
                    </div>
                </div>
            </div>

            <div class="info-grid">
                <div class="card info-card">
                    <h3>Thông tin cá nhân</h3>
                    <dl>
                        <dt>Mã giáo viên</dt><dd>${teacherInfo.idGiaoVien}</dd>
                        <dt>Họ và tên</dt><dd>${teacherInfo.hoTen}</dd>
                        <dt>Ngày sinh</dt><dd>${teacherInfo.ngaySinh}</dd>
                        <dt>Giới tính</dt><dd>${teacherInfo.gioiTinh}</dd>
                        <dt>Số điện thoại</dt><dd>${teacherInfo.soDienThoai}</dd>
                        <dt>Email</dt><dd>${teacherInfo.email}</dd>
                        <dt>Địa chỉ</dt><dd>${teacherInfo.diaChi}</dd>
                        <dt>Ghi chú</dt><dd>${teacherInfo.ghiChu}</dd>
                    </dl>
                </div>

                <div class="card info-card">
                    <h3>Thông tin công tác</h3>
                    <dl>
                        <dt>Chuyên môn</dt><dd>${teacherInfo.chuyenMon}</dd>
                        <dt>Trình độ</dt><dd>${teacherInfo.trinhDo}</dd>
                        <dt>Ngày vào làm</dt><dd>${teacherInfo.ngayVaoLam}</dd>
                        <dt>Vai trò hiện tại</dt><dd>${teacherInfo.currentRole}</dd>
                        <dt>Năm học vai trò</dt><dd>${teacherInfo.currentRoleSchoolYear}</dd>
                        <dt>Lớp bộ môn phụ trách</dt><dd>${teacherInfo.currentSubjectClasses}</dd>
                        <dt>Lớp chủ nhiệm hiện tại</dt><dd>${empty scope.className ? 'Chưa phân công' : scope.className}</dd>
                        <dt>Trạng thái hồ sơ</dt><dd>${teacherInfo.trangThai}</dd>
                    </dl>
                </div>
            </div>

            <div class="card profile-edit-card">
                <div class="profile-edit-head">
                    <div>
                        <h3>Cập nhật thông tin liên hệ</h3>
                        <p>Chỉ cho phép chỉnh sửa email, số điện thoại và ảnh đại diện.</p>
                    </div>
                </div>

                <form method="post" action="<c:url value='/teacher/profile'/>" enctype="multipart/form-data" class="profile-edit-form">
                    <div class="profile-form-grid">
                        <div class="form-group">
                            <label for="email">Email</label>
                            <input id="email" type="email" name="email" value="${form.email}" ${not profileData.editable ? 'disabled' : ''}>
                            <c:if test="${not empty fieldErrors.email}">
                                <div class="field-error">${fieldErrors.email}</div>
                            </c:if>
                        </div>

                        <div class="form-group">
                            <label for="soDienThoai">Số điện thoại</label>
                            <input id="soDienThoai" type="text" name="soDienThoai" value="${form.soDienThoai}" ${not profileData.editable ? 'disabled' : ''}>
                            <c:if test="${not empty fieldErrors.soDienThoai}">
                                <div class="field-error">${fieldErrors.soDienThoai}</div>
                            </c:if>
                        </div>

                        <div class="form-group form-group-full">
                            <label for="avatar">Ảnh đại diện</label>
                            <div class="avatar-preview-wrap">
                                <c:choose>
                                    <c:when test="${not empty teacherInfo.avatar}">
                                        <img id="avatarPreview" class="avatar-preview-image" src="<c:url value='${teacherInfo.avatar}'/>" alt="Xem trước ảnh đại diện"/>
                                    </c:when>
                                    <c:otherwise>
                                        <div id="avatarPreviewFallback" class="avatar-preview-fallback">GV</div>
                                        <img id="avatarPreview" class="avatar-preview-image" src="" alt="Xem trước ảnh đại diện" hidden/>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                            <input id="avatar" type="file" name="avatar" accept=".png,.jpg,.jpeg,.webp" ${not profileData.editable ? 'disabled' : ''}>
                            <small class="field-hint">Hỗ trợ PNG, JPG, JPEG, WEBP. Tối đa 5MB.</small>
                        </div>
                    </div>

                    <div class="profile-form-actions">
                        <button class="btn primary" type="submit" ${not profileData.editable ? 'disabled' : ''}>Lưu thay đổi</button>
                    </div>
                </form>
            </div>

            <div class="card history-card">
                <h3>Lịch sử công tác tại trường</h3>
                <div class="history-table-wrap">
                    <table class="history-table">
                        <thead>
                        <tr>
                            <th>Năm học</th>
                            <th>Vai trò</th>
                            <th>Lớp chủ nhiệm</th>
                            <th>Lớp bộ môn phụ trách</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="item" items="${teacherInfo.workHistory}">
                            <tr>
                                <td>${item.schoolYear}</td>
                                <td>${item.roleName}</td>
                                <td>${item.homeroomClasses}</td>
                                <td>${item.subjectClassNames}</td>
                            </tr>
                        </c:forEach>

                        <c:if test="${empty teacherInfo.workHistory}">
                            <tr>
                                <td class="empty-note" colspan="4">Chưa có lịch sử công tác để hiển thị.</td>
                            </tr>
                        </c:if>
                        </tbody>
                    </table>
                </div>
            </div>
        </section>
    </main>
</div>
<script>
    (function () {
        const avatarInput = document.getElementById('avatar');
        const avatarPreview = document.getElementById('avatarPreview');
        const avatarPreviewFallback = document.getElementById('avatarPreviewFallback');

        if (!avatarInput || !avatarPreview) {
            return;
        }

        avatarInput.addEventListener('change', function () {
            const file = avatarInput.files && avatarInput.files[0];
            if (!file || !file.type || !file.type.startsWith('image/')) {
                return;
            }

            const reader = new FileReader();
            reader.onload = function (event) {
                avatarPreview.src = event.target && event.target.result ? event.target.result : '';
                avatarPreview.hidden = false;
                if (avatarPreviewFallback) {
                    avatarPreviewFallback.hidden = true;
                }
            };
            reader.readAsDataURL(file);
        });
    })();
</script>
</body>
</html>
