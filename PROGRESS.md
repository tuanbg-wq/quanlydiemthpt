# Progress Log (2026-03-09)

## Da hoan thanh

- Tach CSS theo trang:
  - `src/main/resources/static/css/admin-layout.css` (layout + sidebar chung admin)
  - `src/main/resources/static/css/dashboard.css`
  - `src/main/resources/static/css/student-list.css`
  - `src/main/resources/static/css/student-form.css`
  - `src/main/resources/static/css/student-create.css`
  - `src/main/resources/static/css/student-edit.css`
  - `src/main/resources/static/css/auth-common.css`
  - `src/main/resources/static/css/login.css`
  - `src/main/resources/static/css/register.css`
- Xoa file cu `src/main/resources/static/css/admin.css`.
- Tach JS dung chung auth: `src/main/resources/static/js/auth-form.js`.
- Cap nhat JSP de dung CSS/JS moi:
  - `src/main/webapp/WEB-INF/views/login.jsp`
  - `src/main/webapp/WEB-INF/views/register.jsp`
  - `src/main/webapp/WEB-INF/views/admin/dashboard.jsp`
  - `src/main/webapp/WEB-INF/views/admin/student.jsp`
  - `src/main/webapp/WEB-INF/views/admin/student-create.jsp`
  - `src/main/webapp/WEB-INF/views/admin/student-edit.jsp`
  - `src/main/webapp/WEB-INF/views/admin/_sidebar.jsp`

## Toi uu logic (giam lap)

- Them helper chung model cho cac trang hoc sinh:
  - `src/main/java/com/quanly/webdiem/controller/admin/StudentPageModelHelper.java`
- Refactor controller:
  - `StudentCreateController`
  - `StudentEditController`
  - `StudentListController`
- Refactor service:
  - `src/main/java/com/quanly/webdiem/model/service/admin/StudentService.java`
  - Gom map field, xu ly avatar, xu ly history de dung chung.

## Loi dang ky da fix

- Trieu chung: mo `/register` bi `500 Internal Server Error`.
- Nguyen nhan: loi parse JSP o `register.jsp` (`<form:form ... data-auth-form>` thieu `="..."`).
- Da sua:
  - `data-auth-form` -> `data-auth-form="true"` trong:
    - `src/main/webapp/WEB-INF/views/register.jsp`
    - `src/main/webapp/WEB-INF/views/login.jsp`

## Kiem tra

- `./mvnw.cmd -q -DskipTests compile` pass.
- `./mvnw.cmd -q test` pass.
- Da bo sung test trang auth:
  - `src/test/java/com/quanly/webdiem/AuthPagesIntegrationTest.java`
  - Xac nhan `/login` va `/register` tra `200`.

---

# Progress Log (2026-03-10)

## Da hoan thanh

- Hoan tat bo `student-form.css`, tach style rieng cho tung trang:
  - `src/main/resources/static/css/student-create.css`
  - `src/main/resources/static/css/student-edit.css`
  - Xoa file: `src/main/resources/static/css/student-form.css`
- Sua `student-edit` de luu duoc cac truong:
  - `idHocSinh`, `courseId`, `tenKhoa`, `khoi`
  - Cap nhat JSP + controller + service tuong ung.
- Bo sung menu va trang `Quan ly lop` trong sidebar:
  - `src/main/webapp/WEB-INF/views/admin/_sidebar.jsp`
  - `src/main/java/com/quanly/webdiem/controller/admin/AdminController.java`
  - `src/main/webapp/WEB-INF/views/admin/class.jsp`
- Them trang thong tin hoc sinh day du + lich su chinh sua:
  - `src/main/webapp/WEB-INF/views/admin/student-info.jsp`
  - `src/main/resources/static/css/student-info.css`
  - `src/main/java/com/quanly/webdiem/controller/admin/StudentInfoController.java`
- Tich hop ghi lich su chinh sua vao `activity_logs`:
  - `src/main/java/com/quanly/webdiem/model/entity/ActivityLog.java`
  - `src/main/java/com/quanly/webdiem/model/dao/ActivityLogDAO.java`
  - `src/main/java/com/quanly/webdiem/model/service/admin/ActivityLogService.java`
  - `src/main/java/com/quanly/webdiem/model/service/admin/StudentService.java`
  - `src/main/java/com/quanly/webdiem/controller/admin/StudentEditController.java`
- Fix dropdown hanh dong `...` o trang danh sach hoc sinh (khong con bi che):
  - `src/main/resources/static/css/student-list.css`
  - `src/main/webapp/WEB-INF/views/admin/student.jsp`
- Tinh chinh UI trang thong tin hoc sinh theo feedback:
  - Sua o lich su lop hoc bi gian dong
  - Dua nut `Chinh sua / Quay lai danh sach` xuong cuoi trang
  - Tang kich thuoc anh hoc sinh

## Kiem tra

- `./mvnw.cmd -q -DskipTests compile` pass.
- `./mvnw.cmd -q test` pass.

---

# Progress Log (2026-03-11)

## Da hoan thanh

- Hoan thien trang danh sach mon hoc:
  - `src/main/webapp/WEB-INF/views/admin/subject.jsp`
  - Viet hoa text giao dien.
  - Hien thi day du cot: ma mon, ten mon, khoi lop, nam hoc, hoc ky, to bo mon, giao vien.
  - Cot hoc ky hien thi dung: `Ca nam`, `Hoc ky 1`, `Hoc ky 2`.
  - Menu hanh dong `...`:
    - Bo `Chi tiet mon hoc`
    - Them `Chinh sua`
    - Them `Xoa`

- Them trang tao mon hoc:
  - `src/main/webapp/WEB-INF/views/admin/subject-create.jsp`
  - `src/main/resources/static/css/subject-create.css`
  - Form co cac truong:
    - ma mon hoc, ten mon hoc, khoa hoc, nam hoc, ky hoc, khoi lop, to bo mon, giao vien phu trach, mo ta.
  - Khong su dung truong `he so` tren giao dien.

- Them autocomplete/goi y cho form mon hoc:
  - Khoa hoc: goi y theo keyword (vi du `K06`) voi format nhan: `K06(khoa 2025-2027)`.
  - Nam hoc: goi y tat ca nam hoc, loc theo tu khoa nhap.
  - Giao vien: goi y theo ten hoac ma (vi du go `Nguyen` se ra cac giao vien ho Nguyen).
  - Endpoint goi y:
    - `GET /admin/subject/suggest/courses`
    - `GET /admin/subject/suggest/school-years`
    - `GET /admin/subject/suggest/teachers`

- Them trang chinh sua mon hoc:
  - `src/main/webapp/WEB-INF/views/admin/subject-edit.jsp`
  - Route:
    - `GET /admin/subject/{id}/edit`
    - `POST /admin/subject/{id}/edit`
  - Ma mon hoc de readonly khi sua.

- Them xoa mon hoc:
  - Route: `POST /admin/subject/{id}/delete`
  - Co xu ly loi rang buoc du lieu lien quan.

