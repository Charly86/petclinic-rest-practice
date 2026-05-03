package org.springframework.samples.petclinic.service.query;

public class QueryValidationException extends RuntimeException {

    public QueryValidationException(String message) {
        super(message);
    }
}
