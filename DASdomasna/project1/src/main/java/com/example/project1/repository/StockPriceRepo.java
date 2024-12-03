package com.example.project1.repository;

import com.example.project1.model.Company;
import com.example.project1.model.StockPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockPriceRepo extends JpaRepository<StockPrice, Long> {
    Optional<StockPrice> findByDateAndCompany(LocalDate date, Company company);
    List<StockPrice> findByCompanyIdAndDateBetween(Long companyId, LocalDate from, LocalDate to);
}
