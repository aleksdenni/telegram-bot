package com.example.telegrambot.bot;

import com.example.telegrambot.application.vacancy.dto.VacancyDto;
import com.example.telegrambot.application.vacancy.service.VacancyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PiBot extends TelegramLongPollingBot {

    @Value("bot.name")
    private final String name;

    @Autowired
    private VacancyService vacancyService;

    private final Map<Long, String> lastShownVacancyLevel = new HashMap<>();

    public PiBot(@Value("${bot.token}") String botToken,
                 @Value("${bot.name}") String name) {
        super(botToken);
        this.name = name;
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.getMessage() != null) {
                handleStartCommand(update);
            }
            if (update.getCallbackQuery() != null) {
                String callbackData = update.getCallbackQuery().getData();
                if ("showJuniorVacancies".equals(callbackData)) {
                    showJuniorVacancies(update);
                } else if ("showMiddleVacancies".equals(callbackData)) {
                    showMiddleVacancies(update);
                } else if ("showSeniorVacancies".equals(callbackData)) {
                    showSeniorVacancies(update);
                } else if (callbackData.startsWith("vacancyId")) {
                    final int vacancyID = 1;
                    String id = callbackData.split("=")[vacancyID];
                    showVacancyDescription(id, update);
                } else if ("backToVacancies".equals(callbackData)) {
                    handleBackToVacanciesCommand(update);
                } else if ("backToStartMenu".equals(callbackData)) {
                    handleBackToStartCommand(update);
                }
            }
        } catch (TelegramApiException e) {
            throw new RuntimeException("Can't send message to user", e);
        }

    }

    private void handleBackToStartCommand(Update update) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("Choose your title:");
        sendMessage.setChatId(update.getCallbackQuery().getMessage().getChatId());
        sendMessage.setReplyMarkup(getStartMenu());
        execute(sendMessage);
    }

    private void handleBackToVacanciesCommand(Update update) throws TelegramApiException {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        String level = lastShownVacancyLevel.get(chatId);

        if ("junior".equals(level)) {
            showJuniorVacancies(update);
        } else if ("middle".equals(level)) {
            showMiddleVacancies(update);
        } else if ("senior".equals(level)) {
            showSeniorVacancies(update);
        }
    }

    private void showJuniorVacancies(Update update) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("Please choose vacancy:");
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        sendMessage.setChatId(chatId);
        sendMessage.setReplyMarkup(getJuniorVacanciesMenu());
        execute(sendMessage);
        lastShownVacancyLevel.put(chatId, "junior");
    }

    private void showMiddleVacancies(Update update) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("Please choose vacancy:");
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        sendMessage.setChatId(chatId);
        sendMessage.setReplyMarkup(getMiddleVacanciesMenu());
        execute(sendMessage);
        lastShownVacancyLevel.put(chatId, "middle");
    }

    private void showSeniorVacancies(Update update) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("Please choose vacancy:");
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        sendMessage.setChatId(chatId);
        sendMessage.setReplyMarkup(getSeniorVacanciesMenu());
        execute(sendMessage);
        lastShownVacancyLevel.put(chatId, "senior");
    }

    private ReplyKeyboard getJuniorVacanciesMenu() {
        List<VacancyDto> vacancies = vacancyService.getJuniorVacancies();
        return getVacanciesMenu(vacancies);
    }

    private ReplyKeyboard getVacanciesMenu(List<VacancyDto> vacancies) {
        List<InlineKeyboardButton> row = new ArrayList<>();
        for (VacancyDto vacancy : vacancies) {
            InlineKeyboardButton vacancyButton = new InlineKeyboardButton();
            vacancyButton.setText(vacancy.getTitle());
            vacancyButton.setCallbackData("vacancyId=" + vacancy.getId());
            row.add(vacancyButton);
        }

        return new InlineKeyboardMarkup(List.of(row));
    }

    private ReplyKeyboard getMiddleVacanciesMenu() {
        List<VacancyDto> vacancies = vacancyService.getMiddleVacancies();
        return getVacanciesMenu(vacancies);
    }

    private ReplyKeyboard getSeniorVacanciesMenu() {
        List<VacancyDto> vacancies = vacancyService.getSeniorVacancies();
        return getVacanciesMenu(vacancies);
    }


    private void showVacancyDescription(String id, Update update) throws TelegramApiException {
        VacancyDto vacancy = vacancyService.getId(id);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getCallbackQuery().getMessage().getChatId());
        String vacancyInfo = """
                *Title:* %s
                *Short description:* %s
                *Long description:* %s
                *Company:* %s
                *Salary:* [%s](%s)
                """.formatted(
                escapeMarkdownReservedChars(vacancy.getId()),
                escapeMarkdownReservedChars(vacancy.getCompany()),
                escapeMarkdownReservedChars(vacancy.getShortDescription()),
                escapeMarkdownReservedChars(vacancy.getLongDescription()),
                vacancy.getSalary().isBlank() ? "Not specified" : escapeMarkdownReservedChars(vacancy.getSalary()),
                "Click here for more details",
                escapeMarkdownReservedChars(vacancy.getLink())
        );
        sendMessage.setText(vacancyInfo);
        sendMessage.setParseMode(ParseMode.MARKDOWNV2);
        sendMessage.setReplyMarkup(getBackVacanciesMenu());
        execute(sendMessage);
    }

    private String escapeMarkdownReservedChars(String str) {
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

    private ReplyKeyboard getBackVacanciesMenu() {
        List<InlineKeyboardButton> row = new ArrayList<>();

        InlineKeyboardButton backToVacanciesButton = new InlineKeyboardButton();
        backToVacanciesButton.setText("Back to vacancies");
        backToVacanciesButton.setCallbackData("backToVacancies");
        row.add(backToVacanciesButton);

        InlineKeyboardButton backToStartMenuButton = new InlineKeyboardButton();
        backToStartMenuButton.setText("Back to start menu");
        backToStartMenuButton.setCallbackData("backToStartMenu");
        row.add(backToStartMenuButton);

        InlineKeyboardButton getChatGptButton = new InlineKeyboardButton();
        getChatGptButton.setText("Get cover letter");
        getChatGptButton.setUrl("https://chat.openai.com/");
        row.add(getChatGptButton);


        return new InlineKeyboardMarkup(List.of(row));
    }

    private void handleStartCommand(Update update) throws TelegramApiException {
        String message = update.getMessage().getText();
        var sendMessage = new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId());
        sendMessage.setText("Your message : " + message);
        sendMessage.setText("Welcome to my bot");
        sendMessage.setReplyMarkup(getStartMenu());
        if (message.equals("/hh")) {
            String text = "Welcome to Vacancies bot! Please choose your level!\n";
            sendMessage.enableMarkdown(true);
            ReplyKeyboardMarkup keyboardMarkup = getMenuKeyboard();
            sendMessage.setReplyMarkup(keyboardMarkup);
            sendMessage.setText(text);
        }
        execute(sendMessage);
    }

    private ReplyKeyboard getStartMenu() {
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton junior = new InlineKeyboardButton();
        junior.setText("junior");
        junior.setCallbackData("showJuniorVacancies");
        row.add(junior);

        InlineKeyboardButton middle = new InlineKeyboardButton();
        middle.setText("middle");
        middle.setCallbackData("showMiddleVacancies");
        row.add(middle);

        InlineKeyboardButton senior = new InlineKeyboardButton();
        senior.setText("senior");
        senior.setCallbackData("showSeniorVacancies");
        row.add(senior);

        return new InlineKeyboardMarkup(List.of(row));
    }

    private ReplyKeyboardMarkup getMenuKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.add("junior");
        keyboardRow.add("middle");
        keyboardRow.add("senior");
        keyboardRows.add(keyboardRow);
        replyKeyboardMarkup.setKeyboard(keyboardRows);

        return replyKeyboardMarkup;
    }

    @Override
    public String getBotUsername() {
        return name;
    }
}
