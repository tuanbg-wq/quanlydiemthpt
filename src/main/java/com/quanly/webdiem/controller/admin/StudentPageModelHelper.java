package com.quanly.webdiem.controller.admin;

import com.quanly.webdiem.model.dao.ClassDAO;
import com.quanly.webdiem.model.dao.CourseDAO;
import com.quanly.webdiem.model.entity.ClassEntity;
import com.quanly.webdiem.model.entity.Student;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import java.util.Comparator;
import java.util.List;

@Component
public class StudentPageModelHelper {

    private final ClassDAO classDAO;
    private final CourseDAO courseDAO;

    public StudentPageModelHelper(ClassDAO classDAO, CourseDAO courseDAO) {
        this.classDAO = classDAO;
        this.courseDAO = courseDAO;
    }

    public void applyBasePage(Model model, String pageTitle) {
        model.addAttribute("activePage", "student");
        model.addAttribute("pageTitle", pageTitle);
    }

    public void applyCreatePage(Model model) {
        if (!model.containsAttribute("student")) {
            model.addAttribute("student", new Student());
        }
        model.addAttribute("activePage", "student");
    }

    public void applyEditPage(Model model, Student student, String pageTitle) {
        applyBasePage(model, pageTitle);
        model.addAttribute("student", student);
        model.addAttribute("classes", classDAO.findAll().stream()
                .sorted(Comparator.comparing(ClassEntity::getIdLop, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList());
    }

    public void applyListFilters(Model model) {
        List<ClassEntity> classes = classDAO.findAll().stream()
                .sorted(Comparator.comparing(ClassEntity::getIdLop, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
        List<Integer> grades = classes.stream()
                .map(ClassEntity::getKhoi)
                .filter(k -> k != null)
                .distinct()
                .sorted(Comparator.naturalOrder())
                .toList();

        model.addAttribute("courses", courseDAO.findAll());
        model.addAttribute("classes", classes);
        model.addAttribute("grades", grades);
    }
}
