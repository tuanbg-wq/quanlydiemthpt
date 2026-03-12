package com.quanly.webdiem;

import com.quanly.webdiem.model.dao.SubjectDAO;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminSubjectPageIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private SubjectDAO subjectDAO;

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

    @Test
    @WithMockUser(authorities = "ROLE_Admin")
    void teacherCreatePostShouldNotReturn500() throws Exception {
        String subjectId = subjectDAO.findAll().stream()
                .map(s -> s.getIdMonHoc())
                .findFirst()
                .orElse(null);
        Assumptions.assumeTrue(subjectId != null && !subjectId.isBlank());

        String unique = String.valueOf(System.currentTimeMillis() % 1_000_000);
        String teacherId = "GV" + unique;

        mockMvc.perform(post("/admin/teacher/create")
                        .param("idGiaoVien", teacherId)
                        .param("hoTen", "Nguyen Van Test")
                        .param("ngaySinh", LocalDate.now().minusYears(30).toString())
                        .param("gioiTinh", "Nam")
                        .param("soDienThoai", "0912345678")
                        .param("email", "teacher" + unique + "@example.com")
                        .param("diaChi", "HCM")
                        .param("monHocId", subjectId)
                        .param("trinhDo", "CU_NHAN")
                        .param("ngayBatDauCongTac", LocalDate.now().minusYears(5).toString())
                        .param("trangThai", "dang_lam")
                        .param("namHoc", "2025-2026")
                        .param("vaiTroMa", "GVCN"))
                .andExpect(status().is3xxRedirection());
    }
}
