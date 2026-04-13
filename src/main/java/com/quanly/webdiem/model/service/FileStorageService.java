package com.quanly.webdiem.model.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;

@Service
public class FileStorageService {

    private static final long MAX_AVATAR_SIZE_BYTES = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("png", "jpg", "jpeg", "webp");
    private static final String UPLOAD_PREFIX = "/uploads/";

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

    public String moveStudentAvatar(String currentPath, String oldStudentId, String newStudentId) {
        return moveAvatar("students", currentPath, oldStudentId, newStudentId);
    }

    public String moveTeacherAvatar(String currentPath, String oldTeacherId, String newTeacherId) {
        return moveAvatar("teachers", currentPath, oldTeacherId, newTeacherId);
    }

    public void deleteStoredFile(String storedPath) {
        Path resolvedPath = resolveStoredPath(storedPath);
        if (resolvedPath == null) {
            return;
        }

        try {
            Files.deleteIfExists(resolvedPath);
        } catch (IOException ex) {
            throw new RuntimeException("Không thể xóa ảnh cũ: " + ex.getMessage(), ex);
        }
    }

    private String saveAvatar(String subFolder, String id, MultipartFile file) {
        if (file.getSize() > MAX_AVATAR_SIZE_BYTES) {
            throw new RuntimeException("Ảnh quá kích thước cho phép (tối đa 5MB).");
        }

        String original = file.getOriginalFilename();
        String originalExtension = getExtension(original);
        if (originalExtension != null && !ALLOWED_EXTENSIONS.contains(originalExtension.toLowerCase(Locale.ROOT))) {
            throw new RuntimeException("Ảnh chỉ hỗ trợ định dạng: png / jpg / jpeg / webp.");
        }

        try {
            byte[] content = file.getBytes();
            ImageFormat imageFormat = detectImageFormat(content);
            if (imageFormat == null) {
                throw new RuntimeException("Tệp tải lên không phải ảnh hợp lệ hoặc định dạng chưa được hỗ trợ.");
            }

            Path dir = root.resolve(subFolder).normalize();
            Files.createDirectories(dir);

            String safeId = sanitizeId(id);
            String filename = safeId + "." + imageFormat.extension();
            Path target = dir.resolve(filename).normalize();
            validatePathInsideRoot(target);
            deleteExistingVariants(dir, safeId, filename);
            Files.write(target, content);

            return buildPublicPath(subFolder, filename);
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException("Lưu ảnh thất bại: " + ex.getMessage(), ex);
        }
    }

    private String moveAvatar(String subFolder, String currentPath, String oldId, String newId) {
        String normalizedCurrentPath = normalizeStoredPath(currentPath);
        if (normalizedCurrentPath == null) {
            return null;
        }

        String normalizedOldId = sanitizeId(oldId);
        String normalizedNewId = sanitizeId(newId);
        if (normalizedOldId.equalsIgnoreCase(normalizedNewId)) {
            return normalizedCurrentPath;
        }

        Path source = resolveStoredPath(normalizedCurrentPath);
        if (source == null || !Files.exists(source)) {
            return normalizedCurrentPath;
        }

        String extension = getExtension(source.getFileName().toString());
        if (extension == null) {
            return normalizedCurrentPath;
        }

        try {
            Path targetDir = root.resolve(subFolder).normalize();
            Files.createDirectories(targetDir);

            String targetFilename = normalizedNewId + "." + extension.toLowerCase(Locale.ROOT);
            Path target = targetDir.resolve(targetFilename).normalize();
            validatePathInsideRoot(target);
            deleteExistingVariants(targetDir, normalizedNewId, targetFilename);
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
            return buildPublicPath(subFolder, targetFilename);
        } catch (IOException ex) {
            throw new RuntimeException("Không thể cập nhật ảnh theo mã mới: " + ex.getMessage(), ex);
        }
    }

