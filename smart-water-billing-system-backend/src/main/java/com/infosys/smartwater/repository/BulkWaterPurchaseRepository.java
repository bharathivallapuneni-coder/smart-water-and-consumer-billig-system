package com.infosys.smartwater.repository;

import com.infosys.smartwater.entity.BulkWaterPurchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface BulkWaterPurchaseRepository extends JpaRepository<BulkWaterPurchase, UUID> {
    List<BulkWaterPurchase> findByApartmentIdOrderByPurchaseDateDesc(UUID apartmentId);
    List<BulkWaterPurchase> findByApartmentIdAndBillingCycleId(UUID apartmentId, UUID billingCycleId);

    @Query("SELECT COALESCE(SUM(b.purchasedVolumeKl), 0) FROM BulkWaterPurchase b WHERE b.apartment.id = :apartmentId AND b.billingCycle.id = :cycleId")
    BigDecimal sumVolumeByApartmentAndCycle(@Param("apartmentId") UUID apartmentId, @Param("cycleId") UUID cycleId);

    @Query("SELECT COALESCE(SUM(b.totalCost), 0) FROM BulkWaterPurchase b WHERE b.apartment.id = :apartmentId AND b.billingCycle.id = :cycleId")
    BigDecimal sumCostByApartmentAndCycle(@Param("apartmentId") UUID apartmentId, @Param("cycleId") UUID cycleId);
}
