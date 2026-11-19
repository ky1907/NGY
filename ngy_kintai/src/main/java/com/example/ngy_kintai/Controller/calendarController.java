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
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/calendar")
public class calendarController {

    private static final Set<String> ALLOWED_COLORS = Set.of("sky", "blue", "green", "amber", "rose", "purple");
    private static final String COLOR_PREFIX_REGEX = "^\\[color:([a-zA-Z0-9_-]+)\\]\\s*";

    @Autowired
    private calendar_Repository calendarRepository;

    @Autowired
    private user_Repository userRepository;

    /**
     * セッション（"loginUser"）からログイン中のユーザー情報を取得する
     */
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

    /**
     * 月間カレンダー画面の表示 (S0103)
     */
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

        // 日曜始まりでカレンダーの開始日・終了日を算出
        LocalDate startDate = firstOfMonth.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        LocalDate endDate = endOfMonth.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));

        // 週ごとの日付リストを作成
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

        // 前月・次月リンク用の計算
        YearMonth prevMonth = yearMonth.minusMonths(1);
        YearMonth nextMonth = yearMonth.plusMonths(1);

        model.addAttribute("currentYear", targetYear);
        model.addAttribute("currentMonth", targetMonth);
        model.addAttribute("today", now);
        model.addAttribute("calendarWeeks", calendarWeeks);
        model.addAttribute("prevYear", prevMonth.getYear());
        model.addAttribute("prevMonth", prevMonth.getMonthValue());
        model.addAttribute("nextYear", nextMonth.getYear());
        model.addAttribute("nextMonth", nextMonth.getMonthValue());

        // ログイン中のユーザー情報を取得
        user_Entity loginUser = resolveLoginUser(session);
        String loginUserId = (loginUser != null && loginUser.getUserId() != null) ? loginUser.getUserId() : "";
        String loginUserName = (loginUser != null && loginUser.getUserName() != null) ? loginUser.getUserName() : "";

        model.addAttribute("loginUserId", loginUserId);
        model.addAttribute("loginUserName", loginUserName);

        // 該当ユーザーの予定を開始日時順に取得
        List<calendar_Entity> events = !loginUserId.isBlank()
                ? calendarRepository.findByUserIdOrderByStartTimeAsc(loginUserId)
                : new ArrayList<>();
        model.addAttribute("events", events);

        return "calendar";
    }

    /**
     * 予定の登録・更新処理
     */
    @PostMapping("/save")
    public String saveEvent(@ModelAttribute calendar_Entity event,
                            @RequestParam(name = "eventColor", required = false, defaultValue = "sky") String eventColor,
                            HttpSession session) {
        user_Entity loginUser = resolveLoginUser(session);
        if (loginUser != null && loginUser.getUserId() != null && !loginUser.getUserId().isBlank()) {
            event.setUserId(loginUser.getUserId());
        }

        String color = ALLOWED_COLORS.contains(eventColor) ? eventColor : "sky";
        String rawDetail = (event.getDetail() != null) ? event.getDetail() : "";
        String cleanDetail = rawDetail.replaceAll(COLOR_PREFIX_REGEX, "");
        event.setDetail("[color:" + color + "]" + cleanDetail);

        calendarRepository.save(event);
        return "redirect:/calendar";
    }

    /**
     * 予定の削除処理
     */
    @PostMapping("/delete/{id}")
    public String deleteEvent(@PathVariable Long id) {
        calendarRepository.deleteById(id);
        return "redirect:/calendar";
    }
}