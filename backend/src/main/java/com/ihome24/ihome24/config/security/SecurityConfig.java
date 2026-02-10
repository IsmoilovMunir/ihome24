package com.ihome24.ihome24.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        HttpSessionCsrfTokenRepository csrfTokenRepository = new HttpSessionCsrfTokenRepository();
        csrfTokenRepository.setHeaderName("X-CSRF-TOKEN");
        
        http
                .csrf(csrf -> csrf
                        // Отключаем CSRF для всех API endpoints
                        .ignoringRequestMatchers("/api/**", "/h2-console/**")
                )
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/h2-console/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll() // Разрешаем доступ к actuator endpoints для мониторинга
                        .requestMatchers("/api/publicapi/**").permitAll() // Разрешаем публичные API endpoints (регистрация и т.д.)
                        .requestMatchers("/api/auth/**").permitAll() // Разрешаем аутентификацию
                        .requestMatchers("/api/apps/**").permitAll() // Orders, admin - permit for frontend checkout and admin
                        .requestMatchers("/api/files/**").permitAll() // Публичный доступ к файлам
                        .requestMatchers("/api/products", "/api/products/**").permitAll() // Публичный доступ к продуктам
                        .requestMatchers("/api/cart", "/api/cart/**").permitAll() // Валидация корзины
                        .requestMatchers("/api/categories", "/api/categories/**").permitAll() // Публичный доступ к категориям
                        .requestMatchers("/api/admin/categories", "/api/admin/categories/**").permitAll() // Временно разрешаем доступ к категориям для разработки
                        .requestMatchers("/api/admin/products", "/api/admin/products/**").permitAll() // Временно разрешаем доступ к продуктам для разработки
                        .requestMatchers("/api/admin/files/**").permitAll() // Временно разрешаем загрузку файлов для разработки
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        // Используем stateless сессии для REST API, но разрешаем сессии для аутентификации
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )
                .authenticationProvider(authenticationProvider());

        // Для H2 консоли (только для разработки)
        http.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Получаем разрешенные origins из переменных окружения или используем значения по умолчанию
        String allowedOriginsEnv = System.getenv("CORS_ALLOWED_ORIGINS");
        List<String> allowedOrigins;
        
        if (allowedOriginsEnv != null && !allowedOriginsEnv.isEmpty()) {
            // Разделяем по запятой и очищаем от пробелов
            allowedOrigins = Arrays.stream(allowedOriginsEnv.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
        } else {
            // Значения по умолчанию для разработки
            allowedOrigins = List.of(
                "http://localhost:5173", 
                "http://localhost:3000",
                "http://admin:5173",
                "http://frontend:3000"
            );
        }
        
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // Кэшировать preflight запросы на 1 час

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
