package com.infosys.smartwater.repository;

import com.infosys.smartwater.entity.HouseholdInvoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HouseholdInvoiceRepository extends JpaRepository<HouseholdInvoice, UUID> {
    Optional<HouseholdInvoice> findByHouseholdIdAndBillingCycleId(UUID householdId, UUID billingCycleId);
    List<HouseholdInvoice> findByBillingCycleId(UUID billingCycleId);
    Page<HouseholdInvoice> findByHouseholdIdOrderByGeneratedAtDesc(UUID householdId, Pageable pageable);
    Page<HouseholdInvoice> findByApartmentIdOrderByGeneratedAtDesc(UUID apartmentId, Pageable pageable);
    Optional<HouseholdInvoice> findByInvoiceNumber(String invoiceNumber);
    boolean existsByHouseholdIdAndBillingCycleId(UUID householdId, UUID billingCycleId);
    List<HouseholdInvoice> findByResidentIdOrderByGeneratedAtDesc(UUID residentId);
}
