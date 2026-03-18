package com.quanly.webdiem;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminSubjectPageIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(authorities = "ROLE_Admin")
    void subjectPageShouldLoadForAdmin() throws Exception {
        mockMvc.perform(get("/admin/subject"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "ROLE_Admin")
    void subjectCreatePageShouldLoadForAdmin() throws Exception {
        mockMvc.perform(get("/admin/subject/create"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "ROLE_Admin")
    void subjectInfoPageShouldRedirectWhenSubjectNotFound() throws Exception {
        mockMvc.perform(get("/admin/subject/MH999999/info"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(authorities = "ROLE_Admin")
    void subjectSuggestionEndpointsShouldLoadForAdmin() throws Exception {
        mockMvc.perform(get("/admin/subject/suggest/courses").param("q", "K06"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/admin/subject/suggest/school-years").param("q", "202"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/admin/subject/suggest/teachers").param("q", "Nguyen"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "ROLE_Admin")
    void teacherPageShouldLoadForAdmin() throws Exception {
        mockMvc.perform(get("/admin/teacher"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "ROLE_Admin")
    void teacherCreatePageShouldLoadForAdmin() throws Exception {
        mockMvc.perform(get("/admin/teacher/create"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "ROLE_Admin")
    void teacherEditPageShouldRedirectWhenTeacherNotFound() throws Exception {
        mockMvc.perform(get("/admin/teacher/GV999999/edit"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(authorities = "ROLE_Admin")
    void teacherInfoPageShouldRedirectWhenTeacherNotFound() throws Exception {
        mockMvc.perform(get("/admin/teacher/GV999999/info"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(authorities = "ROLE_Admin")
    void teacherInfoExcelExportShouldReturn404WhenTeacherNotFound() throws Exception {
        mockMvc.perform(get("/admin/teacher/GV999999/info/export/excel"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "ROLE_Admin")
    void teacherInfoPdfExportShouldReturn404WhenTeacherNotFound() throws Exception {
        mockMvc.perform(get("/admin/teacher/GV999999/info/export/pdf"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "ROLE_Admin")
    void teacherDeletePostShouldRedirectWhenTeacherNotFound() throws Exception {
        mockMvc.perform(post("/admin/teacher/GV999999/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/teacher"));
    }

    @Test
    @WithMockUser(authorities = "ROLE_Admin")
    void classPageShouldLoadForAdmin() throws Exception {
        mockMvc.perform(get("/admin/class"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "ROLE_Admin")
    void classCreatePageShouldLoadForAdmin() throws Exception {
        mockMvc.perform(get("/admin/class/create"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "ROLE_Admin")
    void classCourseCreatePageShouldLoadForAdmin() throws Exception {
        mockMvc.perform(get("/admin/class/course/create"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "ROLE_Admin")
    void classCourseEditPageShouldRedirectWhenCourseNotFound() throws Exception {
        mockMvc.perform(get("/admin/class/course/K999/edit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/class"));
    }

    @Test
    @WithMockUser(authorities = "ROLE_Admin")
    void classCourseDeletePostShouldRedirectBackToClassList() throws Exception {
        mockMvc.perform(post("/admin/class/course/K999/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/class"));
    }

    @Test
    @WithMockUser(authorities = "ROLE_Admin")
    void classTeacherSuggestionEndpointShouldLoadForAdmin() throws Exception {
        mockMvc.perform(get("/admin/class/suggest/homeroom-teachers").param("q", "Nguyen"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/admin/class/suggest/homeroom-teachers")
                        .param("q", "Nguyen")
                        .param("classId", "10A1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "ROLE_Admin")
    void classEditPageShouldRedirectWhenClassNotFound() throws Exception {
        mockMvc.perform(get("/admin/class/LOP999/edit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/class"));
    }

    @Test
    @WithMockUser(authorities = "ROLE_Admin")
    void classDeletePostShouldRedirectWhenClassNotFound() throws Exception {
        mockMvc.perform(post("/admin/class/LOP999/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/class"));
    }

    @Test
    @WithMockUser(authorities = "ROLE_Admin")
    void scorePageShouldLoadForAdmin() throws Exception {
        mockMvc.perform(get("/admin/score"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "ROLE_Admin")
    void scoreCreatePageShouldLoadForAdmin() throws Exception {
        mockMvc.perform(get("/admin/score/create"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "ROLE_Admin")
    void scoreCreatePostShouldRedirectBackToCreatePage() throws Exception {
        mockMvc.perform(post("/admin/score/create"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/admin/score/create**"));
    }

    @Test
    @WithMockUser(authorities = "ROLE_Admin")
    void scoreDetailPageShouldRedirectWhenScoreGroupNotFound() throws Exception {
        mockMvc.perform(get("/admin/score/detail")
                        .param("studentId", "HS999")
                        .param("subjectId", "MH999")
                        .param("namHoc", "2099-2100"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/score"));
    }

    @Test
    @WithMockUser(authorities = "ROLE_Admin")
    void scoreEditPageShouldRedirectWhenScoreGroupNotFound() throws Exception {
        mockMvc.perform(get("/admin/score/edit")
                        .param("studentId", "HS999")
                        .param("subjectId", "MH999")
                        .param("namHoc", "2099-2100"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/score"));
    }

    @Test
    @WithMockUser(authorities = "ROLE_Admin")
    void scoreEditPageShouldAcceptMonParamWithoutBadRequest() throws Exception {
        mockMvc.perform(get("/admin/score/edit")
                        .param("studentId", "HS999")
                        .param("mon", "MH999")
                        .param("namHoc", "2099-2100")
                        .param("hocKy", "0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/score"));
    }

    @Test
    @WithMockUser(authorities = "ROLE_Admin")
    void scoreDeletePostShouldRedirectBackToScoreList() throws Exception {
        mockMvc.perform(post("/admin/score/delete")
                        .param("studentId", "HS999")
                        .param("subjectId", "MH999")
                        .param("namHoc", "2099-2100"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/score"));
    }

    @Test
    @WithMockUser(authorities = "ROLE_Admin")
    void classInfoPageShouldRejectMalformedClassId() throws Exception {
        mockMvc.perform(get("/admin/class/%20/info"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(authorities = "ROLE_Admin")
    void studentDeletePostShouldRedirectBackToClassInfoWhenClassIdIsProvided() throws Exception {
        mockMvc.perform(post("/admin/student/HS999/delete")
                        .param("classId", "10A1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/class/10A1/info"));
    }
}
