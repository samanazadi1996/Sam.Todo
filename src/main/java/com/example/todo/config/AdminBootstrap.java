package com.example.todo.config;

import com.example.todo.entity.AppUser;
import com.example.todo.entity.Role;
import com.example.todo.repository.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.EnumSet;

@Component
public class AdminBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final AppUserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final String adminUsername;
    private final String adminPassword;

    public AdminBootstrap(AppUserRepository repository, PasswordEncoder passwordEncoder,
                          @Value("${app.bootstrap-admin.username:}") String adminUsername,
                          @Value("${app.bootstrap-admin.password:}") String adminPassword) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!StringUtils.hasText(adminUsername) || !StringUtils.hasText(adminPassword)) {
            log.warn("حساب مدیر ساخته نشد؛ برای دسترسی به /api/users مقادیر app.bootstrap-admin.username و "
                    + "app.bootstrap-admin.password را تنظیم کنید");
            return;
        }

        if (repository.existsByUsername(adminUsername)) {
            log.info("کاربر '{}' از قبل وجود دارد؛ ساخت حساب مدیر رد شد", adminUsername);
            return;
        }

        AppUser admin = new AppUser();
        admin.setUsername(adminUsername);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRoles(EnumSet.of(Role.USER, Role.ADMIN));
        admin.setEnabled(Boolean.TRUE);
        repository.save(admin);

        log.info("حساب مدیر '{}' ساخته شد", adminUsername);
    }
}
