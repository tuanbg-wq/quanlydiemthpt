package com.quanly.webdiem.model.service.admin;

import com.quanly.webdiem.model.dao.ClassDAO;
import com.quanly.webdiem.model.dao.CourseDAO;
import com.quanly.webdiem.model.dao.TeacherDAO;
import com.quanly.webdiem.model.entity.ClassCreateForm;
import com.quanly.webdiem.model.entity.ClassEntity;
import com.quanly.webdiem.model.entity.Course;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClassManagementCreateServiceTest {

    @Mock
    private ClassDAO classDAO;

    @Mock
    private CourseDAO courseDAO;

    @Mock
    private TeacherDAO teacherDAO;

    @InjectMocks
    private ClassManagementCreateService classManagementCreateService;

    @Test
    void createClassShouldRejectDuplicateHomeroomTeacherNameWhenTeacherIdMissing() {
        ClassCreateForm form = createValidForm();
        form.setIdGvcn(null);
        form.setGvcnDisplay("Nguyen Thi B");
        form.setGhiChu("Lop nang cao");

        when(teacherDAO.findAvailableHomeroomTeacherIdsByExactName("Nguyen Thi B"))
                .thenReturn(List.of("GV001", "GV008"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> classManagementCreateService.createClass(form)
        );

        assertEquals(
                "TĂªn giĂ¡o viĂªn chá»§ nhiá»‡m bá»‹ trĂ¹ng. Vui lĂ²ng chá»n Ä‘Ăºng giĂ¡o viĂªn tá»« danh sĂ¡ch gá»£i Ă½.",
                exception.getMessage()
        );
        verify(classDAO, never()).save(any());
    }

    @Test
    void createClassShouldResolveTeacherIdByExactNameWhenNameIsUnique() {
        ClassCreateForm form = createValidForm();
        form.setIdGvcn(null);
        form.setGvcnDisplay("Nguyen Thi B");
        form.setGhiChu("Lop nang cao");

        Course course = new Course();
        course.setIdKhoa("K07");

        when(teacherDAO.findAvailableHomeroomTeacherIdsByExactName("Nguyen Thi B"))
                .thenReturn(List.of("gv002"));
        when(courseDAO.findById("K07")).thenReturn(Optional.of(course));
        when(teacherDAO.countActiveByTeacherId("GV002")).thenReturn(1L);
        when(teacherDAO.countHomeroomClassReferences("GV002")).thenReturn(0L);

        classManagementCreateService.createClass(form);

        ArgumentCaptor<ClassEntity> classCaptor = ArgumentCaptor.forClass(ClassEntity.class);
        verify(classDAO).save(classCaptor.capture());

        ClassEntity savedClass = classCaptor.getValue();
        assertEquals("10A1", savedClass.getIdLop());
        assertEquals("10A1", savedClass.getTenLop());
        assertEquals("GV002", savedClass.getIdGvcn());
        assertEquals("Lop nang cao", savedClass.getGhiChu());
    }

    @Test
    void createClassShouldRejectTeacherWhoAlreadyHasHomeroomClass() {
        ClassCreateForm form = createValidForm();
        form.setIdGvcn("GV001");

        Course course = new Course();
        course.setIdKhoa("K07");

        when(courseDAO.findById("K07")).thenReturn(Optional.of(course));
        when(teacherDAO.countActiveByTeacherId("GV001")).thenReturn(1L);
        when(teacherDAO.countHomeroomClassReferences("GV001")).thenReturn(1L);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> classManagementCreateService.createClass(form)
        );

        assertEquals(
                "GiĂ¡o viĂªn nĂ y Ä‘Ă£ lĂ  chá»§ nhiá»‡m cá»§a lá»›p khĂ¡c.",
                exception.getMessage()
        );
        verify(classDAO, never()).save(any());
    }

    private ClassCreateForm createValidForm() {
        ClassCreateForm form = new ClassCreateForm();
        form.setTenLop("10A1");
        form.setKhoi("10");
        form.setIdKhoa("K07");
        form.setNamHoc("2025-2026");
        return form;
    }
}
