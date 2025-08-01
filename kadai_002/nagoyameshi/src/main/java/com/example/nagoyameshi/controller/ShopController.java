package com.example.nagoyameshi.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.Category;
import com.example.nagoyameshi.entity.Review;
import com.example.nagoyameshi.entity.Shop;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.form.ReservationInputForm;
import com.example.nagoyameshi.repository.CategoryRepository;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.FavoriteService;
import com.example.nagoyameshi.service.ReviewService;
import com.example.nagoyameshi.service.ShopService;

@Controller
@RequestMapping("/shops")
public class ShopController {

    private final ShopService shopService;
    private final CategoryRepository categoryRepository;
    private final ReviewService reviewService;
    private final FavoriteService favoriteService;

    public ShopController(ShopService shopService, CategoryRepository categoryRepository, ReviewService reviewService, FavoriteService favoriteService) {
        this.shopService = shopService;
        this.categoryRepository = categoryRepository;
        this.reviewService = reviewService;
        this.favoriteService = favoriteService;
    }

    @GetMapping
    public String index(@RequestParam(name = "keyword", required = false) String keyword,
                        @RequestParam(name = "area", required = false) String area,
                        @RequestParam(name = "category", required = false) String category,
                        @RequestParam(name = "order", required = false) String order,
                        @PageableDefault(page = 0, size = 6, sort = "id", direction = Direction.ASC) Pageable pageable,
                        Model model) {

        Page<Shop> shopPage;

        if (order != null && order.equals("ratingDesc")) {
            if (keyword != null && !keyword.isEmpty()) {
                shopPage = shopService.findShopsByNameLikeOrAddressLikeOrCategoryNameLikeOrderByAverageRatingDesc(keyword, keyword, keyword, pageable);
            } else if (category != null && !category.isEmpty()) {
                try {
                    Integer categoryId = Integer.parseInt(category);
                    shopPage = shopService.findShopsByCategoryIdOrderByAverageRatingDesc(categoryId, pageable);
                } catch (NumberFormatException e) {
                    shopPage = shopService.findAllShopsByOrderByAverageRatingDesc(pageable); // fallback
                }
            } else {
                shopPage = shopService.findAllShopsByOrderByAverageRatingDesc(pageable);
            }
        } else if (keyword != null && !keyword.isEmpty()) {
            shopPage = shopService.findShopsByNameLikeOrAddressLikeOrderByCreatedAtDesc(keyword, keyword, pageable);
        } else if (area != null && !area.isEmpty()) {
            shopPage = shopService.findShopsByAddressLikeOrderByCreatedAtDesc(area, pageable);
        } else if (category != null && !category.isEmpty()) {
            shopPage = shopService.findByCategoryNameOrderByCreatedAtDesc(category, pageable);
        } else {
            shopPage = shopService.findAllShopsByOrderByCreatedAtDesc(pageable);
        }

        // カテゴリ一覧を取得
        List<Category> categories = categoryRepository.findAll();
        
        model.addAttribute("categories", categories);
        model.addAttribute("shopPage", shopPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("category", category);
        model.addAttribute("order", order);

        return "shops/index";
    }
    
    @GetMapping("/{id}")
    public String show(@PathVariable(name = "id") Integer id,
                       @AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
                       RedirectAttributes redirectAttributes,
                       Model model) {

        Optional<Shop> optionalShop = shopService.findShopById(id);

        if (optionalShop.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "店舗が存在しません。");
            return "redirect:/shops";
        }

        Shop shop = optionalShop.get();
        model.addAttribute("shop", shop);
        model.addAttribute("reservationInputForm", new ReservationInputForm());

        boolean hasUserAlreadyReviewed = false;

        if (userDetailsImpl != null) {
            // ログイン中：レビューを全件取得
            List<Review> newReviews = reviewService.findTop6ReviewsByShopId(shop.getId());
            model.addAttribute("newReviews", newReviews);

            // レビュー投稿済みかどうか確認
            User user = userDetailsImpl.getUser();
            hasUserAlreadyReviewed = reviewService.hasUserAlreadyReviewed(shop, user);

            String userName = user.getName();
            model.addAttribute("userName", userName);

            // お気に入り状態を取得してビューに渡す
            boolean isFavorite = favoriteService.isFavorite(user.getId(), shop.getId());
            model.addAttribute("isFavorite", isFavorite);

        } else {
            // ゲスト：レビューを最新6件だけ取得
            List<Review> guestReviews = reviewService.findTop6ReviewsByShopId(shop.getId());
            model.addAttribute("guestReviews", guestReviews);
        }

        long totalReviewCount = reviewService.countReviewsByShop(shop);
        model.addAttribute("totalReviewCount", totalReviewCount);
        model.addAttribute("hasUserAlreadyReviewed", hasUserAlreadyReviewed);

        return "shops/show";
    }



}