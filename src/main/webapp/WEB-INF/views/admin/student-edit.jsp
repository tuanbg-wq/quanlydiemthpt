<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>${pageTitle}</title>
    <link rel="stylesheet" href="<c:url value='/css/admin-layout.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/admin/student/student-edit.css'/>">
</head>
<body>

<div class="layout">
    <jsp:include page="/WEB-INF/views/admin/_sidebar.jsp"/>

    <main class="main student-edit-page">
        <header class="topbar">
            <div class="topbar-left">
                <h1>Cập nhật thông tin và lớp học sinh</h1>
                <p>Sửa thông tin học sinh, sửa lớp hiện tại hoặc chuyển sang lớp mới</p>
            </div>
        </header>

        <section class="content">
            <c:if test="${not empty error}">
                <div class="alert alert-error">
                    ${error}
                </div>
            </c:if>

            <div class="card">
                <form method="post"
                      action="<c:url value='/admin/student/${student.idHocSinh}/edit'/>"
                      enctype="multipart/form-data">

                    <div class="form-grid">
                        <div class="form-group">
                            <label>Mã học sinh</label>
                            <input type="text" name="idHocSinh" value="${student.idHocSinh}" required/>
                            <small>Có thể đổi mã học sinh. Nếu mã mới đã tồn tại, hệ thống sẽ báo lỗi.</small>
                        </div>

                        <div class="form-group">
                            <label>Họ tên *</label>
                            <input type="text" name="hoTen" value="${student.hoTen}" required/>
                        </div>

                        <div class="form-group">
                            <label>Ngày sinh *</label>
                            <input type="date" name="ngaySinh" value="${student.ngaySinh}" required/>
                        </div>

                        <div class="form-group">
                            <label>Giới tính</label>
                            <select name="gioiTinh">
                                <option value="">-- Chọn --</option>
                                <option value="Nam" ${student.gioiTinhHienThi == 'Nam' ? 'selected' : ''}>Nam</option>
                                <option value="Nữ" ${student.gioiTinhHienThi == 'Nữ' ? 'selected' : ''}>Nữ</option>
                            </select>
                        </div>

                        <div class="form-group">
                            <label>Nơi sinh</label>
                            <input type="text" name="noiSinh" value="${student.noiSinh}"/>
                        </div>

                        <div class="form-group">
                            <label>Dân tộc</label>
                            <input type="text" name="danToc" value="${student.danToc}"/>
                        </div>

                        <div class="form-group">
                            <label>Số điện thoại</label>
                            <input type="text" name="soDienThoai" value="${student.soDienThoai}"/>
                        </div>

                        <div class="form-group">
                            <label>Email</label>
                            <input type="email" name="email" value="${student.email}"/>
                        </div>

                        <div class="form-group full-width">
                            <label>Địa chỉ</label>
                            <textarea name="diaChi" rows="3">${student.diaChi}</textarea>
                        </div>

                        <div class="form-group">
                            <label>Họ tên cha</label>
                            <input type="text" name="hoTenCha" value="${student.hoTenCha}"/>
                        </div>

                        <div class="form-group">
                            <label>SĐT cha</label>
                            <input type="text" name="sdtCha" value="${student.sdtCha}"/>
                        </div>

                        <div class="form-group">
                            <label>Họ tên mẹ</label>
                            <input type="text" name="hoTenMe" value="${student.hoTenMe}"/>
                        </div>

                        <div class="form-group">
                            <label>SĐT mẹ</label>
                            <input type="text" name="sdtMe" value="${student.sdtMe}"/>
                        </div>

                        <div class="form-group">
                            <label>Ngày nhập học</label>
                            <input type="date" name="ngayNhapHoc" value="${student.ngayNhapHoc}"/>
                        </div>

                        <div class="form-group">
                            <label>Trạng thái</label>
                            <select name="trangThai">
                                <option value="dang_hoc" ${student.trangThai == 'dang_hoc' ? 'selected' : ''}>Đang học</option>
                                <option value="da_tot_nghiep" ${student.trangThai == 'da_tot_nghiep' ? 'selected' : ''}>Đã tốt nghiệp</option>
                                <option value="bo_hoc" ${student.trangThai == 'bo_hoc' ? 'selected' : ''}>Bỏ học</option>
                                <option value="chuyen_truong" ${student.trangThai == 'chuyen_truong' ? 'selected' : ''}>Chuyển trường</option>
                            </select>
                        </div>

                        <div class="form-group">
                            <label>Hạnh kiểm học kỳ I</label>
                            <select name="hanhKiemHocKy1">
                                <option value="" ${empty student.hanhKiemHocKy1 ? 'selected' : ''}>(Chưa có)</option>
                                <option value="Tốt" ${student.hanhKiemHocKy1HienThi == 'Tốt' ? 'selected' : ''}>Tốt</option>
                                <option value="Khá" ${student.hanhKiemHocKy1HienThi == 'Khá' ? 'selected' : ''}>Khá</option>
                                <option value="Trung bình" ${student.hanhKiemHocKy1HienThi == 'Trung bình' ? 'selected' : ''}>Trung bình</option>
                                <option value="Yếu" ${student.hanhKiemHocKy1HienThi == 'Yếu' ? 'selected' : ''}>Yếu</option>
                                <option value="Kém" ${student.hanhKiemHocKy1HienThi == 'Kém' ? 'selected' : ''}>Kém</option>
                            </select>
                        </div>

                        <div class="form-group">
                            <label>Hạnh kiểm học kỳ II</label>
                            <select name="hanhKiemHocKy2">
                                <option value="" ${empty student.hanhKiemHocKy2 ? 'selected' : ''}>(Chưa có)</option>
                                <option value="Tốt" ${student.hanhKiemHocKy2HienThi == 'Tốt' ? 'selected' : ''}>Tốt</option>
                                <option value="Khá" ${student.hanhKiemHocKy2HienThi == 'Khá' ? 'selected' : ''}>Khá</option>
                                <option value="Trung bình" ${student.hanhKiemHocKy2HienThi == 'Trung bình' ? 'selected' : ''}>Trung bình</option>
                                <option value="Yếu" ${student.hanhKiemHocKy2HienThi == 'Yếu' ? 'selected' : ''}>Yếu</option>
                                <option value="Kém" ${student.hanhKiemHocKy2HienThi == 'Kém' ? 'selected' : ''}>Kém</option>
                            </select>
                        </div>

                        <div class="form-group">
                            <label>Hạnh kiểm cả năm</label>
                            <select name="hanhKiemCaNam">
                                <option value="" ${empty student.hanhKiemCaNam ? 'selected' : ''}>(Chưa có)</option>
                                <option value="Tốt" ${student.hanhKiemCaNamHienThi == 'Tốt' ? 'selected' : ''}>Tốt</option>
                                <option value="Khá" ${student.hanhKiemCaNamHienThi == 'Khá' ? 'selected' : ''}>Khá</option>
                                <option value="Trung bình" ${student.hanhKiemCaNamHienThi == 'Trung bình' ? 'selected' : ''}>Trung bình</option>
                                <option value="Yếu" ${student.hanhKiemCaNamHienThi == 'Yếu' ? 'selected' : ''}>Yếu</option>
                                <option value="Kém" ${student.hanhKiemCaNamHienThi == 'Kém' ? 'selected' : ''}>Kém</option>
                            </select>
                        </div>

                        <div class="form-group">
                            <label>Khóa học (id_khoa)</label>
                            <input type="text"
                                   name="courseId"
                                   value="${student.lop != null && student.lop.khoaHoc != null ? student.lop.khoaHoc.idKhoa : ''}"
                                   required/>
                        </div>

                        <div class="form-group">
                            <label>Tên khóa</label>
                            <input type="text"
                                   name="tenKhoa"
                                   value="${student.lop != null && student.lop.khoaHoc != null ? student.lop.khoaHoc.tenKhoa : ''}"/>
                        </div>

                        <div class="form-group">
                            <label>Khối</label>
                            <select name="khoi" required>
                                <option value="">-- Chọn --</option>
                                <option value="10" ${student.lop != null && student.lop.khoi == 10 ? 'selected' : ''}>10</option>
                                <option value="11" ${student.lop != null && student.lop.khoi == 11 ? 'selected' : ''}>11</option>
                                <option value="12" ${student.lop != null && student.lop.khoi == 12 ? 'selected' : ''}>12</option>
                            </select>
                        </div>

                        <div class="form-group">
                            <label>Mã lớp hiện tại</label>
                            <input type="text"
                                   value="${student.lop != null ? student.lop.maVaTenLop : '(trống)'}"
                                   readonly/>
                            <input type="hidden"
                                   name="currentClassId"
                                   value="${student.lop != null ? student.lop.idLop : ''}"/>
                            <small>Ô này chỉ hiển thị. Khi chuyển lớp, hệ thống sẽ tự cập nhật lớp hiện tại.</small>
                        </div>

                        <div class="form-group full-width transfer-panel">
                            <label>Lịch sử chuyển</label>
                            <select id="transferActionSelect" name="transferAction">
                                <option value="">-- Không chuyển --</option>
                                <option value="chuyen_lop" ${param.transferAction == 'chuyen_lop' ? 'selected' : ''}>Chuyển lớp</option>
                                <option value="chuyen_truong" ${param.transferAction == 'chuyen_truong' ? 'selected' : ''}>Chuyển trường</option>
                            </select>
                            <small>Chọn hình thức chuyển để nhập thông tin phù hợp.</small>

                            <div class="transfer-extra" id="transferClassWrap">
                                <label for="transferClassNameInput">Nhập tên lớp sẽ chuyển (gợi ý cùng khối)</label>
                                <input type="text"
                                       id="transferClassNameInput"
                                       name="transferClassName"
                                       value="${param.transferClassName}"
                                       list="transferClassSuggestions"
                                       placeholder="VD: 11A1 - Lớp 11A1"/>
                                <input type="hidden" id="transferClassIdInput" name="transferClassId" value="${param.transferClassId}"/>
                                <datalist id="transferClassSuggestions">
                                    <c:forEach var="cl" items="${classes}">
                                        <option value="${cl.maVaTenLop}"
                                                data-id="${cl.idLop}"
                                                data-khoi="${cl.khoi}"
                                                data-label="${cl.maVaTenLop}"></option>
                                    </c:forEach>
                                </datalist>
                            </div>

                            <div class="transfer-extra" id="transferSchoolWrap">
                                <label for="transferSchoolNameInput">Trường chuyển đến</label>
                                <input type="text"
                                       id="transferSchoolNameInput"
                                       name="transferSchoolName"
                                       value="${param.transferSchoolName}"
                                       placeholder="Nhập tên trường chuyển đến"/>
                            </div>

                            <div class="transfer-extra" id="transferDateWrap">
                                <label for="transferDateInput">Ngày chuyển</label>
                                <input type="date" id="transferDateInput" name="transferDate" value="${param.transferDate}"/>
                            </div>
                        </div>

                        <div class="form-group full-width">
                            <label>Ảnh học sinh</label>

                            <c:if test="${not empty student.anh}">
                                <div class="avatar-preview">
                                    <c:choose>
                                        <c:when test="${fn:startsWith(student.anh, '/uploads/')}">
                                            <img src="<c:url value='${student.anh}'/>"
                                                 alt="avatar"/>
                                        </c:when>
                                        <c:otherwise>
                                            <img src="<c:url value='/uploads/${student.anh}'/>"
                                                 alt="avatar"/>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </c:if>

                            <div class="avatar-preview" id="avatarPreviewEditNew" style="display: none;">
                                <img id="avatarPreviewEditNewImg" alt="preview ảnh mới"/>
                            </div>

                            <input id="avatarInputEdit" type="file" name="avatar" accept="image/png,image/jpeg,image/webp"/>
                            <small>Hỗ trợ PNG/JPG/JPEG/WEBP.</small>
                        </div>
                    </div>

                    <div class="form-actions">
                        <button type="submit" class="btn primary">Lưu thay đổi</button>
                        <a href="<c:url value='/admin/student'/>" class="btn">Quay lại</a>
                    </div>
                </form>
            </div>
        </section>
    </main>
