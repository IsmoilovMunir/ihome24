package com.ihome24.ihome24.config;

import com.ihome24.ihome24.entity.user.Permission;
import com.ihome24.ihome24.entity.user.Role;
import com.ihome24.ihome24.entity.user.User;
import com.ihome24.ihome24.repository.user.PermissionRepository;
import com.ihome24.ihome24.repository.user.RoleRepository;
import com.ihome24.ihome24.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.email:ismoilov.munir97@gmail.com}")
    private String adminEmail;

    @Value("${app.admin.password:admin}")
    private String adminPassword;

    @Value("${app.admin.fullName:Администратор}")
    private String adminFullName;

    @Value("${app.admin.passwordChangeRequired:true}")
    private Boolean adminPasswordChangeRequired;

    @Override
    @Transactional
    public void run(String... args) {
        // Keep idempotent seeding: create missing permissions/roles even if tables are non-empty.
        if (permissionRepository.count() == 0) {
            initializePermissions();
        }

        // Always run roles seeding to ensure newly added roles (e.g. SEO specialist)
        // appear in already-initialized environments.
        initializeRoles();

        if (userRepository.count() == 0) {
            initializeUsers();
        }
    }

    private void initializePermissions() {
        log.info("Initializing permissions...");
        
        List<String> permissionNames = Arrays.asList(
            "Управление пользователями",
            "Управление спорами",
            "Управление API",
            "Отчетность",
            "Заработная плата",
            "Управление контентом",
            "Управление SEO",
            "Управление базой данных",
            "Управление репозиторием"
        );

        for (String name : permissionNames) {
            if (!permissionRepository.existsByName(name)) {
                Permission permission = Permission.builder()
                        .name(name)
                        .build();
                permissionRepository.save(permission);
            }
        }
        log.info("Permissions initialized");
    }

    private void initializeRoles() {
        log.info("Initializing roles...");

        // Administrator role
        Role adminRole = createRoleIfNotExists("admin", "Администратор", Arrays.asList(
            "Управление пользователями",
            "Управление спорами",
            "Управление API"
        ));

        // Author role (for frontend compatibility)
        createRoleIfNotExists("author", "Автор", Arrays.asList(
            "Управление контентом"
        ));

        // Editor role (for frontend compatibility)
        createRoleIfNotExists("editor", "Редактор", Arrays.asList(
            "Управление контентом",
            "Управление пользователями"
        ));

        // SEO specialist role (for admin content SEO fields)
        // SEO fields are handled under product editor UI, but permissions should be SEO-only.
        Role seoRole = createRoleIfNotExists("seo-specialist", "SEO специалист", Arrays.asList(
                "Управление SEO"
        ));

        // If the role already existed (e.g. created before adding "Управление SEO"),
        // make sure it doesn't keep the broader "Управление контентом" permission.
        Permission legacyContentPermission = permissionRepository.findByName("Управление контентом").orElse(null);
        if (legacyContentPermission != null && seoRole.getPermissions() != null) {
            seoRole.getPermissions().removeIf(p -> "Управление контентом".equals(p.getName()));
            roleRepository.save(seoRole);
        }

        // Maintainer role (for frontend compatibility)
        createRoleIfNotExists("maintainer", "Сопровождающий", Arrays.asList(
            "Управление репозиторием",
            "Управление базой данных"
        ));

        // Subscriber role (for frontend compatibility)
        createRoleIfNotExists("subscriber", "Подписчик", Arrays.asList(
            "Управление пользователями"
        ));

        // Manager role
        Role managerRole = createRoleIfNotExists("manager", "Менеджер", Arrays.asList(
            "Отчетность",
            "Заработная плата",
            "Управление пользователями"
        ));

        // Users role
        Role usersRole = createRoleIfNotExists("users", "Пользователи", Arrays.asList(
            "Управление пользователями",
            "Управление контентом",
            "Управление спорами",
            "Управление базой данных"
        ));

        // Support role
        Role supportRole = createRoleIfNotExists("support", "Поддержка", Arrays.asList(
            "Управление репозиторием",
            "Управление контентом",
            "Управление базой данных"
        ));

        // Restricted user role
        Role restrictedRole = createRoleIfNotExists("restricted-user", "Ограниченный пользователь", Arrays.asList(
            "Управление пользователями",
            "Управление контентом",
            "Управление спорами",
            "Управление базой данных"
        ));

        log.info("Roles initialized");
    }

    private Role createRoleIfNotExists(String name, String displayName, List<String> permissionNames) {
        Role role = roleRepository.findByName(name).orElse(null);
        if (role == null) {
            role = Role.builder()
                    .name(name)
                    .displayName(displayName)
                    .permissions(new ArrayList<>())
                    .build();
        }

        // Ensure role has all required permissions (idempotent update).
        for (String permissionName : permissionNames) {
            Permission permission = permissionRepository.findByName(permissionName)
                    .orElse(null);
            if (permission != null && role.getPermissions().stream().noneMatch(p -> permissionName.equals(p.getName()))) {
                role.getPermissions().add(permission);
            }
        }

        role = roleRepository.save(role);
        return role;
    }

    private void initializeUsers() {
        log.info("Checking for admin user...");
        
        // Проверяем, есть ли уже администратор
        Role adminRole = roleRepository.findByName("admin").orElse(null);
        if (adminRole == null) {
            log.warn("Admin role not found. Skipping admin user creation.");
            return;
        }

        // Проверяем, существует ли уже администратор
        boolean adminExists = userRepository.findByUsername(adminUsername).isPresent() ||
                             userRepository.findByEmail(adminEmail).isPresent() ||
                             userRepository.findAll().stream()
                                     .anyMatch(user -> user.getRole() != null && "admin".equals(user.getRole().getName()));

        if (!adminExists) {
            log.info("Creating default admin user...");
            
            User admin = User.builder()
                    .username(adminUsername)
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .fullName(adminFullName)
                    .role(adminRole)
                    .status(User.UserStatus.ACTIVE)
                    .enabled(true)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .passwordChangeRequired(adminPasswordChangeRequired)
                    .currentPlan("enterprise")
                    .billing("Автоматическое списание")
                    .build();

            userRepository.save(admin);
            
            log.info("✓ Admin user created successfully");
            log.info("  Username: {}", adminUsername);
            log.info("  Email: {}", adminEmail);
            if (adminPasswordChangeRequired) {
                log.info("  Password change required: true");
            }
        } else {
            log.info("Admin user already exists. Skipping creation.");
        }
    }
}
