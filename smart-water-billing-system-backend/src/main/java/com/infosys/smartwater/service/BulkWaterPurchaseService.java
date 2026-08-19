package com.infosys.smartwater.service;

import com.infosys.smartwater.entity.Apartment;
import com.infosys.smartwater.entity.BillingCycle;
import com.infosys.smartwater.entity.BulkWaterPurchase;
import com.infosys.smartwater.exception.InvalidOperationException;
import com.infosys.smartwater.exception.ResourceNotFoundException;
import com.infosys.smartwater.repository.ApartmentRepository;
import com.infosys.smartwater.repository.BillingCycleRepository;
import com.infosys.smartwater.repository.BulkWaterPurchaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BulkWaterPurchaseService {

    private final BulkWaterPurchaseRepository purchaseRepository;
    private final ApartmentRepository apartmentRepository;
    private final BillingCycleRepository billingCycleRepository;

    @Transactional
    public BulkWaterPurchase createPurchase(
            UUID apartmentId,
            UUID cycleId,
            String sourceType,
            String supplierName,
            LocalDate purchaseDate,
            BigDecimal volumeKl,
            BigDecimal totalCost,
            String notes
    ) {
        Apartment apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Apartment", "id", apartmentId));

        if (volumeKl == null || volumeKl.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOperationException("Purchased volume must be greater than zero");
        }
        if (totalCost == null || totalCost.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidOperationException("Total cost cannot be negative");
        }

        BigDecimal unitCost = totalCost.divide(volumeKl, 2, RoundingMode.HALF_UP);

        BillingCycle cycle = null;
        if (cycleId != null) {
            cycle = billingCycleRepository.findById(cycleId).orElse(null);
        }

        BulkWaterPurchase purchase = BulkWaterPurchase.builder()
                .apartment(apartment)
                .billingCycle(cycle)
                .sourceType(sourceType)
                .supplierName(supplierName)
                .purchaseDate(purchaseDate != null ? purchaseDate : LocalDate.now())
                .purchasedVolumeKl(volumeKl)
                .totalCost(totalCost)
                .unitCostPerKl(unitCost)
                .notes(notes)
                .build();

        return purchaseRepository.save(purchase);
    }

    public List<BulkWaterPurchase> getPurchasesByApartment(UUID apartmentId) {
        return purchaseRepository.findByApartmentIdOrderByPurchaseDateDesc(apartmentId);
    }

    @Transactional
    public BulkWaterPurchase updatePurchase(
            UUID purchaseId,
            String sourceType,
            String supplierName,
            LocalDate purchaseDate,
            BigDecimal volumeKl,
            BigDecimal totalCost,
            String notes
    ) {
        BulkWaterPurchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new ResourceNotFoundException("BulkWaterPurchase", "id", purchaseId));

        if (volumeKl != null && volumeKl.compareTo(BigDecimal.ZERO) > 0) {
            purchase.setPurchasedVolumeKl(volumeKl);
        }
        if (totalCost != null && totalCost.compareTo(BigDecimal.ZERO) >= 0) {
            purchase.setTotalCost(totalCost);
        }
        if (purchase.getPurchasedVolumeKl().compareTo(BigDecimal.ZERO) > 0) {
            purchase.setUnitCostPerKl(purchase.getTotalCost().divide(purchase.getPurchasedVolumeKl(), 2, RoundingMode.HALF_UP));
        }
        if (sourceType != null) purchase.setSourceType(sourceType);
        if (supplierName != null) purchase.setSupplierName(supplierName);
        if (purchaseDate != null) purchase.setPurchaseDate(purchaseDate);
        if (notes != null) purchase.setNotes(notes);

        return purchaseRepository.save(purchase);
    }

    @Transactional
    public void deletePurchase(UUID purchaseId) {
        BulkWaterPurchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new ResourceNotFoundException("BulkWaterPurchase", "id", purchaseId));
        purchaseRepository.delete(purchase);
    }

    public BigDecimal getTotalVolumeForCycle(UUID apartmentId, UUID cycleId) {
        return purchaseRepository.sumVolumeByApartmentAndCycle(apartmentId, cycleId);
    }

    public BigDecimal getTotalCostForCycle(UUID apartmentId, UUID cycleId) {
        return purchaseRepository.sumCostByApartmentAndCycle(apartmentId, cycleId);
    }
}
