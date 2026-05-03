package org.springframework.samples.petclinic.service.query;

import java.time.LocalDate;
import java.util.Set;

public final class QueryRequestParser {

    private QueryRequestParser() {
    }

    public static QueryPageRequest parsePageRequest(Integer page, Integer size, String sort, Set<String> allowedSortFields) {
        int resolvedPage = page == null ? QueryPageRequest.DEFAULT_PAGE : page;
        int resolvedSize = size == null ? QueryPageRequest.DEFAULT_SIZE : size;
        if (resolvedPage < 0) {
            throw new QueryValidationException("Parameter 'page' must be >= 0");
        }
        if (resolvedSize <= 0) {
            throw new QueryValidationException("Parameter 'size' must be > 0");
        }
        if (resolvedSize > QueryPageRequest.MAX_SIZE) {
            throw new QueryValidationException("Parameter 'size' must be <= " + QueryPageRequest.MAX_SIZE);
        }

        SortOption resolvedSort = parseSort(sort, allowedSortFields);
        return new QueryPageRequest(resolvedPage, resolvedSize, resolvedSort);
    }

    public static void validateInclusiveRange(LocalDate from, LocalDate to, String fromName, String toName) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new QueryValidationException(
                "Invalid range: '" + fromName + "' must be <= '" + toName + "'");
        }
    }

    private static SortOption parseSort(String sort, Set<String> allowedSortFields) {
        if (sort == null || sort.isBlank()) {
            return SortOption.defaultByIdAsc();
        }

        String[] rawParts = sort.split(",", -1);
        if (rawParts.length != 2) {
            throw new QueryValidationException("Parameter 'sort' must use format 'field,direction'");
        }

        String field = rawParts[0].trim();
        String directionRaw = rawParts[1].trim();
        if (!allowedSortFields.contains(field)) {
            throw new QueryValidationException("Unsupported sort field: " + field);
        }

        try {
            SortDirection direction = SortDirection.fromValue(directionRaw);
            return new SortOption(field, direction);
        } catch (IllegalArgumentException ex) {
            throw new QueryValidationException("Unsupported sort direction: " + directionRaw);
        }
    }
}
