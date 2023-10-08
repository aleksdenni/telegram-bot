package com.example.telegrambot.application.vacancy.dto;

import com.opencsv.bean.CsvBindByName;

public class VacancyDto {

    @CsvBindByName(column = "Id")
    private String id;

    @CsvBindByName(column = "Title")
    private String title;

    @CsvBindByName(column = "Short description")
    private String shortDescription;

    @CsvBindByName(column = "Long description")
    private String LongDescription;

    @CsvBindByName(column = "Company")
    private String Company;

    @CsvBindByName(column = "Salary")
    private String Salary;

    @CsvBindByName(column = "Link")
    private String Link;

    public String getLongDescription() {
        return LongDescription;
    }

    public void setLongDescription(String longDescription) {
        LongDescription = longDescription;
    }

    public String getCompany() {
        return Company;
    }

    public void setCompany(String company) {
        Company = company;
    }

    public String getSalary() {
        return Salary;
    }

    public void setSalary(String salary) {
        Salary = salary;
    }

    public String getLink() {
        return Link;
    }

    public void setLink(String link) {
        Link = link;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }
}