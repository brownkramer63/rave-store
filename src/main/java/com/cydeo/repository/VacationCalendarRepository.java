package com.cydeo.repository;

import com.cydeo.entity.VacationCalendar;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VacationCalendarRepository extends JpaRepository<VacationCalendar, Long> {

    List<VacationCalendar> findAllByIsDeletedFalseOrderByInsertDateTimeAsc();

    Optional<VacationCalendar> findByNameIgnoreCaseAndIsDeletedFalse(String name);
}
