package com.example.nagoyameshi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.nagoyameshi.entity.Company;
import com.example.nagoyameshi.service.CompanyService;

@Controller
@RequestMapping("/admin/company")
public class AdminCompanyController {

    private final CompanyService companyService;

    public AdminCompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping
    public String adminCompanyPage(Model model) {
        Company company = companyService.findFirstCompanyByOrderByIdDesc();
        model.addAttribute("company", company);
        return "admin/company/index";
    }

    @GetMapping("/edit")
    public String editForm(Model model) {
        Company company = companyService.findFirstCompanyByOrderByIdDesc();
        model.addAttribute("company", company);
        return "admin/company/edit";
    }

    @PostMapping("/edit")
    public String updateCompany(@ModelAttribute Company company) {
        companyService.save(company);
        return "redirect:/admin/company";
    }
}
