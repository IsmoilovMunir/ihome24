package com.ihome24.ihome24.repository.auth;

import com.ihome24.ihome24.entity.auth.RefreshToken;
import com.ihome24.ihome24.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByUser(User user);
}

