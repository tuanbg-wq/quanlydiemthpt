package com.quanly.webdiem;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TeacherSubjectDashboardPageIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "user", authorities = "ROLE_GVBM")
    void teacherSubjectRootShouldRedirectToDashboard() throws Exception {
        mockMvc.perform(get("/teacher-subject"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/teacher-subject/dashboard"));
    }

    @Test
    @WithMockUser(username = "user", authorities = "ROLE_GVBM")
    void teacherSubjectDashboardShouldRenderDashboardView() throws Exception {
        mockMvc.perform(get("/teacher-subject/dashboard"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("pageTitle", "Trang chủ giáo viên bộ môn"))
                .andExpect(forwardedUrl("/WEB-INF/views/teacher-subject/dashboard.jsp"));
    }
}
