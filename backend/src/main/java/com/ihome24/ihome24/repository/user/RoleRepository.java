package com.ihome24.ihome24.repository.user;

import com.ihome24.ihome24.entity.user.Role;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
    
    // Make sure permissions collection is initialized to avoid LazyInitializationException
    @EntityGraph(attributePaths = {"permissions"})
    Optional<Role> findByNameWithPermissions(String name);

    boolean existsByName(String name);
}
