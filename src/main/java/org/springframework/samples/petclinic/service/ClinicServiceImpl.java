/*
 * Copyright 2002-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.service;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.samples.petclinic.model.*;
import org.springframework.samples.petclinic.repository.*;
import org.springframework.samples.petclinic.service.query.OwnerQueryCriteria;
import org.springframework.samples.petclinic.service.query.PagedResult;
import org.springframework.samples.petclinic.service.query.PetQueryCriteria;
import org.springframework.samples.petclinic.service.query.QueryPageRequest;
import org.springframework.samples.petclinic.service.query.QueryPagination;
import org.springframework.samples.petclinic.service.query.SortDirection;
import org.springframework.samples.petclinic.service.query.SortOption;
import org.springframework.samples.petclinic.service.query.VisitQueryCriteria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Mostly used as a facade for all Petclinic controllers
 * Also a placeholder for @Transactional and @Cacheable annotations
 *
 * @author Michael Isvy
 * @author Vitaliy Fedoriv
 */
@Service
public class ClinicServiceImpl implements ClinicService {

    private final PetRepository petRepository;
    private final VetRepository vetRepository;
    private final OwnerRepository ownerRepository;
    private final VisitRepository visitRepository;
    private final SpecialtyRepository specialtyRepository;
    private final PetTypeRepository petTypeRepository;

