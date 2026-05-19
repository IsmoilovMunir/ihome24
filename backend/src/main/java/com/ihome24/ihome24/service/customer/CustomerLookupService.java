package com.ihome24.ihome24.service.customer;

import com.ihome24.ihome24.entity.user.User;
import com.ihome24.ihome24.repository.user.UserRepository;
import com.ihome24.ihome24.service.auth.PhoneAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerLookupService {

    private static final String CUSTOMER_ROLE = "users";

    private final UserRepository userRepository;
    private final PhoneAuthService phoneAuthService;

    public Optional<Long> findStoreCustomerIdByEmailOrPhone(String email, String phone) {
        if (email != null && !email.isBlank()) {
            Optional<User> byEmail = userRepository.findByEmailIgnoreCaseWithRole(email.trim());
            if (byEmail.isPresent() && isStoreCustomer(byEmail.get())) {
                return Optional.of(byEmail.get().getId());
            }
        }
        if (phone != null && !phone.isBlank()) {
            String normalized = phoneAuthService.normalizePhone(phone);
            if (normalized != null) {
                Optional<User> byPhone = userRepository.findByPhoneWithRole(normalized);
                if (byPhone.isPresent() && isStoreCustomer(byPhone.get())) {
                    return Optional.of(byPhone.get().getId());
                }
            }
        }
        return Optional.empty();
    }

    public Optional<User> findStoreCustomerById(Long id) {
        if (id == null) return Optional.empty();
        return userRepository.findByIdWithRole(id).filter(this::isStoreCustomer);
    }

    private boolean isStoreCustomer(User user) {
        return user.getRole() != null
                && user.getRole().getName() != null
                && CUSTOMER_ROLE.equals(user.getRole().getName());
    }
}
