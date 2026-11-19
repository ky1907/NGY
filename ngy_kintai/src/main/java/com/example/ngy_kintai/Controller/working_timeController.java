package com.example.ngy_kintai.Controller;

import com.example.ngy_kintai.Entity.month_time_management_Entity;
import com.example.ngy_kintai.Entity.time_management_Entity;
import com.example.ngy_kintai.Entity.user_Entity;
import com.example.ngy_kintai.Repository.month_time_management_Repository;
import com.example.ngy_kintai.Repository.time_management_Repository;
import com.example.ngy_kintai.Repository.user_Repository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class working_timeController {

    @Autowired
    private user_Repository userRepository;

    @Autowired
    private time_management_Repository timeManagementRepository;

    @Autowired
    private month_time_management_Repository monthTimeManagementRepository;

    @GetMapping("/working_time")
    public String showWorkingTime(
            @RequestParam(value = "yearMonth", required = false) String yearMonth,
            @RequestParam(value = "departmentId", required = false) String departmentId,
            HttpSession session,
            Model model) {

        user_Entity loginUser = (user_Entity) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login";
        model.addAttribute("loginUser", loginUser);

        if (yearMonth == null || yearMonth.isEmpty()) {
            yearMonth = LocalDate.now().toString().substring(0, 7);
        }
        model.addAttribute("selectedYearMonth", yearMonth);
        model.addAttribute("selectedDepartment", departmentId);

        List<user_Entity> userList;
        if (departmentId != null && !departmentId.isEmpty()) {
            userList = userRepository.findByDepartmentIdOrderByUserNameAsc(departmentId);
        } else {
            userList = userRepository.findAll();
        }
        model.addAttribute("userList", userList);

        YearMonth ym = YearMonth.parse(yearMonth);
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();
        int daysInMonth = ym.lengthOfMonth();
        model.addAttribute("daysInMonth", daysInMonth);

        Map<String, Map<LocalDate, time_management_Entity>> userMonthlyData = new HashMap<>();
        Map<String, month_time_management_Entity> monthlySummaryMap = new HashMap<>();

        for (user_Entity user : userList) {
            List<time_management_Entity> records = timeManagementRepository
                    .findByUserIdAndWorkDateBetweenOrderByWorkDateAsc(user.getUserId(), startDate, endDate);

            Map<LocalDate, time_management_Entity> dailyMap = new HashMap<>();
            for (time_management_Entity rec : records) {
                dailyMap.put(rec.getWorkDate(), rec);
            }
            userMonthlyData.put(user.getUserId(), dailyMap);

            month_time_management_Entity monthly = monthTimeManagementRepository
                    .findByUserIdAndTargetYearMonth(user.getUserId(), yearMonth)
                    .orElse(null);
            monthlySummaryMap.put(user.getUserId(), monthly);
        }

        model.addAttribute("userMonthlyData", userMonthlyData);
        model.addAttribute("monthlySummaryMap", monthlySummaryMap);

        return "working_time";
    }
}