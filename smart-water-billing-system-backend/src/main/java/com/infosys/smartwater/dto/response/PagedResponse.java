package com.infosys.smartwater.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Pagination wrapper DTO for paginated REST API responses.
 *
 * <p>Wraps Spring's {@link Page} into a stable, serialisable DTO
 * that does not expose internal Spring Data implementation details.
 *
 * <p>Example response body:
 * <pre>
 * {
 *   "content"       : [ ... ],
 *   "page"          : 0,
 *   "size"          : 10,
 *   "totalElements" : 47,
 *   "totalPages"    : 5,
 *   "first"         : true,
 *   "last"          : false,
 *   "hasNext"       : true,
 *   "hasPrevious"   : false
 * }
 * </pre>
 *
 * @param <T> the type of elements in the page content
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Paginated list response with navigation metadata")
public class PagedResponse<T> {

    @Schema(description = "List of items on the current page")
    private List<T> content;

    @Schema(description = "Zero-based current page number", example = "0")
    private int page;

    @Schema(description = "Number of items per page", example = "10")
    private int size;

    @Schema(description = "Total number of items across all pages", example = "47")
    private long totalElements;

    @Schema(description = "Total number of pages", example = "5")
    private int totalPages;

    @Schema(description = "Whether this is the first page", example = "true")
    private boolean first;

    @Schema(description = "Whether this is the last page", example = "false")
    private boolean last;

    @Schema(description = "Whether a next page exists", example = "true")
    private boolean hasNext;

    @Schema(description = "Whether a previous page exists", example = "false")
    private boolean hasPrevious;

    // -------------------------------------------------------------------------
    // Static factory method — convert from Spring Page
    // -------------------------------------------------------------------------

    /**
     * Creates a {@code PagedResponse} from a Spring Data {@link Page}.
     *
     * <p>Usage in a service or controller:
     * <pre>
     *   Page&lt;ApartmentResponse&gt; page = apartmentService.findAll(pageable);
     *   return PagedResponse.from(page);
     * </pre>
     *
     * @param page the Spring Data page result
     * @param <T>  the element type of the page
     * @return a {@code PagedResponse} wrapping the page's content and metadata
     */
    public static <T> PagedResponse<T> from(Page<T> page) {
        return PagedResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }
}
