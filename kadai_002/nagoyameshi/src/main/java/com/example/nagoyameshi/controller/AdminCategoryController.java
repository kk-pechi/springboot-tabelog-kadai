package com.example.nagoyameshi.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.Category;
import com.example.nagoyameshi.entity.Shop;
import com.example.nagoyameshi.form.CategoryEditForm;
import com.example.nagoyameshi.form.CategoryRegisterForm;
import com.example.nagoyameshi.repository.CategoryRepository;
import com.example.nagoyameshi.repository.ShopRepository;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.CategoryService;

@Controller
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    private final CategoryRepository categoryRepository;
    private final CategoryService categoryService;
    private final ShopRepository shopRepository;

    public AdminCategoryController(CategoryRepository categoryRepository, CategoryService categoryService, ShopRepository shopRepository) {
        this.categoryRepository = categoryRepository;
        this.categoryService = categoryService;
        this.shopRepository = shopRepository;
    }

    @GetMapping
    public String index(Model model,
                        @PageableDefault(page = 0, size = 20, sort = "id", direction = Direction.DESC) Pageable pageable,
                        @RequestParam(name = "keyword", required = false) String keyword,
                        @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {

        Page<Category> categoryPage = (keyword != null && !keyword.isEmpty())
                ? categoryRepository.findByNameLike("%" + keyword + "%", pageable)
                : categoryRepository.findAll(pageable);

        model.addAttribute("categoryPage", categoryPage);
        model.addAttribute("keyword", keyword);

        if (!model.containsAttribute("categoryRegisterForm")) {
            model.addAttribute("categoryRegisterForm", new CategoryRegisterForm());
        }

        if (!model.containsAttribute("categoryEditForm")) {
            model.addAttribute("categoryEditForm", new CategoryEditForm());
        }

        if (userDetailsImpl != null) {
            model.addAttribute("userName", userDetailsImpl.getUser().getName());
        }

        return "admin/categories/index";
    }


    @PostMapping("/create")
    public String create(@ModelAttribute @Validated CategoryRegisterForm categoryRegisterForm,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.categoryRegisterForm", bindingResult);
            redirectAttributes.addFlashAttribute("categoryRegisterForm", categoryRegisterForm);
            return "redirect:/admin/categories";
        }

        categoryService.createCategory(categoryRegisterForm);
        redirectAttributes.addFlashAttribute("successMessage", "カテゴリを登録しました。");
        return "redirect:/admin/categories";
    }

    @PostMapping("/{id}/update")
    public String update(@ModelAttribute @Validated CategoryEditForm categoryEditForm,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.categoryEditForm", bindingResult);
            redirectAttributes.addFlashAttribute("categoryEditForm", categoryEditForm);
            return "redirect:/admin/categories";
        }

        categoryService.updateCategory(categoryEditForm);
        redirectAttributes.addFlashAttribute("successMessage", "カテゴリを編集しました。");
        return "redirect:/admin/categories";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable(name = "id") Integer id, RedirectAttributes redirectAttributes) {
        List<Shop> shopList = shopRepository.findByCategoryId(id);

        if (shopList.isEmpty()) {
            categoryRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "カテゴリを削除しました。");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "そのカテゴリは店舗が登録されているため削除できません。");
        }

        return "redirect:/admin/categories";
    }

}
