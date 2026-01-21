package com.ihome24.ihome24.entity.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String email;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "avatar")
    private String avatar;

    @Column(name = "company")
    private String company;

    @Column(name = "country")
    private String country;

    @Column(name = "contact")
    private String contact;

    @Column(name = "current_plan")
    private String currentPlan;

    @Column(name = "billing")
    private String billing;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")
    private Role role;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "enabled")
    @Builder.Default
    private Boolean enabled = true;

    @Column(name = "account_non_expired")
    @Builder.Default
    private Boolean accountNonExpired = true;

    @Column(name = "account_non_locked")
    @Builder.Default
    private Boolean accountNonLocked = true;

    @Column(name = "credentials_non_expired")
    @Builder.Default
    private Boolean credentialsNonExpired = true;

    @Column(name = "password_change_required")
    @Builder.Default
    private Boolean passwordChangeRequired = false;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // UserDetails implementation
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (role == null) {
            return java.util.Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        }
        java.util.List<GrantedAuthority> authorities = new java.util.ArrayList<>();
        // Add role authority
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName().toUpperCase()));
        // Add permission authorities
        if (role.getPermissions() != null) {
            role.getPermissions().forEach(permission -> 
                authorities.add(new SimpleGrantedAuthority("PERMISSION_" + permission.getName().toUpperCase()))
            );
        }
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled && status == UserStatus.ACTIVE;
    }

    public enum UserStatus {
        ACTIVE, INACTIVE, PENDING
    }
}
