package com.quanly.webdiem;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TeacherSubjectScorePageIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "user", authorities = "ROLE_GVBM")
    void teacherSubjectScorePageShouldRenderNewView() throws Exception {
        mockMvc.perform(get("/teacher-subject/score"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("pageTitle"))
                .andExpect(forwardedUrl("/WEB-INF/views/teacher-subject/score.jsp"));
    }

    @Test
    @WithMockUser(username = "user", authorities = "ROLE_GVBM")
    void teacherSubjectScorePageShouldKeepAcademicLevelFilter() throws Exception {
        mockMvc.perform(get("/teacher-subject/score").param("hocLuc", "gioi"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("search", hasProperty("hocLuc", is("gioi"))))
                .andExpect(forwardedUrl("/WEB-INF/views/teacher-subject/score.jsp"));
    }

    @Test
    @WithMockUser(username = "user", authorities = "ROLE_GVBM")
    void teacherSubjectScoreCreatePageShouldUseTeacherSubjectView() throws Exception {
        mockMvc.perform(get("/teacher-subject/score/create"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/WEB-INF/views/teacher-subject/score-create.jsp"));
    }

    @Test
    @WithMockUser(username = "user", authorities = "ROLE_GVBM")
    void teacherSubjectScoreDetailFailureShouldRedirectBackToTeacherSubjectList() throws Exception {
        mockMvc.perform(get("/teacher-subject/score/detail")
                        .param("studentId", "HS999")
                        .param("subjectId", "MH999")
                        .param("namHoc", "2025-2026")
                        .param("hocKy", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/teacher-subject/score"));
    }

    @Test
    @WithMockUser(username = "user", authorities = "ROLE_GVBM")
    void teacherSubjectScoreEditFailureShouldRedirectBackToTeacherSubjectList() throws Exception {
        mockMvc.perform(get("/teacher-subject/score/edit")
                        .param("studentId", "HS999")
                        .param("subjectId", "MH999")
                        .param("namHoc", "2025-2026")
                        .param("hocKy", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/teacher-subject/score"));
    }

    @Test
    @WithMockUser(username = "user", authorities = "ROLE_GVBM")
    void teacherSubjectScoreDeleteFailureShouldRedirectBackToTeacherSubjectList() throws Exception {
        mockMvc.perform(post("/teacher-subject/score/delete")
                        .with(csrf())
                        .param("studentId", "HS999")
                        .param("subjectId", "MH999")
                        .param("namHoc", "2025-2026"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/teacher-subject/score"));
    }
}
