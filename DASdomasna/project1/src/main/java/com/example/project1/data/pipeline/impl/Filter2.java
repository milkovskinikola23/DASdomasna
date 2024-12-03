package com.example.project1.data.pipeline.impl;

import com.example.project1.data.DataTransform;
import com.example.project1.data.pipeline.Filter;
import com.example.project1.model.Company;
import com.example.project1.model.StockPrice;
import com.example.project1.repository.CompanyRepo;
import com.example.project1.repository.StockPriceRepo;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Filter2 implements Filter<List<Company>> {

    private final CompanyRepo companyRepo;
    private final StockPriceRepo stockPriceRepo;

    private static final String HISTORICAL_DATA_URL = "https://www.mse.mk/mk/stats/symbolhistory/";

    public Filter2(CompanyRepo companyRepo, StockPriceRepo stockPriceRepo) {
        this.companyRepo = companyRepo;
        this.stockPriceRepo = stockPriceRepo;
    }

    public List<Company> execute(List<Company> input) throws IOException {
        List<Company> companies = new ArrayList<>();

        for (Company company : input) {
            if (company.getLastUpdated() == null) {
                for (int i = 1; i <= 10; i++) {
                    int temp = i - 1;
                    LocalDate fromDate = LocalDate.now().minusYears(i);
                    LocalDate toDate = LocalDate.now().minusYears(temp);
                    addHistoricalData(company, fromDate, toDate);
                }
            } else {
                companies.add(company);
            }
        }

        return companies;
    }

    private void addHistoricalData(Company company, LocalDate fromDate, LocalDate toDate) throws IOException {
        Connection.Response response = Jsoup.connect(HISTORICAL_DATA_URL + company.getCompanyCode())
                .data("FromDate", fromDate.toString())
                .data("ToDate", toDate.toString())
                .method(Connection.Method.POST)
                .execute();

        Document document = response.parse();

        Element table = document.select("table#resultsTable").first();

        if (table != null) {
            Elements rows = table.select("tbody tr");

            for (Element row : rows) {
                Elements columns = row.select("td");

                if (columns.size() > 0) {
                    LocalDate date = DataTransform.parseDate(columns.get(0).text(), "d.M.yyyy");

                    if (stockPriceRepo.findByDateAndCompany(date, company).isEmpty()) {


                        NumberFormat format = NumberFormat.getInstance(Locale.GERMANY);

                        Double lastTransactionPrice = DataTransform.parseDouble(columns.get(1).text(), format);
                        Double maxPrice = DataTransform.parseDouble(columns.get(2).text(), format);
                        Double minPrice = DataTransform.parseDouble(columns.get(3).text(), format);
                        Double averagePrice = DataTransform.parseDouble(columns.get(4).text(), format);
                        Double percentageChange = DataTransform.parseDouble(columns.get(5).text(), format);
                        Integer quantity = DataTransform.parseInteger(columns.get(6).text(), format);
                        Integer turnoverBest = DataTransform.parseInteger(columns.get(7).text(), format);
                        Integer totalTurnover = DataTransform.parseInteger(columns.get(8).text(), format);

                        if (maxPrice != null) {

                            if (company.getLastUpdated() == null || company.getLastUpdated().isBefore(date)) {
                                company.setLastUpdated(date);
                            }

                            StockPrice stockPrice = new StockPrice(
                                    date, lastTransactionPrice, maxPrice, minPrice, averagePrice, percentageChange,
                                    quantity, turnoverBest, totalTurnover);
                            stockPrice.setCompany(company);
                            stockPriceRepo.save(stockPrice);
                            company.getHistoricalData().add(stockPrice);
                        }
                    }
                }
            }
        }

        companyRepo.save(company);
    }

}
