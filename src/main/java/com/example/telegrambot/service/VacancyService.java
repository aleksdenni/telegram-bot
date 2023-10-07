package com.example.telegrambot.service;

import com.example.telegrambot.dto.VacancyDto;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VacancyService {

    @Autowired
    private VacanciesReaderService vacanciesReaderService;
    private final Map<String, VacancyDto> vacancies = new HashMap<>();

    @PostConstruct
    public void init() {
        List<VacancyDto> vacanciesList = vacanciesReaderService.getVacanciesFromFile("vacancies.csv");
        for (VacancyDto vacancyDto : vacanciesList) {
            vacancies.put(vacancyDto.getId(), vacancyDto);
        }
    }

    public List<VacancyDto> getJuniorVacancies() {
        return vacancies.values().stream()
                .filter(v -> v.getTitle().toLowerCase().contains("junior"))
                .toList();
    }

    public List<VacancyDto> getMiddleVacancies() {

        return vacancies.values().stream()
                .filter(v -> v.getTitle().toLowerCase().contains("middle"))
                .toList();
    }

    public List<VacancyDto> getSeniorVacancies() {
        return vacancies.values().stream()
                .filter(v -> v.getTitle().toLowerCase().contains("senior"))
                .toList();
    }

    public VacancyDto getId(String id) {
        return vacancies.get(id);
    }
}
