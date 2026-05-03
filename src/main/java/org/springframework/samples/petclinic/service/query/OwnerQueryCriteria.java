package org.springframework.samples.petclinic.service.query;

public record OwnerQueryCriteria(String lastName, String firstName, String city, String telephone) {
}
