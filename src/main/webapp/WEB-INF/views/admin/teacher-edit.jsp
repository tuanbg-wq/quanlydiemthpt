<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

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

  <main class="main teacher-create-page teacher-edit-page">
    <header class="topbar">
      <div class="topbar-left">
        <h1>Chỉnh sửa giáo viên</h1>
        <p>Cập nhật hồ sơ giáo viên và vai trò nghiệp vụ.</p>
      </div>
    </header>

    <section class="content">
      <c:if test="${not empty error}">
        <div class="alert alert-error">
          ${error}
        </div>
      </c:if>

      <section class="card form-card">
        <form id="teacherEditForm"
              method="post"
              action="<c:url value='/admin/teacher/${teacherId}/edit'/>"
              enctype="multipart/form-data"
              novalidate>

          <div class="form-grid">
            <div class="field">
              <label for="idGiaoVien">Mã giáo viên <span class="required">*</span></label>
              <input id="idGiaoVien"
                     name="idGiaoVien"
                     type="text"
                     maxlength="10"
                     data-trim="true"
                     value="${teacherForm.idGiaoVien}"
                     class="${not empty fieldErrors.idGiaoVien ? 'is-invalid' : ''}">
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
                     data-trim="true"
                     placeholder="Ví dụ: 2025-2026"
                     class="${not empty fieldErrors.namHoc ? 'is-invalid' : ''}">
              <c:if test="${not empty fieldErrors.namHoc}">
                <div class="invalid-feedback d-block">${fieldErrors.namHoc}</div>
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

            <div class="field suggest-field role-dependent role-subject-class ${not empty fieldErrors.lopBoMon ? 'has-error' : ''}">
              <label for="lopBoMon">Lớp bộ môn <span class="required">*</span></label>
              <input id="lopBoMon"
                     name="lopBoMon"
                     type="text"
                     data-trim="true"
                     value="${teacherForm.lopBoMon}"
                     placeholder="Nhập nhiều lớp, ví dụ: 10A1, 10A2"
                     autocomplete="off"
                     class="${not empty fieldErrors.lopBoMon ? 'is-invalid' : ''}">
              <div class="suggest-list" data-class-suggest="subject"></div>
              <span class="field-note">Có thể nhập nhiều lớp, cách nhau bằng dấu phẩy.</span>
              <c:if test="${not empty fieldErrors.lopBoMon}">
                <div class="invalid-feedback d-block">${fieldErrors.lopBoMon}</div>
              </c:if>
            </div>

            <div class="field suggest-field role-dependent role-homeroom-class ${not empty fieldErrors.lopChuNhiem ? 'has-error' : ''}">
              <label for="lopChuNhiem">Lớp chủ nhiệm <span class="required">*</span></label>
              <input id="lopChuNhiem"
                     name="lopChuNhiem"
                     type="text"
                     data-trim="true"
                     value="${teacherForm.lopChuNhiem}"
                     placeholder="Nhập mã lớp chủ nhiệm, ví dụ: 10A1"
                     autocomplete="off"
                     class="${not empty fieldErrors.lopChuNhiem ? 'is-invalid' : ''}">
              <div class="suggest-list" data-class-suggest="homeroom"></div>
              <c:if test="${not empty fieldErrors.lopChuNhiem}">
                <div class="invalid-feedback d-block">${fieldErrors.lopChuNhiem}</div>
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
                <c:choose>
                  <c:when test="${not empty currentAvatar}">
                    <c:choose>
                      <c:when test="${fn:startsWith(currentAvatar, '/')}">
                        <c:url var="avatarPreviewUrl" value="${currentAvatar}"/>
                      </c:when>
                      <c:otherwise>
                        <c:url var="avatarPreviewUrl" value="/uploads/${currentAvatar}"/>
                      </c:otherwise>
                    </c:choose>
                    <img id="avatarPreview"
                         alt="Preview ảnh giáo viên"
                         src="${avatarPreviewUrl}"
                         data-initial-src="${avatarPreviewUrl}">
                    <span id="avatarPreviewEmpty" hidden>Chưa chọn ảnh</span>
                  </c:when>
                  <c:otherwise>
                    <img id="avatarPreview"
                         alt="Preview ảnh giáo viên"
                         data-initial-src=""
                         hidden>
                    <span id="avatarPreviewEmpty">Chưa chọn ảnh</span>
                  </c:otherwise>
                </c:choose>
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
          </div>

          <div class="form-bottom">
            <span class="required-note">* Trường bắt buộc</span>
            <div class="actions">
              <a class="btn" href="<c:url value='/admin/teacher'/>">Quay lại danh sách</a>
              <button type="button" class="btn" id="resetFormBtn">Hoàn tác</button>
              <button type="submit" class="btn primary" id="saveTeacherBtn">Lưu cập nhật</button>
            </div>
          </div>
        </form>
      </section>
    </section>
  </main>
