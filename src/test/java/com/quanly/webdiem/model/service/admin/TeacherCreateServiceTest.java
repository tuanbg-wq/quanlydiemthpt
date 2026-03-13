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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeacherCreateServiceTest {

    @Mock
    private TeacherDAO teacherDAO;

    @Mock
    private TeacherRoleDAO teacherRoleDAO;

    @Mock
    private SubjectDAO subjectDAO;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private TeacherCreateService teacherCreateService;

    @Test
    void suggestNextTeacherIdShouldUseLatestCreatedTeacherCode() {
        when(teacherDAO.findLatestCreatedTeacherCode()).thenReturn("GV009");

        String suggestedTeacherId = teacherCreateService.suggestNextTeacherId();

        assertEquals("GV010", suggestedTeacherId);
    }

    @Test
    void suggestNextTeacherIdShouldFallbackToMaxCodeWhenLatestCodeUnavailable() {
        when(teacherDAO.findLatestCreatedTeacherCode()).thenReturn(null);
        when(teacherDAO.findMaxTeacherCodeNumber()).thenReturn(17);

        String suggestedTeacherId = teacherCreateService.suggestNextTeacherId();

        assertEquals("GV018", suggestedTeacherId);
    }

    @Test
    void createTeacherShouldAssignPrimaryTeacherForSelectedSubject() {
        Subject subject = new Subject();
        subject.setIdMonHoc("MH001");
        subject.setTenMonHoc("Toan");

        TeacherCreateForm form = new TeacherCreateForm();
        form.setIdGiaoVien("GV001");
        form.setHoTen("Nguyen Van A");
        form.setSoDienThoai("0912345678");
        form.setEmail("gva@example.com");
        form.setDiaChi("HCM");
        form.setMonHocId("MH001");
        form.setTrinhDo("CU_NHAN");
        form.setTrangThai("dang_lam");
        form.setNamHoc("2025-2026");
        form.setVaiTroMa(List.of("GVCN"));

        when(subjectDAO.findById("MH001")).thenReturn(Optional.of(subject));
        when(subjectDAO.assignPrimaryTeacher("MH001", "GV001")).thenReturn(1);
        when(fileStorageService.saveTeacherAvatar(eq("GV001"), any())).thenReturn(null);
        when(teacherRoleDAO.findRoleTypesByCodes(List.of("GVCN")))
                .thenReturn(List.<Object[]>of(new Object[]{1, "GVCN", "Giáo viên chủ nhiệm"}));
        when(teacherRoleDAO.saveAll(any())).thenReturn(List.of(new TeacherRole()));

        teacherCreateService.createTeacher(form);

        ArgumentCaptor<Teacher> teacherCaptor = ArgumentCaptor.forClass(Teacher.class);
        verify(teacherDAO).save(teacherCaptor.capture());
        Teacher savedTeacher = teacherCaptor.getValue();
        assertNotNull(savedTeacher);
        assertEquals("GV001", savedTeacher.getIdGiaoVien());
        assertEquals("Toan", savedTeacher.getChuyenMon());

        verify(subjectDAO).assignPrimaryTeacher("MH001", "GV001");
    }
}
