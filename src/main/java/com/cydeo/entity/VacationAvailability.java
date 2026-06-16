package com.cydeo.entity;

import com.cydeo.entity.common.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.LocalDate;

@Entity
@NoArgsConstructor
@Table(name = "vacation_availabilities")
@Getter
@Setter
public class VacationAvailability extends BaseEntity {

    private String personName;
    private LocalDate availableFrom;
    private LocalDate availableTo;

    @ManyToOne(fetch = FetchType.LAZY)
    private VacationCalendar vacationCalendar;
}
