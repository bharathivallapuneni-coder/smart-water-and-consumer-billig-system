package com.infosys.smartwater.service.impl;

import com.infosys.smartwater.dto.request.ApartmentRequest;
import com.infosys.smartwater.dto.response.ApartmentResponse;
import com.infosys.smartwater.dto.response.PagedResponse;
import com.infosys.smartwater.entity.Apartment;
import com.infosys.smartwater.exception.DuplicateResourceException;
import com.infosys.smartwater.exception.InvalidOperationException;
import com.infosys.smartwater.exception.ResourceNotFoundException;
import com.infosys.smartwater.mapper.ApartmentMapper;
import com.infosys.smartwater.repository.ApartmentRepository;
import com.infosys.smartwater.service.ApartmentService;
import com.infosys.smartwater.utils.PageableUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Implementation of {@link ApartmentService}.
 *
 * <p>All state-changing operations are transactional. Read operations use
 * {@code readOnly = true} for Hibernate session optimisation.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ApartmentServiceImpl implements ApartmentService {

    private final ApartmentRepository apartmentRepository;
    private final ApartmentMapper     apartmentMapper;

    // -------------------------------------------------------------------------
    // Create
    // -------------------------------------------------------------------------

    @Override
    public ApartmentResponse createApartment(ApartmentRequest request) {
        log.info("Creating apartment with number: '{}'", request.getApartmentNumber());

        if (apartmentRepository.existsByApartmentNumber(request.getApartmentNumber())) {
            throw new DuplicateResourceException("Apartment", "apartmentNumber", request.getApartmentNumber());
        }

        Apartment apartment = apartmentMapper.toEntity(request);
        Apartment saved = apartmentRepository.save(apartment);

        log.info("Apartment created — id={}, number='{}'", saved.getId(), saved.getApartmentNumber());
        return apartmentMapper.toResponse(saved);
    }

    // -------------------------------------------------------------------------
    // Read
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public ApartmentResponse getApartmentById(UUID id) {
        return apartmentMapper.toResponse(findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ApartmentResponse> getAllApartments(int page, int size, String sortBy, String sortDir) {
        Pageable pageable = PageableUtils.createPageable(page, size, sortBy, sortDir);
        return PagedResponse.from(
                apartmentRepository.findAll(pageable).map(apartmentMapper::toResponse)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ApartmentResponse> searchApartments(
            String keyword, int page, int size, String sortBy, String sortDir) {
        Pageable pageable = PageableUtils.createPageable(page, size, sortBy, sortDir);
        return PagedResponse.from(
                apartmentRepository.searchByKeyword(keyword, pageable).map(apartmentMapper::toResponse)
        );
    }

    // -------------------------------------------------------------------------
    // Update
    // -------------------------------------------------------------------------

    @Override
    public ApartmentResponse updateApartment(UUID id, ApartmentRequest request) {
        log.info("Updating apartment id={}", id);
        Apartment apartment = findById(id);

        if (apartmentRepository.existsByApartmentNumberAndIdNot(request.getApartmentNumber(), id)) {
            throw new DuplicateResourceException("Apartment", "apartmentNumber", request.getApartmentNumber());
        }

        apartmentMapper.updateEntityFromRequest(request, apartment);
        Apartment saved = apartmentRepository.save(apartment);

        log.info("Apartment updated — id={}, number='{}'", saved.getId(), saved.getApartmentNumber());
        return apartmentMapper.toResponse(saved);
    }

    // -------------------------------------------------------------------------
    // Delete
    // -------------------------------------------------------------------------

    @Override
    public void deleteApartment(UUID id) {
        log.info("Deleting apartment id={}", id);
        Apartment apartment = findById(id);

        if (!apartment.getHouseholds().isEmpty()) {
            throw new InvalidOperationException(
                    String.format("Cannot delete apartment '%s' — it still has %d associated household(s). " +
                                  "Deactivate or remove all households first.",
                            apartment.getApartmentNumber(), apartment.getHouseholds().size()));
        }

        apartmentRepository.delete(apartment);
        log.info("Apartment '{}' (id={}) deleted.", apartment.getApartmentNumber(), id);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private Apartment findById(UUID id) {
        return apartmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Apartment", "id", id));
    }
}
