package com.example.ngy_kintai.Controller;

import com.example.ngy_kintai.Entity.*;
import com.example.ngy_kintai.Repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
public class attendanceController {

    @Autowired
    private attendance_Repository attendanceRepository;

    @Autowired
    private time_management_Repository timeManagementRepository;

    @Autowired
    private month_time_management_Repository monthTimeManagementRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @GetMapping("/attendance")
    public String showAttendance(HttpSession session, Model model) {
        user_Entity loginUser = (user_Entity) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login";

        model.addAttribute("loginUser", loginUser);
        return "attendance";
    }

    @PostMapping("/attendance")
    @Transactional
    public String submitAttendance(@RequestParam("attendanceType") String attendanceType,
                                   @RequestParam(value = "workStatusId", required = false) String workStatusId,
                                   HttpSession session) {
        user_Entity loginUser = (user_Entity) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login";

        LocalDateTime now = LocalDateTime.now();
        attendance_today_Entity today = entityManager.find(attendance_today_Entity.class, loginUser.getUserId());

        if ("0".equals(attendanceType)) {
            // 出勤打刻
            if (today == null) {
                today = new attendance_today_Entity();
                today.setUserId(loginUser.getUserId());
            }
            today.setStartTime(now);
            today.setEndTime(null);
            today.setWorkStatusId(workStatusId);
            entityManager.merge(today);
        } else if ("1".equals(attendanceType)) {
            // 退勤打刻（出社・在宅は選択されないため、出勤時の勤務場所を引き継ぐ）
            String savedWorkStatusId = (today != null && today.getWorkStatusId() != null && !"W03".equals(today.getWorkStatusId()))
                    ? today.getWorkStatusId()
                    : (workStatusId != null && !workStatusId.isEmpty() ? workStatusId : "W01");

            LocalDateTime startTime = (today != null && today.getStartTime() != null) ? today.getStartTime() : now;
            if (today == null) {
                today = new attendance_today_Entity();
                today.setUserId(loginUser.getUserId());
            }
            today.setEndTime(now);
            today.setWorkStatusId("W03"); // 勤務外
            entityManager.merge(today);

            // 履歴テーブルへ保存
            attendance_Entity history = new attendance_Entity();
            history.setUserId(loginUser.getUserId());
            history.setStartTime(startTime);
            history.setEndTime(now);
            history.setWorkStatusId(savedWorkStatusId);
            attendanceRepository.save(history);

            // 日次・月次就業時間集計（8時間=480分超過を残業とする基本計算）
            int workedMins = (int) Duration.between(startTime, now).toMinutes();
            int actualMins = Math.min(workedMins, 480);
            int overtimeMins = Math.max(0, workedMins - 480);

            LocalDate workDate = now.toLocalDate();
            time_management_Entity daily = timeManagementRepository
                    .findByUserIdAndWorkDate(loginUser.getUserId(), workDate)
                    .orElse(new time_management_Entity());
            daily.setUserId(loginUser.getUserId());
            daily.setWorkDate(workDate);
            daily.setActualWorkMinutes((daily.getActualWorkMinutes() == null ? 0 : daily.getActualWorkMinutes()) + actualMins);
            daily.setOvertimeMinutes((daily.getOvertimeMinutes() == null ? 0 : daily.getOvertimeMinutes()) + overtimeMins);
            daily.setCreatedAt(now);
            timeManagementRepository.save(daily);

            String ym = now.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            month_time_management_Entity monthly = monthTimeManagementRepository
                    .findByUserIdAndTargetYearMonth(loginUser.getUserId(), ym)
                    .orElse(new month_time_management_Entity());
            monthly.setUserId(loginUser.getUserId());
            monthly.setTargetYearMonth(ym);
            monthly.setMonthlyActualMinutes((monthly.getMonthlyActualMinutes() == null ? 0 : monthly.getMonthlyActualMinutes()) + actualMins);
            monthly.setMonthlyOvertimeMinutes((monthly.getMonthlyOvertimeMinutes() == null ? 0 : monthly.getMonthlyOvertimeMinutes()) + overtimeMins);
            monthTimeManagementRepository.save(monthly);
        }

        return "redirect:/calendar";
    }
}