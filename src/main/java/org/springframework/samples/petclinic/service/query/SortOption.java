package org.springframework.samples.petclinic.service.query;

public record SortOption(String field, SortDirection direction) {

    public static SortOption defaultByIdAsc() {
        return new SortOption("id", SortDirection.ASC);
    }
}
