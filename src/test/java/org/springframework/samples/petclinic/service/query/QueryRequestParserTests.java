package org.springframework.samples.petclinic.service.query;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QueryRequestParserTests {

    @Test
    void shouldApplyDefaultsWhenPaginationAndSortAreMissing() {
        QueryPageRequest request = QueryRequestParser.parsePageRequest(null, null, null, Set.of("id"));
        assertThat(request.page()).isEqualTo(0);
        assertThat(request.size()).isEqualTo(20);
        assertThat(request.sort().field()).isEqualTo("id");
        assertThat(request.sort().direction()).isEqualTo(SortDirection.ASC);
    }

    @Test
    void shouldRejectMaliciousSortField() {
        assertThatThrownBy(() -> QueryRequestParser.parsePageRequest(
            0,
            20,
            "lastName;drop table owners,asc",
            Set.of("id", "lastName")))
            .isInstanceOf(QueryValidationException.class)
            .hasMessageContaining("Unsupported sort field");
    }

    @Test
    void shouldRejectInvalidSortDirection() {
        assertThatThrownBy(() -> QueryRequestParser.parsePageRequest(
            0,
            20,
            "id,descending",
            Set.of("id")))
            .isInstanceOf(QueryValidationException.class)
            .hasMessageContaining("Unsupported sort direction");
    }

    @Test
    void shouldRejectInvalidDateRange() {
        assertThatThrownBy(() -> QueryRequestParser.validateInclusiveRange(
            LocalDate.of(2026, 2, 10),
            LocalDate.of(2026, 1, 1),
            "dateFrom",
            "dateTo"))
            .isInstanceOf(QueryValidationException.class)
            .hasMessageContaining("Invalid range");
    }
}
