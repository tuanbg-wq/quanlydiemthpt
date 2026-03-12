<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>${pageTitle}</title>

  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
  <link rel="stylesheet" href="<c:url value='/css/admin-layout.css'/>">
  <link rel="stylesheet" href="<c:url value='/css/teacher-create.css'/>">
</head>
<body>
<div class="layout">
  <jsp:include page="/WEB-INF/views/admin/_sidebar.jsp"/>

  <main class="main teacher-create-page">
    <header class="topbar">
      <div class="topbar-left">
        <h1>Thêm Giáo Viên Mới</h1>
        <p>Tạo hồ sơ giáo viên mới và gán vai trò nghiệp vụ</p>
      </div>
    </header>

    <section class="content">
      <c:if test="${not empty error}">
        <div class="alert alert-error">
          ${error}
        </div>
      </c:if>

      <section class="card form-card">
        <form id="teacherCreateForm"
              method="post"
              action="<c:url value='/admin/teacher/create'/>"
              enctype="multipart/form-data"
              novalidate>

          <div class="form-grid">
            <div class="field">
              <label for="idGiaoVien">Mã giáo viên <span class="required">*</span></label>
              <div class="input-with-action">
                <input id="idGiaoVien"
                       name="idGiaoVien"
                       type="text"
                       maxlength="10"
                       data-trim="true"
                       value="${teacherForm.idGiaoVien}"
                       placeholder="Ví dụ: GV001"
                       class="${not empty fieldErrors.idGiaoVien ? 'is-invalid' : ''}">
                <button type="button"
                        id="suggestTeacherIdBtn"
                        class="btn btn-outline-secondary"
                        data-suggested="${suggestedTeacherId}">
                  Gợi ý mã
                </button>
              </div>
              <c:if test="${not empty fieldErrors.idGiaoVien}">
                <div class="invalid-feedback d-block">${fieldErrors.idGiaoVien}</div>
              </c:if>
            </div>

            <div class="field">
              <label for="hoTen">Họ và tên <span class="required">*</span></label>
              <input id="hoTen"
                     name="hoTen"
                     type="text"
                     maxlength="100"
                     data-trim="true"
                     value="${teacherForm.hoTen}"
                     class="${not empty fieldErrors.hoTen ? 'is-invalid' : ''}">
              <c:if test="${not empty fieldErrors.hoTen}">
                <div class="invalid-feedback d-block">${fieldErrors.hoTen}</div>
              </c:if>
            </div>

            <div class="field">
              <label for="ngaySinh">Ngày sinh <span class="required">*</span></label>
              <input id="ngaySinh"
                     name="ngaySinh"
                     type="date"
                     max="${today}"
                     value="${teacherForm.ngaySinh}"
                     class="${not empty fieldErrors.ngaySinh ? 'is-invalid' : ''}">
              <c:if test="${not empty fieldErrors.ngaySinh}">
                <div class="invalid-feedback d-block">${fieldErrors.ngaySinh}</div>
              </c:if>
            </div>

            <div class="field">
              <label for="gioiTinh">Giới tính <span class="required">*</span></label>
              <select id="gioiTinh"
                      name="gioiTinh"
                      class="${not empty fieldErrors.gioiTinh ? 'is-invalid' : ''}">
                <option value="">-- Chọn giới tính --</option>
                <c:forEach var="gender" items="${genderOptions}">
                  <option value="${gender.value}" ${teacherForm.gioiTinh == gender.value ? 'selected' : ''}>
                    ${gender.label}
                  </option>
                </c:forEach>
              </select>
              <c:if test="${not empty fieldErrors.gioiTinh}">
                <div class="invalid-feedback d-block">${fieldErrors.gioiTinh}</div>
              </c:if>
            </div>

            <div class="field">
              <label for="soDienThoai">Số điện thoại <span class="required">*</span></label>
              <input id="soDienThoai"
                     name="soDienThoai"
                     type="text"
                     maxlength="11"
                     data-trim="true"
                     value="${teacherForm.soDienThoai}"
                     class="${not empty fieldErrors.soDienThoai ? 'is-invalid' : ''}">
              <c:if test="${not empty fieldErrors.soDienThoai}">
                <div class="invalid-feedback d-block">${fieldErrors.soDienThoai}</div>
              </c:if>
            </div>

            <div class="field">
              <label for="email">Email <span class="required">*</span></label>
              <input id="email"
                     name="email"
                     type="email"
                     maxlength="100"
                     data-trim="true"
                     value="${teacherForm.email}"
                     class="${not empty fieldErrors.email ? 'is-invalid' : ''}">
              <c:if test="${not empty fieldErrors.email}">
                <div class="invalid-feedback d-block">${fieldErrors.email}</div>
              </c:if>
            </div>

            <div class="field field-full">
              <label for="diaChi">Địa chỉ <span class="required">*</span></label>
              <input id="diaChi"
                     name="diaChi"
                     type="text"
                     maxlength="255"
                     data-trim="true"
                     value="${teacherForm.diaChi}"
                     class="${not empty fieldErrors.diaChi ? 'is-invalid' : ''}">
              <c:if test="${not empty fieldErrors.diaChi}">
                <div class="invalid-feedback d-block">${fieldErrors.diaChi}</div>
              </c:if>
            </div>

            <div class="field">
              <label for="monHocId">Môn dạy <span class="required">*</span></label>
              <select id="monHocId"
                      name="monHocId"
                      class="${not empty fieldErrors.monHocId ? 'is-invalid' : ''}">
                <option value="">-- Chọn môn dạy --</option>
                <c:forEach var="subject" items="${subjectOptions}">
                  <option value="${subject.idMonHoc}" ${teacherForm.monHocId == subject.idMonHoc ? 'selected' : ''}>
                    ${subject.idMonHoc} - ${subject.tenMonHoc}
                  </option>
                </c:forEach>
              </select>
              <c:if test="${not empty fieldErrors.monHocId}">
                <div class="invalid-feedback d-block">${fieldErrors.monHocId}</div>
              </c:if>
            </div>

            <div class="field">
              <label for="trinhDo">Trình độ học vấn <span class="required">*</span></label>
              <select id="trinhDo"
                      name="trinhDo"
                      class="${not empty fieldErrors.trinhDo ? 'is-invalid' : ''}">
                <option value="">-- Chọn trình độ --</option>
                <c:forEach var="degree" items="${degreeOptions}">
                  <option value="${degree.value}" ${teacherForm.trinhDo == degree.value ? 'selected' : ''}>
                    ${degree.label}
                  </option>
                </c:forEach>
              </select>
              <c:if test="${not empty fieldErrors.trinhDo}">
                <div class="invalid-feedback d-block">${fieldErrors.trinhDo}</div>
              </c:if>
            </div>

            <div class="field">
              <label for="ngayBatDauCongTac">Ngày bắt đầu công tác <span class="required">*</span></label>
              <input id="ngayBatDauCongTac"
                     name="ngayBatDauCongTac"
                     type="date"
                     max="${today}"
                     value="${teacherForm.ngayBatDauCongTac}"
                     class="${not empty fieldErrors.ngayBatDauCongTac ? 'is-invalid' : ''}">
              <c:if test="${not empty fieldErrors.ngayBatDauCongTac}">
                <div class="invalid-feedback d-block">${fieldErrors.ngayBatDauCongTac}</div>
              </c:if>
            </div>

            <div class="field">
              <label for="trangThai">Trạng thái hoạt động <span class="required">*</span></label>
              <select id="trangThai"
                      name="trangThai"
                      class="${not empty fieldErrors.trangThai ? 'is-invalid' : ''}">
                <option value="">-- Chọn trạng thái --</option>
                <c:forEach var="status" items="${statusOptions}">
                  <option value="${status.value}" ${teacherForm.trangThai == status.value ? 'selected' : ''}>
                    ${status.label}
                  </option>
                </c:forEach>
              </select>
              <c:if test="${not empty fieldErrors.trangThai}">
                <div class="invalid-feedback d-block">${fieldErrors.trangThai}</div>
              </c:if>
            </div>

            <div class="field">
              <label for="namHoc">Năm học áp dụng vai trò <span class="required">*</span></label>
              <input id="namHoc"
                     name="namHoc"
                     type="text"
                     value="${teacherForm.namHoc}"
                     placeholder="Ví dụ: 2025-2026"
                     class="${not empty fieldErrors.namHoc ? 'is-invalid' : ''}">
              <c:if test="${not empty fieldErrors.namHoc}">
                <div class="invalid-feedback d-block">${fieldErrors.namHoc}</div>
              </c:if>
            </div>

            <div class="field">
              <label for="avatar">Ảnh đại diện</label>
              <input id="avatar"
                     name="avatar"
                     type="file"
                     accept=".jpg,.jpeg,.png"
                     class="${not empty fieldErrors.avatar ? 'is-invalid' : ''}">
              <span class="field-note">Hỗ trợ jpg, jpeg, png. Tối đa 3MB.</span>
              <c:if test="${not empty fieldErrors.avatar}">
                <div class="invalid-feedback d-block">${fieldErrors.avatar}</div>
              </c:if>
            </div>

            <div class="field">
              <label>Preview ảnh</label>
              <div class="avatar-preview-frame">
                <img id="avatarPreview" alt="Preview ảnh giáo viên" hidden>
                <span id="avatarPreviewEmpty">Chưa chọn ảnh</span>
              </div>
            </div>

            <div class="field field-full">
              <label for="ghiChu">Ghi chú</label>
              <textarea id="ghiChu"
                        name="ghiChu"
                        rows="3"
                        maxlength="1000"
                        data-trim="true"
                        class="${not empty fieldErrors.ghiChu ? 'is-invalid' : ''}">${teacherForm.ghiChu}</textarea>
              <c:if test="${not empty fieldErrors.ghiChu}">
                <div class="invalid-feedback d-block">${fieldErrors.ghiChu}</div>
              </c:if>
            </div>

            <div class="field field-full role-field ${not empty fieldErrors.vaiTroMa ? 'has-error' : ''}">
              <label>Vai trò giáo viên <span class="required">*</span></label>
              <span class="field-note">Chỉ chọn 1 vai trò cho mỗi giáo viên.</span>
              <div class="role-options">
                <c:set var="selectedRoleValue" value=""/>
                <c:if test="${not empty teacherForm.vaiTroMa}">
                  <c:set var="selectedRoleValue" value="${teacherForm.vaiTroMa[0]}"/>
                </c:if>
                <c:forEach var="role" items="${roleOptions}">
                  <label class="role-option">
                    <input type="radio"
                           name="vaiTroMa"
                           value="${role.value}"
                      ${selectedRoleValue == role.value ? 'checked' : ''}>
                    <span>${role.label}</span>
                  </label>
                </c:forEach>
              </div>
              <c:if test="${not empty fieldErrors.vaiTroMa}">
                <div class="invalid-feedback d-block">${fieldErrors.vaiTroMa}</div>
              </c:if>
            </div>
          </div>

          <div class="form-bottom">
            <span class="required-note">* Trường bắt buộc</span>
            <div class="actions">
              <a class="btn" href="<c:url value='/admin/teacher'/>">Quay lại danh sách</a>
              <button type="button" class="btn" id="resetFormBtn">Làm mới</button>
              <button type="submit" class="btn primary" id="saveTeacherBtn">Lưu giáo viên</button>
            </div>
          </div>
        </form>
      </section>
    </section>
  </main>
