package com.example.nagoyameshi.controller;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.entity.VerificationToken;
import com.example.nagoyameshi.event.SignupEventPublisher;
import com.example.nagoyameshi.form.SignupForm;
import com.example.nagoyameshi.service.UserService;
import com.example.nagoyameshi.service.VerificationTokenService;

@Controller
public class AuthController {
    private final UserService userService;
    private final SignupEventPublisher signupEventPublisher;
    private final VerificationTokenService verificationTokenService;

    public AuthController(UserService userService, SignupEventPublisher signupEventPublisher, VerificationTokenService verificationTokenService) { 
        this.userService = userService; 
        this.signupEventPublisher = signupEventPublisher;
        this.verificationTokenService = verificationTokenService;
    }   
    
    @GetMapping("/login")
    public String login(@AuthenticationPrincipal com.example.nagoyameshi.security.UserDetailsImpl userDetailsImpl,
                        Model model) {
        if (userDetailsImpl != null) {
            model.addAttribute("userName", userDetailsImpl.getUser().getName());
        }
        return "auth/login";
    }
    
    
    @GetMapping("/signup")
    public String signup(@AuthenticationPrincipal com.example.nagoyameshi.security.UserDetailsImpl userDetailsImpl,
                         Model model) {
        model.addAttribute("signupForm", new SignupForm());

        if (userDetailsImpl != null) {
            model.addAttribute("userName", userDetailsImpl.getUser().getName());
        }

        return "auth/signup";
    }   
    
    @PostMapping("/signup")
    public String signup(@ModelAttribute @Validated SignupForm signupForm,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         HttpServletRequest httpServletRequest,
                         Model model)
    {
        // メールアドレスが登録済みであれば、BindingResultオブジェクトにエラー内容を追加する
        if (userService.isEmailRegistered(signupForm.getEmail())) {
            FieldError fieldError = new FieldError(bindingResult.getObjectName(), "email", "すでに登録済みのメールアドレスです。");
            bindingResult.addError(fieldError);
        }

        // パスワードとパスワード（確認用）の入力値が一致しなければ、BindingResultオブジェクトにエラー内容を追加する
        if (!userService.isSamePassword(signupForm.getPassword(), signupForm.getPasswordConfirmation())) {
            FieldError fieldError = new FieldError(bindingResult.getObjectName(), "password", "パスワードが一致しません。");
            bindingResult.addError(fieldError);
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("signupForm", signupForm);

            return "auth/signup";
        }

        User createdUser = userService.create(signupForm);
        String requestUrl = new String(httpServletRequest.getRequestURL());
        signupEventPublisher.publishSignupEvent(createdUser, requestUrl);
        redirectAttributes.addFlashAttribute("successMessage", "ご入力いただいたメールアドレスに認証メールを送信しました。メールに記載されているリンクをクリックし、会員登録を完了してください。");        


        return "redirect:/";
    } 
    
    @GetMapping("/signup/verify")
    public String verify(@RequestParam(name = "token") String token,
                         @AuthenticationPrincipal com.example.nagoyameshi.security.UserDetailsImpl userDetailsImpl,
                         Model model) {
        VerificationToken verificationToken = verificationTokenService.getVerificationToken(token);
        
        if (verificationToken != null) {
            User user = verificationToken.getUser();  
            userService.enableUser(user);
            model.addAttribute("successMessage", "会員登録が完了しました。");            
        } else {
            model.addAttribute("errorMessage", "トークンが無効です。");
        }

        if (userDetailsImpl != null) {
            model.addAttribute("userName", userDetailsImpl.getUser().getName());
        }

        return "auth/verify";         
    }
}
