package com.example.ngy_kintai.Controller;

import com.example.ngy_kintai.Entity.user_Entity;
import com.example.ngy_kintai.Repository.user_Repository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class user_editController {

    @Autowired
    private user_Repository userRepository;

    // 管理者権限チェック（ログイン中の user_Entity の roleId を直接参照）
    private boolean isAdmin(user_Entity loginUser) {
        return loginUser != null && "R01".equals(loginUser.getRoleId());
    }

    // ユーザ管理・登録画面（S0108）表示
    @GetMapping("/user_edit")
    public String showUserEdit(HttpSession session, Model model) {
        user_Entity loginUser = (user_Entity) session.getAttribute("loginUser");
        if (!isAdmin(loginUser)) return "redirect:/login";

        model.addAttribute("loginUser", loginUser);

        List<user_Entity> userList = userRepository.findAll();
        model.addAttribute("userList", userList);
        model.addAttribute("userForm", new user_Entity());

        return "user_edit";
    }

    // ユーザ登録・更新処理
    @PostMapping("/user_edit/save")
    public String saveUser(@ModelAttribute("userForm") user_Entity form, HttpSession session) {
        user_Entity loginUser = (user_Entity) session.getAttribute("loginUser");
        if (!isAdmin(loginUser)) return "redirect:/login";

        LocalDateTime now = LocalDateTime.now();
        user_Entity existing = userRepository.findById(form.getUserId()).orElse(null);

        if (existing == null) {
            // 新規登録時
            form.setCreatedAt(now);
            form.setUpdatedAt(now);
            userRepository.save(form);
        } else {
            // 更新時
            existing.setUserName(form.getUserName());
            if (form.getPassword() != null && !form.getPassword().isEmpty()) {
                existing.setPassword(form.getPassword());
            }
            existing.setEmailAddress(form.getEmailAddress());
            existing.setPhoneNumber(form.getPhoneNumber());
            existing.setDepartmentId(form.getDepartmentId());
            existing.setWorkLocationId(form.getWorkLocationId());
            existing.setRoleId(form.getRoleId());
            existing.setUpdatedAt(now);
            userRepository.save(existing);
        }

        return "redirect:/user_edit";
    }

    // ユーザ削除処理
    @PostMapping("/user_edit/delete")
    public String deleteUser(@RequestParam("userId") String userId, HttpSession session) {
        user_Entity loginUser = (user_Entity) session.getAttribute("loginUser");
        if (!isAdmin(loginUser)) return "redirect:/login";
        userRepository.deleteById(userId);
        return "redirect:/user_edit";
    }
}