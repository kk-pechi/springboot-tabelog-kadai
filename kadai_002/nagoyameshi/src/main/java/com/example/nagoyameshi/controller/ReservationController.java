package com.example.nagoyameshi.controller;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.Reservation;
import com.example.nagoyameshi.entity.Shop;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.form.ReservationInputForm;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.ReservationService;
import com.example.nagoyameshi.service.ShopService;

@Controller
public class ReservationController {
    private final ReservationService reservationService;
    private final ShopService shopService;

    public ReservationController(ReservationService reservationService, ShopService shopService) {
        this.reservationService = reservationService;
        this.shopService = shopService;
    }

    // 店舗詳細＋予約フォーム表示
    @GetMapping("/shops/{shopId}/reservations/confirm")
    public String show(@PathVariable(name = "shopId") Integer shopId,
                       @AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
                       Model model,
                       RedirectAttributes redirectAttributes) {

        Optional<Shop> optionalShop = shopService.findShopById(shopId);
        if (optionalShop.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "店舗が存在しません。");
            return "redirect:/shops";
        }

        Shop shop = optionalShop.get();
        model.addAttribute("shop", shop);
        model.addAttribute("reservationInputForm", new ReservationInputForm());

        if (userDetailsImpl != null) {
            model.addAttribute("userName", userDetailsImpl.getUser().getName());
        }

        return "shops/show";
    }


    // 予約登録処理
    @PostMapping("/shops/{id}/reservations/create")
    public String create(@PathVariable(name = "id") Integer id,
                         @ModelAttribute @Validated ReservationInputForm reservationInputForm,
                         BindingResult bindingResult,
                         @AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
                         RedirectAttributes redirectAttributes,
                         Model model) {

        Optional<Shop> optionalShop = shopService.findShopById(id);
        if (optionalShop.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "店舗が存在しません。");
            return "redirect:/shops";
        }

        Shop shop = optionalShop.get();
        User user = userDetailsImpl.getUser();
        
        if (!"ROLE_PREMIUM".equals(user.getRole().getName())) {
            redirectAttributes.addFlashAttribute("errorMessage", "この機能は有料会員限定です。");
            return "redirect:/shops/" + id;
        }
        
        Integer numberOfPeople = reservationInputForm.getNumberOfPeople();

        if (numberOfPeople != null && !reservationService.isWithinCapacity(numberOfPeople, shop.getCapacity())) {
            FieldError error = new FieldError("reservationInputForm", "numberOfPeople", "予約人数が定員を超えています。");
            bindingResult.addError(error);
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("shop", shop);
            model.addAttribute("reservationInputForm", reservationInputForm);
            model.addAttribute("errorMessage", "予約内容に不備があります。");

            return "shops/show";
        }
        
        if (userDetailsImpl != null) {
            model.addAttribute("userName", userDetailsImpl.getUser().getName());
        }

        reservationService.createReservation(reservationInputForm, shop, userDetailsImpl.getUser());
        redirectAttributes.addFlashAttribute("successMessage", "予約が完了しました。");

        return "redirect:/reservations";
    }

    // 予約一覧
    @GetMapping("/reservations")
    public String index(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
                        @PageableDefault(page = 0, size = 10, sort = "id", direction = Direction.ASC) Pageable pageable,
                        Model model) {
        User user = userDetailsImpl.getUser();
        Page<Reservation> reservationPage = reservationService.findReservationsByUserOrderByCreatedAtDesc(user, pageable);
        
        if (userDetailsImpl != null) {
            model.addAttribute("userName", userDetailsImpl.getUser().getName());
        }

        model.addAttribute("reservationPage", reservationPage);
        model.addAttribute("reservations", reservationPage.getContent());
        model.addAttribute("userName", user.getName());

        return "reservations/index";
    }

    // 予約削除
    @PostMapping("/reservations/{reservationId}/delete")
    public String delete(@PathVariable Integer reservationId,
                         @AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
                         RedirectAttributes redirectAttributes) {
        Optional<Reservation> optionalReservation = reservationService.findReservationById(reservationId);
        if (optionalReservation.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "予約が存在しません。");
            return "redirect:/reservations";
        }

        Reservation reservation = optionalReservation.get();
        if (!reservation.getUser().getId().equals(userDetailsImpl.getUser().getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "不正なアクセスです。");
            return "redirect:/reservations";
        }

        reservationService.deleteReservation(reservation);
        redirectAttributes.addFlashAttribute("successMessage", "予約をキャンセルしました。");
        return "redirect:/reservations";
    }
}
