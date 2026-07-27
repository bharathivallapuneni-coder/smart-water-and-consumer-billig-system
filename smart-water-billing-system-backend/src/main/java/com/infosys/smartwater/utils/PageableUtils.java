package com.infosys.smartwater.utils;

import lombok.experimental.UtilityClass;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Utility class for constructing Spring Data {@link Pageable} objects from
 * controller-layer pagination parameters.
 *
 * <p>Centralises the sort-direction parsing logic so that all service
 * implementations use a consistent Pageable construction pattern.
 *
 * <p>Usage:
 * <pre>
 *   Pageable pageable = PageableUtils.createPageable(page, size, "apartmentNumber", "asc");
 *   Page&lt;Apartment&gt; result = apartmentRepository.findAll(pageable);
 * </pre>
 */
@UtilityClass
public class PageableUtils {

    /**
     * Creates a {@link Pageable} with sorting based on the provided parameters.
     *
     * @param page    zero-based page number
     * @param size    number of elements per page (must be ≥ 1)
     * @param sortBy  entity field name to sort by (e.g., {@code "apartmentNumber"})
     * @param sortDir sort direction — {@code "desc"} for descending, anything else for ascending
     * @return a configured {@link Pageable}
     */
    public static Pageable createPageable(int page, int size, String sortBy, String sortDir) {
        Sort sort = "desc".equalsIgnoreCase(sortDir)
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        return PageRequest.of(page, size, sort);
    }

    /**
     * Creates an unsorted {@link Pageable} (natural database order).
     * Use when the caller does not require a specific sort order.
     *
     * @param page zero-based page number
     * @param size number of elements per page
     * @return an unsorted {@link Pageable}
     */
    public static Pageable createPageable(int page, int size) {
        return PageRequest.of(page, size);
    }

    /**
     * Creates a {@link Pageable} with a pre-built {@link Sort} descriptor.
     * Use for compound sorts (e.g., {@code Sort.by("year").descending().and(Sort.by("month").descending())}).
     *
     * @param page  zero-based page number
     * @param size  number of elements per page
     * @param sort  a pre-configured {@link Sort} descriptor
     * @return a configured {@link Pageable}
     */
    public static Pageable createPageable(int page, int size, Sort sort) {
        return PageRequest.of(page, size, sort);
    }
}
