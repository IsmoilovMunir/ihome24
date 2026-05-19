package com.ihome24.ihome24.repository.product;

import com.ihome24.ihome24.entity.product.ProductSlugRedirect;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductSlugRedirectRepository extends JpaRepository<ProductSlugRedirect, Long> {

    Optional<ProductSlugRedirect> findByOldSlug(String oldSlug);

    void deleteByOldSlug(String oldSlug);

    void deleteByNewSlug(String newSlug);

    /**
     * Схлопывание цепочек: все old1→old2 становятся old1→new при смене old2→new.
     */
    @Modifying
    @Query("UPDATE ProductSlugRedirect r SET r.newSlug = :newSlug WHERE r.newSlug = :previousNewSlug")
    int updateNewSlugWhereNewSlugWas(@Param("previousNewSlug") String previousNewSlug,
                                     @Param("newSlug") String newSlug);
}
