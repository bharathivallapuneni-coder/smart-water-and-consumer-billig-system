package com.infosys.smartwater.repository;

import com.infosys.smartwater.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    Page<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
    Page<Notification> findByApartmentIdOrderByCreatedAtDesc(UUID apartmentId, Pageable pageable);
    List<Notification> findByUserIdAndIsReadFalse(UUID userId);
    long countByUserIdAndIsReadFalse(UUID userId);
}
