package com.quanly.webdiem.model.service.admin;

import com.quanly.webdiem.model.entity.TeacherListItem;
import com.quanly.webdiem.model.entity.TeacherSearch;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeacherService {

    private final TeacherQueryService queryService;
    private final TeacherDeleteService deleteService;

    public TeacherService(TeacherQueryService queryService,
                          TeacherDeleteService deleteService) {
        this.queryService = queryService;
        this.deleteService = deleteService;
    }

    public TeacherPageResult search(TeacherSearch search) {
        return queryService.search(search);
    }

    public List<String> getSubjects() {
        return queryService.getSubjects();
    }

    public List<String> getGrades() {
        return queryService.getGrades();
    }

    public List<String> getStatuses() {
        return queryService.getStatuses();
    }

    public void deleteTeacher(String teacherId) {
        deleteService.deleteTeacher(teacherId);
    }

    public static class TeacherPageResult {
        private final List<TeacherListItem> items;
        private final int page;
        private final int totalPages;
        private final int totalItems;
        private final int fromRecord;
        private final int toRecord;

        public TeacherPageResult(List<TeacherListItem> items,
                                 int page,
                                 int totalPages,
                                 int totalItems,
                                 int fromRecord,
                                 int toRecord) {
            this.items = items;
            this.page = page;
            this.totalPages = totalPages;
            this.totalItems = totalItems;
            this.fromRecord = fromRecord;
            this.toRecord = toRecord;
        }

        public List<TeacherListItem> getItems() {
            return items;
        }

        public int getPage() {
            return page;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public int getTotalItems() {
            return totalItems;
        }

        public int getFromRecord() {
            return fromRecord;
        }

        public int getToRecord() {
            return toRecord;
        }
    }
}
