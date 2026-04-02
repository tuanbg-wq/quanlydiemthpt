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
  <link rel="stylesheet" href="<c:url value='/css/admin/teacher-create.css'/>">
</head>
<body>
<div class="layout">
  <jsp:include page="/WEB-INF/views/admin/_sidebar.jsp"/>

  <main class="main teacher-create-page">
    <header class="topbar">
      <div class="topbar-left">
        <h1>ThĂªm giĂ¡o viĂªn má»›i</h1>
        <p>Táº¡o há»“ sÆ¡ giĂ¡o viĂªn má»›i vĂ  gĂ¡n vai trĂ² nghiá»‡p vá»¥.</p>
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
              <label for="idGiaoVien">MĂ£ giĂ¡o viĂªn <span class="required">*</span></label>
              <div class="input-with-action">
                <input id="idGiaoVien"
                       name="idGiaoVien"
                       type="text"
                       maxlength="10"
                       data-trim="true"
                       value="${teacherForm.idGiaoVien}"
                       placeholder="VĂ­ dá»¥: GV001"
                       class="${not empty fieldErrors.idGiaoVien ? 'is-invalid' : ''}">
                <button type="button"
                        id="suggestTeacherIdBtn"
                        class="btn btn-outline-secondary"
                        data-suggested="${suggestedTeacherId}">
                  Gá»£i Ă½ mĂ£
                </button>
              </div>
              <c:if test="${not empty fieldErrors.idGiaoVien}">
                <div class="invalid-feedback d-block">${fieldErrors.idGiaoVien}</div>
              </c:if>
            </div>

            <div class="field">
              <label for="hoTen">Há» vĂ  tĂªn <span class="required">*</span></label>
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
              <label for="ngaySinh">NgĂ y sinh <span class="required">*</span></label>
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
              <label for="gioiTinh">Giá»›i tĂ­nh <span class="required">*</span></label>
              <select id="gioiTinh"
                      name="gioiTinh"
                      class="${not empty fieldErrors.gioiTinh ? 'is-invalid' : ''}">
                <option value="">-- Chá»n giá»›i tĂ­nh --</option>
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
              <label for="soDienThoai">Sá»‘ Ä‘iá»‡n thoáº¡i <span class="required">*</span></label>
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
              <label for="diaChi">Äá»‹a chá»‰ <span class="required">*</span></label>
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
              <label for="monHocId">MĂ´n dáº¡y <span class="required">*</span></label>
              <select id="monHocId"
                      name="monHocId"
                      class="${not empty fieldErrors.monHocId ? 'is-invalid' : ''}">
                <option value="">-- Chá»n mĂ´n dáº¡y --</option>
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
              <label for="trinhDo">TrĂ¬nh Ä‘á»™ há»c váº¥n <span class="required">*</span></label>
              <select id="trinhDo"
                      name="trinhDo"
                      class="${not empty fieldErrors.trinhDo ? 'is-invalid' : ''}">
                <option value="">-- Chá»n trĂ¬nh Ä‘á»™ --</option>
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
              <label for="ngayBatDauCongTac">NgĂ y báº¯t Ä‘áº§u cĂ´ng tĂ¡c <span class="required">*</span></label>
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
              <label for="trangThai">Tráº¡ng thĂ¡i hoáº¡t Ä‘á»™ng <span class="required">*</span></label>
              <select id="trangThai"
                      name="trangThai"
                      class="${not empty fieldErrors.trangThai ? 'is-invalid' : ''}">
                <option value="">-- Chá»n tráº¡ng thĂ¡i --</option>
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
              <label for="namHoc">NÄƒm há»c Ă¡p dá»¥ng vai trĂ² <span class="required">*</span></label>
              <input id="namHoc"
                     name="namHoc"
                     type="text"
                     value="${teacherForm.namHoc}"
                     data-trim="true"
                     placeholder="VĂ­ dá»¥: 2025-2026"
                     class="${not empty fieldErrors.namHoc ? 'is-invalid' : ''}">
              <c:if test="${not empty fieldErrors.namHoc}">
                <div class="invalid-feedback d-block">${fieldErrors.namHoc}</div>
              </c:if>
            </div>

            <div class="field field-full role-field ${not empty fieldErrors.vaiTroMa ? 'has-error' : ''}">
              <label>Vai trĂ² giĂ¡o viĂªn <span class="required">*</span></label>
              <span class="field-note">Chá»‰ chá»n 1 vai trĂ² cho má»—i giĂ¡o viĂªn.</span>
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
              <label for="lopBoMon">Lá»›p bá»™ mĂ´n <span class="required">*</span></label>
              <input id="lopBoMon"
                     name="lopBoMon"
                     type="text"
                     data-trim="true"
                     value="${teacherForm.lopBoMon}"
                     placeholder="Nháº­p nhiá»u mĂ£ lá»›p, vĂ­ dá»¥: K06A1, K06A2"
                     autocomplete="off"
                     class="${not empty fieldErrors.lopBoMon ? 'is-invalid' : ''}">
              <div class="suggest-list" data-class-suggest="subject"></div>
              <span class="field-note">CĂ³ thá»ƒ nháº­p nhiá»u lá»›p, cĂ¡ch nhau báº±ng dáº¥u pháº©y.</span>
              <c:if test="${not empty fieldErrors.lopBoMon}">
                <div class="invalid-feedback d-block">${fieldErrors.lopBoMon}</div>
              </c:if>
            </div>

            <div class="field suggest-field role-dependent role-homeroom-class ${not empty fieldErrors.lopChuNhiem ? 'has-error' : ''}">
              <label for="lopChuNhiem">Lá»›p chá»§ nhiá»‡m <span class="required">*</span></label>
              <input id="lopChuNhiem"
                     name="lopChuNhiem"
                     type="text"
                     data-trim="true"
                     value="${teacherForm.lopChuNhiem}"
                     placeholder="Nháº­p mĂ£ lá»›p chá»§ nhiá»‡m, vĂ­ dá»¥: K06A1 (Khá»‘i 10) - nÄƒm há»c 2025-2026"
                     autocomplete="off"
                     class="${not empty fieldErrors.lopChuNhiem ? 'is-invalid' : ''}">
              <div class="suggest-list" data-class-suggest="homeroom"></div>
              <c:if test="${not empty fieldErrors.lopChuNhiem}">
                <div class="invalid-feedback d-block">${fieldErrors.lopChuNhiem}</div>
              </c:if>
            </div>

            <div class="field">
              <label for="avatar">áº¢nh Ä‘áº¡i diá»‡n</label>
              <input id="avatar"
                     name="avatar"
                     type="file"
                     accept=".jpg,.jpeg,.png"
                     class="${not empty fieldErrors.avatar ? 'is-invalid' : ''}">
              <span class="field-note">Há»— trá»£ jpg, jpeg, png. Tá»‘i Ä‘a 3MB.</span>
              <c:if test="${not empty fieldErrors.avatar}">
                <div class="invalid-feedback d-block">${fieldErrors.avatar}</div>
              </c:if>
            </div>

            <div class="field">
              <label>Preview áº£nh</label>
              <div class="avatar-preview-frame">
                <img id="avatarPreview" alt="Preview áº£nh giĂ¡o viĂªn" hidden>
                <span id="avatarPreviewEmpty">ChÆ°a chá»n áº£nh</span>
              </div>
            </div>

            <div class="field field-full">
              <label for="ghiChu">Ghi chĂº</label>
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
            <span class="required-note">* TrÆ°á»ng báº¯t buá»™c</span>
            <div class="actions">
              <a class="btn" href="<c:url value='/admin/teacher'/>">Quay láº¡i danh sĂ¡ch</a>
              <button type="button" class="btn" id="resetFormBtn">LĂ m má»›i</button>
              <button type="submit" class="btn primary" id="saveTeacherBtn">LÆ°u giĂ¡o viĂªn</button>
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
    const schoolYearInput = document.getElementById('namHoc');
    const subjectSelect = document.getElementById('monHocId');

    const roleInputs = Array.from(form.querySelectorAll('input[name="vaiTroMa"]'));
    const subjectClassField = form.querySelector('.role-subject-class');
    const homeroomClassField = form.querySelector('.role-homeroom-class');
    const subjectClassInput = document.getElementById('lopBoMon');
    const homeroomClassInput = document.getElementById('lopChuNhiem');
    const subjectClassSuggestBox = form.querySelector('[data-class-suggest="subject"]');
    const homeroomClassSuggestBox = form.querySelector('[data-class-suggest="homeroom"]');

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
              const grade = row.grade ? ('(Khá»‘i ' + row.grade + ')') : '';
              const year = row.schoolYear ? (' - nÄƒm há»c ' + row.schoolYear) : '';
              const classCode = (row.id || '').trim();
              const className = (row.name || '').trim();
              const classLabel = className && className.toLowerCase() !== classCode.toLowerCase()
                ? (classCode + ' - ' + className)
                : classCode;
              return {
                id: normalizeClassId(classCode),
                label: classLabel + (grade ? (' ' + grade) : '') + year
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
      const schoolYear = schoolYearInput ? (schoolYearInput.value || '').trim() : '';
      return '<c:url value="/admin/teacher/suggest/homeroom-classes"/>'
        + '?q=' + encodeURIComponent(keyword)
        + '&namHoc=' + encodeURIComponent(schoolYear)
        + '&mode=create';
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
        closeSuggestBox(homeroomClassSuggestBox);
        requestSubjectClassSuggestion(false);
        refreshSuggestForInput(homeroomClassInput);
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
        clearAvatarPreview();
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
        saveBtn.textContent = 'Äang lÆ°u...';
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

