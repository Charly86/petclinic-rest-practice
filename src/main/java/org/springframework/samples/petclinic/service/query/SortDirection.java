package org.springframework.samples.petclinic.service.query;

public enum SortDirection {
    ASC,
    DESC;

    public static SortDirection fromValue(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("Sort direction is required");
        }
        if ("asc".equalsIgnoreCase(raw)) {
            return ASC;
        }
        if ("desc".equalsIgnoreCase(raw)) {
            return DESC;
        }
        throw new IllegalArgumentException("Unsupported sort direction: " + raw);
    }
}
