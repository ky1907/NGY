package com.example.ngy_kintai.Controller;

import com.example.ngy_kintai.Entity.calendar_Entity;
import com.example.ngy_kintai.Entity.user_Entity;
import com.example.ngy_kintai.Repository.calendar_Repository;
import com.example.ngy_kintai.Repository.user_Repository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Controller
@RequestMapping("/calendar")
public class calendarController {

    private static final Set<String> ALLOWED_COLORS = Set.of("sky", "blue", "green", "amber", "rose", "purple");
    private static final String COLOR_PREFIX_REGEX = "^\\[color:([a-zA-Z0-9_-]+)\\]\\s*";
    private static final DateTimeFormatter DT_LOCAL_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    private static final DateTimeFormatter HM_FMT = DateTimeFormatter.ofPattern("HH:mm");

    @Autowired
    private calendar_Repository calendarRepository;

    @Autowired
    private user_Repository userRepository;

    public static class WeekRowView {
        private final List<LocalDate> days;
        private final List<EventBarView> bars;

        public WeekRowView(List<LocalDate> days, List<EventBarView> bars) {
            this.days = days;
            this.bars = bars;
        }

        public List<LocalDate> getDays() {
            return days;
        }

        public List<EventBarView> getBars() {
            return bars;
        }
    }

    public static class EventBarView {
        private final Long calendarId;
        private final String title;
        private final String label;
        private final String color;
        private final String detail;
        private final String startTimeInput;
        private final String endTimeInput;
        private final int startCol;
        private final int spanCols;

        public EventBarView(Long calendarId, String title, String label, String color, String detail,
                            String startTimeInput, String endTimeInput, int startCol, int spanCols) {
            this.calendarId = calendarId;
            this.title = title;
            this.label = label;
            this.color = color;
            this.detail = detail;
            this.startTimeInput = startTimeInput;
            this.endTimeInput = endTimeInput;
            this.startCol = startCol;
            this.spanCols = spanCols;
        }

        public Long getCalendarId() { return calendarId; }
        public String getTitle() { return title; }
        public String getLabel() { return label; }
        public String getColor() { return color; }
        public String getDetail() { return detail; }
        public String getStartTimeInput() { return startTimeInput; }
        public String getEndTimeInput() { return endTimeInput; }
        public int getStartCol() { return startCol; }
        public int getSpanCols() { return spanCols; }
    }

    private user_Entity resolveLoginUser(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object obj = session.getAttribute("loginUser");
        if (obj instanceof user_Entity sessionUser) {
            if (sessionUser.getUserId() != null && !sessionUser.getUserId().isBlank()) {
                return userRepository.findById(sessionUser.getUserId()).orElse(sessionUser);
            }
            return sessionUser;
        }
        return null;
    }

    private String extractColor(String rawDetail) {
        if (rawDetail != null && rawDetail.startsWith("[color:")) {
            int endIdx = rawDetail.indexOf(']');
            if (endIdx > 7) {
                String c = rawDetail.substring(7, endIdx);
                if (ALLOWED_COLORS.contains(c)) {
                    return c;
                }
            }
        }
        return "sky";
    }

    private String stripColorTag(String rawDetail) {
        if (rawDetail == null) {
            return "";
        }
        return rawDetail.replaceAll(COLOR_PREFIX_REGEX, "");
    }

