package com.quanly.webdiem.model.service.admin.report;

import java.util.Arrays;

public enum AdminReportType {

    REWARD_DISCIPLINE("reward_discipline", "Khen thưởng / Kỷ luật", "Tổng hợp quyết định khen thưởng và kỷ luật học sinh", "award"),
    SCORE("score", "Báo cáo Điểm số", "Phân tích phổ điểm, kết quả học tập theo lớp và môn", "score"),
    TEACHER_PROFILE("teacher_profile", "Hồ sơ Giáo viên", "Thống kê hồ sơ công tác, trạng thái giảng dạy", "profile"),
    TEACHER_LIST("teacher_list", "Danh sách Giáo viên", "Danh sách và phân bố giáo viên theo bộ môn", "team");

    private final String code;
    private final String title;
    private final String description;
    private final String icon;

    AdminReportType(String code, String title, String description, String icon) {
        this.code = code;
        this.title = title;
        this.description = description;
        this.icon = icon;
    }

    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getIcon() {
        return icon;
    }

    public static AdminReportType fromCode(String code) {
        if (code == null || code.isBlank()) {
            return SCORE;
        }

        return Arrays.stream(values())
                .filter(item -> item.code.equalsIgnoreCase(code.trim()))
                .findFirst()
                .orElse(SCORE);
    }
}
