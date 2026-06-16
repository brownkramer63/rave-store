package com.cydeo.repository;

import com.cydeo.entity.VacationAvailability;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VacationAvailabilityRepository extends JpaRepository<VacationAvailability, Long> {
}
