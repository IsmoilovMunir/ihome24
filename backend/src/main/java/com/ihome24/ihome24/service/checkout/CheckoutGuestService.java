package com.ihome24.ihome24.service.checkout;

import com.ihome24.ihome24.entity.user.Role;
import com.ihome24.ihome24.entity.user.User;
import com.ihome24.ihome24.repository.user.RoleRepository;
import com.ihome24.ihome24.repository.user.UserRepository;
import com.ihome24.ihome24.service.auth.PhoneAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class CheckoutGuestService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PhoneAuthService phoneAuthService;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public record UpsertResult(User user, boolean created) {}

    @Transactional
    public UpsertResult upsertGuestCustomer(String fullName, String email, String phone) {
        String normalizedPhone = phoneAuthService.normalizePhone(phone);
        if (normalizedPhone == null || normalizedPhone.length() != 11 || normalizedPhone.charAt(1) != '9') {
            throw new IllegalArgumentException("Введите корректный номер телефона РФ (+7 9XX …)");
        }

        String trimmedName = fullName != null ? fullName.trim() : "";
        if (trimmedName.length() < 2) {
            throw new IllegalArgumentException("Укажите ФИО");
        }

        String trimmedEmail = email != null ? email.trim() : "";
        if (!EMAIL_PATTERN.matcher(trimmedEmail).matches()) {
            throw new IllegalArgumentException("Введите корректный email");
        }

        Optional<User> existingOpt = userRepository.findByPhone(normalizedPhone);
        if (existingOpt.isPresent()) {
            User user = existingOpt.get();
            user.setFullName(trimmedName);
            if (!trimmedEmail.isEmpty()) {
                if (!trimmedEmail.equalsIgnoreCase(user.getEmail())
                        && userRepository.existsByEmail(trimmedEmail)
                        && !trimmedEmail.equals(user.getEmail())) {
                    throw new IllegalArgumentException("Эта почта уже привязана к другому аккаунту");
                }
                user.setEmail(trimmedEmail);
            }
            user = userRepository.save(user);
            return new UpsertResult(user, false);
        }

        if (userRepository.existsByEmail(trimmedEmail)) {
            throw new IllegalArgumentException("Эта почта уже привязана к другому аккаунту");
        }

        Role role = roleRepository.findByName("users")
                .orElseThrow(() -> new RuntimeException("Role 'users' not found"));

        User user = User.builder()
                .username("user_" + normalizedPhone)
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .email(trimmedEmail)
                .fullName(trimmedName)
                .phone(normalizedPhone)
                .currentPlan("basic")
                .billing("Автоматическое списание")
                .role(role)
                .status(User.UserStatus.ACTIVE)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .passwordChangeRequired(false)
                .build();

        user = userRepository.save(user);
        return new UpsertResult(user, true);
    }
}
