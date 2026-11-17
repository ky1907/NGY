package com.example.ngy_kintai.Controller;

import com.example.ngy_kintai.Entity.user_Entity;
import com.example.ngy_kintai.Repository.user_Repository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class loginController {

    @Autowired
    private user_Repository userRepository;

    @GetMapping({"/", "/login"})
    public String showLogin() {
        return "login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam("userId") String userId,
                          @RequestParam("password") String password,
                          HttpSession session,
                          Model model) {
        user_Entity user = userRepository.findByUserIdAndPassword(userId, password);
        if (user == null) {
            // M0001: ユーザIDまたはパスワードに誤りがあります
            model.addAttribute("errorMsg", "ユーザIDまたはパスワードに誤りがあります");
            return "login";
        }
        session.setAttribute("loginUser", user);
        return "redirect:/attendance";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}