- Cap nhat backend/service cho module mon hoc:
  - `src/main/java/com/quanly/webdiem/controller/admin/SubjectCreateController.java`
  - `src/main/java/com/quanly/webdiem/controller/admin/SubjectListController.java`
  - `src/main/java/com/quanly/webdiem/model/service/admin/SubjectService.java`
  - `src/main/java/com/quanly/webdiem/model/dao/SubjectDAO.java`
  - `src/main/java/com/quanly/webdiem/model/entity/Subject.java`

- Luu y nghiep vu:
  - Mot mon hoc co the co nhieu giao vien (theo `teaching_assignments`).
  - Danh sach hien thi giao vien chinh + `+N giao vien khac`.

## Kiem tra

- `./mvnw.cmd -q -DskipTests compile` pass.
- `./mvnw.cmd -q test` pass.

---

# Progress Log (2026-03-11 - cap nhat tiep theo chat)

## Da hoan thanh

- Cap nhat UX thong bao thao tac mon hoc:
  - Them flash message thanh cong cho luong `them / sua / xoa`.
  - Chuyen huong thong bao theo flash attribute o controller.
  - File:
    - `src/main/java/com/quanly/webdiem/controller/admin/SubjectCreateController.java`
    - `src/main/webapp/WEB-INF/views/admin/subject.jsp`

- Nang cap thao tac xoa mon hoc:
  - Bo `confirm()` mac dinh.
  - Them modal xac nhan xoa (nut `Huy` / `Xoa mon hoc`, dong bang click nen hoac phim `Esc`).
  - File:
    - `src/main/webapp/WEB-INF/views/admin/subject.jsp`
    - `src/main/resources/static/css/subject-list.css`

- Lam dep menu hanh dong `...` o danh sach mon hoc:
  - Cai tien giao dien item `Chinh sua / Xoa` (icon, spacing, hover state).
  - Sua logic dropdown de khong bi cat, menu hien thi theo viewport.
  - File:
    - `src/main/webapp/WEB-INF/views/admin/subject.jsp`
    - `src/main/resources/static/css/subject-list.css`

- Refactor service mon hoc theo huong tach nghiep vu de de quan ly:
  - Facade:
    - `src/main/java/com/quanly/webdiem/model/service/admin/SubjectService.java`
  - Nhom nghiep vu:
    - `src/main/java/com/quanly/webdiem/model/service/admin/SubjectCreateService.java`
    - `src/main/java/com/quanly/webdiem/model/service/admin/SubjectQueryService.java`
    - `src/main/java/com/quanly/webdiem/model/service/admin/SubjectUpdateService.java`
    - `src/main/java/com/quanly/webdiem/model/service/admin/SubjectDeleteService.java`
    - `src/main/java/com/quanly/webdiem/model/service/admin/SubjectFormService.java`
  - Chuyen cac model dung chung sang `entity`:
    - `src/main/java/com/quanly/webdiem/model/entity/SubjectCreateForm.java`
    - `src/main/java/com/quanly/webdiem/model/entity/SubjectSearch.java`
    - `src/main/java/com/quanly/webdiem/model/entity/SubjectSharedService.java`
  - Cap nhat import va usage tuong ung:
    - `src/main/java/com/quanly/webdiem/controller/admin/SubjectCreateController.java`
    - `src/main/java/com/quanly/webdiem/controller/admin/SubjectListController.java`

- Fix loi ky tu tieng Viet tren tab trinh duyet (title):
  - Nguyen nhan: chuoi `pageTitle` trong controller bi sai encoding.
  - Cach sua: thay chuoi title/message bang Unicode escape de tranh loi mojibake.
  - File:
    - `src/main/java/com/quanly/webdiem/controller/admin/SubjectListController.java`
    - `src/main/java/com/quanly/webdiem/controller/admin/SubjectCreateController.java`

## Kiem tra

- Da compile/test pass o cac buoc refactor service va cap nhat package.
- Rieng buoc check cuoi sau khi sua title bi han che boi moi truong sandbox Maven local repo, can chay lai tren may local:
  - `./mvnw.cmd -q -DskipTests compile`
  - `./mvnw.cmd -q test`

---

# Progress Log (2026-03-12)

## Da hoan thanh

- Cap nhat form them giao vien:
  - Vai tro giao vien chuyen tu checkbox sang radio (chi cho phep chon 1 vai tro).
  - Cap nhat backend validate bat buoc dung 1 vai tro.
  - File:
    - `src/main/webapp/WEB-INF/views/admin/teacher-create.jsp`
    - `src/main/java/com/quanly/webdiem/model/service/admin/TeacherCreateValidator.java`
    - `src/main/java/com/quanly/webdiem/model/service/admin/TeacherCreateService.java`

- Cap nhat trang thai tren form:
  - Doi nhan hien thi `Tam nghi` thanh `Da nghi`.
  - File:
    - `src/main/java/com/quanly/webdiem/model/service/admin/TeacherCreateService.java`

- Fix loi mojibake tieng Viet tren trang them giao vien:
  - Nguyen nhan: noi dung chuoi tieng Viet trong JSP va service/validator bi save sai encoding.
  - Cach sua: viet lai noi dung tieng Viet dung UTF-8 (khong BOM) cho:
    - `src/main/webapp/WEB-INF/views/admin/teacher-create.jsp`
    - `src/main/java/com/quanly/webdiem/model/service/admin/TeacherCreateService.java`
    - `src/main/java/com/quanly/webdiem/model/service/admin/TeacherCreateValidator.java`

## Kiem tra

- `./mvnw.cmd -q -DskipTests compile` pass.
- `./mvnw.cmd -q test` pass.

---

# Progress Log (2026-03-12 - cap nhat tiep theo chat 2)

## Da hoan thanh

- Hoan thien trang sua giao vien voi day du thong tin tu trang them giao vien:
  - Route:
    - `GET /admin/teacher/{id}/edit`
    - `POST /admin/teacher/{id}/edit`
  - File:
    - `src/main/java/com/quanly/webdiem/controller/admin/TeacherEditController.java`
    - `src/main/java/com/quanly/webdiem/model/service/admin/TeacherEditService.java`
    - `src/main/webapp/WEB-INF/views/admin/teacher-edit.jsp`

- Noi luong tu danh sach giao vien sang trang sua:
  - Cap nhat nut `Chinh sua` trong menu hanh dong `...`.
  - File:
    - `src/main/webapp/WEB-INF/views/admin/teacher.jsp`

- Fix hien thi tieng Viet cho module giao vien:
  - Sua text bi mojibake tren trang danh sach/tao/sua giao vien.
  - Fix chuoi message backend bi hien thi raw dang `\u00xx`.
  - File:
    - `src/main/webapp/WEB-INF/views/admin/teacher.jsp`
    - `src/main/webapp/WEB-INF/views/admin/teacher-create.jsp`
    - `src/main/webapp/WEB-INF/views/admin/teacher-edit.jsp`
    - `src/main/java/com/quanly/webdiem/model/service/admin/TeacherCreateValidator.java`
    - `src/main/java/com/quanly/webdiem/model/service/admin/TeacherEditService.java`

- Cap nhat UX form them giao vien theo feedback:
  - Cho phep nhap tay truong `Nam hoc ap dung vai tro` (bo readonly).
  - Dua nut `Quay lai danh sach` tu topbar xuong cuoi form.
  - Bo nut `Quay lai` canh nut `Lam moi`.
  - File:
    - `src/main/webapp/WEB-INF/views/admin/teacher-create.jsp`

