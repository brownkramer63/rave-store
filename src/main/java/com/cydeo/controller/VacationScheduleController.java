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
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/vacation-scheduling")
@RequiredArgsConstructor
public class VacationScheduleController {

    private static final String SHARK_FISHING_DESCRIPTION = "shark fishing at panama city beach Florida night of 08/21/2026. Planning on staying in Florida until Following Sunday. Flying down Wednesday night or Thursday.";
    private static final String BEER_OLYMPICS_DESCRIPTION = "this will be hosted in Pineville Arkansas at Rich's (My dads) Lodge. Still discussing games for this but there will for sure be a battle for the dunkin sunglasses to see who is most worthy to wield them. Will engage in other shenanigans and cause a ruckus on the property.";

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
        if (vacationCalendar.getDescription() != null) {
            vacationCalendar.setDescription(vacationCalendar.getDescription().trim());
        }
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
            views.add(new VacationCalendarView(vacationCalendar, buildCalendarMonths(vacationCalendar)));
        }
        return views;
    }

    private List<CalendarMonthView> buildCalendarMonths(VacationCalendar vacationCalendar) {
        DateRange displayRange = getDisplayRange(vacationCalendar);
        List<CalendarMonthView> months = new ArrayList<>();
        if (displayRange == null) {
            return months;
        }

        YearMonth currentMonth = YearMonth.from(displayRange.getStartDate());
        YearMonth endMonth = YearMonth.from(displayRange.getEndDate());
        while (!currentMonth.isAfter(endMonth)) {
            months.add(new CalendarMonthView(currentMonth, buildMonthDays(vacationCalendar, currentMonth)));
            currentMonth = currentMonth.plusMonths(1);
        }
        return months;
    }

    private DateRange getDisplayRange(VacationCalendar vacationCalendar) {
        LocalDate startDate = vacationCalendar.getStartDate();
        LocalDate endDate = vacationCalendar.getEndDate();

        for (VacationAvailability availability : vacationCalendar.getAvailabilities()) {
            if (startDate == null || availability.getAvailableFrom().isBefore(startDate)) {
                startDate = availability.getAvailableFrom();
            }
            if (endDate == null || availability.getAvailableTo().isAfter(endDate)) {
                endDate = availability.getAvailableTo();
            }
        }

        if (startDate == null || endDate == null) {
            return null;
        }
        return new DateRange(startDate, endDate);
    }

    private List<CalendarDayView> buildMonthDays(VacationCalendar vacationCalendar, YearMonth month) {
        List<CalendarDayView> days = new ArrayList<>();
        LocalDate firstDay = month.atDay(1);
        int leadingBlankDays = firstDay.getDayOfWeek().getValue() % 7;
        for (int index = 0; index < leadingBlankDays; index++) {
            days.add(CalendarDayView.blank());
        }

        for (int dayOfMonth = 1; dayOfMonth <= month.lengthOfMonth(); dayOfMonth++) {
            LocalDate date = month.atDay(dayOfMonth);
            days.add(new CalendarDayView(
                    date,
                    isVacationWindow(vacationCalendar, date),
                    getAvailablePeople(vacationCalendar, date)
            ));
        }
        return days;
    }

    private boolean isVacationWindow(VacationCalendar vacationCalendar, LocalDate date) {
        return vacationCalendar.getStartDate() != null
                && vacationCalendar.getEndDate() != null
                && !date.isBefore(vacationCalendar.getStartDate())
                && !date.isAfter(vacationCalendar.getEndDate());
    }

    private List<String> getAvailablePeople(VacationCalendar vacationCalendar, LocalDate date) {
        Set<String> names = new LinkedHashSet<>();
        for (VacationAvailability availability : vacationCalendar.getAvailabilities()) {
            if (!date.isBefore(availability.getAvailableFrom()) && !date.isAfter(availability.getAvailableTo())) {
                names.add(availability.getPersonName());
            }
        }
        return new ArrayList<>(names);
    }

    private void ensureStarterCalendars() {
        vacationCalendarRepository.findByNameIgnoreCaseAndIsDeletedFalse("Shark Fishing Trip")
                .map(calendar -> {
                    updateStarterCalendar(calendar, SHARK_FISHING_DESCRIPTION, LocalDate.of(2026, 8, 20), LocalDate.of(2026, 8, 23));
                    return vacationCalendarRepository.save(calendar);
                })
                .orElseGet(() -> {
                    VacationCalendar calendar = new VacationCalendar();
                    calendar.setName("Shark Fishing Trip");
                    calendar.setStartDate(LocalDate.of(2026, 8, 20));
                    calendar.setEndDate(LocalDate.of(2026, 8, 23));
                    calendar.setDescription(SHARK_FISHING_DESCRIPTION);
                    return vacationCalendarRepository.save(calendar);
                });

        vacationCalendarRepository.findByNameIgnoreCaseAndIsDeletedFalse("Beer Olympics")
                .map(calendar -> {
                    updateStarterCalendar(calendar, BEER_OLYMPICS_DESCRIPTION, null, null);
                    return vacationCalendarRepository.save(calendar);
                })
                .orElseGet(() -> {
                    VacationCalendar calendar = new VacationCalendar();
                    calendar.setName("Beer Olympics");
                    calendar.setDescription(BEER_OLYMPICS_DESCRIPTION);
                    return vacationCalendarRepository.save(calendar);
                });
    }

    private void updateStarterCalendar(VacationCalendar calendar, String description, LocalDate startDate, LocalDate endDate) {
        if (calendar.getDescription() == null || calendar.getDescription().trim().isEmpty()) {
            calendar.setDescription(description);
        }
        if (startDate != null && calendar.getStartDate() == null) {
            calendar.setStartDate(startDate);
        }
        if (endDate != null && calendar.getEndDate() == null) {
            calendar.setEndDate(endDate);
        }
    }

    public static class VacationCalendarView {
        private final VacationCalendar vacationCalendar;
        private final List<CalendarMonthView> months;

        public VacationCalendarView(VacationCalendar vacationCalendar, List<CalendarMonthView> months) {
            this.vacationCalendar = vacationCalendar;
            this.months = months;
        }

        public VacationCalendar getVacationCalendar() {
            return vacationCalendar;
        }

        public List<CalendarMonthView> getMonths() {
            return months;
        }
    }

    public static class CalendarMonthView {
        private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy");

        private final YearMonth month;
        private final List<CalendarDayView> days;

        public CalendarMonthView(YearMonth month, List<CalendarDayView> days) {
            this.month = month;
            this.days = days;
        }

        public YearMonth getMonth() {
            return month;
        }

        public String getDisplayName() {
            return month.atDay(1).format(MONTH_FORMATTER);
        }

        public List<CalendarDayView> getDays() {
            return days;
        }
    }

    public static class CalendarDayView {
        private final LocalDate date;
        private final boolean vacationWindow;
        private final List<String> availablePeople;

        public CalendarDayView(LocalDate date, boolean vacationWindow, List<String> availablePeople) {
            this.date = date;
            this.vacationWindow = vacationWindow;
            this.availablePeople = availablePeople;
        }

        public static CalendarDayView blank() {
            return new CalendarDayView(null, false, new ArrayList<>());
        }

        public LocalDate getDate() {
            return date;
        }

        public boolean isVacationWindow() {
            return vacationWindow;
        }

        public List<String> getAvailablePeople() {
            return availablePeople;
        }

        public boolean isAvailable() {
            return !availablePeople.isEmpty();
        }
    }

    private static class DateRange {
        private final LocalDate startDate;
        private final LocalDate endDate;

        public DateRange(LocalDate startDate, LocalDate endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
        }

        public LocalDate getStartDate() {
            return startDate;
        }

        public LocalDate getEndDate() {
            return endDate;
        }
    }
}
