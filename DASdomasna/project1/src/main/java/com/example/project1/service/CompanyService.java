package com.example.project1.service;

import com.example.project1.model.Company;
import com.example.project1.repository.CompanyRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepo companyRepo;

    public List<Company> findAll() {
        return companyRepo.findAll();
    }

    public Company findById(Long id) throws Exception {
        return companyRepo.findById(id).orElseThrow(Exception::new);
    }

}
