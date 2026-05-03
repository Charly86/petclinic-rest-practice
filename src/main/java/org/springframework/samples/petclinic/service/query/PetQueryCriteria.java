package org.springframework.samples.petclinic.service.query;

import java.time.LocalDate;

public record PetQueryCriteria(String name, Integer typeId, Integer ownerId, LocalDate birthDateFrom,
                               LocalDate birthDateTo) {
}
