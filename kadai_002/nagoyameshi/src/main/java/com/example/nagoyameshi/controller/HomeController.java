package com.example.nagoyameshi.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.nagoyameshi.entity.Category;
import com.example.nagoyameshi.entity.Shop;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.repository.CategoryRepository;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.ShopService;
import com.example.nagoyameshi.service.UserService;

@Controller
public class HomeController {
    private final ShopService shopService;
    private final CategoryRepository categoryRepository;
    private final UserService userService;

    public HomeController(ShopService shopService,
				            CategoryRepository categoryRepository,
				            UserService userService) { 
			this.shopService = shopService;
			this.categoryRepository = categoryRepository;
			this.userService = userService;
		}
    
    @GetMapping("/")
    public String index(Model model, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<Shop> newShops = shopService.findTop10ShopsByOrderByCreatedAtDesc();
        List<Shop> rankingShops = shopService.findTop10ShopsByAverageRating();

        List<Category> categories = categoryRepository.findAll();
        List<String> imageCategoryNames = List.of("焼き肉", "テイクアウト", "居酒屋", "海鮮料理", "ラーメン", "和食");

        model.addAttribute("newShops", newShops);
        model.addAttribute("rankingShops", rankingShops);
        model.addAttribute("categories", categories);
        model.addAttribute("imageCategoryNames", imageCategoryNames);

        if (userDetails != null) {
            User user = userService.findUserByEmail(userDetails.getUsername());
            model.addAttribute("userName", user.getName());
        }

        return "index";
    }
}