- Fix loi 500 khi luu giao vien:
  - Trieu chung: submit form them giao vien bi `500 Internal Server Error`.
  - Nguyen nhan: query check email trong `TeacherDAO` tra ve gia tri so (0/1), nhung service/validator ep kieu Boolean -> `ClassCastException`.
  - Cach sua:
    - Doi query sang `COUNT(*)` trong DAO.
    - Doi logic validate sang check `count > 0`.
  - File:
    - `src/main/java/com/quanly/webdiem/model/dao/TeacherDAO.java`
    - `src/main/java/com/quanly/webdiem/model/service/admin/TeacherCreateValidator.java`

- Tang do ben vung de tranh trang trang 500:
  - Bổ sung log va bat `Exception` trong create controller.
  - Them fallback an toan cho teacher list controller neu query loi.
  - File:
    - `src/main/java/com/quanly/webdiem/controller/admin/TeacherCreateController.java`
    - `src/main/java/com/quanly/webdiem/controller/admin/TeacherListController.java`

- Bo sung test de bat loi 500 luong them giao vien:
  - Them test POST tao giao vien va assert redirect (khong 500).
  - File:
    - `src/test/java/com/quanly/webdiem/AdminSubjectPageIntegrationTest.java`

## Kiem tra

- `mvn -q -DskipTests compile` pass.
- `mvn -q -Dtest=AdminSubjectPageIntegrationTest#teacherCreatePostShouldNotReturn500 test` pass.
- `mvn -q -Dtest=AdminSubjectPageIntegrationTest test` pass.
- `mvn -q test` pass.

## Git

- Da luu mốc o commit:
  - `1934fa2` - `Fix teacher create 500 and refine teacher form UX`
---

# Progress Log (2026-03-13)

## Da hoan thanh

- Them trang thong tin giao vien day du + lich su cong tac + export:
  - Trang thong tin:
    - `src/main/webapp/WEB-INF/views/admin/teacher-info.jsp`
    - `src/main/resources/static/css/teacher-info.css`
    - `src/main/java/com/quanly/webdiem/controller/admin/TeacherInfoController.java`
    - `src/main/java/com/quanly/webdiem/model/service/admin/TeacherInfoService.java`
  - Export:
    - `src/main/java/com/quanly/webdiem/controller/admin/TeacherInfoExportController.java`
    - `src/main/java/com/quanly/webdiem/model/service/admin/TeacherInfoExportService.java`

- Nang cap xuat PDF giao vien:
  - Co anh avatar.
  - Bo cuc hop ly (profile + thong tin chi tiet + lich su cong tac).
  - Hien thi tieng Viet dung font Unicode.

- Cap nhat xuat Excel giao vien:
  - Toan bo ten sheet / tieu de / nhan cot chuyen sang tieng Viet.

- Hoan thien luu du lieu mon hoc theo cot DB thuc te (khong nhet metadata):
  - Luu truc tiep vao: `id_khoa`, `nam_hoc_ap_dung`, `hoc_ky_ap_dung`, `khoi_ap_dung`, `to_bo_mon`, `id_giao_vien_phu_trach`, `mo_ta`.
  - File chinh:
    - `src/main/java/com/quanly/webdiem/model/entity/Subject.java`
    - `src/main/java/com/quanly/webdiem/model/dao/SubjectDAO.java`
    - `src/main/java/com/quanly/webdiem/model/service/admin/SubjectFormService.java`
    - `src/main/java/com/quanly/webdiem/model/service/admin/SubjectQueryService.java`
    - `src/main/java/com/quanly/webdiem/model/service/admin/SubjectUpdateService.java`

- Loai bo `so_tiet`, `he_so` khoi bang `subjects` tren DB local:
  - Da chay `ALTER TABLE` tren local.
  - Them file SQL de ap dung cho moi truong khac:
    - `src/main/resources/db/manual/2026-03-13-subject-drop-legacy-columns.sql`

- Fix bo loc `khoi lop` o trang danh sach giao vien de khop du lieu DB:
  - Bo sung loc theo du lieu phu trach mon hoc (`subjects.id_giao_vien_phu_trach + subjects.khoi_ap_dung`) ngoai nhom `classes` va `teaching_assignments`.
  - File:
    - `src/main/java/com/quanly/webdiem/model/dao/TeacherDAO.java`

- Them rang buoc ma cheo:
  - Ma giao vien khong duoc trung ma mon hoc.
  - Ma mon hoc khong duoc trung ma giao vien.
  - File:
    - `src/main/java/com/quanly/webdiem/model/service/admin/TeacherCreateValidator.java`
    - `src/main/java/com/quanly/webdiem/model/service/admin/SubjectCreateService.java`

- Cho phep sua `ma mon hoc` tren trang edit + doi ma an toan:
  - UI bo readonly o truong ma mon hoc.
  - Backend doi ma trong transaction va cap nhat bang lien quan truoc khi doi bang `subjects`:
    - `teaching_assignments`
    - `scores`
    - `average_scores`
  - File:
    - `src/main/webapp/WEB-INF/views/admin/subject-edit.jsp`
    - `src/main/java/com/quanly/webdiem/model/service/admin/SubjectUpdateService.java`
    - `src/main/java/com/quanly/webdiem/model/dao/SubjectDAO.java`

## Kiem tra

- `mvn -q -DskipTests compile` pass.
- `mvn -q -Dtest=AdminSubjectPageIntegrationTest test` pass.
- Da verify nhanh tren DB local: loc khoi `10` o danh sach giao vien tra ve giao vien phu hop theo du lieu mon hoc.

## Git

- `8bd0445` - `Add teacher info page and improve teacher PDF export layout`
- `2acb986` - `Localize teacher Excel export to Vietnamese labels`
- `3886ac4` - `Persist subject core fields to DB columns and remove legacy duration/weight fields`
- `e9608b2` - `Fix teacher grade filter to match subject responsibility data`
- `75ac239` - `Prevent code conflicts between teacher IDs and subject IDs`
- `09e1ba5` - `Allow editing subject code with safe reference migration`

---

# Progress Log (2026-03-14)

## Da hoan thanh

- Hoan thien luong cho phep sua ma giao vien:
  - Da mo cho sua ma tren form edit giao vien.
  - Da migrate reference an toan khi doi ma giao vien de khong vo FK/trigger.
  - Bo sung cap nhat cac bang lien quan khi doi ma:
    - `classes`
    - `teaching_assignments`
    - `subjects.id_giao_vien_phu_trach`
    - `scores`
    - `conducts`
  - File chinh:
    - `src/main/java/com/quanly/webdiem/model/service/admin/TeacherEditService.java`
    - `src/main/java/com/quanly/webdiem/model/dao/TeacherDAO.java`
    - `src/test/java/com/quanly/webdiem/model/service/admin/TeacherEditServiceTest.java`

