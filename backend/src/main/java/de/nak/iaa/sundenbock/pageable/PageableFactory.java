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

@Component
public class PageableFactory {

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
