package com.ihome24.ihome24.repository.user;

import com.ihome24.ihome24.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    
    @EntityGraph(attributePaths = {"role", "role.permissions"})
    @Query("SELECT u FROM User u WHERE u.username = :username")
    Optional<User> findByUsernameWithRoleAndPermissions(@Param("username") String username);

    @EntityGraph(attributePaths = {"role", "role.permissions"})
    @Query("SELECT u FROM User u WHERE u.phone = :phone")
    Optional<User> findByPhoneWithRoleAndPermissions(@Param("phone") String phone);
    
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE " +
           "(:q IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :q, '%'))) " +
           "AND (:role IS NULL OR u.role.name = :role) " +
           "AND (:plan IS NULL OR u.currentPlan = :plan) " +
           "AND (:status IS NULL OR u.status = :status)")
    Page<User> findUsersWithFilters(
            @Param("q") String searchQuery,
            @Param("role") String role,
            @Param("plan") String plan,
            @Param("status") User.UserStatus status,
            Pageable pageable
    );
}
