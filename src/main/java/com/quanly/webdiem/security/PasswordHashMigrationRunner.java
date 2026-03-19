package com.quanly.webdiem.security;

import com.quanly.webdiem.model.dao.UserDAO;
import com.quanly.webdiem.model.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PasswordHashMigrationRunner implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordHashMigrationRunner.class);

    private final UserDAO userDAO;
    private final PasswordHasher passwordHasher;

    public PasswordHashMigrationRunner(UserDAO userDAO, PasswordHasher passwordHasher) {
        this.userDAO = userDAO;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<User> users;
        try {
            users = userDAO.findAll();
        } catch (RuntimeException ex) {
            LOGGER.warn("Bo qua migration mat khau vi khong ket noi duoc CSDL: {}", ex.getMessage());
            return;
        }

        if (users.isEmpty()) {
            return;
        }

        List<User> usersNeedUpdate = new ArrayList<>();
        for (User user : users) {
            String currentPassword = user.getMatKhau();
            if (passwordHasher.isHashed(currentPassword) || currentPassword == null || currentPassword.isBlank()) {
                continue;
            }

            user.setMatKhau(passwordHasher.encode(currentPassword));
            usersNeedUpdate.add(user);
        }

        if (usersNeedUpdate.isEmpty()) {
            return;
        }

        try {
            userDAO.saveAll(usersNeedUpdate);
            LOGGER.info("Da nang cap {} tai khoan sang mat khau ma hoa mot chieu.", usersNeedUpdate.size());
        } catch (RuntimeException ex) {
            LOGGER.warn("Khong the luu migration mat khau: {}", ex.getMessage());
        }
    }
}
