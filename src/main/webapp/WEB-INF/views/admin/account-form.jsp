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
  <link rel="stylesheet" href="<c:url value='/css/admin/account.css'/>">
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
        <c:url var="teacherSuggestUrl" value="/admin/account/suggest/teachers"/>
        <c:url var="teacherProfileUrl" value="/admin/account/teacher-profile"/>

        <form:form method="post"
                   modelAttribute="accountForm"
                   action="${submitUrl}"
                   cssClass="account-edit-form"
                   data-teacher-suggest-url="${teacherSuggestUrl}"
                   data-teacher-profile-url="${teacherProfileUrl}"
                   data-account-id="${accountId}">
          <div class="row g-3">
            <div class="col-12 col-md-6">
              <label class="form-label" for="tenDangNhap">Tên đăng nhập</label>
              <form:input path="tenDangNhap" id="tenDangNhap" cssClass="form-control" maxlength="50"/>
              <form:errors path="tenDangNhap" cssClass="field-error"/>
            </div>

            <div class="col-12 col-md-6">
              <c:choose>
                <c:when test="${creatingMode}">
                  <label class="form-label" for="matKhau">
                    Mật khẩu
                    <span class="required">*</span>
                  </label>
                  <form:password path="matKhau" id="matKhau" cssClass="form-control" maxlength="72"/>
                  <small class="field-help">Mật khẩu phải có tối thiểu 5 ký tự, có số và ký tự @.</small>
                  <form:errors path="matKhau" cssClass="field-error"/>
                </c:when>
                <c:otherwise>
                  <label class="form-label" for="matKhauHienTai">Mật khẩu hiện tại (text)</label>
                  <form:input path="matKhauHienTai" id="matKhauHienTai" cssClass="form-control" maxlength="72" readonly="true"/>
                  <small class="field-help">Giá trị này hiển thị để đối chiếu, bạn chỉ cần nhập mật khẩu mới nếu muốn thay đổi.</small>
                  <form:errors path="matKhauHienTai" cssClass="field-error"/>

                  <label class="form-label mt-2" for="matKhau">
                    Mật khẩu mới
                    <span class="optional">(để trống nếu giữ nguyên)</span>
                  </label>
                  <form:password path="matKhau" id="matKhau" cssClass="form-control" maxlength="72"/>
                  <small class="field-help">Mật khẩu mới phải có tối thiểu 5 ký tự, có số và ký tự @.</small>
                  <form:errors path="matKhau" cssClass="field-error"/>
                </c:otherwise>
              </c:choose>
            </div>

            <div class="col-12 col-md-6">
              <label class="form-label" for="email">Email</label>
              <form:input path="email" id="email" cssClass="form-control" maxlength="100"/>
              <form:errors path="email" cssClass="field-error"/>
            </div>

            <div class="col-12 col-md-6">
              <label class="form-label" for="vaiTroMa">Vai trò</label>
              <form:select path="vaiTroMa" id="vaiTroMa" cssClass="form-select">
                <form:option value="">-- Chọn vai trò --</form:option>
                <c:forEach var="role" items="${roleSelections}">
                  <form:option value="${role.value}">${role.label}</form:option>
                </c:forEach>
              </form:select>
              <form:errors path="vaiTroMa" cssClass="field-error"/>
            </div>

            <div class="col-12 col-md-6">
              <label class="form-label" for="trangThai">Trạng thái</label>
              <form:select path="trangThai" id="trangThai" cssClass="form-select">
                <form:option value="hoat_dong">Hoạt động</form:option>
                <form:option value="khoa">Đã khóa</form:option>
              </form:select>
              <form:errors path="trangThai" cssClass="field-error"/>
            </div>
          </div>

          <section class="teacher-fields mt-3" id="teacherFieldsSection">
            <h3 class="teacher-fields-title">Thông tin giáo viên liên kết</h3>
            <div class="row g-3">
              <div class="col-12 col-md-6">
                <label class="form-label" for="idGiaoVien">Mã giáo viên</label>
                <form:input path="idGiaoVien"
                            id="idGiaoVien"
                            cssClass="form-control"
                            maxlength="10"
                            autocomplete="off"
                            list="teacherSuggestList"/>
                <datalist id="teacherSuggestList"></datalist>
                <form:errors path="idGiaoVien" cssClass="field-error"/>
                <small class="field-help">Nhập mã giáo viên, hệ thống sẽ gợi ý giáo viên hiện có.</small>
              </div>

              <div class="col-12 col-md-6">
                <label class="form-label" for="hoTenGiaoVien">Họ và tên giáo viên</label>
                <form:input path="hoTenGiaoVien" id="hoTenGiaoVien" cssClass="form-control" readonly="true"/>
              </div>

              <div class="col-12 col-md-4">
                <label class="form-label" for="gioiTinhGiaoVien">Giới tính</label>
                <form:input path="gioiTinhGiaoVien" id="gioiTinhGiaoVien" cssClass="form-control" readonly="true"/>
              </div>

              <div class="col-12 col-md-4">
                <label class="form-label" for="ngaySinhGiaoVien">Ngày sinh</label>
                <form:input path="ngaySinhGiaoVien" id="ngaySinhGiaoVien" cssClass="form-control" readonly="true"/>
              </div>

              <div class="col-12 col-md-4">
                <label class="form-label" for="soDienThoaiGiaoVien">Số điện thoại</label>
                <form:input path="soDienThoaiGiaoVien" id="soDienThoaiGiaoVien" cssClass="form-control" readonly="true"/>
              </div>

              <div class="col-12">
                <label class="form-label" for="monDayGiaoVien">Môn dạy</label>
                <form:input path="monDayGiaoVien" id="monDayGiaoVien" cssClass="form-control" readonly="true"/>
              </div>
            </div>
          </section>

          <div class="form-actions">
            <a class="btn" href="<c:url value='/admin/account'/>">Quay lại</a>
            <button type="submit" class="btn primary">${creatingMode ? 'Tạo tài khoản' : 'Lưu thay đổi'}</button>
          </div>
        </form:form>
      </section>
    </section>
  </main>
