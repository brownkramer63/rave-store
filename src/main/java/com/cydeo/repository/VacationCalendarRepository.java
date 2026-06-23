package com.cydeo.repository;

import com.cydeo.entity.VacationCalendar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface VacationCalendarRepository extends JpaRepository<VacationCalendar, Long> {

    List<VacationCalendar> findAllByIsDeletedFalseOrderByInsertDateTimeAsc();

    Optional<VacationCalendar> findByNameIgnoreCaseAndIsDeletedFalse(String name);

    List<VacationCalendar> findAllByNameIgnoreCaseAndIsDeletedFalseOrderByInsertDateTimeAsc(String name);

    @Query("select distinct vacationCalendar from VacationCalendar vacationCalendar " +
            "left join fetch vacationCalendar.availabilities " +
            "where vacationCalendar.isDeleted = false " +
            "order by vacationCalendar.insertDateTime asc")
    List<VacationCalendar> findAllActiveWithAvailabilities();
}
