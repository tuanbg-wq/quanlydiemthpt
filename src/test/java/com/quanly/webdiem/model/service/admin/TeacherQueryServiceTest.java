package com.quanly.webdiem.model.service.admin;

import com.quanly.webdiem.model.dao.TeacherDAO;
import com.quanly.webdiem.model.entity.TeacherSearch;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

    @Test
    void searchShouldPrioritizeGvcnRoleWhenTeacherHasHomeroomClass() {
        when(teacherDAO.searchForManagement(null, null, null, null))
                .thenReturn(List.<Object[]>of(new Object[]{
                        "GV001",
                        "Nguyen Van A",
                        "01/01/1990",
                        "Nam",
                        "0123456789",
                        "a@example.com",
                        "Toán",
                        "10A2",
                        "10A1, 10A2",
                        "Giáo viên bộ môn",
                        "dang_lam",
                        ""
                }));

        TeacherService.TeacherPageResult result = teacherQueryService.search(new TeacherSearch());
        String role = result.getItems().get(0).getVaiTro().toLowerCase(Locale.ROOT);

        assertTrue(role.contains("chủ nhiệm"));
    }
}
