package com.example.nagoyameshi.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.nagoyameshi.entity.Company;
import com.example.nagoyameshi.service.CompanyService;


@Controller
@RequestMapping("/company")
public class CompanyController {
   private final CompanyService companyService;

   public CompanyController(CompanyService companyService) {
       this.companyService = companyService;
   }

   @GetMapping
   public String index(Model model) {
       Company company = companyService.findFirstCompanyByOrderByIdDesc();

       model.addAttribute("company", company);

       return "company/index";
   }
   
   @PreAuthorize("hasRole('ADMIN')")
   @GetMapping("/edit")
   public String editForm(Model model) {
       Company company = companyService.findFirstCompanyByOrderByIdDesc();
       model.addAttribute("company", company);
       return "company/edit";
   }

   @PostMapping("/edit")
   public String updateCompany(@ModelAttribute Company company) {
       companyService.save(company);
       return "redirect:/company";
   }
}