- Tao moi trang Quan ly lop hoc day du:
  - Route:
    - `GET /admin/class`
  - Co thong ke dau trang, bo loc, bang du lieu, phan trang.
  - Da bo cot `ID lop`.
  - Cot `Si so` chi hien thi tong si so (khong dang `x/y`).
  - File chinh:
    - `src/main/java/com/quanly/webdiem/controller/admin/ClassListController.java`
    - `src/main/java/com/quanly/webdiem/model/service/admin/ClassManagementService.java`
    - `src/main/java/com/quanly/webdiem/model/service/admin/ClassManagementQueryService.java`
    - `src/main/java/com/quanly/webdiem/model/dao/ClassDAO.java`
    - `src/main/java/com/quanly/webdiem/model/entity/ClassSearch.java`
    - `src/main/webapp/WEB-INF/views/admin/class.jsp`
    - `src/main/resources/static/css/class-list.css`

- Nang cap cot thao tac trang Quan ly lop:
  - Chuyen sang menu `...` doc.
  - Co xu ly hien thi dropdown theo viewport, dong khi click ngoai/resize/scroll/Esc.
  - File:
    - `src/main/webapp/WEB-INF/views/admin/class.jsp`
    - `src/main/resources/static/css/class-list.css`

- Cap nhat cot Khoa hoc o trang danh sach lop:
  - Hien thi them `id_khoa` theo format:
    - `K06( Khoa 2025-2028)`
  - File:
    - `src/main/java/com/quanly/webdiem/model/dao/ClassDAO.java`

- Tao trang Them lop hoc moi:
  - Route:
    - `GET /admin/class/create`
    - `POST /admin/class/create`
  - Them endpoint goi y giao vien chu nhiem:
    - `GET /admin/class/suggest/homeroom-teachers`
  - Yeu cau nghiep vu da ap dung:
    - Bo vi du placeholder o o `Ten lop hoc`.
    - `id_lop = ten_lop` khi tao moi.
    - Giao vien chu nhiem co the goi y + chon.
    - Co nut `Quay lai`.
  - File chinh:
    - `src/main/java/com/quanly/webdiem/model/entity/ClassCreateForm.java`
    - `src/main/java/com/quanly/webdiem/model/service/admin/ClassManagementCreateService.java`
    - `src/main/java/com/quanly/webdiem/controller/admin/ClassListController.java`
    - `src/main/java/com/quanly/webdiem/model/entity/ClassEntity.java`
    - `src/main/java/com/quanly/webdiem/model/dao/TeacherDAO.java`
    - `src/main/webapp/WEB-INF/views/admin/class-create.jsp`
    - `src/main/resources/static/css/class-create.css`

- Bo sung test integration cho module lop:
  - `classPageShouldLoadForAdmin`
  - `classCreatePageShouldLoadForAdmin`
  - `classTeacherSuggestionEndpointShouldLoadForAdmin`
  - File:
    - `src/test/java/com/quanly/webdiem/AdminSubjectPageIntegrationTest.java`

## Kiem tra

- `./mvnw.cmd -q -DskipTests compile` pass.
- `./mvnw.cmd -q -Dtest=TeacherEditServiceTest test` pass.
- `./mvnw.cmd -q -Dtest=AdminSubjectPageIntegrationTest test` pass.
- `./mvnw.cmd -q test` pass.

## Git

- `6a9367e` - `Allow editing teacher code with safe reference migration`
- `df787f7` - `Fix teacher code rename order for teacher_roles update`
- `36c8e12` - `Make teacher code rename migrate all related references safely`
- `61705e9` - `Build class management page with filters and class size totals`
- `6c834fb` - `Use vertical ellipsis action menu in class list`
- `f9c21ab` - `Add class create flow with teacher suggestions and course id display`

---

# Progress Log (2026-03-16)

## Da hoan thanh

- Loai bo thong tin khong can thiet o trang chi tiet lop:
  - Bo `Ma lop` va `Si so he thong`.
  - File:
    - `src/main/webapp/WEB-INF/views/admin/class-info.jsp`

- Tao moi module Quan ly diem so (danh sach) theo cau truc MVC:
  - Route:
    - `GET /admin/score`
    - `GET /admin/score/create`
  - Co thong ke dau trang, bo loc, bang du lieu, phan trang.
  - Co them bo loc `Khoa hoc`.
  - Co nut `+ Them diem so`.
  - File chinh:
    - `src/main/java/com/quanly/webdiem/controller/admin/ScoreListController.java`
    - `src/main/java/com/quanly/webdiem/model/entity/Score.java`
    - `src/main/java/com/quanly/webdiem/model/entity/ScoreSearch.java`
    - `src/main/java/com/quanly/webdiem/model/dao/ScoreDAO.java`
    - `src/main/java/com/quanly/webdiem/model/service/admin/ScoreManagementService.java`
    - `src/main/java/com/quanly/webdiem/model/service/admin/ScoreQueryService.java`
    - `src/main/webapp/WEB-INF/views/admin/score.jsp`
    - `src/main/webapp/WEB-INF/views/admin/score-create.jsp`
    - `src/main/resources/static/css/score-list.css`
  - Dieu huong route cu:
    - `/admin/scores` -> `redirect:/admin/score`
    - File:
      - `src/main/java/com/quanly/webdiem/controller/admin/AdminController.java`

- Nang cap danh sach diem theo feedback:
  - Them option hoc ky `Ca nam`.
  - Bo cac cot `Mieng`, `15 phut`, `1 tiet`, `Hoc ky`.
  - Them cot `Thao tac` gom:
    - `Chi tiet diem`
    - `Chinh sua`
    - `Xoa` (co modal xac nhan truoc khi xoa)
  - Them luong backend cho thao tac diem:
    - `GET /admin/score/detail`
    - `GET /admin/score/edit`
    - `POST /admin/score/edit`
    - `POST /admin/score/delete`
  - Them service tach nghiep vu:
    - `ScoreUpdateService`
    - `ScoreDeleteService`
  - Them view:
    - `src/main/webapp/WEB-INF/views/admin/score-detail.jsp`
    - `src/main/webapp/WEB-INF/views/admin/score-edit.jsp`

- Cap nhat test integration:
  - Them test cho module score:
    - load trang danh sach/them
    - redirect khi detail/edit khong tim thay
    - redirect sau delete
  - File:
    - `src/test/java/com/quanly/webdiem/AdminSubjectPageIntegrationTest.java`

## Dang cap nhat (chua commit)

- Card `Ty le kha gioi` tren trang danh sach diem:
  - Doi sang bieu do tron (donut chart).
  - Them hieu ung animate khi mo trang.
  - File:
    - `src/main/webapp/WEB-INF/views/admin/score.jsp`
    - `src/main/resources/static/css/score-list.css`

## Kiem tra

- `./mvnw.cmd -q -DskipTests compile` pass.
- `./mvnw.cmd -q -Dtest=AdminSubjectPageIntegrationTest test` pass.

## Git

- `6a5310d` - `Remove class code and system size from class detail page`
- `a4e93dc` - `Build score management list page with course filter`
- `c7cc3e7` - `Refine score list columns and add score action workflows`

---

# Progress Log (2026-03-18)

## Da hoan thanh

- Fix trang sua giao vien de hien du lieu cu on dinh cho 2 o lop:
  - `Lop bo mon`
  - `Lop chu nhiem`
