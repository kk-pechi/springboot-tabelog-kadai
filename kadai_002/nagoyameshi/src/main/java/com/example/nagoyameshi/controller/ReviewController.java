package com.example.nagoyameshi.controller;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.Review;
import com.example.nagoyameshi.entity.Shop;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.form.ReviewEditForm;
import com.example.nagoyameshi.form.ReviewRegisterForm;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.ReviewService;
import com.example.nagoyameshi.service.ShopService;

@Controller
@RequestMapping("/shops/{shopId}/reviews")
public class ReviewController {
   private final ShopService shopService;
   private final ReviewService reviewService;

   public ReviewController(ShopService shopService, ReviewService reviewService) {
       this.shopService = shopService;
       this.reviewService = reviewService;
   }

   @GetMapping
   public String index(@PathVariable(name = "shopId") Integer shopId,
		   				@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
                       @PageableDefault(page = 0, size = 10, sort = "id") Pageable pageable,
                       RedirectAttributes redirectAttributes,
                       Model model)
   {
       Optional<Shop> optionalShop  = shopService.findShopById(shopId);

       if (optionalShop.isEmpty()) {
           redirectAttributes.addFlashAttribute("errorMessage", "店舗が存在しません。");

           return "redirect:/shops";
       }
       
       if (userDetailsImpl != null) {
    	    User user = userDetailsImpl.getUser();
    	    String userName = user.getName();
    	    model.addAttribute("userName", userName);
    	}

       Shop shop = optionalShop.get();
       Page<Review> reviewPage = reviewService.findReviewsByShopOrderByCreatedAtDesc(shop, pageable);

       model.addAttribute("shop", shop);
       model.addAttribute("reviewPage", reviewPage);

       return "reviews/index";
   }

   @GetMapping("/register")
   public String register(@PathVariable(name = "shopId") Integer shopId, RedirectAttributes redirectAttributes, Model model) {
       Optional<Shop> optionalShop  = shopService.findShopById(shopId);

       if (optionalShop.isEmpty()) {
           redirectAttributes.addFlashAttribute("errorMessage", "店舗が存在しません。");

           return "redirect:/shops";
       }

       Shop shop = optionalShop.get();

       model.addAttribute("shop", shop);
       model.addAttribute("reviewRegisterForm", new ReviewRegisterForm());

       return "reviews/register";
   }

   @PreAuthorize("hasRole('PREMIUM')")
   @PostMapping("/create")
   public String create(@PathVariable(name = "shopId") Integer shopId,
                        @ModelAttribute @Validated ReviewRegisterForm reviewRegisterForm,
                        BindingResult bindingResult,
                        @AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
                        RedirectAttributes redirectAttributes,
                        Model model)
   {
       Optional<Shop> optionalShop  = shopService.findShopById(shopId);

       if (optionalShop.isEmpty()) {
           redirectAttributes.addFlashAttribute("errorMessage", "店舗が存在しません。");

           return "redirect:/shops";
       }

       Shop shop = optionalShop.get();

       if (bindingResult.hasErrors()) {
           model.addAttribute("shop", shop);
           model.addAttribute("reviewRegisterForm", reviewRegisterForm);

           return "reviews/register";
       }

       User user = userDetailsImpl.getUser();

       reviewService.createReview(reviewRegisterForm, shop, user);
       redirectAttributes.addFlashAttribute("successMessage", "レビューを投稿しました。");

       return "redirect:/shops/{shopId}";
   }

   @PreAuthorize("hasRole('PREMIUM')")
   @GetMapping("/{reviewId}/edit")
   public String edit(@PathVariable(name = "shopId") Integer shopId,
                      @PathVariable(name = "reviewId") Integer reviewId,
                      @AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
                      RedirectAttributes redirectAttributes,
                      Model model)
   {
       Optional<Shop> optionalShop  = shopService.findShopById(shopId);
       Optional<Review> optionalReview  = reviewService.findReviewById(reviewId);

       if (optionalShop.isEmpty() || optionalReview.isEmpty()) {
           redirectAttributes.addFlashAttribute("errorMessage", "指定されたページが見つかりません。");

           return "redirect:/shops";
       }

       Shop shop = optionalShop.get();
       Review review = optionalReview.get();
       User user = userDetailsImpl.getUser();

       if (!review.getShop().equals(shop) || !review.getUser().equals(user)) {
           redirectAttributes.addFlashAttribute("errorMessage", "不正なアクセスです。");

           return "redirect:/shops/{shopId}";
       }

       ReviewEditForm reviewEditForm = new ReviewEditForm(review.getRating(), review.getComment());

       model.addAttribute("shop", shop);
       model.addAttribute("review", review);
       model.addAttribute("reviewEditForm", reviewEditForm);

       return "reviews/edit";
   }

   @PreAuthorize("hasRole('PREMIUM')")
   @PostMapping("/{reviewId}/update")
   public String update(@PathVariable(name = "shopId") Integer shopId,
                        @PathVariable(name = "reviewId") Integer reviewId,
                        @ModelAttribute @Validated ReviewEditForm reviewEditForm,
                        BindingResult bindingResult,
                        @AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
                        RedirectAttributes redirectAttributes,
                        Model model)
   {
       Optional<Shop> optionalShop  = shopService.findShopById(shopId);
       Optional<Review> optionalReview  = reviewService.findReviewById(reviewId);

       if (optionalShop.isEmpty() || optionalReview.isEmpty()) {
           redirectAttributes.addFlashAttribute("errorMessage", "指定されたページが見つかりません。");

           return "redirect:/shops";
       }

       Shop shop = optionalShop.get();
       Review review = optionalReview.get();
       User user = userDetailsImpl.getUser();

       if (!review.getShop().equals(shop) || !review.getUser().equals(user)) {
           redirectAttributes.addFlashAttribute("errorMessage", "不正なアクセスです。");

           return "redirect:/shops/{shopId}";
       }

       if (bindingResult.hasErrors()) {
           model.addAttribute("shop", shop);
           model.addAttribute("review", review);
           model.addAttribute("reviewEditForm", reviewEditForm);

           return "reviews/edit";
       }

       reviewService.updateReview(reviewEditForm, review);
       redirectAttributes.addFlashAttribute("successMessage", "レビューを編集しました。");

       return "redirect:/shops/{shopId}";
   }
   
   @PreAuthorize("hasRole('PREMIUM')")
   @PostMapping("/{reviewId}/delete")
   public String delete(@PathVariable(name = "shopId") Integer shopId,
                        @PathVariable(name = "reviewId") Integer reviewId,
                        @AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
                        RedirectAttributes redirectAttributes)
   {
       Optional<Shop> optionalShop  = shopService.findShopById(shopId);
       Optional<Review> optionalReview  = reviewService.findReviewById(reviewId);

       if (optionalShop.isEmpty() || optionalReview.isEmpty()) {
           redirectAttributes.addFlashAttribute("errorMessage", "指定されたページが見つかりません。");

           return "redirect:/shops";
       }

       Shop shop = optionalShop.get();
       Review review = optionalReview.get();
       User user = userDetailsImpl.getUser();

       if (!review.getShop().equals(shop) || !review.getUser().equals(user)) {
           redirectAttributes.addFlashAttribute("errorMessage", "不正なアクセスです。");

           return "redirect:/shops/{shopId}";
       }

       reviewService.deleteReview(review);
       redirectAttributes.addFlashAttribute("successMessage", "レビューを削除しました。");

       return "redirect:/shops/{shopId}";
   }
}