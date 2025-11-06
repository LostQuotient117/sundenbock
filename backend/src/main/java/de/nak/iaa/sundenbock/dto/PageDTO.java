package de.nak.iaa.sundenbock.dto;

import java.util.List;

/**
 * Simple page wrapper used by REST controllers to return paged results.
 * <p>
 * Holds the current page items along with total count and paging metadata.
 * </p>
 *
 * @param <T> item type
 */
public record PageDTO<T>(List<T> items, long total, int page, int pageSize) {

    /**
     * Convenience factory method to create a {@link PageDTO}.
     */
    public static <T> PageDTO<T> of(List<T> items, long total, int page, int pageSize) {
        return new PageDTO<>(items, total, page, pageSize);
    }
}