- Bo sung fallback nam hoc khi mo form edit:
  - Neu nam hoc hien tai khong co du lieu phan cong/chu nhiem thi tu dong lay nam hoc gan nhat co du lieu.
- Tang do ben mapping mon hoc trong edit service:
  - Match theo `id_mon_hoc`, `ten_mon_hoc` va so khop khong phan biet dau de tranh mat du lieu lop khi `chuyen_mon` luu khac format.

- Fix loi trung role khi sua giao vien (`uq_teacher_roles_unique`):
  - Doi logic upsert role thanh xoa role theo `teacher + nam_hoc` roi tao moi.
  - Them thong bao loi ro rang khi du lieu role trong nam hoc bi trung.

- Cap nhat goi y lop tren form giao vien:
  - `Lop chu nhiem` (trang sua): chi goi y lop chua co GVCN, va van giu lop dang gan cho chinh giao vien dang sua.
  - `Lop bo mon`: goi y tat ca lop (khong con loc theo khoi ap dung mon).
  - Cap nhat format label goi y lop theo dang: `10A1 (Khoi 10) - nam hoc 2025-2026`.

- Bo sung validate nghiep vu khi luu:
  - Neu chon lop chu nhiem da co GVCN khac thi chan luu va bao loi ro rang:
    - `Lop nay da co giao vien chu nhiem, vui long chon lop khac.`

- Chinh placeholder tren form giao vien:
  - `Nhap ma lop chu nhiem, vi du: 10A1 (Khoi 10) - nam hoc 2025-2026`.

## File chinh da cap nhat

- `src/main/java/com/quanly/webdiem/model/service/admin/TeacherEditService.java`
- `src/main/java/com/quanly/webdiem/model/dao/TeacherRoleDAO.java`
- `src/main/java/com/quanly/webdiem/model/dao/TeacherDAO.java`
- `src/main/java/com/quanly/webdiem/model/service/admin/TeacherCreateService.java`
- `src/main/java/com/quanly/webdiem/model/service/admin/TeacherCreateValidator.java`
- `src/main/java/com/quanly/webdiem/controller/admin/TeacherCreateController.java`
- `src/main/java/com/quanly/webdiem/controller/admin/TeacherEditController.java`
- `src/main/webapp/WEB-INF/views/admin/teacher-create.jsp`
- `src/main/webapp/WEB-INF/views/admin/teacher-edit.jsp`

## Kiem tra

- `./mvnw.cmd -q -DskipTests compile` pass.
- `./mvnw.cmd -q -Dtest=AdminSubjectPageIntegrationTest test` pass.

## Trang thai

- Da luu trang thai vao `E:\webdiem\PROGRESS.md`.
- Chua commit (dang o trang thai working tree).

---

# Progress Log (2026-03-20)

## Da hoan thanh

- Thiet ke lai trang chi tiet diem so theo huong chi xem (khong sua truc tiep tai trang detail):
  - Co tab chuyen `Hoc ky I / Hoc ky II / Ca nam`.
  - Co thong tin tong quan hoc sinh + mon + nam hoc.
  - Bo sung hien thi hanh kiem va nhan xet theo tung hoc ky, va ket qua ca nam.
  - File:
    - `src/main/webapp/WEB-INF/views/admin/score-detail.jsp`
    - `src/main/resources/static/css/score-list.css`

- Them xuat file chi tiet diem (Excel + PDF) va dam bao tieng Viet khong loi font:
  - Endpoint:
    - `GET /admin/score/detail/export/excel`
    - `GET /admin/score/detail/export/pdf`
  - PDF su dung font Unicode embed de hien thi tieng Viet dung.
  - File:
    - `src/main/java/com/quanly/webdiem/controller/admin/ScoreListController.java`
    - `src/main/java/com/quanly/webdiem/model/service/admin/ScoreDetailExportService.java`

- Bo sung `Nhan xet` vao luong them/chinh sua diem:
  - Them o nhap nhan xet cho hanh kiem HK1/HK2/Ca nam.
  - Luu/xuat lai nhan xet tu bang `conducts.nhan_xet`.
  - File:
    - `src/main/webapp/WEB-INF/views/admin/score-create.jsp`
    - `src/main/java/com/quanly/webdiem/model/service/admin/ScoreCreateService.java`
    - `src/main/java/com/quanly/webdiem/model/dao/ScoreDAO.java`

- Cap nhat luong detail tu trang danh sach diem:
  - Link `Chi tiet diem` giu theo hoc ky dang loc/de dang xem.
  - File:
    - `src/main/webapp/WEB-INF/views/admin/score.jsp`

- Rang buoc xuat file theo hoc ky co diem:
  - Neu chon xuat HK1/HK2 ma hoc sinh chua co diem hoc ky do -> chan xuat va thong bao ro rang bang tieng Viet.
  - Neu chon `Ca nam` ma thieu diem 1 trong 2 hoc ky -> chan xuat va bao thieu hoc ky nao.
  - Khi chan xuat se redirect lai trang detail va hien flash message (khong roi vao trang loi trang).
  - File:
    - `src/main/java/com/quanly/webdiem/controller/admin/ScoreListController.java`
    - `src/main/webapp/WEB-INF/views/admin/score-detail.jsp`

- Bo sung thong tin tren trang chi tiet theo feedback:
  - Them `Khoi`.
  - Them `Khoa` theo dang: `K05 (Khoa 2023-2026)`.
  - Them cong thuc tinh diem ca nam ngay duoi cong thuc hoc ky:
    - `DTBmcn = (DTBhkI + 2 x DTBhkII) / 3`
  - Dong bo thong tin khoi/khoa vao file xuat Excel/PDF.
  - File:
    - `src/main/java/com/quanly/webdiem/model/dao/ScoreDAO.java`
    - `src/main/java/com/quanly/webdiem/model/service/admin/ScoreManagementService.java`
    - `src/main/java/com/quanly/webdiem/model/service/admin/ScoreQueryService.java`
    - `src/main/java/com/quanly/webdiem/model/service/admin/ScoreDetailExportService.java`
    - `src/main/webapp/WEB-INF/views/admin/score-detail.jsp`

## Kiem tra

- `mvn -q -DskipTests compile` pass.

## Git

- `445db59` - `feat(score): redesign detail view with semester tabs and export; add conduct comments`
- `b324d37` - `fix(score-export): block semester export when selected term has no scores`
- `8721dc0` - `feat(score-detail): add grade/course info and annual formula`

## Trang thai

- Da luu trang thai moi vao `E:\webdiem\PROGRESS.md`.

---

# Progress Log (2026-03-21)

## Da hoan thanh

- Sua luong sua giao vien de cap nhat vai tro on dinh:
  - Khong bo qua upsert vai tro khi nguoi dung da chon role.
  - Giu nguyen nam hoc role tren form sua, khong bi fallback lop ghi de.
  - Doi luu role khi sua thanh xoa role cu theo giao vien roi tao role moi.

- Bo sung rang buoc GVCN khi sua giao vien:
  - Neu lop da co GVCN khac -> chan luu va bao loi ro rang.
  - Neu lop chua co GVCN -> cho phep gan GVCN.

