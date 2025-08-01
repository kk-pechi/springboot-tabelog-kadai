package com.example.nagoyameshi.controller;

import java.time.LocalTime;
import java.util.List;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.Shop;
import com.example.nagoyameshi.form.ShopEditForm;
import com.example.nagoyameshi.form.ShopRegisterForm;
import com.example.nagoyameshi.repository.ShopRepository;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.CategoryService;
import com.example.nagoyameshi.service.ShopService;


@Controller
@RequestMapping("/admin/shops")
public class AdminShopController {
    private final ShopRepository shopRepository;   
    private final ShopService shopService;
    private final CategoryService categoryService;
        
    public AdminShopController(ShopRepository shopRepository, ShopService shopService, CategoryService categoryService) {
        this.shopRepository = shopRepository;
        this.shopService = shopService;
        this.categoryService = categoryService;
    }	
    
    @GetMapping
    public String index(Model model,
                        @PageableDefault(page = 0, size = 10, sort = "id", direction = Direction.ASC) Pageable pageable,
                        @RequestParam(name = "keyword", required = false) String keyword,
                        @AuthenticationPrincipal UserDetailsImpl userDetailsImpl)
    {
        Page<Shop> shopPage;

        if (keyword != null && !keyword.isEmpty()) {
            shopPage = shopRepository.findByNameLike("%" + keyword + "%", pageable);                
        } else {
            shopPage = shopRepository.findAll(pageable);
        }  

        model.addAttribute("shopPage", shopPage);   
        model.addAttribute("keyword", keyword);

        if (userDetailsImpl != null) {
            model.addAttribute("userName", userDetailsImpl.getUser().getName());
        }

        return "admin/shops/index";
    }

    
    @GetMapping("/{id}")
    public String show(@PathVariable(name = "id") Integer id,
                       Model model,
                       @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
        Shop shop = shopRepository.getReferenceById(id);
        model.addAttribute("shop", shop);

        if (userDetailsImpl != null) {
            model.addAttribute("userName", userDetailsImpl.getUser().getName());
        }

        return "admin/shops/show";
    } 
    
    @GetMapping("/register")
    public String register(Model model, @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
    	model.addAttribute("shopRegisterForm", new ShopRegisterForm());
    	model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("holidays", List.of("日曜日", "月曜日", "火曜日", "水曜日", "木曜日", "金曜日", "土曜日", "祝祭日"));
        model.addAttribute("timeOptions", generateTimeOptions());
        
        if (userDetailsImpl != null) {
            model.addAttribute("userName", userDetailsImpl.getUser().getName());
        }
        
        return "admin/shops/register";
    }
    
