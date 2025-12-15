package com.example.bookstore.sellers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SellerRepository extends JpaRepository<Seller, Long> {

    Optional<Seller> findByIdAndDeletedAtIsNull(Long id);

    Page<Seller> findAllByDeletedAtIsNull(Pageable pageable);

    Page<Seller> findAllByDeletedAtIsNullAndStatus(Pageable pageable, SellerStatus status);
}
