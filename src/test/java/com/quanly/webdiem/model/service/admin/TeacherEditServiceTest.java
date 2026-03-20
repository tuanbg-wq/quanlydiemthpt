package com.quanly.webdiem.model.service.admin;

import com.quanly.webdiem.model.dao.SubjectDAO;
import com.quanly.webdiem.model.dao.TeacherDAO;
import com.quanly.webdiem.model.dao.TeacherRoleDAO;
import com.quanly.webdiem.model.entity.Subject;
import com.quanly.webdiem.model.entity.Teacher;
import com.quanly.webdiem.model.entity.TeacherCreateForm;
import com.quanly.webdiem.model.entity.TeacherRole;
import com.quanly.webdiem.model.service.FileStorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeacherEditServiceTest {

    @Mock
    private TeacherDAO teacherDAO;

    @Mock
    private TeacherRoleDAO teacherRoleDAO;

    @Mock
    private SubjectDAO subjectDAO;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private TeacherEditService teacherEditService;

    @Test
    void updateTeacherShouldRenameTeacherIdAndRebindReferences() {
        Teacher teacher = new Teacher();
        teacher.setIdGiaoVien("GV001");
        teacher.setTrangThai("dang_lam");

        Subject subject = new Subject();
        subject.setIdMonHoc("MH001");
        subject.setTenMonHoc("Toan");

        TeacherCreateForm form = new TeacherCreateForm();
        form.setIdGiaoVien("GV010");
        form.setHoTen("Nguyen Van A");
        form.setMonHocId("MH001");
        form.setTrangThai("nghi_viec");

        when(teacherDAO.existsById("GV010")).thenReturn(false);
        when(subjectDAO.existsById("GV010")).thenReturn(false);
        when(teacherDAO.findById("GV001")).thenReturn(Optional.of(teacher));
        when(subjectDAO.findById("MH001")).thenReturn(Optional.of(subject));
        when(teacherDAO.createTemporaryTeacherForRename(eq("GV001"), anyString())).thenReturn(1);
        when(teacherDAO.renameTeacherId("GV001", "GV010")).thenReturn(1);
        when(teacherDAO.deleteByTeacherIdIgnoreCase(anyString())).thenReturn(1);
        when(subjectDAO.assignPrimaryTeacher("MH001", "GV010")).thenReturn(1);

        teacherEditService.updateTeacher("GV001", form);

        ArgumentCaptor<String> temporaryTeacherIdCaptor = ArgumentCaptor.forClass(String.class);

        verify(teacherDAO).save(teacher);
        verify(teacherDAO).updateTeacherStatusById("GV001", "dang_lam");
        verify(teacherDAO).createTemporaryTeacherForRename(eq("GV001"), temporaryTeacherIdCaptor.capture());

        String temporaryTeacherId = temporaryTeacherIdCaptor.getValue();
        assertNotNull(temporaryTeacherId);

        verify(teacherDAO).reassignTeacherIdInClasses("GV001", temporaryTeacherId);
        verify(teacherDAO).reassignTeacherIdInTeachingAssignments("GV001", temporaryTeacherId);
        verify(teacherDAO).reassignTeacherIdInSubjects("GV001", temporaryTeacherId);
        verify(teacherDAO).reassignTeacherIdInScores("GV001", temporaryTeacherId);
        verify(teacherDAO).reassignTeacherIdInConducts("GV001", temporaryTeacherId);
        verify(teacherDAO).reassignTeacherIdInTeacherRoles("GV001", temporaryTeacherId);

        verify(teacherDAO).renameTeacherId("GV001", "GV010");

        verify(teacherDAO).reassignTeacherIdInClasses(temporaryTeacherId, "GV010");
        verify(teacherDAO).reassignTeacherIdInTeachingAssignments(temporaryTeacherId, "GV010");
        verify(teacherDAO).reassignTeacherIdInSubjects(temporaryTeacherId, "GV010");
        verify(teacherDAO).reassignTeacherIdInScores(temporaryTeacherId, "GV010");
        verify(teacherDAO).reassignTeacherIdInConducts(temporaryTeacherId, "GV010");
        verify(teacherDAO).reassignTeacherIdInTeacherRoles(temporaryTeacherId, "GV010");
        verify(teacherDAO).deleteByTeacherIdIgnoreCase(temporaryTeacherId);
        verify(teacherDAO).updateTeacherStatusById("GV010", "nghi_viec");
        verify(subjectDAO).assignPrimaryTeacher("MH001", "GV010");

        verifyNoInteractions(teacherRoleDAO);
    }

    @Test
    void updateTeacherShouldUpsertRoleWhenStatusIsNotWorking() {
        Teacher teacher = new Teacher();
        teacher.setIdGiaoVien("GV001");

        Subject subject = new Subject();
        subject.setIdMonHoc("MH001");
        subject.setTenMonHoc("Toan");

        TeacherCreateForm form = new TeacherCreateForm();
        form.setIdGiaoVien("GV001");
        form.setHoTen("Nguyen Van A");
        form.setMonHocId("MH001");
        form.setTrangThai("nghi_viec");
        form.setNamHoc("2025-2026");
        form.setVaiTroMa(List.of("GVBM"));

        when(teacherDAO.findById("GV001")).thenReturn(Optional.of(teacher));
        when(subjectDAO.findById("MH001")).thenReturn(Optional.of(subject));
        when(subjectDAO.assignPrimaryTeacher("MH001", "GV001")).thenReturn(1);
        when(teacherRoleDAO.findRoleTypesByCodes(List.of("GVBM")))
                .thenReturn(List.<Object[]>of(new Object[]{2, "GVBM", "Giáo viên bộ môn"}));

        teacherEditService.updateTeacher("GV001", form);

        ArgumentCaptor<TeacherRole> roleCaptor = ArgumentCaptor.forClass(TeacherRole.class);
        verify(teacherRoleDAO).deleteByTeacherId("GV001");
        verify(teacherRoleDAO).save(roleCaptor.capture());

        TeacherRole savedRole = roleCaptor.getValue();
        assertEquals("GV001", savedRole.getIdGiaoVien());
        assertEquals("2025-2026", savedRole.getNamHoc());
        assertEquals(2, savedRole.getIdLoaiVaiTro());
        verify(teacherDAO, never()).deleteTeachingAssignmentsByTeacherSubjectAndYear(anyString(), anyString(), anyString());
    }

    @Test
    void getEditFormShouldKeepRoleYearWhenFallbackClassYearExists() {
        Teacher teacher = new Teacher();
        teacher.setIdGiaoVien("GV001");
        teacher.setHoTen("Nguyen Van A");
        teacher.setChuyenMon("Toan");
        teacher.setTrangThai("dang_lam");

        Subject subject = new Subject();
        subject.setIdMonHoc("MH001");
        subject.setTenMonHoc("Toan");

        TeacherRole latestRole = new TeacherRole();
        latestRole.setIdGiaoVien("GV001");
        latestRole.setNamHoc("2025-2026");
        latestRole.setIdLoaiVaiTro(1);

        when(teacherDAO.findById("GV001")).thenReturn(Optional.of(teacher));
        when(subjectDAO.findAll()).thenReturn(List.of(subject));
        when(teacherRoleDAO.findByIdGiaoVienOrderByNamHocDescIdDesc("GV001")).thenReturn(List.of(latestRole));
        when(teacherRoleDAO.findRoleTypesForCreateForm())
                .thenReturn(List.of(
                        new Object[]{1, "GVCN", "Giáo viên chủ nhiệm"},
                        new Object[]{2, "GVBM", "Giáo viên bộ môn"}
                ));
        when(teacherDAO.findAssignedClassIdsForTeacherSubjectAndYear("GV001", "MH001", "2025-2026"))
                .thenReturn(List.of());
        when(teacherDAO.findLatestSchoolYearByTeacherAndSubject("GV001", "MH001")).thenReturn("2024-2025");
        when(teacherDAO.findAssignedClassIdsForTeacherSubjectAndYear("GV001", "MH001", "2024-2025"))
                .thenReturn(List.of("10A1"));
        when(teacherDAO.findHomeroomClassIdByTeacherAndYear("GV001", "2025-2026")).thenReturn(null);
        when(teacherDAO.findLatestHomeroomSchoolYearByTeacher("GV001")).thenReturn(null);

        TeacherCreateForm form = teacherEditService.getEditForm("GV001");

        assertEquals("2025-2026", form.getNamHoc());
        assertEquals(List.of("GVCN"), form.getVaiTroMa());
        assertEquals("10A1", form.getLopBoMon());
    }

    @Test
    void updateTeacherShouldRejectWhenHomeroomClassAlreadyHasAnotherTeacher() {
        Teacher teacher = new Teacher();
        teacher.setIdGiaoVien("GV001");
        teacher.setTrangThai("dang_lam");

        Subject subject = new Subject();
        subject.setIdMonHoc("MH001");
        subject.setTenMonHoc("Toan");

        TeacherCreateForm form = new TeacherCreateForm();
        form.setIdGiaoVien("GV001");
        form.setHoTen("Nguyen Van A");
        form.setMonHocId("MH001");
        form.setTrangThai("dang_lam");
        form.setNamHoc("2025-2026");
        form.setVaiTroMa(List.of("GVCN"));
        form.setLopBoMon("10A2");
        form.setLopChuNhiem("10A2");

        when(teacherDAO.findById("GV001")).thenReturn(Optional.of(teacher));
        when(subjectDAO.findById("MH001")).thenReturn(Optional.of(subject));
        when(subjectDAO.assignPrimaryTeacher("MH001", "GV001")).thenReturn(1);
        when(teacherRoleDAO.findRoleTypesByCodes(List.of("GVCN")))
                .thenReturn(List.<Object[]>of(new Object[]{1, "GVCN", "Giáo viên chủ nhiệm"}));
        when(teacherDAO.findHomeroomTeacherIdByClassId("10A2")).thenReturn("GV999");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> teacherEditService.updateTeacher("GV001", form));

        assertEquals("Lớp này đã có GVCN. Vui lòng chọn lớp khác.", ex.getMessage());
        verify(teacherDAO, never()).assignHomeroomTeacherToClass(anyString(), anyString());
    }
}
