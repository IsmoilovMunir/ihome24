package com.ihome24.ihome24.repository.company;

import com.ihome24.ihome24.entity.company.CompanySettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanySettingsRepository extends JpaRepository<CompanySettings, Long> {
    
    Optional<CompanySettings> findFirstByIsActiveTrue();
    
    Optional<CompanySettings> findFirstByOrderByIdAsc();
}
