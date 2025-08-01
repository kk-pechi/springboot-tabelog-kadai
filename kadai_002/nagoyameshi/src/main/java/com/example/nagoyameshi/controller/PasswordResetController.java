package com.example.nagoyameshi.controller;

import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.PasswordResetToken;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.form.PasswordResetForm;
import com.example.nagoyameshi.repository.UserRepository;
import com.example.nagoyameshi.service.EmailService;
import com.example.nagoyameshi.service.PasswordResetService;

@Controller
@RequestMapping("/password-reset")
public class PasswordResetController {

    private final UserRepository userRepository;
    private final PasswordResetService passwordResetService;
    private final EmailService emailService;

    public PasswordResetController(UserRepository userRepository,
                                   PasswordResetService passwordResetService,
                                   EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordResetService = passwordResetService;
        this.emailService = emailService;
    }

    // メールアドレス入力フォーム表示
    @GetMapping
    public String showResetForm() {
        return "password-reset/request";
    }

    // トークン送信処理
    @PostMapping
    public String handleResetRequest(@RequestParam("email") String email, HttpServletRequest request, Model model) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            model.addAttribute("error", "このメールアドレスは登録されていません。");
            return "password-reset/request";
        }

        User user = optionalUser.get();
        PasswordResetToken resetToken = passwordResetService.createPasswordResetToken(user);
        String token = resetToken.getToken();

        String resetUrl = request.getRequestURL().toString() + "/confirm?token=" + token;
        emailService.sendResetPasswordEmail(email, resetUrl);

        model.addAttribute("message", "パスワードリセット用のリンクを送信しました。");
        return "password-reset/request";
    }

    // トークン確認＋パスワード再設定フォーム
    @GetMapping("/confirm")
    public String showPasswordResetForm(@RequestParam("token") String token, Model model) {
        Optional<User> optionalUser = passwordResetService.validatePasswordResetToken(token);
        if (optionalUser.isEmpty()) {
            model.addAttribute("error", "トークンが無効または期限切れです。");
            return "password-reset/request";
        }

        model.addAttribute("token", token);
        model.addAttribute("passwordResetForm", new PasswordResetForm());
        return "password-reset/reset";
    }

    // パスワード更新処理
    @PostMapping("/confirm")
    public String handlePasswordReset(@RequestParam("token") String token,
                                      @ModelAttribute PasswordResetForm form,
                                      Model model,
                                      RedirectAttributes redirectAttributes) {
        Optional<User> optionalUser = passwordResetService.validatePasswordResetToken(token);
        if (optionalUser.isEmpty()) {
            model.addAttribute("error", "トークンが無効または期限切れです。");
            return "password-reset/request";
        }

        passwordResetService.updatePassword(optionalUser.get(), form.getNewPassword());

        redirectAttributes.addFlashAttribute("message", "パスワードが再設定されました。ログインしてください。");
        return "redirect:/login";
    }
}

