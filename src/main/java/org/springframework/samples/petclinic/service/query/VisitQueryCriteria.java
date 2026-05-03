package org.springframework.samples.petclinic.service.query;

import java.time.LocalDate;

public record VisitQueryCriteria(Integer petId, LocalDate dateFrom, LocalDate dateTo, String descriptionContains) {
}
