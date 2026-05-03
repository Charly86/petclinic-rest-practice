package org.springframework.samples.petclinic.service.query;

import java.util.List;

public record PagedResult<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
}