</div>

<script>
  (function () {
    const form = document.querySelector('.account-edit-form');
    if (!form) {
      return;
    }

    const suggestUrl = form.dataset.teacherSuggestUrl || '';
    const profileUrl = form.dataset.teacherProfileUrl || '';
    const accountId = (form.dataset.accountId || '').trim();

    const roleSelect = document.getElementById('vaiTroMa');
    const teacherSection = document.getElementById('teacherFieldsSection');
    const teacherIdInput = document.getElementById('idGiaoVien');
    const teacherSuggestList = document.getElementById('teacherSuggestList');
    const emailInput = document.getElementById('email');

    const hoTenInput = document.getElementById('hoTenGiaoVien');
    const gioiTinhInput = document.getElementById('gioiTinhGiaoVien');
    const ngaySinhInput = document.getElementById('ngaySinhGiaoVien');
    const monDayInput = document.getElementById('monDayGiaoVien');
    const soDienThoaiInput = document.getElementById('soDienThoaiGiaoVien');

    function isTeacherRole(roleCode) {
      return roleCode === 'GVCN' || roleCode === 'GVBM';
    }

    function clearTeacherFields() {
      hoTenInput.value = '';
      gioiTinhInput.value = '';
      ngaySinhInput.value = '';
      monDayInput.value = '';
      soDienThoaiInput.value = '';
    }

    function toggleTeacherSection() {
      const teacherMode = isTeacherRole(roleSelect.value) || (teacherIdInput.value || '').trim() !== '';
      teacherSection.hidden = false;
      teacherIdInput.disabled = false;

      if (!teacherMode) {
        teacherSuggestList.innerHTML = '';
        clearTeacherFields();
      }
    }

    function buildUrl(base, params) {
      const url = new URL(base, window.location.origin);
      Object.keys(params).forEach(function (key) {
        const value = params[key];
        if (value !== null && value !== undefined && String(value).trim() !== '') {
          url.searchParams.set(key, String(value).trim());
        }
      });
      return url.toString();
    }

    async function loadTeacherSuggestions(q) {
      if (!suggestUrl) {
        return;
      }

      try {
        const endpoint = buildUrl(suggestUrl, { q: q || '', accountId: accountId });
        const response = await fetch(endpoint, { headers: { 'Accept': 'application/json' } });
        if (!response.ok) {
          return;
        }
        const items = await response.json();
        teacherSuggestList.innerHTML = '';
        (items || []).forEach(function (item) {
          const option = document.createElement('option');
          option.value = item.idGiaoVien || '';
          option.label = item.label || option.value;
          teacherSuggestList.appendChild(option);
        });
      } catch (error) {
        teacherSuggestList.innerHTML = '';
      }
    }

    async function loadTeacherProfile() {
      const teacherId = (teacherIdInput.value || '').trim();
      if (!teacherId || !profileUrl) {
        clearTeacherFields();
        return;
      }

      try {
        const endpoint = buildUrl(profileUrl, { teacherId: teacherId, accountId: accountId });
        const response = await fetch(endpoint, { headers: { 'Accept': 'application/json' } });
        if (!response.ok) {
          clearTeacherFields();
          return;
        }
        const profile = await response.json();
        if (!profile) {
          clearTeacherFields();
          return;
        }

        teacherIdInput.value = profile.idGiaoVien || teacherId.toUpperCase();
        hoTenInput.value = profile.hoTen && profile.hoTen !== '-' ? profile.hoTen : '';
        gioiTinhInput.value = profile.gioiTinh && profile.gioiTinh !== '-' ? profile.gioiTinh : '';
        ngaySinhInput.value = profile.ngaySinh && profile.ngaySinh !== '-' ? profile.ngaySinh : '';
        monDayInput.value = profile.monDay && profile.monDay !== '-' ? profile.monDay : '';
        soDienThoaiInput.value = profile.soDienThoai && profile.soDienThoai !== '-' ? profile.soDienThoai : '';

        if (profile.vaiTroMa && isTeacherRole(profile.vaiTroMa)) {
          roleSelect.value = profile.vaiTroMa;
          toggleTeacherSection();
        }

        if ((!emailInput.value || emailInput.value.trim() === '') && profile.email && profile.email !== '-') {
          emailInput.value = profile.email;
        }
      } catch (error) {
        clearTeacherFields();
      }
    }

    let suggestTimer = null;
    teacherIdInput.addEventListener('input', function () {
      if (suggestTimer) {
        window.clearTimeout(suggestTimer);
      }
      suggestTimer = window.setTimeout(function () {
        loadTeacherSuggestions(teacherIdInput.value || '');
      }, 150);
    });

    teacherIdInput.addEventListener('change', loadTeacherProfile);
    teacherIdInput.addEventListener('blur', loadTeacherProfile);
    roleSelect.addEventListener('change', toggleTeacherSection);

    toggleTeacherSection();
    if (isTeacherRole(roleSelect.value) && (teacherIdInput.value || '').trim() !== '') {
      loadTeacherProfile();
    } else if (isTeacherRole(roleSelect.value)) {
      loadTeacherSuggestions('');
    }
  })();
</script>
</body>
</html>