</div>

<script>
  (function () {
    const form = document.getElementById('teacherCreateForm');
    if (!form) {
      return;
    }

    const saveBtn = document.getElementById('saveTeacherBtn');
    const resetBtn = document.getElementById('resetFormBtn');
    const suggestBtn = document.getElementById('suggestTeacherIdBtn');
    const teacherIdInput = document.getElementById('idGiaoVien');
    const avatarInput = document.getElementById('avatar');
    const avatarPreview = document.getElementById('avatarPreview');
    const avatarPreviewEmpty = document.getElementById('avatarPreviewEmpty');

    let isDirty = false;
    let isSubmitting = false;

    function markDirty() {
      if (!isSubmitting) {
        isDirty = true;
      }
    }

    function trimInputs() {
      form.querySelectorAll('[data-trim="true"]').forEach(input => {
        if (typeof input.value === 'string') {
          input.value = input.value.trim();
        }
      });
    }

    function clearAvatarPreview() {
      avatarPreview.removeAttribute('src');
      avatarPreview.hidden = true;
      avatarPreviewEmpty.hidden = false;
    }

    function updateAvatarPreview(file) {
      if (!file) {
        clearAvatarPreview();
        return;
      }

      const reader = new FileReader();
      reader.onload = function (event) {
        avatarPreview.src = event.target.result;
        avatarPreview.hidden = false;
        avatarPreviewEmpty.hidden = true;
      };
      reader.readAsDataURL(file);
    }

    form.querySelectorAll('input, select, textarea').forEach(field => {
      field.addEventListener('input', markDirty);
      field.addEventListener('change', markDirty);
    });

    if (suggestBtn && teacherIdInput) {
      suggestBtn.addEventListener('click', function () {
        const suggested = suggestBtn.dataset.suggested;
        if (!teacherIdInput.value.trim() && suggested) {
          teacherIdInput.value = suggested;
          teacherIdInput.dispatchEvent(new Event('input', { bubbles: true }));
        }
      });
    }

    if (avatarInput) {
      avatarInput.addEventListener('change', function () {
        updateAvatarPreview(avatarInput.files && avatarInput.files[0] ? avatarInput.files[0] : null);
      });
    }

    if (resetBtn) {
      resetBtn.addEventListener('click', function () {
        form.reset();
        clearAvatarPreview();
        isDirty = false;
      });
    }

    form.addEventListener('submit', function () {
      trimInputs();
      isSubmitting = true;
      isDirty = false;

      if (saveBtn) {
        saveBtn.disabled = true;
        saveBtn.textContent = 'Đang lưu...';
      }
    });

    window.addEventListener('beforeunload', function (event) {
      if (!isDirty || isSubmitting) {
        return;
      }
      event.preventDefault();
      event.returnValue = '';
    });
  })();
</script>
</body>
</html>