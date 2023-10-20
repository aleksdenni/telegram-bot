package com.example.telegrambot.bot;

import com.example.telegrambot.application.vacancy.VacancyBot;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;
import java.util.List;

@Component
public abstract class PiBot extends TelegramLongPollingBot {

    VacancyBot vacancyBot;
    @Value("$bot.name")String name;


    public PiBot(String botToken) {
        super(botToken);
    }

    @Override
    public String getBotUsername() {
        return name;
    }

    @Override
    public void onUpdateReceived(Update update) {
        vacancyBot.onUpdateReceived(update);
    }

    @PostConstruct
    public void init() throws TelegramApiException {
        final var telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(this);
    }

    public String escapeMarkdownReservedChars(String str) {
        return str.replace("-", "\\-")
                .replace("_", "\\_")
                .replace("*", "\\*")
                .replace("[", "\\[")
                .replace("]", "\\]")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("`", "\\`")
                .replace("~", "\\~")
                .replace(">", "\\>")
                .replace("#", "\\#")
                .replace("+", "\\+")
                .replace(".", "\\.")
                .replace("!", "\\!");
    }

    private ReplyKeyboard getStartMenu() {
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton games = new InlineKeyboardButton();
        games.setText("games");
        games.setCallbackData("startGameBot");
        row.add(games);

        InlineKeyboardButton vacancies = new InlineKeyboardButton();
        vacancies.setText("vacancies");
        vacancies.setCallbackData("starVacancyBot");
        row.add(vacancies);

        InlineKeyboardButton chat = new InlineKeyboardButton();
        chat.setText("chat");
        chat.setCallbackData("startChatBot");
        row.add(chat);

        return new InlineKeyboardMarkup(List.of(row));
    }
}
