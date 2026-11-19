package com.example.ngy_kintai.Controller;

import com.example.ngy_kintai.Entity.calendar_Entity; // エンティティ名やパッケージが変更になる場合は適宜修正してください
import com.example.ngy_kintai.Repository.calendar_Repository; // リポジトリ名やパッケージが変更になる場合は適宜修正してください
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Controller
@RequestMapping("/calendar")
public class calendarController {

    @Autowired
    private calendar_Repository calendarRepository;

    /**
     * 月間カレンダー画面の表示 (S0103)
     */
    @GetMapping
    public String showCalendar(@RequestParam(required = false) Integer year,
                               @RequestParam(required = false) Integer month,
                               Model model) {
        LocalDate now = LocalDate.now();
        int targetYear = (year != null) ? year : now.getYear();
        int targetMonth = (month != null) ? month : now.getMonthValue();

        YearMonth yearMonth = YearMonth.of(targetYear, targetMonth);
        
        // 前月・次月リンク用の計算
        YearMonth prevMonth = yearMonth.minusMonths(1);
        YearMonth nextMonth = yearMonth.plusMonths(1);

        model.addAttribute("currentYear", targetYear);
        model.addAttribute("currentMonth", targetMonth);
        model.addAttribute("prevYear", prevMonth.getYear());
        model.addAttribute("prevMonth", prevMonth.getMonthValue());
        model.addAttribute("nextYear", nextMonth.getYear());
        model.addAttribute("nextMonth", nextMonth.getMonthValue());

        // 仮のログインユーザーID（ログイン機能実装後はセッション等から取得してください）
        String loginUserId = "test_user";

        // 該当ユーザーの予定を開始日時順に取得
        List<calendar_Entity> events = calendarRepository.findByUserIdOrderByStartTimeAsc(loginUserId);
        model.addAttribute("events", events);

        return "calendar"; // calendar.html に対応
    }

    /**
     * 予定の登録・更新処理
     */
    @PostMapping("/save")
    public String saveEvent(@ModelAttribute calendar_Entity event) {
        // 新規登録時はユーザーIDをセット（編集時は既存のuserIdを保持）
        if (event.getUserId() == null || event.getUserId().isEmpty()) {
            event.setUserId("test_user");
        }
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