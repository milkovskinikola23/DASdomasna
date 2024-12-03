package com.example.project1.data.pipeline.impl;

import com.example.project1.data.pipeline.Filter;
import com.example.project1.model.Company;
import com.example.project1.repository.CompanyRepo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;

public class Filter1 implements Filter<List<Company>> {

    private final CompanyRepo companyRepo;

    public Filter1(CompanyRepo companyRepo) {
        this.companyRepo = companyRepo;
    }

    private static final String STOCK_MARKET_URL = "https://www.mse.mk/mk/stats/symbolhistory/kmb";

    @Override
    public List<Company> execute(List<Company> input) throws IOException {
        Document document = Jsoup.connect(STOCK_MARKET_URL).get();
        Element selectMenu = document.select("select#Code").first();

        if (selectMenu != null) {
            Elements options = selectMenu.select("option");
            for (Element option : options) {
                String code = option.attr("value");
                if (!code.isEmpty() && code.matches("^[a-zA-Z]+$")) {
                    if (companyRepo.findByCompanyCode(code).isEmpty()) {
                        companyRepo.save(new Company(code));
                    }
                }
            }
        }

        return companyRepo.findAll();
    }
}
