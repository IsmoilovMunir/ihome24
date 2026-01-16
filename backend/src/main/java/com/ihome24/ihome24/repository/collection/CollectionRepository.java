package com.ihome24.ihome24.repository.collection;

import com.ihome24.ihome24.entity.collection.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollectionRepository extends JpaRepository<Collection, Long> {
    List<Collection> findByIsActiveTrue();
    Optional<Collection> findBySlug(String slug);
}