    @PostMapping("/create")
    public String create(@ModelAttribute @Validated ShopRegisterForm shopRegisterForm, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {        
        if (bindingResult.hasErrors()) {
        	model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("holidays", List.of("日曜日", "月曜日", "火曜日", "水曜日", "木曜日", "金曜日", "土曜日", "祝祭日"));
            model.addAttribute("timeOptions", generateTimeOptions());
            return "admin/shops/register";
        }

        LocalTime openingTime = shopRegisterForm.getOpeningTime();
        LocalTime closingTime = shopRegisterForm.getClosingTime();

        if (openingTime != null && closingTime != null && !shopService.isValidBusinessHours(openingTime, closingTime)) {
            FieldError openingTimeError = new FieldError(bindingResult.getObjectName(), "openingTime", "開店時間は閉店時間よりも前に設定してください。");
            FieldError closingTimeError = new FieldError(bindingResult.getObjectName(), "closingTime", "閉店時間は開店時間よりも後に設定してください。");
            bindingResult.addError(openingTimeError);
            bindingResult.addError(closingTimeError);
            model.addAttribute("holidays", List.of("日曜日", "月曜日", "火曜日", "水曜日", "木曜日", "金曜日", "土曜日", "祝祭日"));
            return "admin/shops/register";
        }

        shopService.create(shopRegisterForm);
        redirectAttributes.addFlashAttribute("successMessage", "店舗を登録しました。");   
        
        model.addAttribute("timeOptions", generateTimeOptions());

        return "redirect:/admin/shops";
    } 
    
    @GetMapping("/{id}/edit")
    public String edit(@PathVariable(name = "id") Integer id,
                       Model model,
                       @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
        Shop shop = shopRepository.getReferenceById(id);
        String imageName = shop.getImageName();       
        ShopEditForm shopEditForm = new ShopEditForm(
            shop.getId(),
            shop.getName(),
            null,
            shop.getCategory().getId(),
            shop.getDescription(),
            shop.getCapacity(),
            shop.getPostalCode(),
            shop.getAddress(),
            shop.getOpeningTime(),
            shop.getClosingTime(),
            List.of(shop.getRegularHoliday().split(",")),
            shop.getPhoneNumber()
        );
        
        model.addAttribute("shop", shop); 
        model.addAttribute("imageName", imageName);
        model.addAttribute("shopEditForm", shopEditForm);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("holidays", List.of("日曜日", "月曜日", "火曜日", "水曜日", "木曜日", "金曜日", "土曜日", "祝祭日"));
        model.addAttribute("timeOptions", generateTimeOptions());

        if (userDetailsImpl != null) {
            model.addAttribute("userName", userDetailsImpl.getUser().getName());
        }

        return "admin/shops/edit";
    }

    
    @PostMapping("/{id}/update")
    public String update(@ModelAttribute @Validated ShopEditForm shopEditForm,
                         BindingResult bindingResult,
                         @PathVariable(name = "id") Integer id,
                         RedirectAttributes redirectAttributes,
                         Model model,
                         @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {

        Shop shop = shopRepository.getReferenceById(id);

        if (bindingResult.hasErrors()) {
            model.addAttribute("shop", shop); 
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("timeOptions", generateTimeOptions());
            model.addAttribute("holidays", List.of("日曜日", "月曜日", "火曜日", "水曜日", "木曜日", "金曜日", "土曜日", "祝祭日"));

            if (userDetailsImpl != null) {
                model.addAttribute("userName", userDetailsImpl.getUser().getName());
            }

            return "admin/shops/edit";
        }

        LocalTime openingTime = shopEditForm.getOpeningTime();
        LocalTime closingTime = shopEditForm.getClosingTime();

        if (openingTime != null && closingTime != null && !shopService.isValidBusinessHours(openingTime, closingTime)) {
            FieldError openingTimeError = new FieldError(bindingResult.getObjectName(), "openingTime", "開店時間は閉店時間よりも前に設定してください。");
            FieldError closingTimeError = new FieldError(bindingResult.getObjectName(), "closingTime", "閉店時間は開店時間よりも後に設定してください。");
            bindingResult.addError(openingTimeError);
            bindingResult.addError(closingTimeError);

            model.addAttribute("shop", shop); 
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("timeOptions", generateTimeOptions());
            model.addAttribute("holidays", List.of("日曜日", "月曜日", "火曜日", "水曜日", "木曜日", "金曜日", "土曜日", "祝祭日"));

            if (userDetailsImpl != null) {
                model.addAttribute("userName", userDetailsImpl.getUser().getName());
            }

            return "admin/shops/edit";
        }

        shopService.update(shopEditForm);
        redirectAttributes.addFlashAttribute("successMessage", "店舗情報を編集しました。");

        return "redirect:/admin/shops";
    }

    
    //開店と閉店時間の選択を10分刻みにする
    public List<LocalTime> generateTimeOptions() {
        List<LocalTime> timeList = new java.util.ArrayList<>();
        for (int hour = 0; hour < 24; hour++) {
            for (int minute = 0; minute < 60; minute += 10) {
                timeList.add(LocalTime.of(hour, minute));
            }
        }
        return timeList;
    }

}