package de.nak.iaa.sundenbock.pageable;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Set;

/**
 * Factory for creating validated {@link Pageable} instances from request parameters.
 * <p>
 * Applies basic validation (page/size bounds) and a whitelist for sortable fields.
 * Also supports simple aliasing of sort fields and direction parsing (asc/desc).
 * </p>
 */
@Component
public class PageableFactory {

    /**
     * Builds a {@link Pageable} from paging/sorting inputs.
     * <p>
     * If no sort is provided, sorts by {@code createdDate} descending. Validates that the requested
     * sort field is in the provided whitelist (after alias resolution) and that the direction is
     * either {@code asc} or {@code desc}. Throws {@link ResponseStatusException} with 400 status on
     * invalid inputs.
     * </p>
     *
     * @param page          zero-based page index (must be >= 0)
     * @param size          page size (1..100)
     * @param sort          optional sort string in the form {@code <field>:<asc|desc>}
     * @param sortWhitelist set of allowed sort fields (after alias mapping)
     * @param sortAlias     mapping of client-facing field names to entity property names
     * @return a {@link Pageable} applying the requested paging and sorting
     * @throws ResponseStatusException if inputs are invalid
     */
    public Pageable createPageable(int page, int size, String sort,
                                   Set<String> sortWhitelist,
                                   Map<String, String> sortAlias) {

        if (page < 0 || size < 1 || size > 100)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid paging");

        if (!StringUtils.hasText(sort))
            return PageRequest.of(page, size, Sort.by("createdDate").descending());

        String[] parts = sort.split(":", 2);
        if (parts.length != 2)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid sort, expected <field>:<asc|desc>");

        String field = sortAlias.getOrDefault(parts[0], parts[0]);
        String dir = parts[1].toLowerCase();

        if (!sortWhitelist.contains(field) || !(dir.equals("asc") || dir.equals("desc")))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported sort");

        return PageRequest.of(
                page,
                size,
                dir.equals("asc") ? Sort.by(field).ascending() : Sort.by(field).descending()
        );
    }
}
