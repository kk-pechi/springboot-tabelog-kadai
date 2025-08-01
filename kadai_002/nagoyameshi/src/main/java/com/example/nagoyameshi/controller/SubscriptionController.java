package com.example.nagoyameshi.controller;

import java.security.Principal;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.StripeService;
import com.example.nagoyameshi.service.UserService;
import com.stripe.exception.StripeException;

@Controller
@RequestMapping("/subscription")
public class SubscriptionController {

    private final StripeService stripeService;
    private final UserService userService;

    public SubscriptionController(StripeService stripeService, UserService userService) {
        this.stripeService = stripeService;
        this.userService = userService;
    }

    // サブスクリプション登録画面
    @GetMapping("/plan")
    public String showSubscriptionPlanPage(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, Model model) {

        if (userDetailsImpl != null) {
            String userName = userDetailsImpl.getUser().getName();
            model.addAttribute("userName", userName);
        }

        return "subscription/plan";
    }

    // Stripe Checkout セッション作成 → リダイレクト
    @PostMapping("/create-session")
    public String createStripeSession(@AuthenticationPrincipal UserDetails userDetails,
                                      HttpServletRequest request,
                                      Model model) {
        User user = userService.findUserByEmail(userDetails.getUsername());
        String sessionUrl = stripeService.createStripeSession(user, request);

        if (sessionUrl == null) {
            model.addAttribute("error", "Stripeセッションの作成に失敗しました。");
            return "subscription/plan";
        }

        return "redirect:" + sessionUrl;
    }

    // サブスク登録成功後にロール変更
    @GetMapping("/success")
    public String subscriptionSuccess(@RequestParam(name = "session_id", required = false) String sessionId,
                                      Model model,
                                      Principal principal,
                                      @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
        if (principal != null) {
            String email = principal.getName();
            User user = userService.findUserByEmail(email);
            userService.updateUserRole(user.getId(), "ROLE_PREMIUM");
            userService.refreshAuthentication(user);
            model.addAttribute("username", user.getName());
        }

        if (userDetailsImpl != null) {
            model.addAttribute("userName", userDetailsImpl.getUser().getName());
        } else {
            model.addAttribute("error", "ユーザー情報が見つかりません。");
        }

        model.addAttribute("sessionId", sessionId); 

        return "subscription/success";
    }

    // キャンセル時
    @GetMapping("/cancel")
    public String subscriptionCancel(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, Model model) {
    	
        if (userDetailsImpl != null) {
            model.addAttribute("userName", userDetailsImpl.getUser().getName());
        }
    	
        return "subscription/cancel";
    }

    @GetMapping("/unsubscribe")
    public String unsubscribe(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            Object principalObj = authentication.getPrincipal();

            if (principalObj instanceof UserDetails userDetails) {
                String email = userDetails.getUsername();
                User user = userService.findUserByEmail(email);
                userService.updateUserRole(user.getId(), "ROLE_USER");

                // ロール変更後に再認証
                userService.refreshAuthentication(user);

                // 再度認証情報を取得しなおす
                Authentication newAuth = SecurityContextHolder.getContext().getAuthentication();
                Object newPrincipal = newAuth.getPrincipal();

                if (newPrincipal instanceof UserDetailsImpl updatedUserDetails) {
                    model.addAttribute("userName", updatedUserDetails.getUser().getName());
                } else {
                    model.addAttribute("userName", user.getName());
                }
            }
        }

        return "subscription/unsubscribed";
    }

   // 支払い設定編集ページ（画面表示）
    @GetMapping("/edit")
    public String showSubscriptionEditPage(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, Model model) {
        if (userDetailsImpl != null) {
            model.addAttribute("userName", userDetailsImpl.getUser().getName());
        }
        return "subscription/edit";
    }
    
    @PostMapping("/customer-portal")
    public String customerPortal(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, RedirectAttributes redirectAttributes) {
        try {
            User user = userDetailsImpl.getUser();
            String portalUrl = stripeService.createCustomerPortalUrl(user);
            return "redirect:" + portalUrl;
        } catch (StripeException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "ポータルの表示に失敗しました。");
            return "redirect:/subscription/plan";
        }
    }

    
}