    @GetMapping
    public String showCalendar(@RequestParam(required = false) Integer year,
                               @RequestParam(required = false) Integer month,
                               HttpSession session,
                               Model model) {
        LocalDate now = LocalDate.now();
        int targetYear = (year != null) ? year : now.getYear();
        int targetMonth = (month != null) ? month : now.getMonthValue();

        YearMonth yearMonth = YearMonth.of(targetYear, targetMonth);
        LocalDate firstOfMonth = yearMonth.atDay(1);
        LocalDate endOfMonth = yearMonth.atEndOfMonth();

        LocalDate startDate = firstOfMonth.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        LocalDate endDate = endOfMonth.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));

        List<List<LocalDate>> calendarWeeks = new ArrayList<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            List<LocalDate> week = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                week.add(current);
                current = current.plusDays(1);
            }
            calendarWeeks.add(week);
        }

        YearMonth prevMonth = yearMonth.minusMonths(1);
        YearMonth nextMonth = yearMonth.plusMonths(1);

        model.addAttribute("currentYear", targetYear);
        model.addAttribute("currentMonth", targetMonth);
        model.addAttribute("today", now);
        model.addAttribute("prevYear", prevMonth.getYear());
        model.addAttribute("prevMonth", prevMonth.getMonthValue());
        model.addAttribute("nextYear", nextMonth.getYear());
        model.addAttribute("nextMonth", nextMonth.getMonthValue());

        user_Entity loginUser = resolveLoginUser(session);
        model.addAttribute("loginUser", loginUser);

        String loginUserId = (loginUser != null && loginUser.getUserId() != null) ? loginUser.getUserId() : "";

        List<calendar_Entity> events = !loginUserId.isBlank()
                ? calendarRepository.findByUserIdOrderByStartTimeAsc(loginUserId)
                : new ArrayList<>();

        List<WeekRowView> weekRows = new ArrayList<>();
        for (List<LocalDate> week : calendarWeeks) {
            LocalDate weekStart = week.get(0);
            LocalDate weekEnd = week.get(6);
            List<EventBarView> bars = new ArrayList<>();

            for (calendar_Entity ev : events) {
                if (ev.getStartTime() == null) {
                    continue;
                }
                LocalDate evStart = ev.getStartTime().toLocalDate();
                LocalDate evEnd = (ev.getEndTime() != null && !ev.getEndTime().toLocalDate().isBefore(evStart))
                        ? ev.getEndTime().toLocalDate()
                        : evStart;

                if (evEnd.isBefore(weekStart) || evStart.isAfter(weekEnd)) {
                    continue;
                }

                LocalDate segStart = evStart.isAfter(weekStart) ? evStart : weekStart;
                LocalDate segEnd = evEnd.isBefore(weekEnd) ? evEnd : weekEnd;

                int startCol = (int) ChronoUnit.DAYS.between(weekStart, segStart) + 1;
                int spanCols = (int) ChronoUnit.DAYS.between(segStart, segEnd) + 1;

                String color = extractColor(ev.getDetail());
                String cleanDetail = stripColorTag(ev.getDetail());
                String titleStr = (ev.getTitle() != null) ? ev.getTitle() : "";
                String label = segStart.equals(evStart)
                        ? (ev.getStartTime().format(HM_FMT) + " " + titleStr)
                        : titleStr;

                String startInput = ev.getStartTime().format(DT_LOCAL_FMT);
                String endInput = (ev.getEndTime() != null) ? ev.getEndTime().format(DT_LOCAL_FMT) : "";

                bars.add(new EventBarView(
                        ev.getCalendarId(),
                        titleStr,
                        label,
                        color,
                        cleanDetail,
                        startInput,
                        endInput,
                        startCol,
                        spanCols
                ));
            }
            weekRows.add(new WeekRowView(week, bars));
        }

        model.addAttribute("weekRows", weekRows);
        return "calendar";
    }

    @PostMapping("/save")
    public String saveEvent(@ModelAttribute calendar_Entity event,
                            @RequestParam(name = "eventColor", required = false, defaultValue = "sky") String eventColor,
                            HttpSession session) {
        user_Entity loginUser = resolveLoginUser(session);
        if (loginUser != null && loginUser.getUserId() != null && !loginUser.getUserId().isBlank()) {
            event.setUserId(loginUser.getUserId());
        }

        String color = ALLOWED_COLORS.contains(eventColor) ? eventColor : "sky";
        String cleanDetail = stripColorTag(event.getDetail());
        event.setDetail("[color:" + color + "]" + cleanDetail);

        calendarRepository.save(event);
        return "redirect:/calendar";
    }

    @PostMapping("/delete/{id}")
    public String deleteEvent(@PathVariable Long id) {
        calendarRepository.deleteById(id);
        return "redirect:/calendar";
    }
}