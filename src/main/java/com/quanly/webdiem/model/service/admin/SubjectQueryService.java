package com.quanly.webdiem.model.service.admin;

import com.quanly.webdiem.model.dao.SubjectDAO;
import com.quanly.webdiem.model.entity.SubjectSearch;
import com.quanly.webdiem.model.entity.SubjectSharedService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

@Service
public class SubjectQueryService {

    private static final int PAGE_SIZE = 6;

    private final SubjectDAO subjectDAO;
    private final SubjectSharedService sharedService;

    public SubjectQueryService(SubjectDAO subjectDAO, SubjectSharedService sharedService) {
        this.subjectDAO = subjectDAO;
        this.sharedService = sharedService;
    }

    public SubjectService.SubjectPageResult search(SubjectSearch search) {
        String q = sharedService.normalize(search == null ? null : search.getQ());
        Integer grade = sharedService.parseGrade(search == null ? null : search.getKhoi());
        String department = sharedService.normalize(search == null ? null : search.getToBoMon());

        List<Object[]> rows = subjectDAO.searchForManagement(q, grade, department);
        List<SubjectService.SubjectRow> mapped = rows.stream()
                .map(this::mapRow)
                .toList();

        int requestedPage = sharedService.normalizePage(search == null ? null : search.getPage());
        return paginate(mapped, requestedPage);
    }

    public List<Integer> getGrades() {
        return subjectDAO.findDistinctGrades();
    }

    public List<String> getDepartments() {
        return subjectDAO.findDistinctDepartments();
    }

    private SubjectService.SubjectPageResult paginate(List<SubjectService.SubjectRow> rows, int requestedPage) {
        int totalItems = rows.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / PAGE_SIZE));
        int page = Math.min(requestedPage, totalPages);
        int fromIndex = Math.max(0, (page - 1) * PAGE_SIZE);
        int toIndex = Math.min(totalItems, fromIndex + PAGE_SIZE);

        List<SubjectService.SubjectRow> items = totalItems == 0
                ? Collections.emptyList()
                : rows.subList(fromIndex, toIndex);

        int fromRecord = totalItems == 0 ? 0 : fromIndex + 1;
        int toRecord = totalItems == 0 ? 0 : toIndex;

        return new SubjectService.SubjectPageResult(
                items,
                page,
                totalPages,
                totalItems,
                fromRecord,
                toRecord
        );
    }

    private SubjectService.SubjectRow mapRow(Object[] row) {
        String idMonHoc = sharedService.asString(row, 0, "-");
        String tenMonHoc = sharedService.asString(row, 1, "-");
        String khoiCsv = sharedService.asString(row, 2, "");
        String hocKyCode = sharedService.defaultIfBlank(sharedService.toHocKyCode(sharedService.asString(row, 3, null)), "-");
        String toBoMon = sharedService.defaultIfBlank(sharedService.asString(row, 4, null), "-");
        String giaoVienPhuTrach = sharedService.normalize(sharedService.asString(row, 5, ""));
        String giaoVienPhanCongCsv = sharedService.asString(row, 6, "");
        String giaoVienCungMonCsv = sharedService.asString(row, 7, "");
        String namHoc = sharedService.defaultIfBlank(sharedService.asString(row, 8, null), "-");
        String moTa = sharedService.asString(row, 9, "");
        Map<String, String> metadata = sharedService.parseMetadata(moTa);

        if (khoiCsv.isBlank()) {
            khoiCsv = sharedService.defaultIfBlank(metadata.get("Khoi lop ap dung"), "");
        }
        List<String> khoiLop = sharedService.splitCsv(khoiCsv, ",");

        if ("-".equals(hocKyCode)) {
            hocKyCode = sharedService.defaultIfBlank(sharedService.toHocKyCode(metadata.get("Ky hoc ap dung")), "-");
        }

        if ("-".equals(toBoMon)) {
            toBoMon = sharedService.defaultIfBlank(metadata.get("To bo mon"), "-");
        }

        if ("-".equals(namHoc)) {
            namHoc = sharedService.defaultIfBlank(metadata.get("Nam hoc ap dung"), "-");
        }

        LinkedHashSet<String> teachers = new LinkedHashSet<>();
        for (String teacher : sharedService.splitCsv(giaoVienCungMonCsv, "\\|")) {
            teachers.add(teacher);
        }
        if (giaoVienPhuTrach != null) {
            teachers.add(giaoVienPhuTrach);
        }
        for (String teacher : sharedService.splitCsv(giaoVienPhanCongCsv, "\\|")) {
            teachers.add(teacher);
        }
        String teacherMeta = sharedService.normalize(metadata.get("Giao vien phu trach"));
        if (teacherMeta != null && !"-".equals(teacherMeta)) {
            teachers.add(teacherMeta);
        }

        String giaoVienChinh = teachers.isEmpty() ? "-" : String.join(", ", teachers);
        int soGiaoVienKhac = 0;

        return new SubjectService.SubjectRow(
                idMonHoc,
                tenMonHoc,
                khoiLop,
                namHoc,
                hocKyCode,
                toBoMon,
                giaoVienChinh,
                soGiaoVienKhac
        );
    }
}
