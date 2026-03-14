package com.quanly.webdiem.model.service.admin;

import com.quanly.webdiem.model.dao.SubjectDAO;
import com.quanly.webdiem.model.dao.TeacherDAO;
import com.quanly.webdiem.model.dao.TeacherRoleDAO;
import com.quanly.webdiem.model.entity.Subject;
import com.quanly.webdiem.model.entity.Teacher;
import com.quanly.webdiem.model.entity.TeacherCreateForm;
import com.quanly.webdiem.model.service.FileStorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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

        verify(teacherDAO).renameTeacherId("GV001", "GV010");

        verify(teacherDAO).reassignTeacherIdInClasses(temporaryTeacherId, "GV010");
        verify(teacherDAO).reassignTeacherIdInTeachingAssignments(temporaryTeacherId, "GV010");
        verify(teacherDAO).reassignTeacherIdInSubjects(temporaryTeacherId, "GV010");
        verify(teacherDAO).reassignTeacherIdInScores(temporaryTeacherId, "GV010");
        verify(teacherDAO).reassignTeacherIdInConducts(temporaryTeacherId, "GV010");
        verify(teacherDAO).deleteByTeacherIdIgnoreCase(temporaryTeacherId);
        verify(teacherDAO).updateTeacherStatusById("GV010", "nghi_viec");
        verify(subjectDAO).assignPrimaryTeacher("MH001", "GV010");

        verifyNoInteractions(teacherRoleDAO);
    }
}
