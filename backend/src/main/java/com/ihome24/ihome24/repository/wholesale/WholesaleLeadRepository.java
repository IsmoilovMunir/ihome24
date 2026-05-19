package com.ihome24.ihome24.repository.wholesale;

import com.ihome24.ihome24.entity.wholesale.WholesaleLead;
import com.ihome24.ihome24.entity.wholesale.WholesaleLeadStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WholesaleLeadRepository extends JpaRepository<WholesaleLead, Long> {

    List<WholesaleLead> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"assignedManager", "assignedManager.role"})
    @Query("SELECT l FROM WholesaleLead l WHERE " +
           "(:q IS NULL OR :q = '' OR LOWER(l.name) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "OR l.phone LIKE CONCAT('%', :q, '%') OR CAST(l.id AS string) LIKE CONCAT('%', :q, '%')) " +
           "AND (:status IS NULL OR l.status = :status)")
    Page<WholesaleLead> findWithFilters(
            @Param("q") String q,
            @Param("status") WholesaleLeadStatus status,
            Pageable pageable);

    @EntityGraph(attributePaths = {"assignedManager", "assignedManager.role"})
    @Query("SELECT l FROM WholesaleLead l WHERE l.id = :id")
    Optional<WholesaleLead> findByIdWithManager(@Param("id") Long id);

    long countByStatus(WholesaleLeadStatus status);
}