</div>

<script>
  (function () {
    const form = document.getElementById('teacherEditForm');
    if (!form) {
      return;
    }

    const saveBtn = document.getElementById('saveTeacherBtn');
    const resetBtn = document.getElementById('resetFormBtn');
    const avatarInput = document.getElementById('avatar');
    const avatarPreview = document.getElementById('avatarPreview');
    const avatarPreviewEmpty = document.getElementById('avatarPreviewEmpty');
    const schoolYearInput = document.getElementById('namHoc');
    const subjectSelect = document.getElementById('monHocId');

    const roleInputs = Array.from(form.querySelectorAll('input[name="vaiTroMa"]'));
    const subjectClassField = form.querySelector('.role-subject-class');
    const homeroomClassField = form.querySelector('.role-homeroom-class');
    const subjectClassInput = document.getElementById('lopBoMon');
    const homeroomClassInput = document.getElementById('lopChuNhiem');
    const subjectClassSuggestBox = form.querySelector('[data-class-suggest="subject"]');
    const homeroomClassSuggestBox = form.querySelector('[data-class-suggest="homeroom"]');

    const initialAvatarSrc = avatarPreview && avatarPreview.dataset
      ? (avatarPreview.dataset.initialSrc || '').trim()
      : '';

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

    function debounce(fn, wait) {
      let timeoutId = null;
      return function () {
        const context = this;
        const args = arguments;
        clearTimeout(timeoutId);
        timeoutId = setTimeout(function () {
          fn.apply(context, args);
        }, wait);
      };
    }

    function normalizeClassId(value) {
      return (value || '').trim().toUpperCase();
    }

    function splitClassIds(rawValue) {
      return (rawValue || '')
        .split(/[,;\n]+/)
        .map(normalizeClassId)
        .filter(Boolean);
    }

    function appendClassId(rawValue, classId) {
      const normalizedId = normalizeClassId(classId);
      if (!normalizedId) {
        return rawValue || '';
      }

      const ids = splitClassIds(rawValue);
      if (!ids.includes(normalizedId)) {
        ids.push(normalizedId);
      }
      return ids.join(', ');
    }

    function extractKeyword(rawValue, multiValue) {
      if (!multiValue) {
        return normalizeClassId(rawValue);
      }
      const parts = (rawValue || '').split(/[,;\n]+/);
      return normalizeClassId(parts[parts.length - 1] || '');
    }

    function closeSuggestBox(box) {
      if (!box) {
        return;
      }
      box.innerHTML = '';
      box.classList.remove('open');
    }

    function renderSuggestItems(box, items, onSelect) {
      closeSuggestBox(box);
      if (!items || items.length === 0) {
        return;
      }

      const fragment = document.createDocumentFragment();
      items.forEach(function (item) {
        const button = document.createElement('button');
        button.type = 'button';
        button.className = 'suggest-item';
        button.textContent = item.label;
        button.addEventListener('mousedown', function (event) {
          event.preventDefault();
          onSelect(item);
          closeSuggestBox(box);
        });
        fragment.appendChild(button);
      });

      box.appendChild(fragment);
      box.classList.add('open');
    }

    function clearAvatarPreview() {
      if (!avatarPreview || !avatarPreviewEmpty) {
        return;
      }

      avatarPreview.removeAttribute('src');
      avatarPreview.hidden = true;
      avatarPreviewEmpty.hidden = false;
    }

    function restoreInitialAvatarPreview() {
      if (!avatarPreview || !avatarPreviewEmpty) {
        return;
      }

      if (initialAvatarSrc) {
        avatarPreview.src = initialAvatarSrc;
        avatarPreview.hidden = false;
        avatarPreviewEmpty.hidden = true;
        return;
      }

      clearAvatarPreview();
    }

    function updateAvatarPreview(file) {
      if (!file) {
        restoreInitialAvatarPreview();
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

    function getSelectedRole() {
      const checked = roleInputs.find(function (radio) {
        return radio.checked;
      });
      return checked ? (checked.value || '').toUpperCase() : '';
    }

    function syncRoleSections() {
      const selectedRole = getSelectedRole();
      const showSubjectClass = selectedRole === 'GVBM' || selectedRole === 'GVCN';
      const showHomeroomClass = selectedRole === 'GVCN';

      if (subjectClassField) {
        subjectClassField.classList.toggle('hidden', !showSubjectClass);
      }
      if (homeroomClassField) {
        homeroomClassField.classList.toggle('hidden', !showHomeroomClass);
      }

      if (!showSubjectClass && subjectClassInput) {
        subjectClassInput.value = '';
        closeSuggestBox(subjectClassSuggestBox);
      }
      if (!showHomeroomClass && homeroomClassInput) {
        homeroomClassInput.value = '';
        closeSuggestBox(homeroomClassSuggestBox);
      }
    }

    function bindClassSuggest(input, box, multiValue, buildSuggestUrl) {
      if (!input || !box) {
        return;
      }

      const loadSuggestions = debounce(function () {
        const parentField = input.closest('.role-dependent');
        if (parentField && parentField.classList.contains('hidden')) {
          closeSuggestBox(box);
          return;
        }

        const keyword = extractKeyword(input.value, multiValue);
        const url = buildSuggestUrl(keyword);
        if (!url) {
          closeSuggestBox(box);
          return;
        }

        fetch(url, { headers: { 'Accept': 'application/json' } })
          .then(function (response) { return response.ok ? response.json() : []; })
          .then(function (rows) {
            const items = (rows || []).map(function (row) {
              const grade = row.grade ? ('Khối ' + row.grade) : '';
              const year = row.schoolYear ? (' - ' + row.schoolYear) : '';
              return {
                id: normalizeClassId(row.id),
                label: (row.id || '') + ' - ' + (row.name || row.id || '') + (grade ? (' (' + grade + ')') : '') + year
              };
            });

            renderSuggestItems(box, items, function (selected) {
              if (multiValue) {
                input.value = appendClassId(input.value, selected.id);
              } else {
                input.value = normalizeClassId(selected.id);
              }
              input.dispatchEvent(new Event('input', { bubbles: true }));
            });
          })
          .catch(function () {
            closeSuggestBox(box);
          });
      }, 220);

      input.addEventListener('input', loadSuggestions);
      input.addEventListener('focus', loadSuggestions);
      input.addEventListener('blur', function () {
        setTimeout(function () {
          closeSuggestBox(box);
        }, 120);
      });
    }

    function buildSubjectClassSuggestUrl(keyword) {
      const schoolYear = schoolYearInput ? (schoolYearInput.value || '').trim() : '';
      const subjectId = subjectSelect ? (subjectSelect.value || '').trim() : '';
      return '<c:url value="/admin/teacher/suggest/subject-classes"/>'
        + '?q=' + encodeURIComponent(keyword)
        + '&namHoc=' + encodeURIComponent(schoolYear)
        + '&subjectId=' + encodeURIComponent(subjectId);
    }

    function buildHomeroomSuggestUrl(keyword) {
      return '<c:url value="/admin/teacher/suggest/homeroom-classes"/>'
        + '?q=' + encodeURIComponent(keyword)
        + '&mode=edit';
    }

    function refreshSuggestForInput(input) {
      if (!input) {
        return;
      }
      input.dispatchEvent(new Event('input', { bubbles: true }));
    }

    function requestSubjectClassSuggestion(autoFocus) {
      if (!subjectClassInput || !subjectClassField || subjectClassField.classList.contains('hidden')) {
        return;
      }
      if (autoFocus) {
        subjectClassInput.focus();
      }
      refreshSuggestForInput(subjectClassInput);
    }

    form.querySelectorAll('input, select, textarea').forEach(function (field) {
      field.addEventListener('input', markDirty);
      field.addEventListener('change', markDirty);
    });

    if (avatarInput) {
      avatarInput.addEventListener('change', function () {
        updateAvatarPreview(avatarInput.files && avatarInput.files[0] ? avatarInput.files[0] : null);
      });
    }

    bindClassSuggest(subjectClassInput, subjectClassSuggestBox, true, buildSubjectClassSuggestUrl);
    bindClassSuggest(homeroomClassInput, homeroomClassSuggestBox, false, buildHomeroomSuggestUrl);

    if (subjectSelect) {
      subjectSelect.addEventListener('change', function () {
        closeSuggestBox(subjectClassSuggestBox);
        requestSubjectClassSuggestion(true);
      });
    }

    if (schoolYearInput) {
      schoolYearInput.addEventListener('input', function () {
        closeSuggestBox(subjectClassSuggestBox);
        requestSubjectClassSuggestion(false);
      });
    }

    roleInputs.forEach(function (input) {
      input.addEventListener('change', function () {
        syncRoleSections();
        requestSubjectClassSuggestion(false);
      });
    });

    if (resetBtn) {
      resetBtn.addEventListener('click', function () {
        form.reset();
        restoreInitialAvatarPreview();
        closeSuggestBox(subjectClassSuggestBox);
        closeSuggestBox(homeroomClassSuggestBox);
        syncRoleSections();
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

    syncRoleSections();
  })();
</script>
</body>
</html>
