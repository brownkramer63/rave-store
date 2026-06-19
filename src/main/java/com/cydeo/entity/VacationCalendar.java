package com.cydeo.entity;

import com.cydeo.entity.common.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@Table(name = "vacation_calendars")
@Getter
@Setter
public class VacationCalendar extends BaseEntity {

    private String name;
    @Column(columnDefinition = "TEXT")
    private String description;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    @OneToMany(mappedBy = "vacationCalendar", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("availableFrom ASC, personName ASC")
    private List<VacationAvailability> availabilities = new ArrayList<>();
}