    private void deleteExistingVariants(Path dir, String safeId, String keepFilename) throws IOException {
        if (!Files.exists(dir)) {
            return;
        }

        for (String extension : ALLOWED_EXTENSIONS) {
            String candidateName = safeId + "." + normalizeStoredExtension(extension);
            if (candidateName.equalsIgnoreCase(keepFilename)) {
                continue;
            }
            Files.deleteIfExists(dir.resolve(candidateName));
        }
    }

    private void validatePathInsideRoot(Path path) {
        if (!path.normalize().startsWith(root)) {
            throw new RuntimeException("Đường dẫn lưu ảnh không hợp lệ.");
        }
    }

    private Path resolveStoredPath(String storedPath) {
        String normalized = normalizeStoredPath(storedPath);
        if (normalized == null) {
            return null;
        }

        String relative;
        if (normalized.startsWith(UPLOAD_PREFIX)) {
            relative = normalized.substring(UPLOAD_PREFIX.length());
        } else if (normalized.startsWith("uploads/")) {
            relative = normalized.substring("uploads/".length());
        } else {
            relative = normalized;
        }

        Path resolvedPath = root.resolve(relative).normalize();
        if (!resolvedPath.startsWith(root)) {
            throw new RuntimeException("Đường dẫn ảnh không hợp lệ.");
        }
        return resolvedPath;
    }

    private String normalizeStoredPath(String storedPath) {
        if (storedPath == null) {
            return null;
        }

        String normalized = storedPath.trim().replace('\\', '/');
        if (normalized.isEmpty()) {
            return null;
        }
        if (normalized.startsWith("./")) {
            normalized = normalized.substring(2);
        }
        return normalized;
    }

    private String sanitizeId(String id) {
        String normalized = id == null ? "" : id.trim();
        normalized = normalized.replaceAll("[^A-Za-z0-9_-]", "_");
        return normalized.isEmpty() ? "unknown" : normalized;
    }

    private String buildPublicPath(String subFolder, String filename) {
        return UPLOAD_PREFIX + subFolder + "/" + filename;
    }

    private String getExtension(String name) {
        if (name == null) {
            return null;
        }

        int dot = name.lastIndexOf('.');
        if (dot < 0 || dot == name.length() - 1) {
            return null;
        }

        return normalizeStoredExtension(name.substring(dot + 1));
    }

    private String normalizeStoredExtension(String extension) {
        if (extension == null) {
            return null;
        }
        String normalized = extension.trim().toLowerCase(Locale.ROOT);
        return "jpeg".equals(normalized) ? "jpg" : normalized;
    }

    private ImageFormat detectImageFormat(byte[] content) {
        if (content == null || content.length < 12) {
            return null;
        }
        if (isPng(content)) {
            return ImageFormat.PNG;
        }
        if (isJpeg(content)) {
            return ImageFormat.JPG;
        }
        if (isWebp(content)) {
            return ImageFormat.WEBP;
        }
        return null;
    }

    private boolean isPng(byte[] content) {
        byte[] signature = new byte[]{
                (byte) 0x89, 0x50, 0x4E, 0x47,
                0x0D, 0x0A, 0x1A, 0x0A
        };
        if (content.length < signature.length) {
            return false;
        }
        for (int i = 0; i < signature.length; i++) {
            if (content[i] != signature[i]) {
                return false;
            }
        }
        return true;
    }

    private boolean isJpeg(byte[] content) {
        return content.length >= 4
                && (content[0] & 0xFF) == 0xFF
                && (content[1] & 0xFF) == 0xD8
                && (content[content.length - 2] & 0xFF) == 0xFF
                && (content[content.length - 1] & 0xFF) == 0xD9;
    }

    private boolean isWebp(byte[] content) {
        return content.length >= 12
                && content[0] == 'R'
                && content[1] == 'I'
                && content[2] == 'F'
                && content[3] == 'F'
                && content[8] == 'W'
                && content[9] == 'E'
                && content[10] == 'B'
                && content[11] == 'P';
    }

    private enum ImageFormat {
        PNG("png"),
        JPG("jpg"),
        WEBP("webp");

        private final String extension;

        ImageFormat(String extension) {
            this.extension = extension;
        }

        public String extension() {
            return extension;
        }
    }
}
