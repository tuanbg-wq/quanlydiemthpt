<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>${pageTitle}</title>
    <link rel="stylesheet" href="<c:url value='/css/admin-layout.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/teacher/student/student-create.css'/>">
</head>
<body>
<div class="layout">
    <jsp:include page="/WEB-INF/views/teacher/_sidebar.jsp"/>

    <main class="main student-create-page">
        <header class="topbar">
            <div class="topbar-left">
                <h1>Thêm học sinh</h1>
                <p>Học sinh mới sẽ được gắn trực tiếp vào lớp chủ nhiệm của giáo viên.</p>
            </div>
        </header>

        <section class="content">
            <c:if test="${not empty error}">
                <div class="alert alert-error">${error}</div>
            </c:if>

            <div class="card">
                <div class="form-grid" style="margin-bottom: 18px;">
                    <div class="form-group">
                        <label>Lớp chủ nhiệm</label>
                        <input type="text" value="${empty homeroomClassName ? 'Chưa phân công' : homeroomClassName}" readonly>
                    </div>
                    <div class="form-group">
                        <label>Năm học</label>
                        <input type="text" value="${empty homeroomSchoolYear ? '-' : homeroomSchoolYear}" readonly>
                    </div>
                </div>

                <form method="post"
                      action="<c:url value='/teacher/student/create'/>"
                      enctype="multipart/form-data">
                    <input type="hidden" name="schoolYear" value="${selectedSchoolYear}">

                    <div class="form-grid">
                        <div class="form-group">
                            <label>Mã học sinh *</label>
                            <div class="input-with-action">
                                <input id="teacherStudentIdInput" type="text" name="idHocSinh" value="${student.idHocSinh}" required>
                                <button id="teacherSuggestStudentIdBtn"
                                        class="btn suggest-code-btn"
                                        type="button"
                                        data-suggested-student-id="${suggestedStudentId}"
                                        data-suggest-url="<c:url value='/teacher/student/suggest/next-student-id'/>">
                                    Gợi ý mã HS
                                </button>
                            </div>
                            <small id="teacherSuggestStudentIdStatus"></small>
                        </div>
                        <div class="form-group">
                            <label>Họ tên *</label>
                            <input type="text" name="hoTen" value="${student.hoTen}" required>
                        </div>
                        <div class="form-group">
                            <label>Ngày sinh *</label>
                            <input type="date" name="ngaySinh" value="${student.ngaySinh}" required>
                        </div>
                        <div class="form-group">
                            <label>Giới tính</label>
                            <select name="gioiTinh">
                                <option value="">-- Chọn --</option>
                                <option value="Nam" ${student.gioiTinh == 'Nam' ? 'selected' : ''}>Nam</option>
                                <option value="Nữ" ${student.gioiTinh == 'Nữ' || student.gioiTinh == 'Nu' ? 'selected' : ''}>Nữ</option>
                            </select>
                        </div>

                        <div class="form-group">
                            <label>Nơi sinh</label>
                            <input type="text" name="noiSinh" value="${student.noiSinh}">
                        </div>
                        <div class="form-group">
                            <label>Dân tộc</label>
                            <input type="text" name="danToc" value="${student.danToc}">
                        </div>
                        <div class="form-group">
                            <label>Số điện thoại</label>
                            <input type="text" name="soDienThoai" value="${student.soDienThoai}">
                        </div>
                        <div class="form-group">
                            <label>Email</label>
                            <input type="text" name="email" value="${student.email}">
                        </div>

                        <div class="form-group form-group-full">
                            <label>Địa chỉ</label>
                            <textarea name="diaChi" rows="3">${student.diaChi}</textarea>
                        </div>

                        <div class="form-group">
                            <label>Họ tên cha</label>
                            <input type="text" name="hoTenCha" value="${student.hoTenCha}">
                        </div>
                        <div class="form-group">
                            <label>SĐT cha</label>
                            <input type="text" name="sdtCha" value="${student.sdtCha}">
                        </div>
                        <div class="form-group">
                            <label>Họ tên mẹ</label>
                            <input type="text" name="hoTenMe" value="${student.hoTenMe}">
                        </div>
                        <div class="form-group">
                            <label>SĐT mẹ</label>
                            <input type="text" name="sdtMe" value="${student.sdtMe}">
                        </div>

                        <div class="form-group">
                            <label>Ngày nhập học *</label>
                            <input type="date" name="ngayNhapHoc" value="${student.ngayNhapHoc}" required>
                        </div>
                        <div class="form-group">
                            <label>Trạng thái</label>
                            <select name="trangThai">
                                <option value="dang_hoc" ${empty student.trangThai || student.trangThai == 'dang_hoc' ? 'selected' : ''}>Đang học</option>
                                <option value="da_tot_nghiep" ${student.trangThai == 'da_tot_nghiep' ? 'selected' : ''}>Đã tốt nghiệp</option>
                                <option value="bo_hoc" ${student.trangThai == 'bo_hoc' ? 'selected' : ''}>Bỏ học</option>
                                <option value="chuyen_truong" ${student.trangThai == 'chuyen_truong' ? 'selected' : ''}>Chuyển trường</option>
                            </select>
                        </div>

                        <div class="form-group">
                            <label>Hạnh kiểm học kỳ I</label>
                            <select name="hanhKiemHocKy1">
                                <option value="" ${empty student.hanhKiemHocKy1 ? 'selected' : ''}>(Chưa có)</option>
                                <option value="Tốt">Tốt</option>
                                <option value="Khá">Khá</option>
                                <option value="Trung bình">Trung bình</option>
                                <option value="Yếu">Yếu</option>
                                <option value="Kém">Kém</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label>Hạnh kiểm học kỳ II</label>
                            <select name="hanhKiemHocKy2">
                                <option value="" ${empty student.hanhKiemHocKy2 ? 'selected' : ''}>(Chưa có)</option>
                                <option value="Tốt">Tốt</option>
                                <option value="Khá">Khá</option>
                                <option value="Trung bình">Trung bình</option>
                                <option value="Yếu">Yếu</option>
                                <option value="Kém">Kém</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label>Hạnh kiểm cả năm</label>
                            <select name="hanhKiemCaNam">
                                <option value="" ${empty student.hanhKiemCaNam ? 'selected' : ''}>(Chưa có)</option>
                                <option value="Tốt">Tốt</option>
                                <option value="Khá">Khá</option>
                                <option value="Trung bình">Trung bình</option>
                                <option value="Yếu">Yếu</option>
                                <option value="Kém">Kém</option>
                            </select>
                        </div>

                        <div class="form-group form-group-full">
                            <label>Ảnh học sinh</label>
                            <div class="avatar-preview" id="avatarPreviewTeacherCreate" style="display: none;">
                                <img id="avatarPreviewTeacherCreateImg" alt="preview ảnh học sinh"/>
                            </div>
                            <input id="avatarInputTeacherCreate" type="file" name="avatar" accept="image/png,image/jpeg,image/webp">
                        </div>
                    </div>

                    <div class="form-actions">
                        <button class="btn primary" type="submit">Lưu</button>
                        <c:url var="backStudentListUrl" value="/teacher/student">
                            <c:if test="${not empty selectedSchoolYear}">
                                <c:param name="schoolYear" value="${selectedSchoolYear}"/>
                            </c:if>
                        </c:url>
                        <a class="btn" href="${backStudentListUrl}">Quay lại danh sách</a>
                    </div>
                </form>
            </div>
        </section>
    </main>
