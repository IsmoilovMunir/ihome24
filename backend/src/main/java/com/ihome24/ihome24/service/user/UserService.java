package com.ihome24.ihome24.service.user;

import com.ihome24.ihome24.dto.request.user.UserRequest;
import com.ihome24.ihome24.dto.request.user.AdminCreateAdminUserRequest;
import com.ihome24.ihome24.dto.response.user.UserListResponse;
import com.ihome24.ihome24.dto.response.user.AdminCreateAdminUserResponse;
import com.ihome24.ihome24.dto.response.user.UserResponse;
import com.ihome24.ihome24.entity.user.Role;
import com.ihome24.ihome24.entity.user.User;
import com.ihome24.ihome24.repository.user.RoleRepository;
import com.ihome24.ihome24.repository.user.UserRepository;
import com.ihome24.ihome24.repository.user.UserLoginDeviceRepository;
import com.ihome24.ihome24.service.email.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;
import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserLoginDeviceRepository userLoginDeviceRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Transactional(readOnly = true)
    public UserListResponse getUsers(String q, String role, String plan, String status, 
                                     Integer page, Integer itemsPerPage, String sortBy, String orderBy) {
        Pageable pageable = createPageable(page, itemsPerPage, sortBy, orderBy);
        
        User.UserStatus userStatus = null;
        if (status != null && !status.isEmpty()) {
            try {
                userStatus = User.UserStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid status, ignore
            }
        }

        // In admin panel users list we don't want frontend customers (role: users).
        final String excludeRole = "users";
        final String roleFilter = (role != null && !role.isBlank() && !"users".equalsIgnoreCase(role)) ? role : null;
        Page<User> userPage = userRepository.findUsersWithFiltersExcludingRole(q, roleFilter, excludeRole, plan, userStatus, pageable);
        
        return UserListResponse.builder()
                .users(userPage.getContent().stream()
                        .map(this::mapToResponse)
                        .collect(Collectors.toList()))
                .totalUsers(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .page(userPage.getNumber() + 1)
                .build();
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return mapToResponse(user);
    }

    @Transactional
    public UserResponse createUser(UserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists: " + request.getEmail());
        }

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + request.getRoleId()));

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword() != null ? request.getPassword() : "password"))
                .email(request.getEmail())
                .fullName(request.getFullName())
                .avatar(request.getAvatar())
                .company(request.getCompany())
                .country(request.getCountry())
                .contact(request.getContact())
                .currentPlan(request.getCurrentPlan())
                .billing(request.getBilling() != null ? request.getBilling() : "Автоматическое списание")
                .role(role)
                .status(request.getStatus() != null ? User.UserStatus.valueOf(request.getStatus().toUpperCase()) : User.UserStatus.ACTIVE)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();

        user = userRepository.save(user);
        return mapToResponse(user);
    }

    @Transactional
    public UserResponse updateUser(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        // Check if username is being changed and if it's already taken
        if (!user.getUsername().equals(request.getUsername()) && 
            userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists: " + request.getUsername());
        }

        // Check if email is being changed and if it's already taken
        if (!user.getEmail().equals(request.getEmail()) && 
            userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists: " + request.getEmail());
        }

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + request.getRoleId()));

        user.setUsername(request.getUsername());
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            // Снимаем флаг обязательной смены пароля, если пароль был изменен
            user.setPasswordChangeRequired(false);
        }
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setAvatar(request.getAvatar());
        user.setCompany(request.getCompany());
        user.setCountry(request.getCountry());
        user.setContact(request.getContact());
        user.setCurrentPlan(request.getCurrentPlan());
        if (request.getBilling() != null) {
            user.setBilling(request.getBilling());
        }
        user.setRole(role);
        if (request.getStatus() != null) {
            user.setStatus(User.UserStatus.valueOf(request.getStatus().toUpperCase()));
        }

        user = userRepository.save(user);
        return mapToResponse(user);
    }

    @Transactional
    public void changeUserPassword(Long id, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        
        user.setPassword(passwordEncoder.encode(newPassword));
        // Снимаем флаг обязательной смены пароля
        user.setPasswordChangeRequired(false);
        
        userRepository.save(user);
    }

    @Transactional
    public UserResponse createAdminPanelUser(AdminCreateAdminUserRequest request) {
        String normalizedEmail = request.getEmail() != null ? request.getEmail().trim().toLowerCase() : null;
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new RuntimeException("Email already exists: " + normalizedEmail);
        }

        String username = normalizeOrGenerateUsername(request.getUsername(), normalizedEmail);
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists: " + username);
        }

        String roleName = request.getRoleName() != null ? request.getRoleName().trim().toLowerCase() : null;
        if ("users".equals(roleName)) {
            throw new RuntimeException("Role 'users' is not allowed for admin panel accounts");
        }

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));

        String tmp = request.getTemporaryPassword();
        if (tmp == null || tmp.isBlank())
            tmp = generateTemporaryPassword(10);

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(tmp))
                .email(normalizedEmail)
                .fullName(request.getFullName().trim())
                .role(role)
                .status(User.UserStatus.ACTIVE)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .passwordChangeRequired(true)
                .build();

        user = userRepository.save(user);
        return mapToResponse(user);
    }

    @Transactional
    public AdminCreateAdminUserResponse createAdminPanelUserWithTempPassword(AdminCreateAdminUserRequest request) {
        String generatedTempPassword = request.getTemporaryPassword();
        if (generatedTempPassword == null || generatedTempPassword.isBlank())
            generatedTempPassword = generateTemporaryPassword(10);

        AdminCreateAdminUserRequest req = new AdminCreateAdminUserRequest();
        req.setFullName(request.getFullName());
        req.setUsername(request.getUsername());
        req.setEmail(request.getEmail());
        req.setRoleName(request.getRoleName());
        req.setTemporaryPassword(generatedTempPassword);

        UserResponse created = createAdminPanelUser(req);
        boolean emailSent = false;
        String message = "User created. Temporary password must be delivered manually.";
        try {
            emailService.sendAdminUserCredentials(
                    created.getEmail(),
                    created.getFullName(),
                    created.getUsername(),
                    generatedTempPassword
            );
            emailSent = true;
            message = "User created. Credentials email sent successfully.";
        } catch (Exception e) {
            // User is already created; frontend should still show temporary password.
        }

        return AdminCreateAdminUserResponse.builder()
                .user(created)
                .temporaryPassword(generatedTempPassword)
                .emailSent(emailSent)
                .message(message)
                .build();
    }

    @Transactional
    public UserResponse changeUserRole(Long id, String roleName) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        String rn = roleName != null ? roleName.trim().toLowerCase() : null;
        if (rn == null || rn.isBlank()) {
            throw new RuntimeException("Role name is required");
        }
        if ("users".equals(rn)) {
            throw new RuntimeException("Role 'users' is reserved for customers");
        }

        Role role = roleRepository.findByName(rn)
                .orElseThrow(() -> new RuntimeException("Role not found: " + rn));
        user.setRole(role);
        user = userRepository.save(user);
        return mapToResponse(user);
    }

    private String generateTemporaryPassword(int length) {
        final String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private String normalizeOrGenerateUsername(String requestedUsername, String email) {
        if (requestedUsername != null && !requestedUsername.isBlank()) {
            return requestedUsername.trim().toLowerCase();
        }

        String base = (email != null ? email : "user").split("@")[0]
                .replaceAll("[^a-zA-Z0-9._-]", "")
                .toLowerCase();
        if (base.isBlank()) {
            base = "user";
        }

        String candidate = base;
        int suffix = 1;
        while (userRepository.existsByUsername(candidate)) {
            candidate = base + suffix;
            suffix++;
        }
        return candidate;
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        // Clean up dependent rows to satisfy FK constraints (user_login_devices.user_id -> users.id)
        userLoginDeviceRepository.deleteByUserId(id);
        userRepository.deleteById(id);
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatar(user.getAvatar())
                .company(user.getCompany())
                .country(user.getCountry())
                .phone(user.getPhone())
                .contact(user.getContact())
                .currentPlan(user.getCurrentPlan())
                .billing(user.getBilling())
                .status(user.getStatus() != null ? user.getStatus().name() : "ACTIVE")
                .role(user.getRole() != null ? user.getRole().getName() : null)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private Pageable createPageable(Integer page, Integer itemsPerPage, String sortBy, String orderBy) {
        int pageNumber = (page != null && page > 0) ? page - 1 : 0;
        int pageSize = (itemsPerPage != null && itemsPerPage > 0) ? itemsPerPage : 10;

        Sort sort = Sort.unsorted();
        if (sortBy != null && !sortBy.isEmpty()) {
            Sort.Direction direction = "desc".equalsIgnoreCase(orderBy) ? 
                    Sort.Direction.DESC : Sort.Direction.ASC;
            
            // Map frontend sortBy to entity field names
            String fieldName = mapSortField(sortBy);
            sort = Sort.by(direction, fieldName);
        }

        return PageRequest.of(pageNumber, pageSize, sort);
    }

    private String mapSortField(String sortBy) {
        return switch (sortBy.toLowerCase()) {
            case "user" -> "fullName";
            case "email" -> "email";
            case "role" -> "role.name";
            case "plan" -> "currentPlan";
            case "status" -> "status";
            case "billing" -> "billing";
            default -> "id";
        };
    }
}
