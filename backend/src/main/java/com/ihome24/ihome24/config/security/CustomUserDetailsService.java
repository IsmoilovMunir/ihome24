package com.ihome24.ihome24.config.security;

import com.ihome24.ihome24.entity.user.User;
import com.ihome24.ihome24.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Поддержка входа как по username, так и по email
        // Сначала пробуем найти по username
        User user = userRepository.findByUsernameWithRoleAndPermissions(username)
                .orElse(null);
        
        // Если не нашли по username, пробуем найти по email
        if (user == null) {
            user = userRepository.findByEmail(username)
                    .orElse(null);
            
            // Если нашли по email, загружаем с ролями и правами
            if (user != null) {
                user = userRepository.findByUsernameWithRoleAndPermissions(user.getUsername())
                        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
            }
        }
        
        if (user == null) {
            throw new UsernameNotFoundException("User not found with username/email: " + username);
        }

        // Принудительно инициализируем коллекции, чтобы избежать LazyInitializationException
        // Это гарантирует, что все данные загружены в рамках текущей транзакции
        if (user.getRole() != null && user.getRole().getPermissions() != null) {
            // Вызываем size() чтобы загрузить коллекцию permissions
            user.getRole().getPermissions().size();
        }

        // Создаем UserPrincipal с уже загруженными authorities
        // Это гарантирует, что authorities будут доступны даже после закрытия транзакции
        return UserPrincipal.create(user);
    }
}
