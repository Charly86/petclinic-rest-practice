package org.springframework.samples.petclinic.service.query;

import java.util.Collections;
import java.util.List;

public final class QueryPagination {

    private QueryPagination() {
    }

    public static <T> PagedResult<T> paginate(List<T> sorted, QueryPageRequest pageRequest) {
        int totalElements = sorted.size();
        int totalPages = pageRequest.size() == 0 ? 0 : (int) Math.ceil((double) totalElements / pageRequest.size());
        int fromIndex = pageRequest.page() * pageRequest.size();
        if (fromIndex >= totalElements) {
            return new PagedResult<>(Collections.emptyList(), pageRequest.page(), pageRequest.size(), totalElements, totalPages);
        }
        int toIndex = Math.min(fromIndex + pageRequest.size(), totalElements);
        return new PagedResult<>(sorted.subList(fromIndex, toIndex), pageRequest.page(), pageRequest.size(), totalElements, totalPages);
    }
}
