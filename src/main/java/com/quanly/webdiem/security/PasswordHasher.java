package com.quanly.webdiem.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordHasher implements PasswordEncoder {

    private final BCryptPasswordEncoder delegate = new BCryptPasswordEncoder(12);

    @Override
    public String encode(CharSequence rawPassword) {
        return delegate.encode(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (encodedPassword == null || rawPassword == null) {
            return false;
        }

        if (!isHashed(encodedPassword)) {
            return encodedPassword.contentEquals(rawPassword);
        }

        try {
            return delegate.matches(rawPassword, encodedPassword);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    public boolean isHashed(String password) {
        if (password == null) {
            return false;
        }
        return password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$");
    }
}
