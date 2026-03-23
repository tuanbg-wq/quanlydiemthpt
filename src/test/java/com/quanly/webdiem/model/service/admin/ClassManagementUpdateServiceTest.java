package com.quanly.webdiem.model.service.admin;

import com.quanly.webdiem.model.dao.ClassDAO;
import com.quanly.webdiem.model.dao.CourseDAO;
import com.quanly.webdiem.model.dao.TeacherDAO;
import com.quanly.webdiem.model.entity.ClassEntity;
import com.quanly.webdiem.model.entity.Course;
import com.quanly.webdiem.model.form.ClassCreateForm;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClassManagementUpdateServiceTest {

    @Mock
    private ClassDAO classDAO;

    @Mock
    private CourseDAO courseDAO;

    @Mock
    private TeacherDAO teacherDAO;

    @InjectMocks
    private ClassManagementUpdateService classManagementUpdateService;

    @Test
    void updateClassShouldRenameClassCodeAndReassignReferences() {
        ClassEntity classEntity = createClassEntity();
        Course course = new Course();
        course.setIdKhoa("K07");

        ClassCreateForm form = new ClassCreateForm();
        form.setMaLop("K07A2");
        form.setTenLop("10A2");
        form.setKhoi("10");
        form.setIdKhoa("K07");
        form.setNamHoc("2025-2026");
        form.setIdGvcn("GV001");
        form.setGhiChu("Cap nhat ma lop");

        when(classDAO.findById("K07A1")).thenReturn(Optional.of(classEntity));
        when(courseDAO.findById("K07")).thenReturn(Optional.of(course));
        when(teacherDAO.countActiveByTeacherId("GV001")).thenReturn(1L);
        when(classDAO.countOtherHomeroomClassesByTeacherId("GV001", "K07A1")).thenReturn(0L);
        when(classDAO.countByClassIdIgnoreCase("K07A2")).thenReturn(0L);
        when(classDAO.renameClassId("K07A1", "K07A2")).thenReturn(1);

        classManagementUpdateService.updateClass("K07A1", form);

        verify(classDAO).save(classEntity);
        verify(classDAO).reassignClassIdInStudents("K07A1", "K07A2");
        verify(classDAO).reassignClassIdInTeachingAssignments("K07A1", "K07A2");
        verify(classDAO).reassignOldClassIdInStudentHistory("K07A1", "K07A2");
        verify(classDAO).reassignNewClassIdInStudentHistory("K07A1", "K07A2");
        verify(classDAO).renameClassId("K07A1", "K07A2");

        assertEquals("10A2", classEntity.getTenLop());
        assertEquals(10, classEntity.getKhoi());
        assertEquals("GV001", classEntity.getIdGvcn());
    }

    @Test
    void updateClassShouldRejectWhenClassCodeDoesNotMatchClassName() {
        ClassEntity classEntity = createClassEntity();

        ClassCreateForm form = new ClassCreateForm();
        form.setMaLop("K07A2");
        form.setTenLop("10A1");
        form.setKhoi("10");
        form.setIdKhoa("K07");
        form.setNamHoc("2025-2026");
        form.setIdGvcn("GV001");

        when(classDAO.findById("K07A1")).thenReturn(Optional.of(classEntity));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> classManagementUpdateService.updateClass("K07A1", form)
        );

        assertEquals("Ma lop khong khop voi ten lop.", exception.getMessage());
        verify(classDAO, never()).save(any());
    }

    private ClassEntity createClassEntity() {
        ClassEntity classEntity = new ClassEntity();
        classEntity.setIdLop("K07A1");
        classEntity.setTenLop("10A1");
        classEntity.setKhoi(10);
        classEntity.setNamHoc("2025-2026");
        classEntity.setIdGvcn("GV001");
        return classEntity;
    }
}
