package com.quanly.webdiem.model.service.admin;

import com.quanly.webdiem.model.dao.TeacherDAO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeacherQueryServiceTest {

    @Mock
    private TeacherDAO teacherDAO;

    @InjectMocks
    private TeacherQueryService teacherQueryService;

    @Test
    void getGradesShouldReturnDefaultWhenNoData() {
        when(teacherDAO.findDistinctGrades()).thenReturn(List.of());

        List<String> grades = teacherQueryService.getGrades();

        assertEquals(List.of("10", "11", "12"), grades);
    }

    @Test
    void getStatusesShouldReturnDefaultsWhenNoData() {
        when(teacherDAO.findDistinctStatuses()).thenReturn(List.of());

        List<String> statuses = teacherQueryService.getStatuses();

        assertEquals(List.of("dang_lam", "nghi_viec"), statuses);
    }
}
