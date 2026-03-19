package com.quanly.webdiem.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.regex.Pattern;

public class PasswordHasher implements PasswordEncoder {

    private static final Pattern BCRYPT_PATTERN = Pattern.compile("^\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}$");
    private final BCryptPasswordEncoder delegate = new BCryptPasswordEncoder(12);

    @Override
    public String encode(CharSequence rawPassword) {
        return delegate.encode(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (!isHashed(encodedPassword)) {
            return false;
        }
        return delegate.matches(rawPassword, encodedPassword);
    }

    public boolean isHashed(String password) {
        if (password == null || password.isBlank()) {
            return false;
        }
        return BCRYPT_PATTERN.matcher(password).matches();
    }
}
