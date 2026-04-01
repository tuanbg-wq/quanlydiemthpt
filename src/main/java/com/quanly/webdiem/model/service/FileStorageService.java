package com.quanly.webdiem.model.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;

@Service
public class FileStorageService {

    private static final long MAX_AVATAR_SIZE_BYTES = 5L * 1024 * 1024;

    private final Path root;

    public FileStorageService(@Value("${app.upload.dir:${app.upload-dir:uploads}}") String uploadDir) {
        this.root = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    public String saveStudentAvatar(String studentId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        return saveAvatar("students", studentId, file);
    }

    public String saveTeacherAvatar(String teacherId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        return saveAvatar("teachers", teacherId, file);
    }

    private String saveAvatar(String subFolder, String id, MultipartFile file) {
        if (file.getSize() > MAX_AVATAR_SIZE_BYTES) {
            throw new RuntimeException("Ảnh quá kích thước cho phép (tối đa 5MB).");
        }

        String original = file.getOriginalFilename();
        String ext = getExtension(original);

        Set<String> allowed = Set.of("png", "jpg", "jpeg", "webp");
        if (ext != null && !allowed.contains(ext.toLowerCase())) {
            throw new RuntimeException("Ảnh chỉ hỗ trợ định dạng: png / jpg / jpeg / webp.");
        }

        try {
            Path dir = root.resolve(subFolder);
            Files.createDirectories(dir);

            String safeId = id == null ? "unknown" : id.trim();
            String filename = (ext == null) ? (safeId + ".png") : (safeId + "." + ext.toLowerCase());

            Path target = dir.resolve(filename).normalize();
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/" + subFolder + "/" + filename;
        } catch (Exception e) {
            throw new RuntimeException("Lưu ảnh thất bại: " + e.getMessage());
        }
    }

    private String getExtension(String name) {
        if (name == null) {
            return null;
        }

        int dot = name.lastIndexOf('.');
        if (dot < 0 || dot == name.length() - 1) {
            return null;
        }

        return name.substring(dot + 1);
    }
}