</div>

<script>
    (function () {
        const input = document.getElementById('avatarInputEdit');
        const preview = document.getElementById('avatarPreviewEditNew');
        const img = document.getElementById('avatarPreviewEditNewImg');

        if (!input || !preview || !img) {
            return;
        }

        input.addEventListener('change', function () {
            const file = input.files && input.files[0];
            if (!file) {
                preview.style.display = 'none';
                img.removeAttribute('src');
                return;
            }

            const objectUrl = URL.createObjectURL(file);
            img.src = objectUrl;
            preview.style.display = 'block';
            img.onload = function () {
                URL.revokeObjectURL(objectUrl);
            };
        });
    })();

    (function () {
        const form = document.querySelector('.student-edit-page form');
        const transferActionSelect = document.getElementById('transferActionSelect');
        const transferClassWrap = document.getElementById('transferClassWrap');
        const transferSchoolWrap = document.getElementById('transferSchoolWrap');
        const transferDateWrap = document.getElementById('transferDateWrap');
        const transferClassNameInput = document.getElementById('transferClassNameInput');
        const transferClassIdInput = document.getElementById('transferClassIdInput');
        const transferSchoolNameInput = document.getElementById('transferSchoolNameInput');
        const transferDateInput = document.getElementById('transferDateInput');
        const transferClassSuggestions = document.getElementById('transferClassSuggestions');
        const khoiSelect = document.querySelector('select[name=\"khoi\"]');
        const currentClassHidden = document.querySelector('input[name=\"currentClassId\"]');

        if (!form || !transferActionSelect || !transferClassWrap || !transferSchoolWrap || !transferDateWrap
            || !transferClassNameInput || !transferClassIdInput || !transferSchoolNameInput
            || !transferDateInput || !transferClassSuggestions) {
            return;
        }

        const classRecords = Array.from(transferClassSuggestions.querySelectorAll('option'))
            .map(function (option) {
                return {
                    id: (option.dataset.id || '').trim(),
                    khoi: (option.dataset.khoi || '').trim(),
                    label: (option.dataset.label || option.value || '').trim()
                };
            })
            .filter(function (record) {
                return record.id && record.label;
            });

        function normalizeValue(value) {
            return (value || '').trim().toLowerCase();
        }

        function rebuildSuggestions() {
            const selectedKhoi = (khoiSelect ? khoiSelect.value : '').trim();
            const currentClassId = normalizeValue(currentClassHidden ? currentClassHidden.value : '');

            transferClassSuggestions.innerHTML = '';
            classRecords.forEach(function (record) {
                const sameGrade = !selectedKhoi || !record.khoi || record.khoi === selectedKhoi;
                const notCurrentClass = !currentClassId || normalizeValue(record.id) !== currentClassId;
                if (!sameGrade || !notCurrentClass) {
                    return;
                }

                const option = document.createElement('option');
                option.value = record.label;
                option.dataset.id = record.id;
                option.dataset.khoi = record.khoi;
                option.dataset.label = record.label;
                transferClassSuggestions.appendChild(option);
            });
        }

        function findClassRecordByInput(inputValue) {
            const normalizedInput = normalizeValue(inputValue);
            if (!normalizedInput) {
                return null;
            }
            for (const record of classRecords) {
                if (normalizeValue(record.label) === normalizedInput || normalizeValue(record.id) === normalizedInput) {
                    return record;
                }
            }
            return null;
        }

        function syncTransferClassId() {
            const matched = findClassRecordByInput(transferClassNameInput.value);
            transferClassIdInput.value = matched ? matched.id : '';
            transferClassNameInput.setCustomValidity('');
        }

        function updateTransferUi() {
            const action = (transferActionSelect.value || '').trim();
            const isTransferClass = action === 'chuyen_lop';
            const isTransferSchool = action === 'chuyen_truong';
            const hasTransferAction = isTransferClass || isTransferSchool;

            transferClassWrap.style.display = isTransferClass ? 'grid' : 'none';
            transferSchoolWrap.style.display = isTransferSchool ? 'grid' : 'none';
            transferDateWrap.style.display = hasTransferAction ? 'grid' : 'none';

            transferClassNameInput.required = isTransferClass;
            transferSchoolNameInput.required = isTransferSchool;
            transferDateInput.required = hasTransferAction;

            if (isTransferClass) {
                rebuildSuggestions();
                syncTransferClassId();
            }
            if (!isTransferClass) {
                transferClassNameInput.value = '';
                transferClassIdInput.value = '';
                transferClassNameInput.setCustomValidity('');
            }
            if (!isTransferSchool) {
                transferSchoolNameInput.value = '';
            }
            if (!hasTransferAction) {
                transferDateInput.value = '';
            }
        }

        transferActionSelect.addEventListener('change', updateTransferUi);
        transferClassNameInput.addEventListener('input', syncTransferClassId);
        transferClassNameInput.addEventListener('change', syncTransferClassId);

        if (khoiSelect) {
            khoiSelect.addEventListener('change', function () {
                rebuildSuggestions();
                syncTransferClassId();
            });
        }
        form.addEventListener('submit', function (event) {
            syncTransferClassId();
            if ((transferActionSelect.value || '').trim() !== 'chuyen_lop') {
                return;
            }
            if (transferClassIdInput.value) {
                return;
            }

            event.preventDefault();
            transferClassNameInput.setCustomValidity('Vui lòng chọn lớp hợp lệ từ gợi ý.');
            transferClassNameInput.reportValidity();
        });

        const initialClassId = (transferClassIdInput.value || '').trim();
        if (!transferClassNameInput.value && initialClassId) {
            const matchedById = classRecords.find(function (record) {
                return normalizeValue(record.id) === normalizeValue(initialClassId);
            });
            if (matchedById) {
                transferClassNameInput.value = matchedById.label;
            }
        }

        rebuildSuggestions();
        updateTransferUi();
    })();
</script>

</body>
</html>
