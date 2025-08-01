package com.example.nagoyameshi.service;

import org.springframework.stereotype.Service;

import com.example.nagoyameshi.entity.Company;
import com.example.nagoyameshi.repository.CompanyRepository;

@Service
public class CompanyService {
    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public Company findFirstCompanyByOrderByIdDesc() {
        return companyRepository.findFirstByOrderByIdDesc();
    }
    public void save(Company company) {
        companyRepository.save(company);
    }
}
