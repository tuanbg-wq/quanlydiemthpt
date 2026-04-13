package com.quanly.webdiem.model.service.teacher_subject;

import com.quanly.webdiem.model.form.TeacherProfileUpdateForm;
import com.quanly.webdiem.model.service.teacher.TeacherProfileService;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class TeacherSubjectProfileService {

    private final TeacherProfileService teacherProfileService;

    public TeacherSubjectProfileService(TeacherProfileService teacherProfileService) {
        this.teacherProfileService = teacherProfileService;
    }

    public TeacherProfileService.TeacherProfilePageData getProfilePageData(String username) {
        return teacherProfileService.getProfilePageData(username);
    }

    public Map<String, String> validateForUpdate(String username, TeacherProfileUpdateForm form) {
        return teacherProfileService.validateForUpdate(username, form);
    }

    public void updateProfile(String username, TeacherProfileUpdateForm form) {
        teacherProfileService.updateProfile(username, form);
    }
}

