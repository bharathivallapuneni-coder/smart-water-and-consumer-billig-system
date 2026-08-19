package com.infosys.smartwater.repository;

import com.infosys.smartwater.entity.Notification;
import com.infosys.smartwater.entity.enums.AlertType;
import com.infosys.smartwater.entity.enums.Severity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    Page<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
    List<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId);
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(UUID userId);
    long countByUserIdAndIsReadFalse(UUID userId);
    Page<Notification> findByApartmentIdOrderByCreatedAtDesc(UUID apartmentId, Pageable pageable);
    List<Notification> findByUserIdAndSeverityOrderByCreatedAtDesc(UUID userId, Severity severity);

    boolean existsByHouseholdIdAndAlertTypeAndBillingCycleId(UUID householdId, AlertType alertType, UUID billingCycleId);
}
