package com.infosys.smartwater.controller;

import com.infosys.smartwater.dto.response.ApiResponse;
import com.infosys.smartwater.entity.HouseholdInvoice;
import com.infosys.smartwater.exception.ResourceNotFoundException;
import com.infosys.smartwater.repository.HouseholdInvoiceRepository;
import com.infosys.smartwater.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/api/v1/invoices", "/api/invoices"})
@RequiredArgsConstructor
@Tag(name = "Household Invoices", description = "Endpoints for viewing itemized bills and recording payments")
public class InvoiceController {

    private final HouseholdInvoiceRepository invoiceRepository;
    private final AuthService authService;

    @Data
    public static class PaymentConfirmDTO {
        private String paymentRef;
    }

    @GetMapping("/resident")
    @PreAuthorize("hasRole('RESIDENT')")
    @Operation(summary = "Get invoices for logged-in resident")
    public ResponseEntity<ApiResponse<List<HouseholdInvoice>>> getResidentInvoices() {
        UUID residentId = authService.getCurrentUser().getId();
        List<HouseholdInvoice> invoices = invoiceRepository.findByResidentIdOrderByGeneratedAtDesc(residentId);
        return ResponseEntity.ok(ApiResponse.success("Invoices retrieved successfully", invoices, HttpStatus.OK.value()));
    }

    @GetMapping("/household/{householdId}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'BUILDING_OWNER', 'ADMIN', 'RESIDENT')")
    @Operation(summary = "Get invoices for a household")
    public ResponseEntity<ApiResponse<List<HouseholdInvoice>>> getHouseholdInvoices(
            @PathVariable UUID householdId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        List<HouseholdInvoice> invoices = invoiceRepository
                .findByHouseholdIdOrderByGeneratedAtDesc(householdId, PageRequest.of(page, size))
                .getContent();
        return ResponseEntity.ok(ApiResponse.success("Household invoices retrieved successfully", invoices, HttpStatus.OK.value()));
    }

    @GetMapping("/cycle/{cycleId}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'BUILDING_OWNER', 'ADMIN')")
    @Operation(summary = "Get all household invoices generated for a billing cycle")
    public ResponseEntity<ApiResponse<List<HouseholdInvoice>>> getCycleInvoices(@PathVariable UUID cycleId) {
        List<HouseholdInvoice> invoices = invoiceRepository.findByBillingCycleId(cycleId);
        return ResponseEntity.ok(ApiResponse.success("Billing cycle invoices retrieved", invoices, HttpStatus.OK.value()));
    }

    @PostMapping("/{invoiceId}/pay")
    @Transactional
    @PreAuthorize("hasAnyRole('RESIDENT', 'BUILDING_OWNER', 'SUPERADMIN')")
    @Operation(summary = "Confirm invoice payment")
    public ResponseEntity<ApiResponse<HouseholdInvoice>> payInvoice(
            @PathVariable UUID invoiceId,
            @RequestBody PaymentConfirmDTO dto
    ) {
        HouseholdInvoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("HouseholdInvoice", "id", invoiceId));

        invoice.setStatus("PAID");
        invoice.setPaidAt(LocalDateTime.now());
        if (dto != null && dto.getPaymentRef() != null) {
            invoice.setPaymentId(dto.getPaymentRef());
        }

        HouseholdInvoice saved = invoiceRepository.save(invoice);
        return ResponseEntity.ok(ApiResponse.success("Payment recorded successfully", saved, HttpStatus.OK.value()));
    }
}
