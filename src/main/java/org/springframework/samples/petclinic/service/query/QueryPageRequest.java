package org.springframework.samples.petclinic.service.query;

public record QueryPageRequest(int page, int size, SortOption sort) {

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;

    public static QueryPageRequest defaults() {
        return new QueryPageRequest(DEFAULT_PAGE, DEFAULT_SIZE, SortOption.defaultByIdAsc());
    }
}
