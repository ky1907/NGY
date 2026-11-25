package com.example.ngy_kintai.Controller;

import com.example.ngy_kintai.Entity.mail_Entity;
import com.example.ngy_kintai.Entity.user_Entity;
import com.example.ngy_kintai.Repository.mail_Repository;
import com.example.ngy_kintai.Repository.user_Repository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class mailController {

    @Autowired
    private mail_Repository mailRepository;

    @Autowired
    private user_Repository userRepository;

    @GetMapping("/mail")
    public String showMail(
            @RequestParam(value = "box", defaultValue = "inbox") String box,
            HttpSession session,
            Model model) {

        user_Entity loginUser = (user_Entity) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login";
        model.addAttribute("loginUser", loginUser);

        List<user_Entity> allUsers = userRepository.findAll();
        Map<String, String> userNameMap = new HashMap<>();
        for (user_Entity u : allUsers) {
            userNameMap.put(u.getUserId(), u.getUserName());
        }

        List<mail_Entity> inboxMails = mailRepository.findByReceiverUserIdOrderBySentAtDesc(loginUser.getUserId());
        List<mail_Entity> sentMails = mailRepository.findBySenderUserIdOrderBySentAtDesc(loginUser.getUserId());

        model.addAttribute("activeBox", box);
        model.addAttribute("inboxMails", inboxMails);
        model.addAttribute("sentMails", sentMails);
        model.addAttribute("userList", allUsers);
        model.addAttribute("userNameMap", userNameMap);

        return "mail";
    }

    @PostMapping("/mail/send")
    public String sendMail(
            @RequestParam("receiverUserId") String receiverUserId,
            @RequestParam("subject") String subject,
            @RequestParam("body") String body,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        user_Entity loginUser = (user_Entity) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login";

        String targetId = receiverUserId != null ? receiverUserId.trim() : "";
        if (targetId.isEmpty() || !userRepository.existsById(targetId)) {
            redirectAttributes.addFlashAttribute("errorMessage", "指定された宛先（ユーザID）が見つかりません。検索ボタンから選択してください。");
            return "redirect:/mail";
        }

        mail_Entity mail = new mail_Entity();
        mail.setSenderUserId(loginUser.getUserId());
        mail.setReceiverUserId(targetId);
        mail.setSubject(subject);
        mail.setBody(body);
        mail.setSentAt(LocalDateTime.now());
        mailRepository.save(mail);

        redirectAttributes.addFlashAttribute("successMessage", "メールを送信しました。");
        return "redirect:/mail?box=sent";
    }
}