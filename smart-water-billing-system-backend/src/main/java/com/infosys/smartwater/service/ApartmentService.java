package com.infosys.smartwater.service;

import com.infosys.smartwater.dto.request.ApartmentRequest;
import com.infosys.smartwater.dto.response.ApartmentResponse;
import com.infosys.smartwater.dto.response.PagedResponse;

import java.util.UUID;

/**
 * Service contract for {@code Apartment} management operations.
 *
 * <p>All methods enforce business rules before delegating to the repository.
 * Implementations must be transactional.
 */
public interface ApartmentService {

    /**
     * Creates a new apartment.
     *
     * @param request the apartment creation data
     * @return the created apartment as a response DTO
     * @throws com.infosys.smartwater.exception.DuplicateResourceException if {@code apartmentNumber} already exists
     */
    ApartmentResponse createApartment(ApartmentRequest request);

    /**
     * Retrieves an apartment by its UUID.
     *
     * @param id the apartment UUID
     * @return the apartment response DTO
     * @throws com.infosys.smartwater.exception.ResourceNotFoundException if not found
     */
    ApartmentResponse getApartmentById(UUID id);

    /**
     * Returns a paginated list of all apartments.
     *
     * @param page    zero-based page number
     * @param size    page size
     * @param sortBy  field to sort by (e.g., {@code "apartmentNumber"})
     * @param sortDir sort direction ({@code "asc"} or {@code "desc"})
     * @return paginated apartment list
     */
    PagedResponse<ApartmentResponse> getAllApartments(int page, int size, String sortBy, String sortDir);

    /**
     * Full-text search across apartment number, building name, and address.
     *
     * @param keyword search term
     * @param page    zero-based page number
     * @param size    page size
     * @param sortBy  sort field
     * @param sortDir sort direction
     * @return paginated search results
     */
    PagedResponse<ApartmentResponse> searchApartments(String keyword, int page, int size, String sortBy, String sortDir);

    /**
     * Updates an existing apartment.
     *
     * @param id      the apartment UUID to update
     * @param request the update data
     * @return the updated apartment response DTO
     * @throws com.infosys.smartwater.exception.ResourceNotFoundException  if not found
     * @throws com.infosys.smartwater.exception.DuplicateResourceException if new apartment number already taken
     */
    ApartmentResponse updateApartment(UUID id, ApartmentRequest request);

    /**
     * Hard-deletes an apartment.
     *
     * @param id the apartment UUID to delete
     * @throws com.infosys.smartwater.exception.ResourceNotFoundException if not found
     * @throws com.infosys.smartwater.exception.InvalidOperationException if the apartment still has associated households
     */
    void deleteApartment(UUID id);
}
