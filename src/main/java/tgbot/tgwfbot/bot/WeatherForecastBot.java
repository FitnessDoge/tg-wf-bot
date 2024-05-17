package tgbot.tgwfbot.bot;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.IOException;

@Component
public class WeatherForecastBot implements LongPollingSingleThreadUpdateConsumer, SpringLongPollingBot {
    private final TelegramClient client;

    public WeatherForecastBot() {
        client = new OkHttpTelegramClient(getBotToken());
    }

    @Override
    public String getBotToken() {
        return System.getenv("TG_BOT_TOKEN");
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    public void consume(Update update) {
        Message message;
        if (update.hasMessage()) {
            message = update.getMessage();
        } else if (update.hasCallbackQuery()) {
            handleCallback(update);
            return;
        } else {
            return;
        }

        User from = message.getFrom();
        Long chatId = message.getChatId();
        System.out.println("Message:[" + message.getText() + "] from =>" + from);

        if (message.isCommand()) {
            String command = message.getText();
            handleCommand(command, chatId);
        } else {
            String answerMessage = "Please enter the correct command.";
            sendSimpleMessage(chatId, answerMessage);
        }

    }

    private void handleCallback(Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        String data = callbackQuery.getData();
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();

        String[] split = data.split("=");
        if (split.length == 2) {
            if ("province".equals(split[0])) {
                sendCityMenu(chatId, messageId);
            } else if ("city".equals(split[0])) {
                sendCountyMenu(chatId, messageId);
            } else if ("county".equals(split[0])) {
                // TODO query forecast
                System.out.println("County code => " + split[1]);

                String key = System.getenv("AMAP_KEY");
                String urlString = "https://restapi.amap.com/v3/weather/weatherInfo?extensions=all"
                        + "&city=" + split[1]
                        + "&key=" + key;
                OkHttpClient httpClient = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(urlString)
                        .build();
                try {
                    Response response = httpClient.newCall(request).execute();
                    String responseString = response.body().string();
                    System.out.println("Amap response => " + responseString);
                    sendSimpleMessage(chatId, responseString);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            System.out.println("Unknown error...");
        }
    }

    private void sendCountyMenu(Long chatId, Integer messageId) {
        InlineKeyboardButton yb = InlineKeyboardButton.builder()
                .text("渝北区")
                .callbackData("county=500112")
                .build();
        InlineKeyboardButton na = InlineKeyboardButton.builder()
                .text("南岸区")
                .callbackData("county=500108")
                .build();
        InlineKeyboardButton jb = InlineKeyboardButton.builder()
                .text("江北区")
                .callbackData("county=500105")
                .build();
        InlineKeyboardMarkup keyboardMarkup = InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(yb, na, jb))
                .build();
        EditMessageReplyMarkup editMessageReplyMarkup = EditMessageReplyMarkup.builder()
                .chatId(chatId)
                .messageId(messageId)
                .replyMarkup(keyboardMarkup)
                .build();
        EditMessageText editMessageText = EditMessageText.builder()
                .chatId(chatId)
                .messageId(messageId)
                .text("Please select the county you want to query: ")
                .build();
        try {
            client.execute(editMessageText);
            client.execute(editMessageReplyMarkup);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendCityMenu(Long chatId, Integer messageId) {
        InlineKeyboardButton cq = InlineKeyboardButton.builder()
                .text("重庆市")
                .callbackData("city=500000")
                .build();
        InlineKeyboardMarkup keyboardMarkup = InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(cq))
                .build();
        EditMessageReplyMarkup editMessageReplyMarkup = EditMessageReplyMarkup.builder()
                .chatId(chatId)
                .messageId(messageId)
                .replyMarkup(keyboardMarkup)
                .build();
        EditMessageText editMessageText = EditMessageText.builder()
                .chatId(chatId)
                .messageId(messageId)
                .text("Please select the city you want to query: ")
                .build();
        try {
            client.execute(editMessageText);
            client.execute(editMessageReplyMarkup);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void handleCommand(String command, Long chatId) {
        switch (command) {
            case "/start" -> {
                String answerMessage = "Hello,there is Techjoy bot. I'll give you the weather forecast.";
                sendSimpleMessage(chatId, answerMessage);
            }
            case "/forecast" -> {
                sendProvinceMenu(chatId);
            }
            case "/forecast3" -> {
                // TODO Forecast for the incoming three days
                sendProvinceMenu(chatId);
            }
            case null, default -> {
                String answerMessage = "Command is currently not supported.";
                sendSimpleMessage(chatId, answerMessage);
            }
        }
    }

    private void sendProvinceMenu(Long chatId) {
        InlineKeyboardButton bj = InlineKeyboardButton.builder()
                .text("北京")
                .callbackData("province=110000")
                .build();
        InlineKeyboardButton cq = InlineKeyboardButton.builder()
                .text("重庆")
                .callbackData("province=500000")
                .build();
        InlineKeyboardMarkup keyboardMarkup = InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(bj, cq))
                .build();
        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text("Please select the province you want to query:")
                .replyMarkup(keyboardMarkup)
                .build();
        try {
            client.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendSimpleMessage(Long chatId, String answerMessage) {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(answerMessage)
                .build();
        try {
            client.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
