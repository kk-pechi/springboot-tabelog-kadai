package com.example.nagoyameshi.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.Favorite;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.FavoriteService;

@Controller
@RequestMapping("/favorites")
public class FavoriteController {
	
    private final FavoriteService favoriteService;
	
    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }
	
//	お気に入り一覧にアクセスされたときに表示する
    @GetMapping
    public String list(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
                       @PageableDefault(page = 0, size = 10, sort = "id", direction = Direction.ASC) Pageable pageable,
                       Model model) {
        Integer userId = userDetailsImpl.getUser().getId();
        Page<Favorite> favoritePage = favoriteService.getFavoritesByUserWithShop(userId, pageable);
        
        if (userDetailsImpl != null) {
    	    User user = userDetailsImpl.getUser();
    	    String userName = user.getName();
    	    model.addAttribute("userName", userName);
    	}
        
        model.addAttribute("favoritePage", favoritePage);
        return "favorites/index";
    }
	
    // お気に入り追加
    @PostMapping("/add")
    public String addFavorite(@RequestParam("shopId") Integer shopId,
                               @AuthenticationPrincipal UserDetailsImpl userDetails,
                               RedirectAttributes redirectAttributes) {
        favoriteService.addFavorite(userDetails.getUser().getId(), shopId);
        return "redirect:/shops/" + shopId;
    }
    
    // お気に入り解除
    @PostMapping("/remove")
    public String removeFavorite(@RequestParam("shopId") Integer shopId,
                                  @AuthenticationPrincipal UserDetailsImpl userDetails,
                                  RedirectAttributes redirectAttributes) {
        favoriteService.removeFavorite(userDetails.getUser().getId(), shopId);
        return "redirect:/shops/" + shopId;
    }
}
