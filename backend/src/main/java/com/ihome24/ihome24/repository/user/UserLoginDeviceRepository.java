package com.ihome24.ihome24.repository.user;

import com.ihome24.ihome24.entity.user.User;
import com.ihome24.ihome24.entity.user.UserLoginDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserLoginDeviceRepository extends JpaRepository<UserLoginDevice, Long> {

    List<UserLoginDevice> findTop10ByUserOrderByCreatedAtDesc(User user);
}