- Sua danh sach giao vien:
  - Cot `Vai tro` uu tien hien thi theo phan cong thuc te:
    - Co `Lop chu nhiem` -> hien `Giao vien chu nhiem`.
    - Co `Lop bo mon` -> hien `Giao vien bo mon`.
  - Fix hien thi bo loc `Khoi` va `Trang thai` de khong bi thieu option.
  - Fix dropdown cot `Thao tac` bi chen/lop giao dien (z-index + button state).

- Sua module quan ly tai khoan:
  - Trang them tai khoan dung chung giao dien/hanh vi voi trang sua (form dong bo, khong an block thong tin giao vien).
  - Danh sach tai khoan phan trang 6 tai khoan/trang.

## File chinh da cap nhat

- `src/main/java/com/quanly/webdiem/model/service/admin/TeacherEditService.java`
- `src/main/java/com/quanly/webdiem/model/dao/TeacherDAO.java`
- `src/main/java/com/quanly/webdiem/model/service/admin/TeacherQueryService.java`
- `src/main/resources/static/css/teacher-list.css`
- `src/test/java/com/quanly/webdiem/model/service/admin/TeacherEditServiceTest.java`
- `src/test/java/com/quanly/webdiem/model/service/admin/TeacherQueryServiceTest.java`
- `src/main/java/com/quanly/webdiem/model/service/admin/AccountManagementService.java`
- `src/main/webapp/WEB-INF/views/admin/account-form.jsp`

## Kiem tra

- `./mvnw.cmd -q -DskipTests compile` pass.
- `./mvnw.cmd -q "-Dtest=TeacherEditServiceTest,TeacherQueryServiceTest" test` pass.
- `./mvnw.cmd -q test`:
  - Fail 2 test cu khong lien quan (`ClassManagementCreateServiceTest`) do sai khac chuoi ky vong/encoding tieng Viet.

## Git

- `9feead0` - `fix(teacher-edit): persist role updates reliably and preserve role school year`
- `d692e94` - `fix(teacher): enforce GVCN assignment rules and stabilize list filters`
- `1fbe3ef` - `fix(teacher-list): prioritize GVCN role when homeroom class exists`
- `6110d80` - `fix(account): align create form behavior and set page size to 6`

## Trang thai

- Da luu trang thai moi vao `E:\webdiem\PROGRESS.md`.

---

# Progress Log (2026-03-27)

## Da hoan thanh

- Sua luong doi ma lop hoc de khong bi chan boi khoa ngoai:
  - Nguyen nhan loi: FK `students.id_lop` va `teaching_assignments.id_lop` dang `RESTRICT` toi `classes.id_lop`.
  - Doi sang luong FK-safe:
    1) Tao ban ghi lop moi theo ma lop moi (tam thoi chua gan GVCN).
    2) Chuyen toan bo tham chieu `id_lop` sang ma moi.
    3) Xoa lop cu.
    4) Gan lai GVCN cho lop moi.

- Bo sung/hoan thien API + logic quan ly ma lop:
  - Ho tro doi ma lop khi sua.
  - Ho tro goi y ma lop tren trang them/sua.

- Chuan hoa thong diep loi va text tieng Viet (khong con loi chuoi khong dau trong class service/controller).

- Danh sach lop:
  - Them cot `Ma lop` rieng.
  - Cot `Ten lop` hien thi rieng (`tenLopHienThi`).

## File chinh da cap nhat

- `src/main/java/com/quanly/webdiem/model/dao/ClassDAO.java`
- `src/main/java/com/quanly/webdiem/model/service/admin/ClassManagementCreateService.java`
- `src/main/java/com/quanly/webdiem/model/service/admin/ClassManagementUpdateService.java`
- `src/main/java/com/quanly/webdiem/controller/admin/ClassListController.java`
- `src/main/java/com/quanly/webdiem/controller/admin/ClassInfoController.java`
- `src/main/webapp/WEB-INF/views/admin/class.jsp`
- `src/main/resources/static/css/class-list.css`
- `src/test/java/com/quanly/webdiem/model/service/admin/ClassManagementUpdateServiceTest.java`

## Kiem tra

- `mvn -Dtest=ClassCodeSupportTest,ClassManagementCreateServiceTest,ClassManagementUpdateServiceTest test` pass.

## Git

- `d5d4e5b` - `feat(class): allow editing class code with create/edit suggestions`
- `82f9cce` - `fix(class): support class-code rename with FK-safe flow and restore Vietnamese labels`

## Trang thai

- Da luu trang thai moi vao `E:\webdiem\PROGRESS.md`.

---

# Progress Log (2026-04-01)

## Da hoan thanh

- Chuyen toan bo hanh kiem ra khoi module quan ly diem:
  - Danh sach diem bo cot hanh kiem.
  - Form them/sua diem bo cac o hanh kiem.
  - Trang chi tiet diem bo hanh kiem/nhan xet.
  - Xuat Excel/PDF danh sach diem va chi tiet diem bo hanh kiem.
  - File chinh: `score.jsp`, `score-create.jsp`, `score-detail.jsp`, `ScoreListExportService`, `ScoreDetailExportService`.

- Chuyen hanh kiem ve module hoc sinh (HK1/HK2/Ca nam):
  - Them luu/doc hanh kiem theo hoc sinh + nam hoc.
  - Form them/sua hoc sinh co hanh kiem (cho phep de trong).
  - Trang thong tin hoc sinh hien thi hanh kiem.
  - File chinh: `Student`, `StudentService`, `ConductDAO`, `student-create.jsp`, `student-edit.jsp`, `student-info.jsp`.

- Dong bo hien thi tieng Viet + dinh dang ngay:
  - Ngay hien thi theo `dd/MM/yyyy` tren cac trang hoc sinh.
  - Trang thai hien thi dang nguoi dung (`Dang hoc`, `Da tot nghiep`, ...), khong hien raw `dang_hoc`.
  - Gioi tinh/hanh kiem hien thi co dau (`Nu`, `Tot`, `Kha`, `Trung binh`, ...).

- Sua loi font/encoding o trang hoc sinh:
  - Viet lai 4 JSP hoc sinh dang UTF-8 sach:
    - `student.jsp`
    - `student-create.jsp`
    - `student-edit.jsp`
    - `student-info.jsp`
  - Bo sung map hien thi de tranh chuoi bi vo dau/vo ma.

- Cap nhat danh sach hoc sinh:
  - Them cot `Hanh kiem`.
  - Neu hoc sinh chua co hanh kiem thi hien thi `Chua co`.

- Lich su chinh sua:
  - Bo sung xu ly normalize mojibake khi doc activity log de giam loi chuoi vo font cu.
  - Chuan hoa hien thi hanh dong/thoi gian tren trang thong tin hoc sinh.

## Kiem tra

- `mvn -f E:\webdiem\pom.xml -DskipTests compile` pass.
- `mvn -f E:\webdiem\pom.xml -Dtest=TeacherEditServiceTest test` pass.
- Full test suite van co the fail neu may khong ket noi MySQL (Communications link failure), khong phai loi compile code.

## Ghi chu

