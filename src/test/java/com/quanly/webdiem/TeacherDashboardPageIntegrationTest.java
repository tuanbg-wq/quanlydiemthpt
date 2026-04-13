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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TeacherDashboardPageIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(authorities = "ROLE_Giao_vien")
    void teacherDashboardShouldRenderVietnameseTitleWithoutMojibake() throws Exception {
        mockMvc.perform(get("/teacher/dashboard"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("pageTitle", "Trang chủ giáo viên chủ nhiệm"))
                .andExpect(forwardedUrl("/WEB-INF/views/teacher/dashboard.jsp"));
    }
}
