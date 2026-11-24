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
     * 画面ID: S0104 勤務場所画面
     */
    @GetMapping("/work_location")
    public String showWorkLocation(@RequestParam(value = "userName", required = false) String userName,
                                   HttpSession session,
                                   Model model) {
        user_Entity loginUser = (user_Entity) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login";

        // 検索条件（氏名）が入力されていれば部分一致検索、なければ全件取得
        List<user_Entity> users;
        if (userName != null && !userName.isEmpty()) {
            users = userRepository.findByUserNameContainingOrderByUserIdAsc(userName);
        } else {
            users = userRepository.findAllByOrderByUserIdAsc();
        }

        // 画面表示用に部署名・勤務地名・本日の勤務状況名を紐づけ
        List<Map<String, String>> displayList = new ArrayList<>();
        for (user_Entity u : users) {
            Map<String, String> row = new HashMap<>();
            row.put("userId", u.getUserId());
            row.put("userName", u.getUserName());

            department_Entity dept = (u.getDepartmentId() != null)
                    ? entityManager.find(department_Entity.class, u.getDepartmentId()) : null;
            row.put("departmentName", dept != null ? dept.getDepartmentName() : "-");

            work_location_Entity loc = (u.getWorkLocationId() != null)
                    ? entityManager.find(work_location_Entity.class, u.getWorkLocationId()) : null;
            row.put("workLocationName", loc != null ? loc.getWorkLocationName() : "-");

            attendance_today_Entity today = entityManager.find(attendance_today_Entity.class, u.getUserId());
            String statusName = "勤務外";
            if (today != null && today.getWorkStatusId() != null) {
                work_status_Entity ws = entityManager.find(work_status_Entity.class, today.getWorkStatusId());
                if (ws != null) statusName = ws.getWorkStatusName();
            }
            row.put("workStatusName", statusName);

            displayList.add(row);
        }

        model.addAttribute("loginUser", loginUser);
        model.addAttribute("userName", userName);
        model.addAttribute("displayList", displayList);
        return "work_location";
    }

    /**
     * 画面ID: S0105 連絡先登録画面（初期表示）
     */
    @GetMapping("/contact_add")
    public String showContactAdd(HttpSession session, Model model) {
        user_Entity loginUser = (user_Entity) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login";

        // 最新のユーザー情報をDBから取得して入力欄の初期値にする
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

        // 必須・半角数字のみ（ハイフンなし）・メール半角英数字記号1〜128文字チェック
        boolean phoneValid = trimmedPhone.matches("^[0-9]+$");
        boolean emailValid = trimmedEmail.matches("^[!-~]{1,128}$");

        if (trimmedPhone.isEmpty() || trimmedEmail.isEmpty() || !phoneValid || !emailValid) {
            currentUser.setPhoneNumber(trimmedPhone);
            currentUser.setEmailAddress(trimmedEmail);
            model.addAttribute("loginUser", currentUser);
            model.addAttribute("errorMessage", "電話番号またはメールアドレスに誤りがあります");
            return "contact_add";
        }

        // DBのユーザー情報を更新
        currentUser.setPhoneNumber(trimmedPhone);
        currentUser.setEmailAddress(trimmedEmail);
        userRepository.save(currentUser);

        // セッション情報も最新に更新
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

        // プルダウン選択肢用にマスタデータを取得
        List<work_status_Entity> statusMasterList = entityManager
                .createQuery("SELECT s FROM work_status_Entity s ORDER BY s.workStatusId ASC", work_status_Entity.class)
                .getResultList();
        List<work_location_Entity> locationMasterList = entityManager
                .createQuery("SELECT l FROM work_location_Entity l ORDER BY l.workLocationId ASC", work_location_Entity.class)
                .getResultList();

        // ログインユーザーと同じ部署のメンバーを50音順（userName昇順）で取得
        List<user_Entity> deptUsers;
        if (loginUser.getDepartmentId() != null) {
            deptUsers = userRepository.findByDepartmentIdOrderByUserNameAsc(loginUser.getDepartmentId());
        } else {
            deptUsers = userRepository.findAllByOrderByUserIdAsc();
        }

        List<Map<String, String>> statusList = new ArrayList<>();
        for (user_Entity u : deptUsers) {
            // 勤務場所の絞り込み条件チェック
            if (workLocationId != null && !workLocationId.isEmpty()
                    && !workLocationId.equals(u.getWorkLocationId())) {
                continue;
            }

            // 本日の勤怠状況を取得（未打刻は W03:勤務外 扱い）
            attendance_today_Entity today = entityManager.find(attendance_today_Entity.class, u.getUserId());
            String currentStatusId = (today != null && today.getWorkStatusId() != null) ? today.getWorkStatusId() : "W03";

            // 出社・在宅・勤務外の絞り込み条件チェック
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

            row.put("phoneNumber", u.getPhoneNumber() != null ? u.getPhoneNumber() : "-");
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
}