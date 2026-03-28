package com.quanly.webdiem.model.service.admin;

import java.util.List;

public class ConductRewardCreatePageData {

    private final ConductRewardCreateFilter filter;
    private final List<String> grades;
    private final List<ConductManagementService.FilterOption> classOptions;
    private final List<ConductManagementService.FilterOption> courseOptions;
    private final List<ConductStudentCandidate> studentCandidates;
    private final ConductStudentCandidate selectedStudent;

    public ConductRewardCreatePageData(ConductRewardCreateFilter filter,
                                       List<String> grades,
                                       List<ConductManagementService.FilterOption> classOptions,
                                       List<ConductManagementService.FilterOption> courseOptions,
                                       List<ConductStudentCandidate> studentCandidates,
                                       ConductStudentCandidate selectedStudent) {
        this.filter = filter;
        this.grades = grades;
        this.classOptions = classOptions;
        this.courseOptions = courseOptions;
        this.studentCandidates = studentCandidates;
        this.selectedStudent = selectedStudent;
    }

    public ConductRewardCreateFilter getFilter() {
        return filter;
    }

    public List<String> getGrades() {
        return grades;
    }

    public List<ConductManagementService.FilterOption> getClassOptions() {
        return classOptions;
    }

    public List<ConductManagementService.FilterOption> getCourseOptions() {
        return courseOptions;
    }

    public List<ConductStudentCandidate> getStudentCandidates() {
        return studentCandidates;
    }

    public ConductStudentCandidate getSelectedStudent() {
        return selectedStudent;
    }
}
