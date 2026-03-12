package com.quanly.webdiem.model.service.admin;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudentSearch {
    private String q;            // keyword
    private String courseId;     // id_khoa
    private String khoi;         // 10/11/12
    private String classId;      // id_lop
    private String historyType;  // CLASS_CHANGE
}