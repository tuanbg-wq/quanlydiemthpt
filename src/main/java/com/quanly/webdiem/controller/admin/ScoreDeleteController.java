package com.quanly.webdiem.controller.admin;

import com.quanly.webdiem.model.service.admin.ScoreManagementService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/score")
public class ScoreDeleteController {

    private final ScoreManagementService scoreManagementService;

    public ScoreDeleteController(ScoreManagementService scoreManagementService) {
        this.scoreManagementService = scoreManagementService;
    }

    @PostMapping("/delete")
    public String deleteScoreGroup(@RequestParam("studentId") String studentId,
                                   @RequestParam("subjectId") String subjectId,
                                   @RequestParam("namHoc") String namHoc,
                                   RedirectAttributes redirectAttributes) {
        try {
            scoreManagementService.deleteScoreGroup(studentId, subjectId, namHoc);
            redirectAttributes.addFlashAttribute("flashType", "success");
            redirectAttributes.addFlashAttribute("flashMessage", ScorePageSupport.FLASH_DELETE_SUCCESS);
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", ex.getMessage());
        }
        return "redirect:/admin/score";
    }
}
