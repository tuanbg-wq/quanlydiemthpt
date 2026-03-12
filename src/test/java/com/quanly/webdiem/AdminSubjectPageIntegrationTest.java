package com.quanly.webdiem;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
}