- Worktree hien tai con nhieu file dang modified theo chuoi thay doi lien tiep, chua tach commit theo tung cum tinh nang.

---

# Progress Log (2026-04-02)

## Da hoan thanh trong ngay

- Fix quyen truy cap module hoc sinh phia giao vien chu nhiem:
  - Chap nhan `ROLE_Giao_vien` cho cac controller `/teacher/student`.

- Dong bo UTF-8 + thong diep loi ro rang cho module hoc sinh:
  - Sua chuoi hien thi/exception bi vo font.
  - Bo sung validate tuoi theo khoi tai ngay nhap hoc.
  - Bo sung thong diep loi avatar (dung luong, dinh dang).

- Bo sung lich su thao tac hoc sinh theo bo loc:
  - Hien thi 5 ban ghi gan nhat.
  - Co che `Xem them`/`Thu gon` lich su.

- Fix runtime JSP EL:
  - Nguyen nhan: script trong `teacher/student.jsp` dung `${...}` (template literal JS) bi JSP EL parse.
  - Da doi sang noi chuoi JS thuong de tranh `MethodNotFoundException`.

## Commit da tao

- `d6879a2` - `fix: allow teacher role access to homeroom student module and add filtered history panel`
- `773cf55` - `fix: normalize student module UTF-8 messages and add filtered history panel`
- `4a0e342` - `fix: avoid JSP EL parsing in teacher student action-menu script`

## Trang thai hien tai (chua commit)

- Dang co cum thay doi cho admin:
  - Phan trang danh sach hoc sinh (6 hoc sinh / trang).
  - Preview anh truoc khi them/sua hoc sinh.
- Worktree hien tai van con modified files (chua commit/push):
  - `pom.xml`
  - `src/main/java/com/quanly/webdiem/controller/admin/StudentListController.java`
  - `src/main/java/com/quanly/webdiem/controller/teacher/TeacherStudentListController.java`
  - `src/main/java/com/quanly/webdiem/model/entity/ActivityLog.java`
  - `src/main/java/com/quanly/webdiem/model/service/admin/ActivityLogService.java`
  - `src/main/resources/static/css/student-list.css`
  - `src/main/webapp/WEB-INF/views/admin/student-create.jsp`
  - `src/main/webapp/WEB-INF/views/admin/student-edit.jsp`
  - `src/main/webapp/WEB-INF/views/admin/student-info.jsp`
  - `src/main/webapp/WEB-INF/views/admin/student.jsp`
  - `src/main/webapp/WEB-INF/views/teacher/student-create.jsp`
  - `src/main/webapp/WEB-INF/views/teacher/student-edit.jsp`
  - `src/main/webapp/WEB-INF/views/teacher/student-info.jsp`

---

# Progress Log (2026-04-02 - luu trang thai)

## Snapshot

- Da luu trang thai hien tai theo yeu cau "luu lai trang thai".
- Commit gan nhat:
  - `4a0e342` - `fix: avoid JSP EL parsing in teacher student action-menu script`
  - `773cf55` - `fix: normalize student module UTF-8 messages and add filtered history panel`
  - `d6879a2` - `fix: allow teacher role access to homeroom student module and add filtered history panel`

## Worktree hien tai (chua commit)

- `PROGRESS.md`
- `pom.xml`
- `src/main/java/com/quanly/webdiem/controller/admin/StudentListController.java`
- `src/main/java/com/quanly/webdiem/controller/teacher/TeacherStudentListController.java`
- `src/main/java/com/quanly/webdiem/model/entity/ActivityLog.java`
- `src/main/java/com/quanly/webdiem/model/service/admin/ActivityLogService.java`
- `src/main/resources/static/css/student-list.css`
- `src/main/webapp/WEB-INF/views/admin/student-create.jsp`
- `src/main/webapp/WEB-INF/views/admin/student-edit.jsp`
- `src/main/webapp/WEB-INF/views/admin/student-info.jsp`
- `src/main/webapp/WEB-INF/views/admin/student.jsp`
- `src/main/webapp/WEB-INF/views/teacher/student-create.jsp`
- `src/main/webapp/WEB-INF/views/teacher/student-edit.jsp`
- `src/main/webapp/WEB-INF/views/teacher/student-info.jsp`

---

# Progress Log (2026-04-03 - luu trang thai)

## Da hoan thanh trong ngay (module Khen thuong / Ky luat)

- Hoan thien luong quan ly Khen thuong / Ky luat:
  - Them trang danh sach + bo loc + thong ke + phan trang 6 hoc sinh/trang.
  - Them trang tao Khen thuong va trang tao Ky luat.
  - Them goi y tim hoc sinh, chon hoc sinh va dong bo cac o loc theo hoc sinh duoc chon.
  - Fix menu thao tac `...` (Thong tin / Sua / Xoa), khong bi cat o cuoi bang.

- Rang buoc nghiep vu:
  - So quyet dinh khong duoc trung.
  - Ngay ban hanh/ngay vi pham:
    - khong nho hon ngay nhap hoc
    - khong lon hon nam ket thuc khoa.

- Export + thong bao:
  - Chan export Excel/PDF neu khong co du lieu.
  - Bo sung thong bao thao tac thanh cong/that bai va thong bao trung so quyet dinh.

- Lich su hoat dong:
  - Them lich su tao/sua khen thuong-ky luat (co thong tin nguoi thuc hien).

- UTF-8 / tieng Viet:
  - Da fix cac chuoi thong bao va text giao dien bi vo font o cac man hinh lien quan.

- O ngay ban hanh/ngay vi pham:
  - Da chot lai theo huong nhap tay (text) va validate khi bam Luu.
  - Da bo toan bo logic auto-format khi go/blur de tranh chan ban phim.

## Commit da tao trong dot nay

- `b2622f9` - `Add conduct activity history with actor tracking`
- `f87baa1` - `Fix conduct action menu clipping on bottom rows`
- `919dcc5` - `Improve conduct date typing support with frontend normalization`
- `3fe171b` - `Fix conduct decision date input and backend normalization`
- `1447ff4` - `Improve decision date input mask for dd/mm/yyyy format`
- `ea902e9` - `Use native date input for conduct decision dates`
- `81dae26` - `Allow manual dd/mm/yyyy input for conduct decision dates`
- `43810ad` - `Remove date auto-format handlers for manual conduct date entry`

## Trang thai hien tai

- Da luu snapshot trang thai vao `E:\\webdiem\\PROGRESS.md`.
- Worktree van con nhieu file modified khac (cum hoc sinh/giao vien/diem) chua commit trong dot nay.

---

# Progress Log (2026-04-03)

## Da hoan thanh trong ngay

- Fix nhan dien vai tro giao vien o trang sua giao vien:
  - Neu giao vien co `lop chu nhiem` thi form edit uu tien hien dung `GVCN`.
  - Khong con bi mac dinh thanh `GVBM` trong truong hop GVCN chi co 1 lop bo mon.
  - GVCN co the phu trach 1 hoac nhieu lop bo mon.

- Dong bo hien thi lop theo dang `ma-ten lop` cho module giao vien:
  - `lop bo mon`
  - `lop chu nhiem`
  - Vi du: `K05A1-12A1`

