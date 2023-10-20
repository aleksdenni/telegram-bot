package com.example.telegrambot.application.vacancy;

public enum Position {
    JUNIOR("junior"),
    MIDDLE("middle"),
    SENIOR("senior");

    String position;
    Position(String position) {
        this.position = position;
    }
}
