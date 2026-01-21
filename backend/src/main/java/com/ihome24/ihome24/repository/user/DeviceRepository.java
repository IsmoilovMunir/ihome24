package com.ihome24.ihome24.repository.user;

import com.ihome24.ihome24.entity.user.Device;
import com.ihome24.ihome24.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
    Optional<Device> findByUserAndDeviceFingerprint(User user, String deviceFingerprint);
    boolean existsByUserAndDeviceFingerprint(User user, String deviceFingerprint);
}
