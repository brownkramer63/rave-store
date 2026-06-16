package com.cydeo.controller;

import com.cydeo.entity.VacationAvailability;
import com.cydeo.entity.VacationCalendar;
import com.cydeo.repository.VacationAvailabilityRepository;
import com.cydeo.repository.VacationCalendarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/vacation-scheduling")
@RequiredArgsConstructor
public class VacationScheduleController {

    private final VacationCalendarRepository vacationCalendarRepository;
    private final VacationAvailabilityRepository vacationAvailabilityRepository;

    @GetMapping
    public String vacationScheduling(Model model) {
        ensureStarterCalendars();
        List<VacationCalendar> vacationCalendars = vacationCalendarRepository.findAllByIsDeletedFalseOrderByInsertDateTimeAsc();
        model.addAttribute("title", "Vacation Scheduling");
        model.addAttribute("vacationCalendars", vacationCalendars);
        model.addAttribute("vacationCalendarViews", buildVacationCalendarViews(vacationCalendars));
        model.addAttribute("newVacationCalendar", new VacationCalendar());
        model.addAttribute("newAvailability", new VacationAvailability());
        return "vacation-scheduling";
    }

    @PostMapping("/calendars")
    public String createCalendar(@ModelAttribute VacationCalendar vacationCalendar, RedirectAttributes redirectAttributes) {
        if (vacationCalendar.getName() == null || vacationCalendar.getName().trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vacation name is required.");
            return "redirect:/vacation-scheduling";
        }

        vacationCalendar.setName(vacationCalendar.getName().trim());
        vacationCalendarRepository.save(vacationCalendar);
        redirectAttributes.addFlashAttribute("successMessage", "Vacation calendar created.");
        return "redirect:/vacation-scheduling";
    }

    @PostMapping("/availability")
    public String addAvailability(@RequestParam Long vacationCalendarId,
                                  @RequestParam String personName,
                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate availableFrom,
                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate availableTo,
                                  RedirectAttributes redirectAttributes) {
        if (personName == null || personName.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Name is required before adding availability.");
            return "redirect:/vacation-scheduling";
        }

        if (availableTo.isBefore(availableFrom)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Available to date must be on or after available from date.");
            return "redirect:/vacation-scheduling";
        }

        VacationCalendar vacationCalendar = vacationCalendarRepository.findById(vacationCalendarId)
                .orElseThrow(() -> new IllegalArgumentException("Vacation calendar not found."));

        VacationAvailability availability = new VacationAvailability();
        availability.setVacationCalendar(vacationCalendar);
        availability.setPersonName(personName.trim());
        availability.setAvailableFrom(availableFrom);
        availability.setAvailableTo(availableTo);
        vacationAvailabilityRepository.save(availability);

        redirectAttributes.addFlashAttribute("successMessage", "Availability added.");
        return "redirect:/vacation-scheduling";
    }

    private List<VacationCalendarView> buildVacationCalendarViews(List<VacationCalendar> vacationCalendars) {
        List<VacationCalendarView> views = new ArrayList<>();
        for (VacationCalendar vacationCalendar : vacationCalendars) {
            views.add(new VacationCalendarView(vacationCalendar, buildCalendarDays(vacationCalendar)));
        }
        return views;
    }

    private List<LocalDate> buildCalendarDays(VacationCalendar vacationCalendar) {
        List<LocalDate> days = new ArrayList<>();
        if (vacationCalendar.getStartDate() == null || vacationCalendar.getEndDate() == null) {
            return days;
        }

        long daysBetween = ChronoUnit.DAYS.between(vacationCalendar.getStartDate(), vacationCalendar.getEndDate());
        for (int index = 0; index <= daysBetween; index++) {
            days.add(vacationCalendar.getStartDate().plusDays(index));
        }
        return days;
    }

    private void ensureStarterCalendars() {
        vacationCalendarRepository.findByNameIgnoreCaseAndIsDeletedFalse("Shark Fishing Trip")
                .orElseGet(() -> {
                    VacationCalendar calendar = new VacationCalendar();
                    calendar.setName("Shark Fishing Trip");
                    calendar.setStartDate(LocalDate.of(2026, 8, 20));
                    calendar.setEndDate(LocalDate.of(2026, 8, 23));
                    return vacationCalendarRepository.save(calendar);
                });

        vacationCalendarRepository.findByNameIgnoreCaseAndIsDeletedFalse("Beer Olympics")
                .orElseGet(() -> {
                    VacationCalendar calendar = new VacationCalendar();
                    calendar.setName("Beer Olympics");
                    return vacationCalendarRepository.save(calendar);
                });
    }

    public static class VacationCalendarView {
        private final VacationCalendar vacationCalendar;
        private final List<LocalDate> days;

        public VacationCalendarView(VacationCalendar vacationCalendar, List<LocalDate> days) {
            this.vacationCalendar = vacationCalendar;
            this.days = days;
        }

        public VacationCalendar getVacationCalendar() {
            return vacationCalendar;
        }

        public List<LocalDate> getDays() {
            return days;
        }
    }
}