</div>
<script>
    (function () {
        const studentIdInput = document.getElementById('teacherStudentIdInput');
        const suggestButton = document.getElementById('teacherSuggestStudentIdBtn');
        const suggestStatus = document.getElementById('teacherSuggestStudentIdStatus');

        if (studentIdInput && suggestButton) {
            const suggestedFromServer = suggestButton.getAttribute('data-suggested-student-id');
            if ((!studentIdInput.value || !studentIdInput.value.trim()) && suggestedFromServer) {
                studentIdInput.value = suggestedFromServer;
            }

            const suggestUrl = suggestButton.getAttribute('data-suggest-url');
            suggestButton.addEventListener('click', async function () {
                if (!suggestUrl) {
                    return;
                }

                const originalText = suggestButton.textContent;
                suggestButton.disabled = true;
                suggestButton.textContent = 'Đang gợi ý...';
                if (suggestStatus) {
                    suggestStatus.textContent = '';
                }

                try {
                    const response = await fetch(suggestUrl, {
                        headers: {
                            'X-Requested-With': 'XMLHttpRequest'
                        }
                    });
                    const payload = await response.json();
                    if (!response.ok || !payload || !payload.suggestedStudentId) {
                        throw new Error((payload && payload.error) ? payload.error : 'Không thể gợi ý mã học sinh.');
                    }

                    studentIdInput.value = payload.suggestedStudentId;
                    studentIdInput.dispatchEvent(new Event('input', { bubbles: true }));
                    if (suggestStatus) {
                        suggestStatus.textContent = 'Đã điền mã học sinh gợi ý mới nhất.';
                    }
                } catch (error) {
                    if (suggestStatus) {
                        suggestStatus.textContent = error && error.message
                                ? error.message
                                : 'Không thể gợi ý mã học sinh.';
                    }
                } finally {
                    suggestButton.disabled = false;
                    suggestButton.textContent = originalText;
                }
            });
        }

        const input = document.getElementById('avatarInputTeacherCreate');
        const preview = document.getElementById('avatarPreviewTeacherCreate');
        const img = document.getElementById('avatarPreviewTeacherCreateImg');

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
</script>
</body>
</html>
