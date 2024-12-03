package com.example.project1.repository;

import com.example.project1.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepo extends JpaRepository<Company, Long> {
    Optional<Company> findByCompanyCode(String companyCode);
}
