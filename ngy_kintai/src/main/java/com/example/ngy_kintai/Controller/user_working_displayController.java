package com.example.ngy_kintai.Controller;

import com.example.ngy_kintai.Entity.*;
import com.example.ngy_kintai.Repository.user_Repository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class user_working_displayController {

    @Autowired
    private user_Repository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * 画面ID: S0104 勤務場所登録画面（初期表示）
     */
    @GetMapping("/work_location")
    public String showWorkLocation(HttpSession session, Model model) {
        user_Entity loginUser = (user_Entity) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login";

        user_Entity currentUser = userRepository.findById(loginUser.getUserId()).orElse(loginUser);
        List<work_location_Entity> locationList = entityManager
                .createQuery("SELECT l FROM work_location_Entity l ORDER BY l.workLocationId ASC", work_location_Entity.class)
                .getResultList();

        model.addAttribute("loginUser", currentUser);
        model.addAttribute("locationList", locationList);
        return "work_location";
    }

    /**
     * 画面ID: S0104 勤務場所登録画面（登録処理）
     */
    @PostMapping("/work_location")
    @Transactional
    public String registerWorkLocation(@RequestParam(value = "workLocationId", required = false, defaultValue = "") String workLocationId,
                                       HttpSession session,
                                       Model model) {
        user_Entity loginUser = (user_Entity) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login";

        user_Entity currentUser = userRepository.findById(loginUser.getUserId()).orElse(loginUser);
        List<work_location_Entity> locationList = entityManager
                .createQuery("SELECT l FROM work_location_Entity l ORDER BY l.workLocationId ASC", work_location_Entity.class)
                .getResultList();

        if (workLocationId == null || workLocationId.trim().isEmpty()) {
            model.addAttribute("loginUser", currentUser);
            model.addAttribute("locationList", locationList);
            model.addAttribute("errorMessage", "勤務場所を選択してください");
            return "work_location";
        }

        currentUser.setWorkLocationId(workLocationId.trim());
        currentUser.setUpdatedAt(LocalDateTime.now());
        userRepository.save(currentUser);

        session.setAttribute("loginUser", currentUser);

        model.addAttribute("loginUser", currentUser);
        model.addAttribute("locationList", locationList);
        model.addAttribute("completeMessage", "勤務場所を登録しました");
        return "work_location";
    }

    /**
     * 画面ID: S0105 連絡先登録画面（初期表示）
     */
    @GetMapping("/contact_add")
    public String showContactAdd(HttpSession session, Model model) {
        user_Entity loginUser = (user_Entity) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login";

        user_Entity currentUser = userRepository.findById(loginUser.getUserId()).orElse(loginUser);

        model.addAttribute("loginUser", currentUser);
        return "contact_add";
    }

    /**
     * 画面ID: S0105 連絡先登録画面（登録処理）
     */
    @PostMapping("/contact_add")
    @Transactional
    public String registerContact(@RequestParam(value = "phoneNumber", required = false, defaultValue = "") String phoneNumber,
                                  @RequestParam(value = "emailAddress", required = false, defaultValue = "") String emailAddress,
                                  HttpSession session,
                                  Model model) {
        user_Entity loginUser = (user_Entity) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login";

        String trimmedPhone = phoneNumber.trim();
        String trimmedEmail = emailAddress.trim();

        user_Entity currentUser = userRepository.findById(loginUser.getUserId()).orElse(loginUser);

        // 電話番号：半角数字8〜15桁（固定電話・海外番号対応）
        boolean phoneValid = trimmedPhone.matches("^[0-9]{8,15}$");
        boolean emailValid = trimmedEmail.matches("^[!-~]{1,128}$");

        if (trimmedPhone.isEmpty() || trimmedEmail.isEmpty() || !phoneValid || !emailValid) {
            currentUser.setPhoneNumber(trimmedPhone);
            currentUser.setEmailAddress(trimmedEmail);
            model.addAttribute("loginUser", currentUser);
            model.addAttribute("errorMessage", "電話番号またはメールアドレスに誤りがあります");
            return "contact_add";
        }

        currentUser.setPhoneNumber(trimmedPhone);
        currentUser.setEmailAddress(trimmedEmail);
        userRepository.save(currentUser);

        session.setAttribute("loginUser", currentUser);

        model.addAttribute("loginUser", currentUser);
        model.addAttribute("completeMessage", "ユーザ情報を登録しました");
        return "contact_add";
    }

    /**
     * 画面ID: S0106 勤務状況一覧（部署内勤務状況）画面
     */
    @GetMapping("/working_status")
    public String showWorkingStatus(@RequestParam(value = "workStatusId", required = false) String workStatusId,
                                    @RequestParam(value = "workLocationId", required = false) String workLocationId,
                                    HttpSession session,
                                    Model model) {
        user_Entity loginUser = (user_Entity) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login";

        List<work_status_Entity> statusMasterList = entityManager
                .createQuery("SELECT s FROM work_status_Entity s ORDER BY s.workStatusId ASC", work_status_Entity.class)
                .getResultList();
        List<work_location_Entity> locationMasterList = entityManager
                .createQuery("SELECT l FROM work_location_Entity l ORDER BY l.workLocationId ASC", work_location_Entity.class)
                .getResultList();

        List<user_Entity> deptUsers;
        if (loginUser.getDepartmentId() != null) {
            deptUsers = userRepository.findByDepartmentIdOrderByUserNameAsc(loginUser.getDepartmentId());
        } else {
            deptUsers = userRepository.findAllByOrderByUserIdAsc();
        }

        List<Map<String, String>> statusList = new ArrayList<>();
        for (user_Entity u : deptUsers) {
            if (workLocationId != null && !workLocationId.isEmpty()
                    && !workLocationId.equals(u.getWorkLocationId())) {
                continue;
            }

            attendance_today_Entity today = entityManager.find(attendance_today_Entity.class, u.getUserId());
            String currentStatusId = (today != null && today.getWorkStatusId() != null) ? today.getWorkStatusId() : "W03";

            if (workStatusId != null && !workStatusId.isEmpty()
                    && !workStatusId.equals(currentStatusId)) {
                continue;
            }

            Map<String, String> row = new HashMap<>();
            row.put("userName", u.getUserName());

            work_status_Entity ws = entityManager.find(work_status_Entity.class, currentStatusId);
            row.put("workStatusName", ws != null ? ws.getWorkStatusName() : "勤務外");

            work_location_Entity loc = (u.getWorkLocationId() != null)
                    ? entityManager.find(work_location_Entity.class, u.getWorkLocationId()) : null;
            row.put("workLocationName", loc != null ? loc.getWorkLocationName() : "-");

            row.put("phoneNumber", formatPhoneNumber(u.getPhoneNumber()));
            row.put("emailAddress", u.getEmailAddress() != null ? u.getEmailAddress() : "-");

            statusList.add(row);
        }

        model.addAttribute("loginUser", loginUser);
        model.addAttribute("selectedStatusId", workStatusId);
        model.addAttribute("selectedLocationId", workLocationId);
        model.addAttribute("statusMasterList", statusMasterList);
        model.addAttribute("locationMasterList", locationMasterList);
        model.addAttribute("statusList", statusList);

        return "working_status";
    }

    /**
     * 国内番号（0始まりの10桁・11桁）はハイフン付きに整形し、
     * それ以外（8〜9桁や海外番号12〜15桁など）はそのまま表示する
     */
    private String formatPhoneNumber(String rawPhone) {
        if (rawPhone == null || rawPhone.isEmpty()) {
            return "-";
        }
        String digits = rawPhone.replaceAll("[^0-9]", "");
        if (digits.startsWith("0") && digits.length() == 11) {
            // 国内携帯(090/080/070)・IP電話(050): 3桁-4桁-4桁
            return digits.substring(0, 3) + "-" + digits.substring(3, 7) + "-" + digits.substring(7);
        } else if (digits.startsWith("0") && digits.length() == 10) {
            // 東京(03)・大阪(06): 2桁-4桁-4桁
            if (digits.startsWith("03") || digits.startsWith("06")) {
                return digits.substring(0, 2) + "-" + digits.substring(2, 6) + "-" + digits.substring(6);
            }
            // 名古屋(052)などその他の固定電話: 3桁-3桁-4桁
            return digits.substring(0, 3) + "-" + digits.substring(3, 6) + "-" + digits.substring(6);
        }
        // 8〜9桁や海外番号（12〜15桁、0以外始まり）はそのまま返す
        return digits;
    }
}