- Bo sung helper tach `ma lop` tu chuoi hien thi de luu du lieu an toan:
  - Ap dung cho create/edit/validator.
  - Nguoi dung nhin thay `ma-ten lop` nhung backend van luu dung theo `ma lop`.

- Cap nhat form tao/sua giao vien:
  - Placeholder va goi y lop duoc doi sang format `ma-ten lop`.
  - Chon tu autocomplete se do vao input theo dang hien thi moi.

- Bo sung test cho luong giao vien:
  - Assert uu tien `GVCN` khi co lop chu nhiem thuc te.
  - Assert parse dung `ma lop` tu chuoi `ma-ten lop` khi luu create/edit.

## Kiem tra

- `./mvnw.cmd -q -DskipTests compile` pass
- `./mvnw.cmd -q "-Dtest=TeacherEditServiceTest,TeacherCreateServiceTest,TeacherQueryServiceTest" test` pass
- `./mvnw.cmd -q test` pass

## Trang thai hien tai

- Da cap nhat cac file lien quan:
  - `src/main/java/com/quanly/webdiem/model/service/admin/TeacherClassDisplaySupport.java`
  - `src/main/java/com/quanly/webdiem/model/service/admin/TeacherCreateService.java`
  - `src/main/java/com/quanly/webdiem/model/service/admin/TeacherCreateValidator.java`
  - `src/main/java/com/quanly/webdiem/model/service/admin/TeacherEditService.java`
  - `src/main/java/com/quanly/webdiem/model/dao/TeacherDAO.java`
  - `src/main/webapp/WEB-INF/views/admin/teacher-create.jsp`
  - `src/main/webapp/WEB-INF/views/admin/teacher-edit.jsp`
  - `src/test/java/com/quanly/webdiem/model/service/admin/TeacherCreateServiceTest.java`
  - `src/test/java/com/quanly/webdiem/model/service/admin/TeacherEditServiceTest.java`

- Chua tao commit cho cum fix nay.

---

# Progress Log (2026-04-04)

## Da hoan thanh trong ngay

- Cap nhat module diem phia giao vien chu nhiem / giao vien bo mon:
  - Dua thao tac o danh sach diem vao menu `...` dung kieu dropdown.
  - Them modal xac nhan xoa nhom diem.

- Cap nhat trang chi tiet diem:
  - Bo cong thuc o khung thong tin dau trang.
  - Khi bam `Hoc ky I`, `Hoc ky II`, `Ca nam` se hien cong thuc tinh diem theo dung tab dang xem.
  - O `Diem trung binh hoc ky` / `Diem trung binh ca nam` duoc dua vao box mau de nhin ro hon.

- Cap nhat trang them / sua diem:
  - O `Diem trung binh hoc ky` duoc lam noi bat bang box mau.
  - Bo sung rang buoc va thong bao khi bam `Loc hoc sinh` nhung chua chon du lop / mon / hoc sinh hop le.
  - Chan luu neu thieu thong tin hoac diem nam ngoai khoang `0-10`.

- Dong bo bang quy dinh so cot diem thuong xuyen theo mon hoc tu phia admin:
  - Quy dinh lay theo mon hoc giao vien dang duoc phan cong day.
  - Doc tu metadata mon hoc va fallback theo quy tac mac dinh neu mo ta chua day du.

- Them xuat du lieu danh sach diem theo bo loc hien tai:
  - Xuat `Excel`.
  - Xuat `PDF`.
  - File xuat dung tieng Viet co dau day du, co thong tin bo loc, can chinh de nhin.
  - Backend van chan xuat neu khong co du lieu.

- Cap nhat giao dien nut export:
  - Chi cho bam `Xuat Excel` / `Xuat PDF` khi danh sach dang co du lieu.
  - Nut `Excel` va `PDF` co mau rieng de de nhan biet.

## Kiem tra

- `./mvnw.cmd -q -DskipTests compile` pass
- `./mvnw.cmd -q "-Dtest=AdminSubjectPageIntegrationTest#scoreCreatePageShouldLoadForAdmin+scoreCreatePostShouldRedirectBackToCreatePage+scoreDetailPageShouldRedirectWhenScoreGroupNotFound" test` pass
- `./mvnw.cmd -q test` pass

## Trang thai hien tai

- Da cap nhat cac file lien quan:
  - `src/main/java/com/quanly/webdiem/controller/teacher/TeacherScoreController.java`
  - `src/main/java/com/quanly/webdiem/model/dao/ScoreDAO.java`
  - `src/main/java/com/quanly/webdiem/model/service/teacher/TeacherScoreService.java`
  - `src/main/java/com/quanly/webdiem/model/service/teacher/TeacherScoreListExportService.java`
  - `src/main/webapp/WEB-INF/views/teacher/score.jsp`
  - `src/main/webapp/WEB-INF/views/teacher/score-create.jsp`
  - `src/main/webapp/WEB-INF/views/teacher/score-detail.jsp`
  - `src/main/webapp/WEB-INF/views/teacher/score-edit.jsp`
  - `src/main/resources/static/css/teacher/score/score-list.css`
  - `src/main/resources/static/css/teacher/score/score-edit.css`

- Chua tao commit cho cum thay doi nay.
- Worktree van con nhieu file modified khac ngoai cum diem giao vien.

---

# Progress Log (2026-04-04 - lich su diem)

## Da hoan thanh

- Bo sung lich su thao tac cho module diem:
  - Ghi nhan `nhap diem`, `sua diem`, `xoa diem` vao `activity_logs`.
  - Ghi log tu luong dung chung cua admin va giao vien bo mon/GVCN.

- Them trang xem lich su diem rieng:
  - Route: `GET /admin/score/history`
  - Bo loc theo:
    - tu khoa
    - hanh dong (`THEM_DIEM`, `SUA_DIEM`, `XOA_DIEM`)
    - vai tro (`Admin`, `GVCN`, `GVBM`)
  - Co nut truy cap nhanh tu trang `Quan ly diem so`.

- Truy van lich su diem:
  - Phan loai vai tro nguoi thao tac.
  - Loc tren noi dung thao tac + ten dang nhap + giao vien + ma ban ghi.
  - Gioi han toi da 200 ban ghi gan nhat cho moi lan tai.

## File chinh da cap nhat

- `src/main/java/com/quanly/webdiem/model/dao/ActivityLogDAO.java`
- `src/main/java/com/quanly/webdiem/model/service/admin/ActivityLogService.java`
- `src/main/java/com/quanly/webdiem/model/service/admin/ScoreCreateService.java`
- `src/main/java/com/quanly/webdiem/model/service/admin/ScoreDeleteService.java`
- `src/main/java/com/quanly/webdiem/controller/admin/ScoreHistoryController.java`
- `src/main/java/com/quanly/webdiem/model/search/ScoreHistorySearch.java`
- `src/main/webapp/WEB-INF/views/admin/score-history.jsp`
- `src/main/webapp/WEB-INF/views/admin/score.jsp`
- `src/main/resources/static/css/admin/score-list.css`

## Kiem tra

- `mvn -q -DskipTests compile` pass.

## Trang thai

- Da luu snapshot vao `E:\\webdiem\\PROGRESS.md`.
