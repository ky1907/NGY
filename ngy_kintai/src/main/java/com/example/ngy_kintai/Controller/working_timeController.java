package com.example.ngy_kintai.Controller;

import com.example.ngy_kintai.Entity.user_Entity;
import com.example.ngy_kintai.Entity.time_management_Entity;
import com.example.ngy_kintai.Entity.month_time_management_Entity;
import com.example.ngy_kintai.Repository.user_Repository;
import com.example.ngy_kintai.Repository.time_management_Repository;
import com.example.ngy_kintai.Repository.month_time_management_Repository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
public class working_timeController {

    @Autowired
    private user_Repository userRepository;

    @Autowired
    private time_management_Repository timeManagementRepository;

    @Autowired
    private month_time_management_Repository monthTimeManagementRepository;

    private boolean isAdmin(HttpSession session) {
        user_Entity loginUser = (user_Entity) session.getAttribute("loginUser");
        return loginUser != null && "R01".equals(loginUser.getRoleId());
    }

    // 勤務時間参照・集計画面（S0109）表示
    @GetMapping("/working_time")
    public String showWorkingTime(@RequestParam(value = "userId", required = false) String userId,
                                  @RequestParam(value = "yearMonth", required = false) String yearMonth,
                                  HttpSession session,
                                  Model model) {
        if (!isAdmin(session)) return "redirect:/login";

        List<user_Entity> userList = userRepository.findAll();
        model.addAttribute("userList", userList);

        if (userId != null && !userId.isEmpty()) {
            model.addAttribute("selectedUserId", userId);
            
            // 月間集計の取得
            if (yearMonth != null && !yearMonth.isEmpty()) {
                model.addAttribute("selectedYearMonth", yearMonth);
                month_time_management_Entity monthly = monthTimeManagementRepository
                        .findByUserIdAndTargetYearMonth(userId, yearMonth)
                        .orElse(null);
                model.addAttribute("monthlySummary", monthly);

                // 日別データの取得（該当月の一杯から末日まで）
                try {
                    LocalDate startOfMonth = LocalDate.parse(yearMonth + "-01");
                    LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());
                    List<time_management_Entity> dailyList = timeManagementRepository
                            .findByUserIdAndWorkDateBetweenOrderByWorkDateAsc(userId, startOfMonth, endOfMonth);
                    model.addAttribute("dailyList", dailyList);
                } catch (Exception e) {
                    // パースエラー時のフォールバック
                }
            }
        }

        return "working_time";
    }
}