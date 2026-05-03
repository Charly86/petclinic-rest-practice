package org.springframework.samples.petclinic.service.query;

import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.repository.PetRepository;
import org.springframework.samples.petclinic.repository.PetTypeRepository;
import org.springframework.samples.petclinic.repository.SpecialtyRepository;
import org.springframework.samples.petclinic.repository.VetRepository;
import org.springframework.samples.petclinic.repository.VisitRepository;
import org.springframework.samples.petclinic.service.ClinicServiceImpl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdvancedQueryPerformanceTests {

    @Test
    void shouldKeepOwnersAdvancedQueryP95Under500msFor10kDataset() {
        OwnerRepository ownerRepository = mock(OwnerRepository.class);
        ClinicServiceImpl service = buildService(null, ownerRepository, null);
        when(ownerRepository.findAllForAdvancedQuery()).thenReturn(generateOwners(10_000));

        OwnerQueryCriteria criteria = new OwnerQueryCriteria("Last", null, "City", null);
        QueryPageRequest pageRequest = new QueryPageRequest(0, 50, new SortOption("lastName", SortDirection.DESC));

        long p95Millis = runP95Millis(() -> service.findOwners(criteria, pageRequest), 30);
        assertThat(p95Millis).isLessThanOrEqualTo(500);
    }

    @Test
    void shouldKeepPetsAdvancedQueryP95Under500msFor10kDataset() {
        PetRepository petRepository = mock(PetRepository.class);
        ClinicServiceImpl service = buildService(petRepository, null, null);
        when(petRepository.findAllForAdvancedQuery()).thenReturn(generatePets(10_000));

        PetQueryCriteria criteria = new PetQueryCriteria("pet-", 2, 15, LocalDate.of(2019, 1, 1), LocalDate.of(2025, 1, 1));
        QueryPageRequest pageRequest = new QueryPageRequest(1, 50, new SortOption("name", SortDirection.ASC));

        long p95Millis = runP95Millis(() -> service.findPets(criteria, pageRequest), 30);
        assertThat(p95Millis).isLessThanOrEqualTo(500);
    }

    @Test
    void shouldKeepVisitsAdvancedQueryP95Under500msFor10kDataset() {
        VisitRepository visitRepository = mock(VisitRepository.class);
        ClinicServiceImpl service = buildService(null, null, visitRepository);
        when(visitRepository.findAllForAdvancedQuery()).thenReturn(generateVisits(10_000));

        VisitQueryCriteria criteria = new VisitQueryCriteria(7, LocalDate.of(2024, 1, 1), LocalDate.of(2026, 12, 31), "desc");
        QueryPageRequest pageRequest = new QueryPageRequest(0, 50, new SortOption("date", SortDirection.DESC));

        long p95Millis = runP95Millis(() -> service.findVisits(criteria, pageRequest), 30);
        assertThat(p95Millis).isLessThanOrEqualTo(500);
    }

    private ClinicServiceImpl buildService(PetRepository petRepository, OwnerRepository ownerRepository, VisitRepository visitRepository) {
        PetRepository resolvedPetRepo = petRepository == null ? mock(PetRepository.class) : petRepository;
        OwnerRepository resolvedOwnerRepo = ownerRepository == null ? mock(OwnerRepository.class) : ownerRepository;
        VisitRepository resolvedVisitRepo = visitRepository == null ? mock(VisitRepository.class) : visitRepository;
        VetRepository vetRepository = mock(VetRepository.class);
        SpecialtyRepository specialtyRepository = mock(SpecialtyRepository.class);
        PetTypeRepository petTypeRepository = mock(PetTypeRepository.class);
        return new ClinicServiceImpl(
            resolvedPetRepo,
            vetRepository,
            resolvedOwnerRepo,
            resolvedVisitRepo,
            specialtyRepository,
            petTypeRepository
        );
    }

    private long runP95Millis(Runnable runnable, int runs) {
        List<Long> durations = new ArrayList<>();
        for (int i = 0; i < runs; i++) {
            long start = System.nanoTime();
            runnable.run();
            long elapsed = System.nanoTime() - start;
            durations.add(elapsed / 1_000_000);
        }
        durations.sort(Comparator.naturalOrder());
        int index = Math.min((int) Math.ceil(0.95 * runs) - 1, runs - 1);
        return durations.get(index);
    }

    private Collection<Owner> generateOwners(int size) {
        List<Owner> owners = new ArrayList<>(size);
        for (int i = 1; i <= size; i++) {
            Owner owner = new Owner();
            owner.setId(i);
            owner.setFirstName("First" + i);
            owner.setLastName("Last" + (i % 200));
            owner.setCity("City" + (i % 50));
            owner.setAddress("Address " + i);
            owner.setTelephone(String.format("%010d", i));
            owners.add(owner);
        }
        return owners;
    }

    private Collection<Pet> generatePets(int size) {
        List<Pet> pets = new ArrayList<>(size);
        for (int i = 1; i <= size; i++) {
            Owner owner = new Owner();
            owner.setId((i % 200) + 1);
            PetType type = new PetType();
            type.setId((i % 6) + 1);
            type.setName("type-" + type.getId());

            Pet pet = new Pet();
            pet.setId(i);
            pet.setName("pet-" + i);
            pet.setBirthDate(LocalDate.of(2018, 1, 1).plusDays(i % 2000));
            pet.setOwner(owner);
            pet.setType(type);
            pets.add(pet);
        }
        return pets;
    }

    private Collection<Visit> generateVisits(int size) {
        List<Visit> visits = new ArrayList<>(size);
        for (int i = 1; i <= size; i++) {
            Pet pet = new Pet();
            pet.setId((i % 30) + 1);

            Visit visit = new Visit();
            visit.setId(i);
            visit.setPet(pet);
            visit.setDate(LocalDate.of(2024, 1, 1).plusDays(i % 600));
            visit.setDescription("desc-" + i);
            visits.add(visit);
        }
        return visits;
    }
}