    public ClinicServiceImpl(
        PetRepository petRepository,
        VetRepository vetRepository,
        OwnerRepository ownerRepository,
        VisitRepository visitRepository,
        SpecialtyRepository specialtyRepository,
        PetTypeRepository petTypeRepository) {
        this.petRepository = petRepository;
        this.vetRepository = vetRepository;
        this.ownerRepository = ownerRepository;
        this.visitRepository = visitRepository;
        this.specialtyRepository = specialtyRepository;
        this.petTypeRepository = petTypeRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<Pet> findAllPets() throws DataAccessException {
        return petRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResult<Pet> findPets(PetQueryCriteria criteria, QueryPageRequest pageRequest) throws DataAccessException {
        List<Pet> filtered = this.petRepository.findAllForAdvancedQuery()
            .stream()
            .filter(matchesPetCriteria(criteria))
            .collect(Collectors.toCollection(ArrayList::new));

        filtered.sort(resolvePetComparator(pageRequest.sort()));
        return QueryPagination.paginate(filtered, pageRequest);
    }

    @Override
    @Transactional
    public void deletePet(Pet pet) throws DataAccessException {
        petRepository.delete(pet);
    }

    @Override
    @Transactional(readOnly = true)
    public Visit findVisitById(int visitId) throws DataAccessException {
        return findEntityById(() -> visitRepository.findById(visitId));
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<Visit> findAllVisits() throws DataAccessException {
        return visitRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResult<Visit> findVisits(VisitQueryCriteria criteria, QueryPageRequest pageRequest) throws DataAccessException {
        List<Visit> filtered = this.visitRepository.findAllForAdvancedQuery()
            .stream()
            .filter(matchesVisitCriteria(criteria))
            .collect(Collectors.toCollection(ArrayList::new));

        filtered.sort(resolveVisitComparator(pageRequest.sort()));
        return QueryPagination.paginate(filtered, pageRequest);
    }

    @Override
    @Transactional
    public void deleteVisit(Visit visit) throws DataAccessException {
        visitRepository.delete(visit);
    }

    @Override
    @Transactional(readOnly = true)
    public Vet findVetById(int id) throws DataAccessException {
        return findEntityById(() -> vetRepository.findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<Vet> findAllVets() throws DataAccessException {
        return vetRepository.findAll();
    }

    @Override
    @Transactional
    public void saveVet(Vet vet) throws DataAccessException {
        vetRepository.save(vet);
    }

    @Override
    @Transactional
    public void deleteVet(Vet vet) throws DataAccessException {
        vetRepository.delete(vet);
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<Owner> findAllOwners() throws DataAccessException {
        return ownerRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResult<Owner> findOwners(OwnerQueryCriteria criteria, QueryPageRequest pageRequest) throws DataAccessException {
        List<Owner> filtered = this.ownerRepository.findAllForAdvancedQuery()
            .stream()
            .filter(matchesOwnerCriteria(criteria))
            .collect(Collectors.toCollection(ArrayList::new));

        filtered.sort(resolveOwnerComparator(pageRequest.sort()));
        return QueryPagination.paginate(filtered, pageRequest);
    }

    @Override
    @Transactional
    public void deleteOwner(Owner owner) throws DataAccessException {
        ownerRepository.delete(owner);
    }

    @Override
    @Transactional(readOnly = true)
    public PetType findPetTypeById(int petTypeId) {
        return findEntityById(() -> petTypeRepository.findById(petTypeId));
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<PetType> findAllPetTypes() throws DataAccessException {
        return petTypeRepository.findAll();
    }

    @Override
    @Transactional
    public void savePetType(PetType petType) throws DataAccessException {
        petTypeRepository.save(petType);
    }

    @Override
    @Transactional
    public void deletePetType(PetType petType) throws DataAccessException {
        petTypeRepository.delete(petType);
    }

    @Override
    @Transactional(readOnly = true)
    public Specialty findSpecialtyById(int specialtyId) {
        return findEntityById(() -> specialtyRepository.findById(specialtyId));
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<Specialty> findAllSpecialties() throws DataAccessException {
        return specialtyRepository.findAll();
    }

    @Override
    @Transactional
    public void saveSpecialty(Specialty specialty) throws DataAccessException {
        specialtyRepository.save(specialty);
    }

    @Override
    @Transactional
    public void deleteSpecialty(Specialty specialty) throws DataAccessException {
        specialtyRepository.delete(specialty);
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<PetType> findPetTypes() throws DataAccessException {
        return petRepository.findPetTypes();
    }

    @Override
    @Transactional(readOnly = true)
    public Owner findOwnerById(int id) throws DataAccessException {
        return findEntityById(() -> ownerRepository.findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Pet findPetById(int id) throws DataAccessException {
        return findEntityById(() -> petRepository.findById(id));
    }

    @Override
    @Transactional
    public void savePet(Pet pet) throws DataAccessException {
        pet.setType(findPetTypeById(pet.getType().getId()));
        petRepository.save(pet);
    }

    @Override
    @Transactional
    public void saveVisit(Visit visit) throws DataAccessException {
        visitRepository.save(visit);

    }

    @Override
    @Transactional(readOnly = true)
    public Collection<Vet> findVets() throws DataAccessException {
        return vetRepository.findAll();
    }

    @Override
    @Transactional
    public void saveOwner(Owner owner) throws DataAccessException {
        ownerRepository.save(owner);

    }

    @Override
    @Transactional(readOnly = true)
    public Collection<Owner> findOwnerByLastName(String lastName) throws DataAccessException {
        return ownerRepository.findByLastName(lastName);
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<Visit> findVisitsByPetId(int petId) {
        return visitRepository.findByPetId(petId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Specialty> findSpecialtiesByNameIn(Set<String> names) {
        return findEntityById(() -> specialtyRepository.findSpecialtiesByNameIn(names));
    }

    private Predicate<Owner> matchesOwnerCriteria(OwnerQueryCriteria criteria) {
        return owner -> startsWithIgnoreCase(owner.getLastName(), criteria.lastName())
            && startsWithIgnoreCase(owner.getFirstName(), criteria.firstName())
            && startsWithIgnoreCase(owner.getCity(), criteria.city())
            && equalsOrNoFilter(owner.getTelephone(), criteria.telephone());
    }

    private Predicate<Pet> matchesPetCriteria(PetQueryCriteria criteria) {
        return pet -> containsIgnoreCase(pet.getName(), criteria.name())
            && equalsOrNoFilter(extractTypeId(pet), criteria.typeId())
            && equalsOrNoFilter(extractOwnerId(pet), criteria.ownerId())
            && isDateAfterOrEqual(pet.getBirthDate(), criteria.birthDateFrom())
            && isDateBeforeOrEqual(pet.getBirthDate(), criteria.birthDateTo());
    }

    private Predicate<Visit> matchesVisitCriteria(VisitQueryCriteria criteria) {
        return visit -> equalsOrNoFilter(extractPetId(visit), criteria.petId())
            && isDateAfterOrEqual(visit.getDate(), criteria.dateFrom())
            && isDateBeforeOrEqual(visit.getDate(), criteria.dateTo())
            && containsIgnoreCase(visit.getDescription(), criteria.descriptionContains());
    }

    private Comparator<Owner> resolveOwnerComparator(SortOption sort) {
        Comparator<Owner> primary = switch (sort.field()) {
            case "id" -> directionAwareComparator(Owner::getId, sort.direction());
            case "lastName" -> directionAwareComparator(owner -> lower(owner.getLastName()), sort.direction());
            case "firstName" -> directionAwareComparator(owner -> lower(owner.getFirstName()), sort.direction());
            case "city" -> directionAwareComparator(owner -> lower(owner.getCity()), sort.direction());
            default -> directionAwareComparator(Owner::getId, SortDirection.ASC);
        };
        return primary.thenComparing(Owner::getId, Comparator.nullsLast(Integer::compareTo));
    }

    private Comparator<Pet> resolvePetComparator(SortOption sort) {
        Comparator<Pet> primary = switch (sort.field()) {
            case "id" -> directionAwareComparator(Pet::getId, sort.direction());
            case "name" -> directionAwareComparator(pet -> lower(pet.getName()), sort.direction());
            case "birthDate" -> directionAwareComparator(Pet::getBirthDate, sort.direction());
            case "typeId" -> directionAwareComparator(this::extractTypeId, sort.direction());
            case "ownerId" -> directionAwareComparator(this::extractOwnerId, sort.direction());
            default -> directionAwareComparator(Pet::getId, SortDirection.ASC);
        };
        return primary.thenComparing(Pet::getId, Comparator.nullsLast(Integer::compareTo));
    }

    private Comparator<Visit> resolveVisitComparator(SortOption sort) {
        Comparator<Visit> primary = switch (sort.field()) {
            case "id" -> directionAwareComparator(Visit::getId, sort.direction());
            case "date" -> directionAwareComparator(Visit::getDate, sort.direction());
            case "petId" -> directionAwareComparator(this::extractPetId, sort.direction());
            default -> directionAwareComparator(Visit::getId, SortDirection.ASC);
        };
        return primary.thenComparing(Visit::getId, Comparator.nullsLast(Integer::compareTo));
    }

    private <T, U extends Comparable<? super U>> Comparator<T> directionAwareComparator(Function<T, U> extractor,
                                                                                          SortDirection direction) {
        Comparator<T> comparator = Comparator.comparing(extractor, Comparator.nullsLast(Comparator.naturalOrder()));
        return direction == SortDirection.DESC ? comparator.reversed() : comparator;
    }

    private Integer extractTypeId(Pet pet) {
        return pet.getType() == null ? null : pet.getType().getId();
    }

    private Integer extractOwnerId(Pet pet) {
        return pet.getOwner() == null ? null : pet.getOwner().getId();
    }

    private Integer extractPetId(Visit visit) {
        return visit.getPet() == null ? null : visit.getPet().getId();
    }

    private boolean isDateAfterOrEqual(LocalDate value, LocalDate from) {
        return from == null || (value != null && !value.isBefore(from));
    }

    private boolean isDateBeforeOrEqual(LocalDate value, LocalDate to) {
        return to == null || (value != null && !value.isAfter(to));
    }

    private boolean equalsOrNoFilter(Object value, Object filter) {
        return filter == null || Objects.equals(value, filter);
    }

    private boolean containsIgnoreCase(String value, String filter) {
        return filter == null
            || (value != null && lower(value).contains(lower(filter)));
    }

    private boolean startsWithIgnoreCase(String value, String filter) {
        return filter == null
            || (value != null && lower(value).startsWith(lower(filter)));
    }

    private String lower(String value) {
        return value == null ? null : value.toLowerCase(Locale.ROOT);
    }

    private <T> T findEntityById(Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (ObjectRetrievalFailureException | EmptyResultDataAccessException e) {
            // Just ignore not found exceptions for Jdbc/Jpa realization
            return null;
        }
    }

}
